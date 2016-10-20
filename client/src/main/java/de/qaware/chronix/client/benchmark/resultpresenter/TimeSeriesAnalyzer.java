package de.qaware.chronix.client.benchmark.resultpresenter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.management.OperatingSystemMXBean;
import de.qaware.chronix.database.TimeSeries;
import de.qaware.chronix.database.TimeSeriesPoint;
import de.qaware.chronix.shared.QueryUtil.JsonTimeSeriesHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
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
        statsFilePath = statisticsDirectory + File.separator + "timeseries_analytics.json";
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

        Collections.shuffle(allFiles);

        List<Double> pointsPerTimeSeries = new ArrayList<>(allFiles.size());
        List<Double> minimumValuePerTimeSeries = new ArrayList<>(allFiles.size());
        List<Double> maximumValuePerTimeSeries = new ArrayList<>(allFiles.size());
        List<Double> meanValuePerTimeSeries = new ArrayList<>(allFiles.size());
        List<Double> sampleCovarianceValuePerTimeSeries = new ArrayList<>(allFiles.size());
        List<Double> meanValueChangeRatePerTimeSeries = new ArrayList<>(allFiles.size());
        List<Double> measurementDurationPerTimeSeries = new ArrayList<>(allFiles.size());
        List<Double> meanSamplingIntervalPerTimeSeries = new ArrayList<>(allFiles.size());


        // start the threads
        Future<Long> futureFileSize = executorService.submit(new FileSizeCalculator(allFiles));

        List<Future<Map<String, List<Double>>>> futureAnalyticsListOfMaps = new LinkedList<>();
        int threadId = 0;
        int batchsize = allFiles.size() / oSMXBean.getAvailableProcessors();
        int from = 0;
        for(int i = batchsize; i < allFiles.size(); i = i + batchsize){
            futureAnalyticsListOfMaps.add(executorService.submit(new AnalyzerThread(threadId++, allFiles.subList(from, i))));

            from = i;
        }
        futureAnalyticsListOfMaps.add(executorService.submit(new AnalyzerThread(threadId, allFiles.subList(from, allFiles.size()))));

        // collect results
        try {
            Long totalFileSize = futureFileSize.get();

            for(Future<Map<String, List<Double>>> futureMap : futureAnalyticsListOfMaps){
                Map<String, List<Double>> analyticsMap = futureMap.get();
                pointsPerTimeSeries.addAll(analyticsMap.get("pointsPerTimeSeries"));
                minimumValuePerTimeSeries.addAll(analyticsMap.get("minimumValuePerTimeSeries"));
                maximumValuePerTimeSeries.addAll(analyticsMap.get("maximumValuePerTimeSeries"));
                meanValuePerTimeSeries.addAll(analyticsMap.get("meanValuePerTimeSeries"));
                sampleCovarianceValuePerTimeSeries.addAll(analyticsMap.get("sampleCovarianceValuePerTimeSeries"));
                meanValueChangeRatePerTimeSeries.addAll(analyticsMap.get("meanValueChangeRatePerTimeSeries"));
                measurementDurationPerTimeSeries.addAll(analyticsMap.get("measurementDurationPerTimeSeries"));
                meanSamplingIntervalPerTimeSeries.addAll(analyticsMap.get("meanSamplingIntervalPerTimeSeries"));

            }

            executorService.shutdown();
            if(!executorService.awaitTermination(10000, TimeUnit.MILLISECONDS)){
                executorService.shutdownNow();
            }

            // calc set statistics
            Collections.sort(pointsPerTimeSeries);

            double numberOfTotalPoints = 0d;
            for(Double points : pointsPerTimeSeries){
                numberOfTotalPoints += points;
            }

            int numberOfTimeSeries = 1;
            if(pointsPerTimeSeries.size() > 0){
                numberOfTimeSeries = pointsPerTimeSeries.size();
            }
            double minNumberOfPointsPerTimeSeries = Collections.min(pointsPerTimeSeries);
            double maxNumberOfPointsPerTimeSeries = Collections.max(pointsPerTimeSeries);
            double averagePointsPerTimeSeries = numberOfTotalPoints / numberOfTimeSeries;
            double medianPointsPerTimeSeries = pointsPerTimeSeries.get(pointsPerTimeSeries.size() / 2);

            //calc value statistics
            Collections.sort(meanValuePerTimeSeries);
            Collections.sort(sampleCovarianceValuePerTimeSeries);

            double minValueOfAllTimeSeries = Collections.min(minimumValuePerTimeSeries);
            double maxValueOfAllTimeSeries = Collections.max(maximumValuePerTimeSeries);

            double meanValueofAllTimeSeries = 0d;
            for(Double meanValue : meanValuePerTimeSeries){
                meanValueofAllTimeSeries += meanValue;
            }
            if(meanValuePerTimeSeries.size() > 0) {
                meanValueofAllTimeSeries /= meanValuePerTimeSeries.size();
            }
            double medianOfAllMeanValues = meanValuePerTimeSeries.get(meanValuePerTimeSeries.size() / 2);

            double meanSampleCovarianceOfAllTimeSeries = 0d;
            for(Double s_2 : sampleCovarianceValuePerTimeSeries){
                meanSampleCovarianceOfAllTimeSeries += s_2;
            }
            if(sampleCovarianceValuePerTimeSeries.size() > 0) {
                meanSampleCovarianceOfAllTimeSeries /= sampleCovarianceValuePerTimeSeries.size();
            }
            double medianSampleCovarianceOfAllTimeSeries = sampleCovarianceValuePerTimeSeries.get(sampleCovarianceValuePerTimeSeries.size() / 2);

            double meanValueChangeRateOfAllTimeseries = 0d;
            for(Double changeRate : meanValueChangeRatePerTimeSeries){
                meanValueChangeRateOfAllTimeseries += changeRate;
            }
            if(meanValueChangeRatePerTimeSeries.size() > 0){
                meanValueChangeRateOfAllTimeseries /= meanValueChangeRatePerTimeSeries.size();
            }

            double meanMeasurementDurationOfAllTimeSeries = 0d;
            for(Double duration : measurementDurationPerTimeSeries){
                meanMeasurementDurationOfAllTimeSeries += duration;
            }
            if(measurementDurationPerTimeSeries.size() > 0){
                meanMeasurementDurationOfAllTimeSeries /= measurementDurationPerTimeSeries.size();
            }

            double meanSamplingIntervalOfAllTimeSeries = 0d;
            for(Double interval : meanSamplingIntervalPerTimeSeries){
                meanSamplingIntervalOfAllTimeSeries += interval;
            }
            if(meanSamplingIntervalPerTimeSeries.size() > 0){
                meanSamplingIntervalOfAllTimeSeries /= meanSamplingIntervalPerTimeSeries.size();
            }

            //set data set stats
            timeSeriesStatistics.setDate(Instant.now().toString());
            timeSeriesStatistics.setMeasurements(measurements);
            timeSeriesStatistics.setTotalSizeInBytes(totalFileSize);
            timeSeriesStatistics.setNumberOfTimeSeries(numberOfTimeSeries);
            timeSeriesStatistics.setNumberOfTotalPoints((long)numberOfTotalPoints);
            timeSeriesStatistics.setMinNumberOfPointsPerTimeSeries((long)minNumberOfPointsPerTimeSeries);
            timeSeriesStatistics.setMaxNumberOfPointsPerTimeSeries((long)maxNumberOfPointsPerTimeSeries);
            timeSeriesStatistics.setAveragePointsPerTimeSeries((long)averagePointsPerTimeSeries);
            timeSeriesStatistics.setMedianPointsPerTimeSeries((long)medianPointsPerTimeSeries);

            //set value set stats
            timeSeriesStatistics.setMinValueOfAllTimeSeries(minValueOfAllTimeSeries);
            timeSeriesStatistics.setMaxValueOfAllTimeSeries(maxValueOfAllTimeSeries);
            timeSeriesStatistics.setMeanValueOfAllTimeSeries(meanValueofAllTimeSeries);
            timeSeriesStatistics.setMedianOfAllMeanValues(medianOfAllMeanValues);
            timeSeriesStatistics.setMeanSampleStandardDeviationOfAllTimeSeries((long)(Math.sqrt(meanSampleCovarianceOfAllTimeSeries)));
            timeSeriesStatistics.setMedianSampleStandardDeviationOfAllTimeSeries((long)(Math.sqrt(medianSampleCovarianceOfAllTimeSeries)));
            timeSeriesStatistics.setMeanValueChangeRateOfAllTimeSeries((long)meanValueChangeRateOfAllTimeseries);
            timeSeriesStatistics.setMeanMeasurementDurationOfAllTimeSeries_inSeconds((long)(meanMeasurementDurationOfAllTimeSeries / 1000));
            timeSeriesStatistics.setMeanSamplingIntervalOfAllTimeSeries_inMilliseconds((long)meanSamplingIntervalOfAllTimeSeries);

            // write to file
            writeStatsJson(timeSeriesStatistics);

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
            logger.info("File size calculator finished.");
            return byteSum;
        }
    }

    private class AnalyzerThread implements Callable<Map<String, List<Double>>>{

        private final int id;
        private List<File> files;
        private final int BATCHSIZE = 25;

        public AnalyzerThread(int id, List<File> files){
            this.id = id;
            this.files = files;
        }

        @Override
        public Map<String, List<Double>> call() {
            List<Double> pointsPerTimeSeries = new ArrayList<>(files.size());
            List<Double> minimumValuePerTimeSeries = new ArrayList<>(files.size());
            List<Double> maximumValuePerTimeSeries = new ArrayList<>(files.size());
            List<Double> meanValuePerTimeSeries = new ArrayList<>(files.size());
            List<Double> sampleCovarianceValuePerTimeSeries = new ArrayList<>(files.size());
            List<Double> meanValueChangeRatePerTimeSeries = new ArrayList<>(files.size());
            List<Double> measurementDurationPerTimeSeries = new ArrayList<>(files.size());
            List<Double> meanSamplingIntervalPerTimeSeries = new ArrayList<>(files.size());

            List<TimeSeries> timeSeries;
            int from = 0;
            for(int i = BATCHSIZE; i < files.size(); i = i + BATCHSIZE){
                logger.info("Thread {}, reading timeseries from {} to {}, left {}", id, from, i, files.size() - i);

                timeSeries = JsonTimeSeriesHandler.getInstance().readTimeSeriesJson(files.subList(from, i).toArray(new File[]{}));
                from = i;
                Map<String, List<Double>> valueAnalytics = analyzeValues(timeSeries);

                pointsPerTimeSeries.addAll(valueAnalytics.get("pointsPerTimeSeries"));
                minimumValuePerTimeSeries.addAll(valueAnalytics.get("minimumValuePerTimeSeries"));
                maximumValuePerTimeSeries.addAll(valueAnalytics.get("maximumValuePerTimeSeries"));
                meanValuePerTimeSeries.addAll(valueAnalytics.get("meanValuePerTimeSeries"));
                sampleCovarianceValuePerTimeSeries.addAll(valueAnalytics.get("sampleCovarianceValuePerTimeSeries"));
                meanValueChangeRatePerTimeSeries.addAll(valueAnalytics.get("meanValueChangeRatePerTimeSeries"));
                measurementDurationPerTimeSeries.addAll(valueAnalytics.get("measurementDurationPerTimeSeries"));
                meanSamplingIntervalPerTimeSeries.addAll(valueAnalytics.get("meanSamplingIntervalPerTimeSeries"));

            }
            timeSeries = JsonTimeSeriesHandler.getInstance().readTimeSeriesJson(files.subList(from, files.size()).toArray(new File[]{}));
            // get last analytics for left over points and add the previous analytics to the map
            Map<String, List<Double>> valueAnalytics = analyzeValues(timeSeries);
            valueAnalytics.get("pointsPerTimeSeries").addAll(pointsPerTimeSeries);
            valueAnalytics.get("minimumValuePerTimeSeries").addAll(minimumValuePerTimeSeries);
            valueAnalytics.get("maximumValuePerTimeSeries").addAll(maximumValuePerTimeSeries);
            valueAnalytics.get("meanValuePerTimeSeries").addAll(meanValuePerTimeSeries);
            valueAnalytics.get("sampleCovarianceValuePerTimeSeries").addAll(sampleCovarianceValuePerTimeSeries);
            valueAnalytics.get("meanValueChangeRatePerTimeSeries").addAll(meanValueChangeRatePerTimeSeries);
            valueAnalytics.get("measurementDurationPerTimeSeries").addAll(measurementDurationPerTimeSeries);
            valueAnalytics.get("meanSamplingIntervalPerTimeSeries").addAll(meanSamplingIntervalPerTimeSeries);

            logger.info("Thread {} finished.", id);
            return valueAnalytics;

        }

        private Map<String, List<Double>> analyzeValues(List<TimeSeries> timeSeries){
            List<Double> pointsPerTimeSeries = new ArrayList<>(timeSeries.size());
            List<Double> minimumValuePerTimeSeries = new ArrayList<>(timeSeries.size());
            List<Double> maximumValuePerTimeSeries = new ArrayList<>(timeSeries.size());
            List<Double> meanValuePerTimeSeries = new ArrayList<>(timeSeries.size());
            List<Double> sampleCovarianceValuePerTimeSeries = new ArrayList<>(timeSeries.size());
            List<Double> meanValueChangeRatePerTimeSeries = new ArrayList<>(timeSeries.size());
            List<Double> measurementDurationPerTimeSeries = new ArrayList<>(timeSeries.size());
            List<Double> meanSamplingIntervalPerTimeSeries = new ArrayList<>(timeSeries.size());
            for(TimeSeries ts : timeSeries){
                pointsPerTimeSeries.add((double)ts.getPoints().size());

                // analyze values
                double min = Double.MAX_VALUE;
                double max = Double.MIN_VALUE;
                double sum = 0d;
                try {
                    for (TimeSeriesPoint point : ts.getPoints()) {
                        if (!point.getValue().isNaN()) {
                            sum += point.getValue();
                            if (point.getValue().compareTo(min) < 0) {
                                min = point.getValue();
                            }
                            if (point.getValue().compareTo(max) > 0) {
                                max = point.getValue();
                            }
                        }
                    }
                } catch (Exception e){
                    logger.error("Error calculating point value statistics: {}", e.getLocalizedMessage());
                }

                minimumValuePerTimeSeries.add(min);
                maximumValuePerTimeSeries.add(max);

                //sample covariance -> https://de.wikipedia.org/wiki/Korrigierte_Stichprobenvarianz
                double mean = 0d;
                if(ts.getPoints().size() > 0) {
                    mean = sum / ts.getPoints().size();
                }
                double sum_for_sample_covariance = 0d;
                double lastChangedValue = ts.getPoints().get(0).getValue();
                double changeCounter = 0L;
                double sum_for_sampling_interval = 0d;
                long previousTimeStamp = ts.getPoints().get(0).getTimeStamp();
                try {
                    for (TimeSeriesPoint point : ts.getPoints()) {
                        if (!point.getValue().isNaN()) {
                            sum_for_sample_covariance += Math.pow((point.getValue() - mean),2);

                            // calc meanValueChangeRatePerTimeSeries (count if value changes)
                            if(point.getValue().compareTo(lastChangedValue) != 0){
                                changeCounter++;
                                lastChangedValue = point.getValue();
                            }

                            //calc sampling interval (points are sorted)
                            if(point.getTimeStamp() != previousTimeStamp){
                                sum_for_sampling_interval += (point.getTimeStamp() - previousTimeStamp);
                                previousTimeStamp = point.getTimeStamp();
                            }

                        }
                    }
                } catch (Exception e){
                    logger.error("Error calculating sample covariance: {}", e.getLocalizedMessage());
                }
                double sample_covariance = 0d;
                if(ts.getPoints().size() > 1) {
                    sample_covariance = sum_for_sample_covariance / (ts.getPoints().size() - 1);
                }

                meanValuePerTimeSeries.add(mean);
                sampleCovarianceValuePerTimeSeries.add(sample_covariance);

                // calc meanValueChangeRatePerTimeSeries
                if(changeCounter != 0d){
                    meanValueChangeRatePerTimeSeries.add(ts.getPoints().size() / changeCounter);
                } else {
                    meanValueChangeRatePerTimeSeries.add(changeCounter);
                }

                // measurementDurationPerTimeSeries
                measurementDurationPerTimeSeries.add((double)(ts.getEnd() - ts.getStart()));

                // meanSamplingIntervalPerTimeSeries (point.size() - 1 comparisons)
                meanSamplingIntervalPerTimeSeries.add(sum_for_sampling_interval / ts.getPoints().size() - 1);
                /*
            List<Double> meanSamplingIntervalPerTimeSeries = new ArrayList<>(timeSeries.size());

            valueAnalytics.get("meanSamplingIntervalPerTimeSeries").addAll(meanSamplingIntervalPerTimeSeries);
                 */


            }

            Map<String, List<Double>> valueAnalytics = new HashMap<>();
            valueAnalytics.put("pointsPerTimeSeries", pointsPerTimeSeries);
            valueAnalytics.put("minimumValuePerTimeSeries", minimumValuePerTimeSeries);
            valueAnalytics.put("maximumValuePerTimeSeries", maximumValuePerTimeSeries);
            valueAnalytics.put("meanValuePerTimeSeries", meanValuePerTimeSeries);
            valueAnalytics.put("sampleCovarianceValuePerTimeSeries", sampleCovarianceValuePerTimeSeries);
            valueAnalytics.put("meanValueChangeRatePerTimeSeries", meanValueChangeRatePerTimeSeries);
            valueAnalytics.put("measurementDurationPerTimeSeries", measurementDurationPerTimeSeries);
            valueAnalytics.put("meanSamplingIntervalPerTimeSeries", meanSamplingIntervalPerTimeSeries);

            return valueAnalytics;
        }
    }

    private void writeStatsJson(TimeSeriesStatistics timeSeriesStatistics){
        File statsFile = new File(statsFilePath);
        ObjectMapper mapper = new ObjectMapper();
        try {
            final String statisticsString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(timeSeriesStatistics);
            if (statsFile.exists()) {
                Files.write(statsFile.toPath(), Arrays.asList(statisticsString), StandardOpenOption.APPEND);
            } else {
                Files.write(statsFile.toPath(), Arrays.asList(statisticsString), StandardOpenOption.CREATE);
            }
        } catch (Exception e){
            logger.error("Error writing stats to json file: {}", e.getLocalizedMessage());
        }
    }


}
