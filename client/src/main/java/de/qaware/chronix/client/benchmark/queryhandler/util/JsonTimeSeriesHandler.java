package de.qaware.chronix.client.benchmark.queryhandler.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.management.OperatingSystemMXBean;
import de.qaware.chronix.database.TimeSeries;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
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


    public List<String> writeTimeSeriesJson(List<TimeSeries> timeSeriesList){
        List<String> writtenList = new LinkedList<>();

        OperatingSystemMXBean oSMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        ExecutorService executorService = Executors.newFixedThreadPool(oSMXBean.getAvailableProcessors());

        File timeSeriesJsonRecordDirecotry = new File(timeSeriesJsonRecordDirectoryPath);

        if(!timeSeriesJsonRecordDirecotry.exists()){
           if(!timeSeriesJsonRecordDirecotry.mkdirs()){
               // could not make directories
               writtenList.add("Error JsonTimeSeriesHandler: could not make directories");
               return writtenList;
           }
        }

        List<TimesSeriesWriter> timesSeriesWriterList = new ArrayList<>();
        for(TimeSeries timeSeries : timeSeriesList){
            timesSeriesWriterList.add(new TimesSeriesWriter(timeSeries, timeSeriesJsonRecordDirecotry));
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


   private class TimesSeriesWriter implements Callable<String>{

       private TimeSeries timeSeries;
       private final ObjectMapper mapper;
       private File dirPath;


       public TimesSeriesWriter(TimeSeries timeSeries, File dirPath){
           this.timeSeries = timeSeries;
           this.mapper = new ObjectMapper();
           this.dirPath = dirPath;
       }

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
