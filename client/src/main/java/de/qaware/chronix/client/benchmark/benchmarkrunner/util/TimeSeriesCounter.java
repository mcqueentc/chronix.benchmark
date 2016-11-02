package de.qaware.chronix.client.benchmark.benchmarkrunner.util;

import de.qaware.chronix.shared.QueryUtil.JsonTimeSeriesHandler;
import de.qaware.chronix.database.TimeSeriesMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by mcqueen666 on 13.09.16.
 */
public class TimeSeriesCounter {

    private static TimeSeriesCounter instance;

    private final Logger logger = LoggerFactory.getLogger(TimeSeriesCounter.class);
    private JsonTimeSeriesHandler jsonTimeSeriesHandler;


    private TimeSeriesCounter(){
        jsonTimeSeriesHandler = JsonTimeSeriesHandler.getInstance();
    }

    public static TimeSeriesCounter getInstance(){
        if(instance == null){
            instance = new TimeSeriesCounter();
        }
        return instance;
    }

    /**
     * Returns meta data for all previously imported time series.
     *
     * @return list of all imported meta data. Empty if nothing was imported previously.
     */
    public List<TimeSeriesMetaData> getAll(){
        List<TimeSeriesMetaData> metaDataList = new ArrayList<>();
        File directory = new File(jsonTimeSeriesHandler.getTimeSeriesMetaDataRecordDirectoryPath());
        if(directory.exists() && directory.isDirectory()){
            File[] measurements = directory.listFiles();
            if(measurements != null && measurements.length > 0) {
                for (File measurement : measurements) {
                    if (measurement.isFile() && measurement.getName().endsWith(".json")) {
                        metaDataList.addAll(jsonTimeSeriesHandler.readTimeSeriesMetaDatafromJson(measurement.getName().replace(".json","")));
                    }
                }
            } else {
                logger.error("TimeSeriesCounter: No meta data to read.");
            }
        }
        return metaDataList;
    }

    /**
     * Returns meta data for a random time series size times in a list. (for cache testing)
     *
     * @param size number of how many elements the list should have.
     * @return list if meta data.
     */
    public List<TimeSeriesMetaData> getCachingTestMetaData(int size){
        List<TimeSeriesMetaData> allMetaData = this.getAll();

        return this.getCachingTestMetaData(allMetaData, size);
    }

    /**
     * Returns meta data for a random time series size times in a list from given meta data. (for cache testing)
     *
     * @param timeSeriesMetaDataList meta data from which a random time series should be chosen.
     * @param size number of how many elements the list should have.
     * @return list if meta data.
     */
    public List<TimeSeriesMetaData> getCachingTestMetaData(List<TimeSeriesMetaData> timeSeriesMetaDataList, int size){
        if(size < 0) size *= -1;
        List<TimeSeriesMetaData> cachingTestMetaData = new ArrayList<>(size);
        if(!timeSeriesMetaDataList.isEmpty()){
            Random random = new Random();
            TimeSeriesMetaData metaData = timeSeriesMetaDataList.get(random.nextInt(timeSeriesMetaDataList.size()));
            for(int i = 0; i < size; i++){
                cachingTestMetaData.add(metaData);
            }
        }
        return cachingTestMetaData;
    }

    /**
     * Returns meta data for random time series of all previously imported time series.
     *
     * @param size number of how many elements the list should have.
     * @return list of meta data.
     */
    public List<TimeSeriesMetaData> getRandomTimeSeriesMetaData(int size){
        List<TimeSeriesMetaData> allMetaData = this.getAll();

        return this.getRandomTimeSeriesMetaData(allMetaData, size);
    }

    /**
     * Returns meta data for random time series from given meta data.
     *
     * @param timeSeriesMetaDataList meta data from which the random time series should be chosen.
     * @param size number of how many elements the list should have.
     * @return list of meta data.
     */
    public List<TimeSeriesMetaData> getRandomTimeSeriesMetaData(List<TimeSeriesMetaData> timeSeriesMetaDataList, int size){
        if(size < 0) size *= -1;
        List<TimeSeriesMetaData> metaDataList = new ArrayList<>(size);
        if(!timeSeriesMetaDataList.isEmpty()){
            Random random = new Random();
            int maxRetryCount = 10000;
            int retryCount = 0;
            for(int i = 0; i < size; i++){
                if(retryCount >= maxRetryCount)break;
                TimeSeriesMetaData ts = timeSeriesMetaDataList.get(random.nextInt(timeSeriesMetaDataList.size()));
                TimeSeriesMetaData randomMetaData = new TimeSeriesMetaData(ts);
                /*logger.info("random ts: Measurement: {}, MetricName: {}, Start: {}, End: {}",
                        randomMetaData.getMeasurementName(),
                        randomMetaData.getMetricName(),
                        randomMetaData.getStart(),
                        randomMetaData.getEnd());
                logger.info("random ts: Measurement: {}, MetricName: {}, Start: {}, End: {}",
                        ts.getMeasurementName(),
                        ts.getMetricName(),
                        ts.getStart(),
                        ts.getEnd());*/


                if(randomMetaData.getStart() < randomMetaData.getEnd()) {
                    //logger.info("Start: {}, End: {}", randomMetaData.getStart(), randomMetaData.getEnd());
                    long randomStart = random.longs(randomMetaData.getStart(), randomMetaData.getEnd()).iterator().next();
                    long randomEnd = random.longs(randomStart, randomMetaData.getEnd() + 1L).iterator().next();
                    randomMetaData.setStart(randomStart);
                    randomMetaData.setEnd(randomEnd);

                    /*logger.info("randomized ts: Measurement: {}, MetricName: {}, Start: {}, End: {}",
                            randomMetaData.getMeasurementName(),
                            randomMetaData.getMetricName(),
                            randomMetaData.getStart(),
                            randomMetaData.getEnd());
                    logger.info("randomized ts: Measurement: {}, MetricName: {}, Start: {}, End: {}",
                            ts.getMeasurementName(),
                            ts.getMetricName(),
                            ts.getStart(),
                            ts.getEnd());*/

                    metaDataList.add(randomMetaData);
                } else {
                    i--; // ugly quick fix
                    retryCount++; // prevent possible endless loop
                }
                /*else {
                    // time series only contains one point, adjust end for kairosdb to not get error during query
                    //randomMetaData.setEnd(randomMetaData.getEnd() + 1L);
                    //metaDataList.add(randomMetaData);
                    *//*logger.info("start >= end: Measurement: {}, MetricName: {}, Start: {}, End: {}",
                            randomMetaData.getMeasurementName(),
                            randomMetaData.getMetricName(),
                            randomMetaData.getStart(),
                            randomMetaData.getEnd());*//*
                }*/
            }
        }
        return metaDataList;
    }

}

