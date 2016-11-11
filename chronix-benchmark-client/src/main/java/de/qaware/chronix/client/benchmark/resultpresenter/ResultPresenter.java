package de.qaware.chronix.client.benchmark.resultpresenter;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.qaware.chronix.client.benchmark.resultpresenter.plot.StatisticsBarPlotter;
import de.qaware.chronix.database.BenchmarkDataSource.QueryFunction;
import de.qaware.chronix.common.QueryUtil.JsonTimeSeriesHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * Created by mcqueen666 on 20.06.16.
 */
public class ResultPresenter {
    private static ResultPresenter instance;

    private final Logger logger = LoggerFactory.getLogger(ResultPresenter.class);
    private final String statisticsDirectory = System.getProperty("user.home")
            + File.separator
            + "chronixBenchmark"
            + File.separator
            + "statistics";

    private ResultPresenter(){
        File dir = new File(statisticsDirectory);
        if(!dir.exists()){
            dir.mkdirs();
        }

    }

    public static ResultPresenter getInstance(){
        if(instance == null){
            instance = new ResultPresenter();
        }
        return instance;
    }

    public String getStatisticsDirectory() {
        return statisticsDirectory;
    }

    /**
     * Analyzes all json time series saved at chronixBenchmark/timeseries_records
     */
    public void analyzeTimeSeries(){
        File directory = new File(JsonTimeSeriesHandler.getInstance().getTimeSeriesJsonRecordDirectoryPath());
        File[] files = directory.listFiles();
        List<File> measurements = new LinkedList<>();
        for(File dir : files){
            if(dir.isDirectory()){
                measurements.add(dir);
            }
        }

        TimeSeriesAnalyzer timeSeriesAnalyzer = new TimeSeriesAnalyzer();
        TimeSeriesStatistics statistics = timeSeriesAnalyzer.analyzeJsonTimeSeries(measurements);
        ObjectMapper mapper = new ObjectMapper();
        try {
            final String statisticsJsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(statistics);
            System.out.println(statisticsJsonString);

        } catch (Exception e) {
           logger.error("Error generating json string: " + e.getLocalizedMessage());
        }
    }

    /**
     * Analyzes json time series in given directory.
     *
     * @param timeSeriesDirectory the absolute directory path to the time series to analyse.
     */
    public void analyzeTimeSeries(File timeSeriesDirectory){
        if(timeSeriesDirectory != null && timeSeriesDirectory.exists() && timeSeriesDirectory.isDirectory()) {
            List<File> measurements = new LinkedList<>();
            measurements.add(timeSeriesDirectory);

            TimeSeriesAnalyzer timeSeriesAnalyzer = new TimeSeriesAnalyzer();
            TimeSeriesStatistics statistics = timeSeriesAnalyzer.analyzeJsonTimeSeries(measurements);
            ObjectMapper mapper = new ObjectMapper();
            try {
                final String statisticsJsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(statistics);
                System.out.println(statisticsJsonString);

            } catch (Exception e) {
                logger.error("Error generating json string: " + e.getLocalizedMessage());
            }
        } else {
            logger.error(timeSeriesDirectory + "is not a directory or does not exist.");
        }
    }

    /**
     * Analyzes and prints the benchmark records download from benchmark server.
     */
    public void doBenchmarkRecordsAnalysis(){
        logger.info("Analyzing benchmark records ...");
        TsdbStatisticsAnalyzer tsdbStatisticsAnalyzer = new TsdbStatisticsAnalyzer(statisticsDirectory);
        List<TsdbStatistics> tsdbStatisticsList = tsdbStatisticsAnalyzer.analyzeBenchmarkRecords();
        if(tsdbStatisticsList != null && ! tsdbStatisticsList.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                for(TsdbStatistics tsdbStatistics : tsdbStatisticsList){
                    final String tsdbStatisticsJsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(tsdbStatistics);
                    System.out.println(tsdbStatisticsJsonString);
                }

            } catch (Exception e) {
                logger.error("Error generating json sting: {}", e.getLocalizedMessage());
            }
        } else {
            logger.info("No benchmark statistics found");
        }
    }

    /**
     * Plots bar charts of the analyzed tsdb statistics to jpeg files to chronixBenchmark/statistics/bar_plots
     */
    public void plotBenchmarkStatistics(){
        TsdbStatisticsAnalyzer tsdbStatisticsAnalyzer = new TsdbStatisticsAnalyzer(statisticsDirectory);
        if(! tsdbStatisticsAnalyzer.tsdbStatisticsFileExists()){
            doBenchmarkRecordsAnalysis();
        }
        // plot for queryfunctions.
        List<String> queryFunctions = new ArrayList<>(QueryFunction.values().length-1);
        for(QueryFunction function : QueryFunction.values()){
            if(function != QueryFunction.QUERY_ONLY) {
                queryFunctions.add(function.toString());
            }
        }

        StatisticsBarPlotter statisticsBarPlotter = new StatisticsBarPlotter(statisticsDirectory);
        //plot the aggregation functions
        statisticsBarPlotter.plotTsdbStatisticsForQueryFunctions(queryFunctions);

        //plot range query
        queryFunctions.clear();
        queryFunctions.add(QueryFunction.QUERY_ONLY.toString());
        statisticsBarPlotter.plotTsdbStatisticsForQueryFunctions(queryFunctions);

        //plot for import only
        statisticsBarPlotter.plotTsdbStatisticsForQueryFunctions(Collections.singletonList("import"));


        //plot throughput
        TimeSeriesStatistics throughputTimeSeriesStatistics = getTimeSeriesStatisticsForThroughput();
        if(throughputTimeSeriesStatistics != null){
            statisticsBarPlotter.plotThroughput(throughputTimeSeriesStatistics);
        } else {
            logger.info("No TimeSeriesStatistics for plotting found!");
        }


    }

    private TimeSeriesStatistics getTimeSeriesStatisticsForThroughput(){
        TimeSeriesStatistics throughputTimeSeriesStatistics = null;

        // get imported measurements
        List<String> measurementNames = new LinkedList<>();
        File[] measurementDirs = new File(JsonTimeSeriesHandler.getInstance().getTimeSeriesMetaDataRecordDirectoryPath()).listFiles();
        if(measurementDirs != null){
            for(File measurement : measurementDirs){
                if(measurement.isFile() && measurement.getName().endsWith(".json")){
                    measurementNames.add(measurement.getName().replaceAll(".json", ""));
                }
            }
        }
        Collections.sort(measurementNames);
        String importedMeasurements = String.join("_", measurementNames);

        // get the stats for imported data set.
        List<TimeSeriesStatistics> timeSeriesStatisticsList = new TimeSeriesAnalyzer().readStatsJson();
        for(TimeSeriesStatistics timeSeriesStatistics : timeSeriesStatisticsList){
            List<String> recordedMeasurements = timeSeriesStatistics.getMeasurements();
            Collections.sort(recordedMeasurements);
            String joinedRecordedMeasurements = String.join("_", recordedMeasurements);

            if(joinedRecordedMeasurements.equals(importedMeasurements)){
                throughputTimeSeriesStatistics = timeSeriesStatistics;
                break;
            }

        }
        return throughputTimeSeriesStatistics;
    }

}
