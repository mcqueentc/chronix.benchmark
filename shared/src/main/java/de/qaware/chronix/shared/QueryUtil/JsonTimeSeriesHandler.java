package de.qaware.chronix.shared.QueryUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.management.OperatingSystemMXBean;
import de.qaware.chronix.database.TimeSeries;
import de.qaware.chronix.database.TimeSeriesMetaData;
import de.qaware.chronix.database.TimeSeriesPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by mcqueen666 on 03.09.16.
 */
public class JsonTimeSeriesHandler {

    private static JsonTimeSeriesHandler instance;
    private final Logger logger = LoggerFactory.getLogger(JsonTimeSeriesHandler.class);
    private String timeSeriesJsonRecordDirectoryPath = System.getProperty("user.home")
            + File.separator
            + "chronixBenchmark"
            + File.separator
            + "timeseries_records";
    private String timeSeriesMetaDataRecordDirectoryPath = System.getProperty("user.home")
            + File.separator
            + "chronixBenchmark"
            + File.separator
            + "timeseries_metadata";
    private String BenchmarkTimeSeriesMetaDataDirectoryPath = System.getProperty("user.home")
            + File.separator
            + "chronixBenchmark"
            + File.separator
            + "benchmark_timeseries_metadata";


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

    public String getTimeSeriesMetaDataRecordDirectoryPath() {
        return timeSeriesMetaDataRecordDirectoryPath;
    }

    public String getBenchmarkTimeSeriesMetaDataDirectoryPath() {
        return BenchmarkTimeSeriesMetaDataDirectoryPath;
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

    public boolean benchmarkMetaDataExists(){
        File benchmarkMetaDataDirectory = new File(BenchmarkTimeSeriesMetaDataDirectoryPath);
        if(benchmarkMetaDataDirectory.exists()){
            File[] files = benchmarkMetaDataDirectory.listFiles();
            if(files != null && files.length > 0){
                return true;
            }
        }
        return false;
    }

    /**
     * Deletes the previously saved meta data for a given measurement name.
     *
     * @param measurementName the measurement name for which to delete the meta data.
     * @return true if success.
     */
    public boolean deleteTimeSeriesMetaDataJsonFile(String measurementName){
            File recordFile = new File(timeSeriesMetaDataRecordDirectoryPath + File.separator + measurementName + ".json");
            try {
                Files.deleteIfExists(recordFile.toPath());

            } catch (IOException e) {
                logger.error("Could not delete json file for measurement: " + measurementName + " -> " + e.getLocalizedMessage());
                return false;
            }
            return true;
    }

    /**
     * Writes a benchmark time series meta data list to a json file into directory benchmark_timeseries_metadata.
     * The json file is named with given sequence number.
     *
     * @param metaDataList the time series meta data list to be saved to json.
     * @param sequenceNumber the sequence number for this list.
     */
    public void writeBenchmarkTimeSeriesMetaDataJson(List<TimeSeriesMetaData> metaDataList, int sequenceNumber){
        if(metaDataList != null && ! metaDataList.isEmpty()){
            ObjectMapper mapper = new ObjectMapper();
            File recordFile = new File(BenchmarkTimeSeriesMetaDataDirectoryPath + File.separator + "benchmark_metadata_" + sequenceNumber + ".json");
            if(!recordFile.getParentFile().exists()){
                recordFile.getParentFile().mkdirs();
            }
            // don't append to existing file
            if(recordFile.exists()){
                recordFile.delete();
            }
            for(TimeSeriesMetaData metaData : metaDataList){
                try {
                    final String timeSeriesMetaDataRecord = mapper.writeValueAsString(metaData);
                    if(recordFile.exists()) {
                        Files.write(recordFile.toPath(), Arrays.asList(timeSeriesMetaDataRecord), StandardOpenOption.APPEND);
                    } else {
                        Files.write(recordFile.toPath(), Arrays.asList(timeSeriesMetaDataRecord), StandardOpenOption.CREATE);
                    }

                } catch (Exception e) {
                    logger.error("Could not write measurement meta data to json: " + e.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * Reads the predefined meta data for running the benchmark
     * from the corresponding json file in benchmark time series meta data directory.
     *
     * @return map containing the file sequence number as key and the meta data list as value.
     */
    public Map<Integer, List<TimeSeriesMetaData>> readBenchmarkTimeSeriesMetaDataJson(){
        Map<Integer, List<TimeSeriesMetaData>> sequenceMetaDataList = new HashMap<>();
        File directory = new File(BenchmarkTimeSeriesMetaDataDirectoryPath);

        if(directory.exists()){
            File[] files = directory.listFiles();
            if(files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".json")) {
                        String sequenceNumberString = file.getName().split("_")[2].replaceFirst(".json", "");
                        List<TimeSeriesMetaData> metaDataList = readTimeSeriesMetaDataFromFile(file);
                        if (!metaDataList.isEmpty()) {
                            try {
                                Integer sequenceNumber = Integer.valueOf(sequenceNumberString);
                                sequenceMetaDataList.put(sequenceNumber, metaDataList);
                            } catch (Exception e) {
                                logger.error("Could not create sequence number from {}", sequenceNumberString);
                            }
                        }
                    }
                }
            }
        }

        return sequenceMetaDataList;
    }

    /**
     * Writes a list of TimeSeriesMetaData to timeseries_metadata_records.json file
     *
     * @param timeSeriesList the list of TimeSeriesMetaData
     */
    public List<TimeSeriesMetaData> writeTimeSeriesMetaDataJson(List<TimeSeries> timeSeriesList){
        List<TimeSeriesMetaData> timeSeriesMetaDataList = new ArrayList<>();
        if(timeSeriesList != null && !timeSeriesList.isEmpty()){

            ObjectMapper mapper = new ObjectMapper();
            timeSeriesMetaDataList = convertTimeSeriesToMetaData(timeSeriesList);
            for (TimeSeriesMetaData metaData : timeSeriesMetaDataList) {
                File recordFile = new File(timeSeriesMetaDataRecordDirectoryPath + File.separator + metaData.getMeasurementName() + ".json");
                if(!recordFile.getParentFile().exists()){
                    recordFile.getParentFile().mkdirs();
                }
                try {
                    final String timeSeriesMetaDataRecord = mapper.writeValueAsString(metaData);
                    if(recordFile.exists()) {
                        Files.write(recordFile.toPath(), Arrays.asList(timeSeriesMetaDataRecord), StandardOpenOption.APPEND);
                    } else {
                        Files.write(recordFile.toPath(), Arrays.asList(timeSeriesMetaDataRecord), StandardOpenOption.CREATE);
                    }


                } catch (Exception e) {
                    logger.error("Could not write meta data to json: " + e.getLocalizedMessage());
                }
            }
        }
        return timeSeriesMetaDataList;

    }

    /**
     * Reads the TimeSeriesMetaData for given measurement name from TimeSeriesMetaDataRecord json file.
     *
     * @param measurementName the measurement name which meta data should be read.
     * @return a list of TimeSeriesMetaData for the given measurement name.
     */
    public List<TimeSeriesMetaData> readTimeSeriesMetaDatafromJson(String measurementName){
        List<TimeSeriesMetaData> timeSeriesMetaDataList = new ArrayList<>();
        if(measurementName != null){
            File recordFile = new File(timeSeriesMetaDataRecordDirectoryPath + File.separator + measurementName + ".json");
            timeSeriesMetaDataList.addAll(readTimeSeriesMetaDataFromFile(recordFile));
        }

        return timeSeriesMetaDataList;
    }

    private List<TimeSeriesMetaData> readTimeSeriesMetaDataFromFile(File file){
        List<TimeSeriesMetaData> timeSeriesMetaDataList = new LinkedList<>();
        if(file.exists()){
            ObjectMapper mapper = new ObjectMapper();
            try {
                Stream<String> lines = Files.lines(file.toPath());
                lines.forEach(line -> {
                    try {
                        TimeSeriesMetaData timeSeriesMetaData = mapper.readValue(line, TimeSeriesMetaData.class);
                        /*//DEBUG
                        logger.info("read from meta data json: Measurement: {}, metricName: {}, start: {}, end: {}",
                                timeSeriesMetaData.getMeasurementName(),
                                timeSeriesMetaData.getMetricName(),
                                timeSeriesMetaData.getStart(),
                                timeSeriesMetaData.getEnd());*/
                        timeSeriesMetaDataList.add(timeSeriesMetaData);
                    } catch (IOException e) {
                        logger.error("Could not generate object from json: " + e.getLocalizedMessage());
                    }
                });

            } catch (IOException e) {
                logger.error("Could not read from json file: " + e.getLocalizedMessage());
            }
        } else {
            logger.error("Json file does not exist.");
        }
        return timeSeriesMetaDataList;
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
            if (jsonFile.exists() && jsonFile.isFile() && jsonFile.getName().endsWith(".gz")){
                timesSeriesReaderList.add(new TimesSeriesReader(jsonFile));
            }
        }

        try {
            List<Future<TimeSeries>> futureList = executorService.invokeAll(timesSeriesReaderList);

            for(Future<TimeSeries> future : futureList){
                TimeSeries ts = future.get();
                if(ts != null) {
                    timeSeriesList.add(ts);
                }
            }

            executorService.shutdown();
            if(!executorService.awaitTermination(10000, TimeUnit.MILLISECONDS)){
                executorService.shutdownNow();
            }

        } catch (Exception e) {
            logger.error("Error JsonTimeSeriesHandler readTimeSeriesJson: " + e.getLocalizedMessage());
        }


        return timeSeriesList;
    }


    /**
     * Writes a list of TimeSeries to gzipped TimeSeries json files in directory named after TimeSeries measurement.
     *
     * @param timeSeriesList the list of TimeSeries
     * @return the written file names.
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

        } catch (Exception e) {
            logger.error("Error JsonTimeSeriesHandler writeTimeSeriesJson: " + e.getLocalizedMessage());
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
                /*//DEBUG
                logger.info("Read from gzip json file: Measurement: {}, metricName: {}, start: {}, end: {}",
                        timeSeries.getMeasurementName(),
                        timeSeries.getMetricName(),
                        timeSeries.getStart(),
                        timeSeries.getEnd());*/

                List<TimeSeriesPoint> pointList = timeSeries.getPoints();
                Collections.sort(pointList);
                timeSeries.setPoints(pointList);

                /*//DEBUG
                logger.info("Sorted list, returned from reader thread: Measurement: {}, metricName: {}, start: {}, end: {}",
                        timeSeries.getMeasurementName(),
                        timeSeries.getMetricName(),
                        timeSeries.getStart(),
                        timeSeries.getEnd());*/


            } catch (Exception e){
                logger.error("TimesSeriesReader Error: {}, file: {}", e.getLocalizedMessage(), jsonFile.getName());
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

           /*//DEBUG
           logger.info("writer thread start: Measurement: {}, metricName: {}, start: {}, end: {}",
                   timeSeries.getMeasurementName(),
                   timeSeries.getMetricName(),
                   timeSeries.getStart(),
                   timeSeries.getEnd());*/

           //host_process_metricGroup;
           String fileName = timeSeries.getMeasurementName() + "_"
                   + escapeString(timeSeries.getTagKey_tagValue().get("host")) + "_"
                   + escapeString(timeSeries.getTagKey_tagValue().get("process")) + "_"
                   + escapeString(timeSeries.getTagKey_tagValue().get("metricGroup")) + "_"
                   + escapeString(timeSeries.getMetricName())
                   + ".json";

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
               logger.error("Error TimeSeriesWriter: " + e.getLocalizedMessage() + " fileName: " +fileName);
               return "";
           }
            return fileName;
       }
   }

    private List<TimeSeriesMetaData> convertTimeSeriesToMetaData(List<TimeSeries> timeSeriesList){
        List<TimeSeriesMetaData> timeSeriesMetaDataList = new ArrayList<>();
        if(timeSeriesList != null && !timeSeriesList.isEmpty()){
            for(TimeSeries timeSeries : timeSeriesList){
                timeSeriesMetaDataList.add(new TimeSeriesMetaData(timeSeries));
            }
        }

        return timeSeriesMetaDataList;
    }

    private String escapeString(String s) {
        return s.replaceAll("(\\s|\\.|:|=|,|/|\\\\|\\*|\\(|\\)|_|#)","_").replaceAll("(-)", "_");
    }

}
