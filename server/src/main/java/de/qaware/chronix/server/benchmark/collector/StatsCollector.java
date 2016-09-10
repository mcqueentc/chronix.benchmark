package de.qaware.chronix.server.benchmark.collector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

/**
 * Created by mcqueen666 on 19.08.16.
 */
public class StatsCollector {

    private static StatsCollector instance;
    private final Logger logger = LoggerFactory.getLogger(StatsCollector.class);
    private final String recordDirectory = System.getProperty("user.home") + File.separator
            + "chronixBenchmark" + File.separator
            + "queryRecords" + File.separator;
    private final String recordFile = "queryRecords.json";
    private final ObjectMapper mapper;
    private volatile BlockingDeque<BenchmarkRecord> blockingDequeWriteJobs;
    private volatile BlockingDeque<Pair<BenchmarkRecord, List<Tuple<Double,Double,Long,Long>>>> blockingDequeEditJobs;

    private StatsWriter statsWriter;
    private QueryRecordEditor queryRecordEditor;

    private StatsCollector(){
        initRecordFile();
        mapper = new ObjectMapper();
        //Ignore TimeSeries at writing
        //mapper.addMixIn(QueryRecord.class, IgnoreTimesSeriesForJSON.class);
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
    public void addQueryRecordEditJob(BenchmarkRecord benchmarkRecord, List<Tuple<Double,Double,Long,Long>> dockerMeasurement){
        try {
            blockingDequeEditJobs.put(Pair.of(benchmarkRecord,dockerMeasurement));
        } catch (InterruptedException e) {
            logger.error("Error StatsCollector: " + e.getLocalizedMessage());
        }
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
        private volatile BlockingDeque<Pair<BenchmarkRecord, List<Tuple<Double,Double,Long,Long>>>> blockingDequeEditJobs;

        public QueryRecordEditor(BlockingDeque<BenchmarkRecord> blockingDequeWriteJobs,
                                 BlockingDeque<Pair<BenchmarkRecord, List<Tuple<Double,Double,Long,Long>>>> blockingDequeEditJobs){
            this.blockingDequeWriteJobs = blockingDequeWriteJobs;
            this.blockingDequeEditJobs = blockingDequeEditJobs;

        }

        public void run(){
            while(true){
                try {
                    Pair<BenchmarkRecord, List<Tuple<Double,Double,Long,Long>>> queryRecordListPair = blockingDequeEditJobs.take();
                    List<Double> cpuUsage = new LinkedList<>();
                    List<Double> memoryUsage = new LinkedList<>();
                    List<Long> readBytes = new LinkedList<>();
                    List<Long> writtenBytes = new LinkedList<>();
                    BenchmarkRecord benchmarkRecord = queryRecordListPair.getFirst();
                    for(Tuple<Double,Double,Long,Long> measurePair : queryRecordListPair.getSecond()){
                        cpuUsage.add(measurePair.getFirst());
                        memoryUsage.add(measurePair.getSecond());
                        readBytes.add(measurePair.getThird());
                        writtenBytes.add(measurePair.getFourth());


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

                    // add job for writer
                    blockingDequeWriteJobs.put(benchmarkRecord);

                } catch (InterruptedException e) {
                    logger.error("Error StatsCollector: " + e.getLocalizedMessage());
                }
            }

        }

    }

}
