package de.qaware.chronix.client.benchmark.resultpresenter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.qaware.chronix.client.benchmark.benchmarkrunner.BenchmarkRunner;
import de.qaware.chronix.database.BenchmarkDataSource;
import de.qaware.chronix.shared.QueryUtil.BenchmarkRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * Created by mcqueen666 on 16.10.16.
 */
public class TsdbStatisticsAnalyzer {

    private final Logger logger;
    private String statisticsDirectory;
    private String tsdbStatisticsFilename;

    public TsdbStatisticsAnalyzer(String statisticsDirectory){
        this.statisticsDirectory = statisticsDirectory;
        logger = LoggerFactory.getLogger(TsdbStatisticsAnalyzer.class);
        tsdbStatisticsFilename = "tsdb_analytics.json";
    }

    public String getStatisticsDirectory() {
        return statisticsDirectory;
    }

    public String getTsdbStatisticsFilename() {
        return tsdbStatisticsFilename;
    }

    public boolean tsdbStatisticsFileExists(){
        File tsdbStatisticsFile = new File(statisticsDirectory + File.separator + tsdbStatisticsFilename);
        return tsdbStatisticsFile.exists();
    }

    public List<TsdbStatistics> analyzeBenchmarkRecords(){
        File recordFile = new File(BenchmarkRunner.getInstance().getRecordFileDirectory()
                + File.separator
                + BenchmarkRunner.getInstance().getRecordFileName());

        if(recordFile.exists() && recordFile.isFile()){
            Map<String, List<BenchmarkRecord>> tsdbBenchmarkRecords = new HashMap<>();
            // read record file
            ObjectMapper mapper = new ObjectMapper();
            try {
                Stream<String> lines = Files.lines(recordFile.toPath(), Charset.defaultCharset());
                lines.forEach(line -> {
                    try {
                        BenchmarkRecord benchmarkRecord = mapper.readValue(line, new TypeReference<BenchmarkRecord>() {});
                        if(tsdbBenchmarkRecords.containsKey(benchmarkRecord.getTsdbName())){
                            tsdbBenchmarkRecords.get(benchmarkRecord.getTsdbName()).add(benchmarkRecord);
                        } else {
                            List<BenchmarkRecord> newRecordList = new LinkedList<>();
                            newRecordList.add(benchmarkRecord);
                            tsdbBenchmarkRecords.put(benchmarkRecord.getTsdbName(), newRecordList);
                        }

                    } catch (Exception e) {
                        logger.error("Error reading json objects from record file: {}",e.getLocalizedMessage());
                    }
                });

                // generate stats per tsdb
                // start threads
                ExecutorService executorService = Executors.newFixedThreadPool(tsdbBenchmarkRecords.size());
                List<Future<TsdbStatistics>> futureTsdbStatistics = new ArrayList<>(tsdbBenchmarkRecords.size());
                for(Map.Entry<String, List<BenchmarkRecord>> entry : tsdbBenchmarkRecords.entrySet()){
                    futureTsdbStatistics.add(executorService.submit(new TsdbStatsAnalyzerRunner(entry.getKey(), entry.getValue())));
                }

                //collect results
                List<TsdbStatistics> tsdbStatisticsList = new ArrayList<>(futureTsdbStatistics.size());
                for(Future<TsdbStatistics> future : futureTsdbStatistics){
                    TsdbStatistics tsdbStatistics = future.get();
                    if(tsdbStatistics != null) {
                        tsdbStatisticsList.add(tsdbStatistics);
                    }
                }

                executorService.shutdown();
                if(!executorService.awaitTermination(10000, TimeUnit.MILLISECONDS)){
                    executorService.shutdownNow();
                }

                // save stats
                saveTsdbStatistics(tsdbStatisticsList);

                return tsdbStatisticsList;

            } catch (Exception e){
                logger.error("Error analyzing record file: {}",e.getLocalizedMessage());
            }
        }
        return null;
    }

    private synchronized void saveTsdbStatistics(List<TsdbStatistics> tsdbStatisticsList){
        File statsDirectory = new File(statisticsDirectory);
        File tsdbStatisticsFile = new File(statisticsDirectory + File.separator + tsdbStatisticsFilename);

        if(!statsDirectory.exists()){
            statsDirectory.mkdirs();
        }

        if(tsdbStatisticsFile.exists()){
            tsdbStatisticsFile.delete();
        }

        ObjectMapper mapper = new ObjectMapper();
        try{
            mapper.writerWithDefaultPrettyPrinter().writeValue(tsdbStatisticsFile, tsdbStatisticsList);
        } catch (Exception e){
            logger.error("Error saving tsdb statistics to json file");
        }
    }

    public synchronized List<TsdbStatistics> readTsdbStatistics(){
        List<TsdbStatistics> tsdbStatisticsList = null;
        File tsdbStatisticsFile = new File(statisticsDirectory + File.separator + tsdbStatisticsFilename);
        if(tsdbStatisticsFile.exists()){
            ObjectMapper mapper = new ObjectMapper();
            try {
                tsdbStatisticsList = mapper.readValue(tsdbStatisticsFile, new TypeReference<List<TsdbStatistics>>() {});

            } catch (Exception e) {
                logger.error("Error reading tsdb statistics file: {}",e.getLocalizedMessage());
            }
        }

        return tsdbStatisticsList;
    }

    private class TsdbStatsAnalyzerRunner implements Callable<TsdbStatistics>{
        private String tsdbName;
        private List<BenchmarkRecord> recordList;

        public TsdbStatsAnalyzerRunner(String tsdbName, List<BenchmarkRecord> recordList) {
            this.tsdbName = tsdbName;
            this.recordList = recordList;
        }

        @Override
        public TsdbStatistics call() throws Exception {
            TsdbStatistics tsdbStatistics = null;
            if(tsdbName != null && ! tsdbName.isEmpty()){
                if(recordList != null && ! recordList.isEmpty()){
                    Map<String, List<BenchmarkRecord>> benchmarkRecordsPerQueryFunction = new HashMap<>();
                    //sort the benchmarkrecords according to queryfunction / import
                    for(BenchmarkRecord benchmarkRecord : recordList){
                        if(benchmarkRecord.getQueryID().contains("_import_")){
                            //import record
                            if(benchmarkRecordsPerQueryFunction.containsKey("import")){
                                benchmarkRecordsPerQueryFunction.get("import").add(benchmarkRecord);
                            } else {
                                List<BenchmarkRecord> importRecordList = new LinkedList<>();
                                importRecordList.add(benchmarkRecord);
                                benchmarkRecordsPerQueryFunction.put("import", importRecordList);
                            }

                        } else {
                            //query record
                            String[] firstSplit = benchmarkRecord.getQueryID().split("&");
                            String[] secondSplit = firstSplit[1].split("=");
                            String function = secondSplit[1];

                            if(benchmarkRecordsPerQueryFunction.containsKey(function)){
                                benchmarkRecordsPerQueryFunction.get(function).add(benchmarkRecord);
                            } else {
                                List<BenchmarkRecord> queryRecordList = new LinkedList<>();
                                queryRecordList.add(benchmarkRecord);
                                benchmarkRecordsPerQueryFunction.put(function, queryRecordList);
                            }
                        }
                    }

                    // analyze per query function
                    List<QueryFunctionStatistics> queryFunctionStatisticsList = new ArrayList<>(benchmarkRecordsPerQueryFunction.size());
                    for(Map.Entry<String, List<BenchmarkRecord>> entry : benchmarkRecordsPerQueryFunction.entrySet()){
                        QueryFunctionStatistics queryFunctionStatistics = analyzePerQueryFunction(entry.getKey(), entry.getValue());
                        if(queryFunctionStatistics != null){
                            queryFunctionStatisticsList.add(queryFunctionStatistics);
                        }
                    }
                    // work done
                    BenchmarkRunner benchmarkRunner = BenchmarkRunner.getInstance();
                    tsdbStatistics = new TsdbStatistics(tsdbName, queryFunctionStatisticsList);
                    tsdbStatistics.setNumberOfTimeSeriesPerQuery(benchmarkRunner.getBENCHMARK_TIMESERIES_METADATA_SIZE());
                    tsdbStatistics.setNumberOfTimeSeriesPer_QUERY_ONLY_Function(benchmarkRunner.getBENCHMARK_TIMESERIES_METADATA_SIZE_QUERY_ONLY());
                    tsdbStatistics.setNumberOfQueriesPerQueryFunction(benchmarkRunner.getNUMBER_OF_BENCHMARK_METADATA_LISTS() / BenchmarkDataSource.QueryFunction.values().length);
                    tsdbStatistics.setTotalNumberOfPerformedQueries(benchmarkRunner.getNUMBER_OF_BENCHMARK_METADATA_LISTS());
                    tsdbStatistics.setTotalNumberOfPerformedImports(benchmarkRecordsPerQueryFunction.get("import").size());

                }
            }

            return  tsdbStatistics;
        }

        private QueryFunctionStatistics analyzePerQueryFunction(String queryfunction, List<BenchmarkRecord> benchmarkRecordList){
            QueryFunctionStatistics queryFunctionStatistics = null;

            // query time
            List<Long> queryTimes = new ArrayList<>(benchmarkRecordList.size());
            long queryTimesSum = 0L;

            // cpu usage
            List<Double> totalCpuUsages = new ArrayList<>(benchmarkRecordList.size());
            double totalCpuUsagesSum = 0d;

            // disk usage
            List<Long> diskUsages = new ArrayList<>(benchmarkRecordList.size());
            List<Long> totalDiskUsages = new ArrayList<>(benchmarkRecordList.size());
            long diskUsagesSum = 0L;

            // memory usage
            List<Double> memoryUsages = new ArrayList<>(benchmarkRecordList.size());
            double memoryUsagesSum = 0d;
            List<Double> memoryTotalUsages = new ArrayList<>(benchmarkRecordList.size());
            double memoryTotalUsagesSum = 0d;

            // disk write
            List<Long> diskWrites = new ArrayList<>(benchmarkRecordList.size());
            long diskWritesSum = 0L;

            // disk read
            List<Long> diskReads = new ArrayList<>(benchmarkRecordList.size());
            long diskReadsSum = 0L;

            // network down
            List<Long> networkDowns = new ArrayList<>(benchmarkRecordList.size());
            long networkDownsSum = 0L;

            // network up
            List<Long> networkUps = new ArrayList<>(benchmarkRecordList.size());
            long networkUpsSum = 0L;

            // latency
            List<Long> latencies = new ArrayList<>(benchmarkRecordList.size());
            long latenciesSum = 0L;

            for(BenchmarkRecord benchmarkRecord : benchmarkRecordList){
                try {
                    //query time
                    queryTimes.add(benchmarkRecord.getQueryTimeMilliseconds());
                    queryTimesSum += benchmarkRecord.getQueryTimeMilliseconds();

                    // cpu usage
                    Double cpuUsage = Double.valueOf(benchmarkRecord.getCpuUsage());
                    totalCpuUsages.add(cpuUsage);
                    totalCpuUsagesSum += cpuUsage;

                    // disk usage
                    Long diskUsage = Long.valueOf(benchmarkRecord.getDiskUsage());
                    Long totalDiskUsage = Long.valueOf(benchmarkRecord.getDiskUsageTotal());
                    diskUsages.add(diskUsage);
                    diskUsagesSum += diskUsage;
                    totalDiskUsages.add(totalDiskUsage);

                    // memory usage
                    Double memoryUsage = Double.valueOf(benchmarkRecord.getMemoryUsage());
                    Double memoryTotalUsage = Double.valueOf(benchmarkRecord.getMemoryUsageTotal());
                    memoryUsages.add(memoryUsage);
                    memoryUsagesSum += memoryUsage;
                    memoryTotalUsages.add(memoryTotalUsage);
                    memoryTotalUsagesSum += memoryTotalUsage;

                    // disk write
                    Long diskWrite = Long.valueOf(benchmarkRecord.getWrittenBytes());
                    diskWrites.add(diskWrite);
                    diskWritesSum += diskWrite;

                    // disk read
                    Long diskRead = Long.valueOf(benchmarkRecord.getReadBytes());
                    diskReads.add(diskRead);
                    diskReadsSum += diskRead;

                    // network down
                    Long networkDown = Long.valueOf(benchmarkRecord.getNetworkDownloadedBytes());
                    networkDowns.add(networkDown);
                    networkDownsSum += networkDown;

                    // network up
                    Long networkUp = Long.valueOf(benchmarkRecord.getNetworkUploadedBytes());
                    networkUps.add(networkUp);
                    networkUpsSum += networkUp;

                    // latency
                    Long latency = benchmarkRecord.getLatency();
                    if (latency != null) {
                        latencies.add(latency);
                        latenciesSum += latency;
                    }
                } catch (Exception e){
                    logger.error("Error reading values from benchmark record. queryId: {}, error: {}",benchmarkRecord.getQueryID(), e.getLocalizedMessage());
                }
            }

            queryFunctionStatistics = new QueryFunctionStatistics(queryfunction);
            //query time
            queryFunctionStatistics.setTotalQueryTimePerQueryFunction_inMilliseconds(queryTimesSum);
            if(queryTimes.size() > 1) {
                queryFunctionStatistics.setMeanQueryTime_inMilliseconds(queryTimesSum / queryTimes.size());
                Collections.sort(queryTimes);
                queryFunctionStatistics.setMedianQueryTime_inMilliseconds(queryTimes.get(queryTimes.size() / 2));
            } else {
                queryFunctionStatistics.setMeanQueryTime_inMilliseconds(queryTimesSum);
                queryFunctionStatistics.setMedianQueryTime_inMilliseconds(queryTimesSum);
            }

            //cpu
            if(totalCpuUsages.size() > 1) {
                queryFunctionStatistics.setMeanTotalCpuUsagePerQuery_inPercent(totalCpuUsagesSum / totalCpuUsages.size());
                Collections.sort(totalCpuUsages);
                queryFunctionStatistics.setMedianTotalCpuUsagePerQuery_inPercent(totalCpuUsages.get(totalCpuUsages.size() / 2));
                queryFunctionStatistics.setMaximumCpuUsageRecorded_inPercent(totalCpuUsages.get(totalCpuUsages.size()-1));
            } else {
                queryFunctionStatistics.setMeanTotalCpuUsagePerQuery_inPercent(totalCpuUsagesSum);
                queryFunctionStatistics.setMedianTotalCpuUsagePerQuery_inPercent(totalCpuUsagesSum);
                queryFunctionStatistics.setMaximumCpuUsageRecorded_inPercent(totalCpuUsagesSum);
            }

            //disk usage
            if(diskUsages.size() > 1){
                queryFunctionStatistics.setMeanDiskUsagePerQuery_inBytes(diskUsagesSum / diskUsages.size());
                Collections.sort(diskUsages);
                queryFunctionStatistics.setMedianDiskUsagePerQuery_inBytes(diskUsages.get(diskUsages.size() / 2));
                queryFunctionStatistics.setMaximumDiskUsageRecorded_inBytes(Collections.max(totalDiskUsages));
            } else {
                queryFunctionStatistics.setMeanDiskUsagePerQuery_inBytes(diskUsagesSum);
                queryFunctionStatistics.setMedianDiskUsagePerQuery_inBytes(diskUsagesSum);
                queryFunctionStatistics.setMaximumDiskUsageRecorded_inBytes(diskUsagesSum);
            }

            // memory
            if(memoryUsages.size() > 1){
                queryFunctionStatistics.setMeanMemoryUsagePerQuery_inPercent(memoryUsagesSum / memoryUsages.size());
                Collections.sort(memoryUsages);
                queryFunctionStatistics.setMedianMemoryUsagePerQuery_inPercent(memoryUsages.get(memoryUsages.size() / 2));
            } else {
                queryFunctionStatistics.setMeanMemoryUsagePerQuery_inPercent(memoryUsagesSum);
                queryFunctionStatistics.setMedianMemoryUsagePerQuery_inPercent(memoryUsagesSum);
            }
            if(memoryTotalUsages.size() > 1){
                queryFunctionStatistics.setMeanTotalMemoryUsage_inPercent(memoryTotalUsagesSum / memoryTotalUsages.size());
                Collections.sort(memoryTotalUsages);
                queryFunctionStatistics.setMedianTotalMemoryUsage_inPercent(memoryTotalUsages.get(memoryTotalUsages.size() / 2));
                queryFunctionStatistics.setMaximumMemoryUsageRecorded_inPercent(memoryTotalUsages.get(memoryTotalUsages.size()-1));
            } else {
                queryFunctionStatistics.setMeanTotalMemoryUsage_inPercent(memoryTotalUsagesSum);
                queryFunctionStatistics.setMedianTotalMemoryUsage_inPercent(memoryTotalUsagesSum);
                queryFunctionStatistics.setMaximumMemoryUsageRecorded_inPercent(memoryTotalUsagesSum);
            }

            //disk write
            if(diskWrites.size() > 1){
                queryFunctionStatistics.setMeanDiskWrite_inBytes(diskWritesSum / diskWrites.size());
                Collections.sort(diskWrites);
                queryFunctionStatistics.setMedianDiskWrite_inBytes(diskWrites.get(diskWrites.size() / 2));
                queryFunctionStatistics.setTotalDiskWrite_inBytes(diskWritesSum);
            } else {
                queryFunctionStatistics.setMeanDiskWrite_inBytes(diskWritesSum);
                queryFunctionStatistics.setMedianDiskWrite_inBytes(diskWritesSum);
                queryFunctionStatistics.setTotalDiskWrite_inBytes(diskWritesSum);
            }

            //disk read
            if(diskReads.size() > 1){
                queryFunctionStatistics.setMeanDiskRead_inBytes(diskReadsSum / diskReads.size());
                Collections.sort(diskReads);
                queryFunctionStatistics.setMedianDiskRead_inBytes(diskReads.get(diskReads.size() / 2));
                queryFunctionStatistics.setTotalDiskRead_inBytes(diskReadsSum);
            } else {
                queryFunctionStatistics.setMeanDiskRead_inBytes(diskReadsSum);
                queryFunctionStatistics.setMedianDiskRead_inBytes(diskReadsSum);
                queryFunctionStatistics.setTotalDiskRead_inBytes(diskReadsSum);
            }

            // net download
            if(networkDowns.size() > 1){
                queryFunctionStatistics.setMeanNetworkDownload_inBytes(networkDownsSum / networkDowns.size());
                Collections.sort(networkDowns);
                queryFunctionStatistics.setMedianNetworkDownload_inBytes(networkDowns.get(networkDowns.size() / 2));
                queryFunctionStatistics.setTotalNetworkDownload_inBytes(networkDownsSum);
            } else {
                queryFunctionStatistics.setMeanNetworkDownload_inBytes(networkDownsSum);
                queryFunctionStatistics.setMedianNetworkDownload_inBytes(networkDownsSum);
                queryFunctionStatistics.setTotalNetworkDownload_inBytes(networkDownsSum);
            }

            // net upload
            if(networkUps.size() > 1){
                queryFunctionStatistics.setMeanNetworkUpload_inBytes(networkUpsSum / networkUps.size());
                Collections.sort(networkUps);
                queryFunctionStatistics.setMedianNetworkUpload_inBytes(networkUps.get(networkUps.size() / 2));
                queryFunctionStatistics.setTotalNetworkUpload_inBytes(networkUpsSum);
            } else {
                queryFunctionStatistics.setMeanNetworkUpload_inBytes(networkUpsSum);
                queryFunctionStatistics.setMedianNetworkUpload_inBytes(networkUpsSum);
                queryFunctionStatistics.setTotalNetworkUpload_inBytes(networkUpsSum);
            }

            // latency
            if(latencies.size() > 1){
                queryFunctionStatistics.setMeanLatency_inMilliseconds(latenciesSum / latencies.size());
                Collections.sort(latencies);
                queryFunctionStatistics.setMedianLatency_inMilliseconds(latencies.get(latencies.size() / 2));
            } else {
                queryFunctionStatistics.setMeanLatency_inMilliseconds(latenciesSum);
                queryFunctionStatistics.setMedianLatency_inMilliseconds(latenciesSum);
            }

            return queryFunctionStatistics;
        }
    }

}
