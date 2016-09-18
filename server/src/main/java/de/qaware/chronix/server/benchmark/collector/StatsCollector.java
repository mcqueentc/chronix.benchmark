package de.qaware.chronix.server.benchmark.collector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.qaware.chronix.server.util.DockerStatsRecord;
import de.qaware.chronix.shared.DataModels.Pair;
import de.qaware.chronix.shared.DataModels.Tuple;
import de.qaware.chronix.shared.QueryUtil.BenchmarkRecord;
import de.qaware.chronix.shared.QueryUtil.IgnoreTimesSeriesForJSON;
import de.qaware.chronix.shared.QueryUtil.ImportRecord;
import de.qaware.chronix.shared.QueryUtil.QueryRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Stream;

/**
 * Created by mcqueen666 on 19.08.16.
 */
public class StatsCollector {

    private static StatsCollector instance;
    private final Logger logger = LoggerFactory.getLogger(StatsCollector.class);
    private final int MAX_WAIT_TIME = 120_000;
    private final int WAIT_TIME_SLICE = 250;
    private final String recordDirectory = System.getProperty("user.home") + File.separator
            + "chronixBenchmark" + File.separator
            + "queryRecords" + File.separator;
    private final String recordFile = "queryRecords.json";
    private final ObjectMapper mapper;
    private volatile BlockingDeque<BenchmarkRecord> blockingDequeWriteJobs;
    private volatile BlockingDeque<Pair<BenchmarkRecord, List<DockerStatsRecord>>> blockingDequeEditJobs;

    private StatsWriter statsWriter;
    private QueryRecordEditor queryRecordEditor;

    private StatsCollector(){
        initRecordFile();
        mapper = new ObjectMapper();
        //Ignore TimeSeries and query list at writing
        mapper.addMixIn(QueryRecord.class, IgnoreTimesSeriesForJSON.class);
        mapper.addMixIn(ImportRecord.class, IgnoreTimesSeriesForJSON.class);
        blockingDequeWriteJobs = new LinkedBlockingDeque<>();
        blockingDequeEditJobs = new LinkedBlockingDeque<>();
        // start writer
        statsWriter = new StatsWriter(new File(recordDirectory + recordFile), blockingDequeWriteJobs, mapper);
        statsWriter.start();

        //start editor
        queryRecordEditor = new QueryRecordEditor(blockingDequeWriteJobs, blockingDequeEditJobs);
        queryRecordEditor.start();

    }

    private synchronized void initRecordFile(){
        File dir = new File(recordDirectory);
        if( !(dir.exists()) ){
            dir.mkdirs();
        }
    }

    public static synchronized StatsCollector getInstance(){
        if(instance == null){
            instance = new StatsCollector();
        }
        return instance;
    }

    /**
     * Adds a BenchmarkRecord and a List of measurement pairs to the job queue of the QueryRecordEditor.
     *
     * @param benchmarkRecord the BenchmarkRecord to be edited and written.
     * @param dockerMeasurement the list of measurement pairs.
     */
    public void addQueryRecordEditJob(BenchmarkRecord benchmarkRecord, List<DockerStatsRecord> dockerMeasurement){
        try {
            blockingDequeEditJobs.put(Pair.of(benchmarkRecord,dockerMeasurement));
        } catch (InterruptedException e) {
            logger.error("Error StatsCollector: " + e.getLocalizedMessage());
        }
    }

    /**
     * Reads the benchmark record file and returns a list of BenchmarkRecords.
     *
     * @return list of BenchmarkRecords or empty list if error occurred.
     */
    List<BenchmarkRecord> getBenchmarkRecords(){
        List<BenchmarkRecord> benchmarkRecordList = new LinkedList<>();
        int elapsedTime = 0;
        //wait until statsWriters job queue is empty or max wait time has elapsed.
        while(!blockingDequeWriteJobs.isEmpty() && elapsedTime < MAX_WAIT_TIME){
           try {
               Thread.sleep(WAIT_TIME_SLICE);
               elapsedTime += WAIT_TIME_SLICE;
           } catch (InterruptedException e) {
               logger.error("StatsCollector: Error waiting on writers job queue: {}",e.getLocalizedMessage());
           }
        }
        if(elapsedTime < MAX_WAIT_TIME){

                ObjectMapper mapper = new ObjectMapper();
                File benchmarkRecordFile = new File(recordDirectory + recordFile);
                if(benchmarkRecordFile.exists() && benchmarkRecordFile.isFile()){
                    try {
                        Stream<String> lines = Files.lines(benchmarkRecordFile.toPath());
                        lines.forEach(line -> {
                            try {
                                benchmarkRecordList.add(mapper.readValue(line, BenchmarkRecord.class));
                            } catch (IOException e) {
                                logger.error("StatsCollector: Error reading from json: {}",e.getLocalizedMessage());
                            }
                        });
                    } catch (IOException e) {
                        logger.error("StatsCollector: Error reading from benchmark record file: {}",e.getLocalizedMessage());
                    }
                }

        } else {
            logger.error("StatsCollector: Error waiting on writers job queue: max wait time elapsed.");
        }

        return benchmarkRecordList;
    }



    /**
     * Writes QueryRecords to the query record json file.
     */
    private class StatsWriter extends Thread{
        private volatile BlockingDeque<BenchmarkRecord> blockingDequeWriteJobs;
        private File recordFile;
        private final ObjectMapper mapper;

        public StatsWriter(File recordFile, BlockingDeque<BenchmarkRecord> blockingDequeWriteJobs, ObjectMapper mapper){
            this.blockingDequeWriteJobs = blockingDequeWriteJobs;
            this.recordFile = recordFile;
            this.mapper = mapper;
        }

        public void run() {
            while (true) {
                try {

                    BenchmarkRecord benchmarkRecord = blockingDequeWriteJobs.take();
                    final String queryRecordJSON = mapper.writeValueAsString(benchmarkRecord);
                    if (recordFile.exists()) {
                        Files.write(recordFile.toPath(), Arrays.asList(queryRecordJSON), StandardOpenOption.APPEND);
                    } else {
                        initRecordFile(); // in case record file is deleted while server is running.
                        Files.write(recordFile.toPath(), Arrays.asList(queryRecordJSON), StandardOpenOption.CREATE);
                    }


                } catch (Exception e) {
                    logger.error("Error StatsCollector: " + e.getLocalizedMessage());
                }
            }
        }
    }


    /**
     * Determines the cpu and memory usage from previous docker measurement.
     * Edits the BenchmarkRecord and adds a new write job the the writers queue.
     */
    private class QueryRecordEditor extends Thread{
        private volatile BlockingDeque<BenchmarkRecord> blockingDequeWriteJobs;
        private volatile BlockingDeque<Pair<BenchmarkRecord, List<DockerStatsRecord>>> blockingDequeEditJobs;

        public QueryRecordEditor(BlockingDeque<BenchmarkRecord> blockingDequeWriteJobs,
                                 BlockingDeque<Pair<BenchmarkRecord, List<DockerStatsRecord>>> blockingDequeEditJobs){
            this.blockingDequeWriteJobs = blockingDequeWriteJobs;
            this.blockingDequeEditJobs = blockingDequeEditJobs;

        }

        public void run(){
            while(true){
                try {
                    Pair<BenchmarkRecord, List<DockerStatsRecord>> queryRecordListPair = blockingDequeEditJobs.take();
                    List<Double> cpuUsage = new LinkedList<>();
                    List<Double> memoryUsage = new LinkedList<>();
                    List<Long> readBytes = new LinkedList<>();
                    List<Long> writtenBytes = new LinkedList<>();
                    List<Long> networkDownloadedBytes = new LinkedList<>();
                    List<Long> networkUploadedBytes = new LinkedList<>();
                    BenchmarkRecord benchmarkRecord = queryRecordListPair.getFirst();
                    // seperate the measurements.
                    for(DockerStatsRecord dockerStatsRecord : queryRecordListPair.getSecond()){
                        cpuUsage.add(dockerStatsRecord.getCpuUsage());
                        memoryUsage.add(dockerStatsRecord.getMemoryUsage());
                        readBytes.add(dockerStatsRecord.getReadBytes());
                        writtenBytes.add(dockerStatsRecord.getWrittenBytes());
                        networkDownloadedBytes.add(dockerStatsRecord.getNetworkDownloadedBytes());
                        networkUploadedBytes.add(dockerStatsRecord.getNetworkUploadedBytes());


                    }
                    // cpu usage
                    Double maxCpuUsage = Collections.max(cpuUsage);
                    Double minCpuUsage = Collections.min(cpuUsage);
                    benchmarkRecord.setCpuUsage(String.valueOf(maxCpuUsage - minCpuUsage));
                    benchmarkRecord.setCpuUsageTotal(String.valueOf(maxCpuUsage));

                    // memory usage
                    Double maxMemoryUsage = Collections.max(memoryUsage);
                    Double minMemoryUsage = Collections.min(memoryUsage);
                    benchmarkRecord.setMemoryUsage(String.valueOf((maxMemoryUsage - minMemoryUsage)));
                    benchmarkRecord.setMemoryUsageTotal(String.valueOf(maxMemoryUsage));

                    // readBytes
                    Long maxReadBytes = Collections.max(readBytes);
                    Long minReadBytes = Collections.min(readBytes);
                    benchmarkRecord.setReadBytes(String.valueOf((maxReadBytes - minReadBytes)));

                    // writtenBytes
                    Long maxWrittenBytes = Collections.max(writtenBytes);
                    Long minWrittenBytes = Collections.min(writtenBytes);
                    benchmarkRecord.setWrittenBytes(String.valueOf((maxWrittenBytes - minWrittenBytes)));

                    // networkDownloadedBytes
                    Long maxDownloadedBytes = Collections.max(networkDownloadedBytes);
                    Long minDownloadedBytes = Collections.min(networkDownloadedBytes);
                    benchmarkRecord.setNetworkDownloadedBytes(String.valueOf(maxDownloadedBytes - minDownloadedBytes));

                    // networkUploadedBytes
                    Long maxUploadedBytes = Collections.max(networkUploadedBytes);
                    Long minUploadedBytes = Collections.min(networkUploadedBytes);
                    benchmarkRecord.setNetworkUploadedBytes(String.valueOf(maxUploadedBytes - minUploadedBytes));

                    // add job for writer
                    blockingDequeWriteJobs.put(benchmarkRecord);

                } catch (InterruptedException e) {
                    logger.error("Error StatsCollector: " + e.getLocalizedMessage());
                }
            }

        }

    }

}
