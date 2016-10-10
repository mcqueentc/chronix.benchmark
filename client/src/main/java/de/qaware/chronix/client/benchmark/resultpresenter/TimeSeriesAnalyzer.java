package de.qaware.chronix.client.benchmark.resultpresenter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.management.OperatingSystemMXBean;
import de.qaware.chronix.database.TimeSeries;
import de.qaware.chronix.shared.QueryUtil.JsonTimeSeriesHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by mcqueen666 on 10.10.16.
 */
public class TimeSeriesAnalyzer {

    private final Logger logger = LoggerFactory.getLogger(TimeSeriesAnalyzer.class);
    private String statisticsDirectory;
    private String statsFilePath;

    public TimeSeriesAnalyzer(){
        statisticsDirectory = ResultPresenter.getInstance().getStatisticsDirectory();
        statsFilePath = statisticsDirectory + File.separator + "timeseries_analytics";
    }

    /**
     * Analyzes gzipped json time series and saves the results to statistics directory
     *
     * @param directories the measurement directories containing the time series.
     * @return the generated TimeSeriesStatistics.
     */
    public TimeSeriesStatistics analyzeJsonTimeSeries(List<File> directories){
        TimeSeriesStatistics timeSeriesStatistics = new TimeSeriesStatistics();
        OperatingSystemMXBean oSMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        ExecutorService executorService = Executors.newFixedThreadPool(oSMXBean.getAvailableProcessors());

        List<File> allFiles = new LinkedList<>();
        List<String> measurements = new LinkedList<>();
        // get all files
        for(File dir : directories){
            if(dir != null && dir.exists() && dir.isDirectory()){
                measurements.add(dir.getName());
                File[] files = dir.listFiles();
                if(files != null) {
                    allFiles.addAll(Arrays.asList(files));
                }
            }
        }

        // start the threads
        Future<Long> futureFileSize = executorService.submit(new FileSizeCalculator(allFiles));

        List<Future<List<Integer>>> futurePointCounts = new LinkedList<>();
        int batchsize = allFiles.size() / oSMXBean.getAvailableProcessors() - 1;
        int from = 0;
        for(int i = batchsize; i < allFiles.size(); i = i + batchsize){
            futurePointCounts.add(executorService.submit(new AnalyzerThread(allFiles.subList(from, i))));

            from = i;
        }
        futurePointCounts.add(executorService.submit(new AnalyzerThread(allFiles.subList(from, allFiles.size()))));

        // collect results
        try {
            Long totalFileSize = futureFileSize.get();
            List<Integer> pointCounts = new ArrayList<>(allFiles.size());
            for(Future<List<Integer>> future : futurePointCounts){
                pointCounts.addAll(future.get());
            }

            executorService.shutdown();
            if(!executorService.awaitTermination(10000, TimeUnit.MILLISECONDS)){
                executorService.shutdownNow();
            }

            // calc values
            Collections.sort(pointCounts);

            long numberOfTotalPoints = 0L;
            for(Integer pointsPerTimeSeries : pointCounts){
                numberOfTotalPoints += (long)pointsPerTimeSeries;
            }
            int numberOfTimeSeries = pointCounts.size();
            int minNumberOfPointsPerTimeSeries = Collections.min(pointCounts);
            int maxNumberOfPointsPerTimeSeries = Collections.max(pointCounts);
            int averagePointsPerTimeSeries = (int)(numberOfTotalPoints / (long)numberOfTimeSeries);
            int medianPointsPerTimeSeries = pointCounts.get(pointCounts.size() / 2);

            //set values
            timeSeriesStatistics.setDate(Instant.now().toString());
            timeSeriesStatistics.setMeasurements(measurements);
            timeSeriesStatistics.setTotalSizeInBytes(totalFileSize);
            timeSeriesStatistics.setNumberOfTimeSeries(numberOfTimeSeries);
            timeSeriesStatistics.setNumberOfTotalPoints(numberOfTotalPoints);
            timeSeriesStatistics.setMinNumberOfPointsPerTimeSeries(minNumberOfPointsPerTimeSeries);
            timeSeriesStatistics.setMaxNumberOfPointsPerTimeSeries(maxNumberOfPointsPerTimeSeries);
            timeSeriesStatistics.setAveragePointsPerTimeSeries(averagePointsPerTimeSeries);
            timeSeriesStatistics.setMedianPointsPerTimeSeries(medianPointsPerTimeSeries);

            // write to file
            File statsFile = new File(statsFilePath);
            ObjectMapper mapper = new ObjectMapper();
            final String statisticsString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(timeSeriesStatistics);
            if(statsFile.exists()){
                Files.write(statsFile.toPath(), Arrays.asList(statisticsString), StandardOpenOption.APPEND);
            } else {
                Files.write(statsFile.toPath(), Arrays.asList(statisticsString), StandardOpenOption.CREATE);
            }

        } catch (Exception e) {
            logger.error("Error retrieving results from threads:" + e.getLocalizedMessage());
        }

        return timeSeriesStatistics;
    }

    private class FileSizeCalculator implements Callable<Long> {

        private List<File> allFiles;

        public FileSizeCalculator(List<File> allFiles){
            this.allFiles = allFiles;
        }

        @Override
        public Long call() {
            long byteSum = 0L;
            for(File file : allFiles){
                if(file != null && file.isFile() && file.getName().endsWith(".gz")){
                    try {
                        byteSum += file.length();
                    } catch (Exception e){
                        //ignore
                    }
                }
            }
            return byteSum;
        }
    }

    private class AnalyzerThread implements Callable<List<Integer>>{

        private List<File> files;
        private final int BATCHSIZE = 25;

        public AnalyzerThread(List<File> files){
            this.files = files;
        }

        @Override
        public List<Integer> call() {
            List<Integer> pointsPerTimeSeries = new ArrayList<>(files.size());

            List<TimeSeries> timeSeries;
            int from = 0;
            for(int i = BATCHSIZE; i < files.size(); i = i + BATCHSIZE){
                    timeSeries = JsonTimeSeriesHandler.getInstance().readTimeSeriesJson(files.subList(from, i).toArray(new File[]{}));
                    from = i;

                    for(TimeSeries ts : timeSeries){
                        pointsPerTimeSeries.add(ts.getPoints().size());
                    }
            }
            timeSeries = JsonTimeSeriesHandler.getInstance().readTimeSeriesJson(files.subList(from, files.size()).toArray(new File[]{}));
            for(TimeSeries ts : timeSeries){
                pointsPerTimeSeries.add(ts.getPoints().size());
            }

            return pointsPerTimeSeries;
        }
    }


}
