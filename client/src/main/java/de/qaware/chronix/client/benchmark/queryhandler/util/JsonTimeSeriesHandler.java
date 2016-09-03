package de.qaware.chronix.client.benchmark.queryhandler.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.management.OperatingSystemMXBean;
import de.qaware.chronix.database.TimeSeries;
import de.qaware.chronix.database.TimeSeriesPoint;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by mcqueen666 on 03.09.16.
 */
public class JsonTimeSeriesHandler {

    private static JsonTimeSeriesHandler instance;
    private String timeSeriesJsonRecordDirectoryPath = System.getProperty("user.home")
            + File.separator
            + "chronixBenchmark"
            + File.separator
            + "timeseries_records";


    private JsonTimeSeriesHandler(){
    }

    public static JsonTimeSeriesHandler getInstance(){
        if(instance == null){
            instance = new JsonTimeSeriesHandler();
        }
        return instance;
    }

    public String getTimeSeriesJsonRecordDirectoryPath(){
        return timeSeriesJsonRecordDirectoryPath;
    }

    /**
     * Checks if a TimesSeries was previously imported into chronixBenchmark directory
     *
     * @param measurement the measurement name of previously imported time series.
     * @return true if measurement was previously imported.
     */
    public boolean isMeasurementImportedAsJson(String measurement){
        File measurementDirectory = new File(timeSeriesJsonRecordDirectoryPath + File.separator + measurement);
        return measurementDirectory.exists();
    }

    /**
     * Generates a list of TimeSeries from gzipped json files.
     *
     * @param files the gzipped TimeSeries json files
     * @return list of TimeSeries
     */
    public List<TimeSeries> readTimeSeriesJson(File[] files){
        List<TimeSeries> timeSeriesList = new LinkedList<>();
        OperatingSystemMXBean oSMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        ExecutorService executorService = Executors.newFixedThreadPool(oSMXBean.getAvailableProcessors());

        List<TimesSeriesReader> timesSeriesReaderList = new ArrayList<>();
        for(File jsonFile : files){
            if (jsonFile.exists() && jsonFile.isFile()){
                timesSeriesReaderList.add(new TimesSeriesReader(jsonFile));
            }
        }

        try {
            List<Future<TimeSeries>> futureList = executorService.invokeAll(timesSeriesReaderList);

            for(Future<TimeSeries> future : futureList){
                timeSeriesList.add(future.get());
            }

            executorService.shutdown();
            if(!executorService.awaitTermination(1000, TimeUnit.MILLISECONDS)){
                executorService.shutdownNow();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        return timeSeriesList;
    }


    /**
     * Writes a list of TimeSeries to gzipped TimeSeries json files in directory named after TimeSeries measurement.
     *
     * @param timeSeriesList the list of TimeSeries
     * @return the written file names of error messages
     */
    public List<String> writeTimeSeriesJson(List<TimeSeries> timeSeriesList){
        List<String> writtenList = new LinkedList<>();

        OperatingSystemMXBean oSMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        ExecutorService executorService = Executors.newFixedThreadPool(oSMXBean.getAvailableProcessors());

        //File timeSeriesJsonRecordDirecotry = new File(timeSeriesJsonRecordDirectoryPath);



        List<TimesSeriesWriter> timesSeriesWriterList = new ArrayList<>();
        for(TimeSeries timeSeries : timeSeriesList){
            File measurementDirectory = new File(timeSeriesJsonRecordDirectoryPath + File.separator + timeSeries.getMeasurementName());
            if(!measurementDirectory.exists()){
                if(!measurementDirectory.mkdirs()){
                    // could not make directories
                    writtenList.add("Error JsonTimeSeriesHandler: could not make directories");
                    return writtenList;
                }
            }
            timesSeriesWriterList.add(new TimesSeriesWriter(timeSeries, measurementDirectory));
        }

        try {
            List<Future<String>> futureList = executorService.invokeAll(timesSeriesWriterList);

            for(Future<String> future : futureList){
                writtenList.add(future.get());
            }

            executorService.shutdown();
            if(!executorService.awaitTermination(1000, TimeUnit.MILLISECONDS)){
                executorService.shutdownNow();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return writtenList;
    }

    private class TimesSeriesReader implements Callable<TimeSeries>{

        File jsonFile;
        private final ObjectMapper mapper;

        public TimesSeriesReader(File jsonFile){
            this.jsonFile = jsonFile;
            mapper = new ObjectMapper();
        }

        @Override
        public TimeSeries call() throws Exception {

            TimeSeries timeSeries = null;
            try {
                InputStream inputStream = new GZIPInputStream(new FileInputStream(jsonFile));
                timeSeries = mapper.readValue(inputStream, TimeSeries.class);

                List<TimeSeriesPoint> pointList = timeSeries.getPoints();
                Collections.sort(pointList);
                timeSeries.setPoints(pointList);


            } catch (Exception e){
                System.err.println("TimesSeriesReader Error: " + e.getLocalizedMessage());
            }

            return timeSeries;
        }
    }

   private class TimesSeriesWriter implements Callable<String>{

       private TimeSeries timeSeries;
       private final ObjectMapper mapper;
       private File dirPath;


       public TimesSeriesWriter(TimeSeries timeSeries, File dirPath){
           this.timeSeries = timeSeries;
           this.mapper = new ObjectMapper();
           this.dirPath = dirPath;
       }

       @Override
       public String call(){

           //host_process_metricGroup;
           String fileName = timeSeries.getMeasurementName() + "_"
                   + escapeString(timeSeries.getTagKey_tagValue().get("host")) + "_"
                   + escapeString(timeSeries.getTagKey_tagValue().get("process")) + "_"
                   + escapeString(timeSeries.getTagKey_tagValue().get("metricGroup")) + "_"
                   + escapeString(timeSeries.getMetricName())
                   + ".tsjson";

           try {

               File jsonFile = new File(dirPath.getAbsolutePath() + File.separator + fileName + ".gz");
               final String TimeSeriesRecordJSON = mapper.writeValueAsString(timeSeries);
               //Files.write(jsonFile.toPath(), Arrays.asList(TimeSeriesRecordJSON), StandardOpenOption.CREATE);
               GZIPOutputStream fileOutputStream = new GZIPOutputStream(new FileOutputStream(jsonFile, false));
               BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
               bufferedWriter.write(TimeSeriesRecordJSON);

               bufferedWriter.close();
               fileOutputStream.close();

           } catch (IOException e) {
               return "Error: " + e.getLocalizedMessage() + " fileName: " +fileName;
           }
            return fileName;
       }
   }

    private String escapeString(String s) {
        return s.replaceAll("(\\s|\\.|:|=|,|/|\\\\|\\*|\\(|\\)|_|#)","_").replaceAll("(-)", "_");
    }

}
