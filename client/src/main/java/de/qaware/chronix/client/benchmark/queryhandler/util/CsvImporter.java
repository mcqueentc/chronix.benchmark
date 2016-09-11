package de.qaware.chronix.client.benchmark.queryhandler.util;

import com.sun.management.OperatingSystemMXBean;
import de.qaware.chronix.database.TimeSeries;
import de.qaware.chronix.database.TimeSeriesPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.text.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.GZIPInputStream;

/**
 * Created by mcqueen666 on 29.08.16.
 */
public class CsvImporter {

    private final Logger logger = LoggerFactory.getLogger(CsvImporter.class);


    /**
     * Converts time series in each given directory to json into chronixBenchmark directory.
     *
     * @apiNote File format: /measurement/host_process_group_metric.csv.(gz)
     *          file header line: Date;metricName1;metricName2;...
     *          file data:        2015-03-04T13:59:46.673Z;0.0;0.0;...
     *
     * @param directories the directories containing the time series csv files.
     */
    public void convertCsvToJson(List<File> directories){

        JsonTimeSeriesHandler jsonTimeSeriesHandler = JsonTimeSeriesHandler.getInstance();
        for(File directory : directories){
            if(directory.exists() && directory.isDirectory()){
                File[] files = directory.listFiles();
                List<File> fileParts = new ArrayList<>();
                for(int i = 0; i < files.length; i++){
                    fileParts.add(files[i]);
                    if(i != 0 && i % 500 == 0){
                        jsonTimeSeriesHandler.writeTimeSeriesJson(getTimeSeriesFromFiles(fileParts.toArray(new File[]{})));
                        fileParts.clear();
                        System.out.println(directory.getName() + ": " + i + " files converted.");
                    }
                }
                jsonTimeSeriesHandler.writeTimeSeriesJson(getTimeSeriesFromFiles(fileParts.toArray(new File[]{})));
                System.out.println(directory.getName() + ": " + files.length + " files converted.");
            }
        }
    }




    /**
     * Creates time series for each metricName from a csv file.
     *
     * @apiNote File format: /measurement/host_process_group_metric.csv.(gz)
     *          file header line: Date;metricName1;metricName2;...
     *          file data:        2015-03-04T13:59:46.673Z;0.0;0.0;...
     *
     * @param csvFiles the csv files from which to import (can also be gzip compressed)
     * @return a TimeSeries for each metricName or empty if nothing found.
     */
    public List<TimeSeries> getTimeSeriesFromFiles(File[] csvFiles){
        OperatingSystemMXBean oSMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        ExecutorService executorService = Executors.newFixedThreadPool(oSMXBean.getAvailableProcessors());

        List<TimeSeries> timeSeriesList = new LinkedList<>();
        if(csvFiles != null){
            List<TimeSeriesFileReader> fileReaders = new ArrayList<>();
            for(int i = 0; i < csvFiles.length; i++){
                if(csvFiles[i] != null && csvFiles[i].exists() && csvFiles[i].isFile()) {
                    fileReaders.add(new TimeSeriesFileReader(csvFiles[i]));
                }
            }
            try {
                List<Future<List<TimeSeries>>> futureList = executorService.invokeAll(fileReaders);

                for(Future<List<TimeSeries>> future : futureList){
                    timeSeriesList.addAll(future.get());
                }

                executorService.shutdown();
                if(!executorService.awaitTermination(1000, TimeUnit.MILLISECONDS)){
                    executorService.shutdownNow();
                }

            } catch (Exception e) {
                logger.error("Error CsvImporter getTimeSeriesFromFiles: " + e.getLocalizedMessage());
            }

        }

        return timeSeriesList;
    }


    class TimeSeriesFileReader implements Callable<List<TimeSeries>> {

        File csvFile;

        TimeSeriesFileReader(File csvFile){
            this.csvFile = csvFile;
        }

        @Override
        public List<TimeSeries> call() throws Exception {
            List<TimeSeries> timeSeries = new LinkedList<>();
            if(csvFile !=null && csvFile.exists()){
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
                NumberFormat nf = DecimalFormat.getInstance(Locale.ENGLISH);

                try {
                    InputStream inputStream = new FileInputStream(csvFile);

                    if(csvFile.getName().endsWith("gz")){
                        inputStream = new GZIPInputStream(inputStream);
                    }

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                    //read the first line
                    String headerLine = bufferedReader.readLine();
                    if(headerLine != null && !headerLine.isEmpty()){
                        //host _ process _ metricGroup
                        String[] fileNameMetaData = csvFile.getName().split("_");
                        String[] metrics = headerLine.split(";");
                        //build meta data object

                        String host = "";
                        String process = "";
                        String metricGroup = "";

                        try {
                            host = fileNameMetaData[0];
                            process = fileNameMetaData[1];
                            metricGroup = fileNameMetaData[2];
                        } catch (Exception e){
                            logger.info("Ignoring file: " + csvFile.getAbsolutePath());
                            return new LinkedList<>();
                        }


                        String measurement = csvFile.getParentFile().getName();

                        // create the tags
                        Map<String, String> tags = new HashMap<>();
                        tags.put("host", host);
                        tags.put("process", process);
                        tags.put("metricGroup",metricGroup);


                        // create metadata per metric
                        Map<Integer, TimeSeries> timeSeriesMapPerMetric = new HashMap<>();
                        for(int i = 1; i < metrics.length; i++){
                            String metric = metrics[i];
                            String metricNameOnlyAscii = Normalizer.normalize(metric, Normalizer.Form.NFD);
                            metricNameOnlyAscii = metricNameOnlyAscii.replaceAll("[^\\x00-\\x7F]", "");
                            metricNameOnlyAscii = metricNameOnlyAscii.replaceAll("\\*", "");
                            TimeSeries timeSeriesForMetric = new TimeSeries(measurement,metricNameOnlyAscii,new LinkedList<TimeSeriesPoint>(),tags,null,null);
                            timeSeriesMapPerMetric.put(i,timeSeriesForMetric);
                        }

                        // create the data points
                        String line;
                        boolean instantDate = true;
                        Instant dateObject = null;
                        while((line = bufferedReader.readLine()) != null){
                            String[] splits = line.split(";");
                            String date = splits[0];

                            try {
                                dateObject = Instant.parse(date);
                            } catch (Exception e) {
                                instantDate = false;
                            }
                            if(!instantDate){
                                try {
                                    dateObject = simpleDateFormat.parse(date).toInstant();
                                } catch (ParseException e) {
                                    dateObject = Instant.MIN;
                                }
                            }

                            String[] values = splits;
                            for(int column = 1; column < values.length; column++){
                                String value = values[column];
                                double numericValue = Double.MIN_VALUE;
                                try {
                                    numericValue = nf.parse(value).doubleValue();
                                } catch (ParseException e) {
                                }

                                if(!dateObject.equals(Instant.MIN) && numericValue != Double.MIN_VALUE){
                                    TimeSeriesPoint point = new TimeSeriesPoint(dateObject.toEpochMilli(),new Double(numericValue));
                                    timeSeriesMapPerMetric.get(column).addPointToTimeSeries(point);

                                }

                            }


                        }

                        // add the points to corresponding metricnName time series
                        timeSeries = new LinkedList<>();
                        for(Map.Entry<Integer, TimeSeries> entry : timeSeriesMapPerMetric.entrySet()){
                            TimeSeries ts = entry.getValue();
                            List<TimeSeriesPoint> allPoints = ts.getPoints();

                            /*
                            List<Long> timeStamps = new LinkedList<>();
                            allPoints.forEach(point -> timeStamps.add(point.getTimeStamp()));
                            Long start = Collections.min(timeStamps);
                            Long end = Collections.max(timeStamps);
                            */

                            // sort points and set sorted list in time series as well start and end.
                            /*Collections.sort(allPoints);
                            List<TimeSeriesPoint> retainPoints = new ArrayList<>();
                            for(int i = 0; i < allPoints.size() ; i++){
                                // if timestamp is equal
                                if(retainPoints.contains(allPoints.get(i))){
                                    TimeSeriesPoint retainedPoint = retainPoints.get(retainPoints.indexOf(allPoints.get(i)));
                                    //retainedPoint is less and is to be replaced
                                    if(retainedPoint.ownCompareTo(allPoints.get(i)) == -2){
                                        retainPoints.remove(retainedPoint);
                                        retainPoints.add(allPoints.get(i));
                                    }
                                } else {
                                    retainPoints.add(allPoints.get(i));
                                }

                            }
                            */

                            List<TimeSeriesPoint> retainPoints = new ArrayList<>(new LinkedHashSet<>(allPoints));






                            Collections.sort(retainPoints);
                            ts.setPoints(retainPoints);
                            ts.setStart(retainPoints.get(0).getTimeStamp());
                            ts.setEnd(retainPoints.get(retainPoints.size()-1).getTimeStamp());
                            timeSeries.add(ts);
                        }

                    }

                } catch (Exception e) {
                   logger.error("Error CsvImporter TimeSeriesFileReader: " + e.getLocalizedMessage());
                }
            }

            return timeSeries;
        }
    }

}
