package de.qaware.chronix.client.benchmark.resultpresenter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.qaware.chronix.client.benchmark.benchmarkrunner.BenchmarkRunner;
import de.qaware.chronix.client.benchmark.configurator.Configurator;
import de.qaware.chronix.shared.QueryUtil.BenchmarkRecord;
import de.qaware.chronix.shared.QueryUtil.JsonTimeSeriesHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Stream;

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

    public void doBenchmarkRecordsAnalysis(){
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


}
