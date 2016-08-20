package de.qaware.chronix.server.benchmark.collector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.qaware.chronix.shared.DataModels.Pair;
import de.qaware.chronix.shared.QueryUtil.QueryRecord;

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
    private final String recordDirectory = System.getProperty("user.home") + File.separator
            + "chronixBenchmark" + File.separator
            + "queryRecords" + File.separator;
    private final String recordFile = "queryRecords.json";
    private final ObjectMapper mapper;
    private volatile BlockingDeque<QueryRecord> blockingDequeWriteJobs;
    private volatile BlockingDeque<Pair<QueryRecord, List<Pair<Double,Double>>>> blockingDequeEditJobs;

    private StatsWriter statsWriter;
    private QueryRecordEditor queryRecordEditor;

    private StatsCollector(){
        File dir = new File(recordDirectory);
        if( !(dir.exists()) ){
            dir.mkdirs();
        }
        mapper = new ObjectMapper();
        blockingDequeWriteJobs = new LinkedBlockingDeque<>();
        blockingDequeEditJobs = new LinkedBlockingDeque<>();
        // start writer
        statsWriter = new StatsWriter(new File(recordDirectory + recordFile), blockingDequeWriteJobs, mapper);
        statsWriter.start();

        //start editor
        queryRecordEditor = new QueryRecordEditor(blockingDequeWriteJobs, blockingDequeEditJobs);
        queryRecordEditor.start();

    }

    public static synchronized StatsCollector getInstance(){
        if(instance == null){
            instance = new StatsCollector();
        }
        return instance;
    }

    /**
     * Adds a QueryRecord and a List of measurement pairs to the job queue of the QueryRecordEditor.
     *
     * @param queryRecord the QueryRecord to be edited and written.
     * @param dockerMeasurement the list of measurement pairs.
     */
    public void addQueryRecordEditJob(QueryRecord queryRecord, List<Pair<Double,Double>> dockerMeasurement){
        try {
            blockingDequeEditJobs.put(Pair.of(queryRecord,dockerMeasurement));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }





    /**
     * Writes QueryRecords to the query record json file.
     */
    private class StatsWriter extends Thread{
        private volatile BlockingDeque<QueryRecord> blockingDequeWriteJobs;
        private File recordFile;
        private final ObjectMapper mapper;

        public StatsWriter(File recordFile, BlockingDeque<QueryRecord> blockingDequeWriteJobs, ObjectMapper mapper){
            this.blockingDequeWriteJobs = blockingDequeWriteJobs;
            this.recordFile = recordFile;
            this.mapper = mapper;
        }

        public void run() {
            while (true) {
                try {

                    QueryRecord queryRecord = blockingDequeWriteJobs.take();
                    final String queryRecordJSON = mapper.writeValueAsString(queryRecord);
                    if (recordFile.exists()) {
                        Files.write(recordFile.toPath(), Arrays.asList(queryRecordJSON), StandardOpenOption.APPEND);
                    } else {
                        Files.write(recordFile.toPath(), Arrays.asList(queryRecordJSON), StandardOpenOption.CREATE);
                    }


                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * Determines the cpu and memory usage from previous docker measurement.
     * Edits the QueryRecord and adds a new write job the the writers queue.
     */
    private class QueryRecordEditor extends Thread{
        private volatile BlockingDeque<QueryRecord> blockingDequeWriteJobs;
        private volatile BlockingDeque<Pair<QueryRecord, List<Pair<Double,Double>>>> blockingDequeEditJobs;

        public QueryRecordEditor(BlockingDeque<QueryRecord> blockingDequeWriteJobs,
                                 BlockingDeque<Pair<QueryRecord, List<Pair<Double,Double>>>> blockingDequeEditJobs){
            this.blockingDequeWriteJobs = blockingDequeWriteJobs;
            this.blockingDequeEditJobs = blockingDequeEditJobs;

        }

        public void run(){
            while(true){
                try {
                    Pair<QueryRecord, List<Pair<Double,Double>>> queryRecordListPair = blockingDequeEditJobs.take();
                    List<Double> cpuUsage = new LinkedList<>();
                    List<Double> memoryUsage = new LinkedList<>();
                    QueryRecord queryRecord = queryRecordListPair.getFirst();
                    for(Pair<Double,Double> measurePair : queryRecordListPair.getSecond()){
                        cpuUsage.add(measurePair.getFirst());
                        memoryUsage.add(measurePair.getSecond());
                    }
                    // cpu usage
                    Double maxCpuUsage = Collections.max(cpuUsage);
                    Double minCpuUsage = Collections.min(cpuUsage);
                    queryRecord.setCpuUsage(String.valueOf(maxCpuUsage-minCpuUsage));

                    // memory usage
                    Double maxMemoryUsage = Collections.max(memoryUsage);
                    Double minMemoryUsage = Collections.min(memoryUsage);
                    queryRecord.setMemoryUsage(String.valueOf((maxMemoryUsage-minMemoryUsage)));

                    // add job for writer
                    blockingDequeWriteJobs.put(queryRecord);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

    }

}
