package de.qaware.chronix.client.benchmark.benchmarkrunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;
import de.qaware.chronix.client.benchmark.benchmarkrunner.util.BenchmarkRunnerHelper;
import de.qaware.chronix.client.benchmark.benchmarkrunner.util.TimeSeriesCounter;
import de.qaware.chronix.client.benchmark.configurator.Configurator;
import de.qaware.chronix.client.benchmark.queryhandler.QueryHandler;
import de.qaware.chronix.shared.DataModels.Pair;
import de.qaware.chronix.shared.QueryUtil.JsonTimeSeriesHandler;
import de.qaware.chronix.database.BenchmarkDataSource.QueryFunction;
import de.qaware.chronix.database.TimeSeries;
import de.qaware.chronix.database.TimeSeriesMetaData;
import de.qaware.chronix.shared.DataModels.ImportRecordWrapper;
import de.qaware.chronix.shared.QueryUtil.BenchmarkRecord;
import de.qaware.chronix.shared.QueryUtil.ImportRecord;
import de.qaware.chronix.shared.QueryUtil.QueryRecord;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by mcqueen666 on 20.06.16.
 */
public class BenchmarkRunner {
    private static BenchmarkRunner instance;

    private final Logger logger = LoggerFactory.getLogger(BenchmarkRunner.class);
    private final String recordFileDirectory = System.getProperty("user.home")
            + File.separator
            + "chronixBenchmark"
            + File.separator
            + "downloaded_benchmark_records";
    private final String recordFileName = "benchmarkRecords.json";
    private final int BENCHMARK_TIMESERIES_METADATA_SIZE = 400;
    private final int NUMBER_OF_BENCHMARK_METADATA_LISTS = 1400;
    private BenchmarkRunnerHelper benchmarkRunnerHelper;
    private QueryHandler queryHandler;
    private JsonTimeSeriesHandler jsonTimeSeriesHandler;

    private BenchmarkRunner(){
        benchmarkRunnerHelper = BenchmarkRunnerHelper.getInstance();
        queryHandler = QueryHandler.getInstance();
        jsonTimeSeriesHandler = JsonTimeSeriesHandler.getInstance();

        File recordFileDirecotry = new File(recordFileDirectory);
        if(!recordFileDirecotry.exists()){
            recordFileDirecotry.mkdirs();
        }

    }

    public static BenchmarkRunner getInstance(){
        if(instance == null){
            instance = new BenchmarkRunner();
        }
        return instance;
    }

    /**
     * Downloads the benchmark records from the given server.
     *
     * @param serverAddress the server address or ip
     * @return
     */
    public boolean getBenchmarkRecordsFromServer(String serverAddress){
        try {
            Client client = ClientBuilder.newBuilder().register(JacksonFeatures.class).build();
            final WebTarget target = client.target("http://"
                    + serverAddress
                    + ":"
                    + Configurator.getInstance().getApplicationPort()
                    + "/collector/benchmarkrecords");

            final Response response = target.request(MediaType.APPLICATION_JSON_TYPE).get();
            List<BenchmarkRecord> benchmarkRecordList = response.readEntity(new GenericType<List<BenchmarkRecord>>() {
            });
            //logger.info("benchmark record list size: {}",benchmarkRecordList.size());
            this.saveBenchmarkRecords(benchmarkRecordList);
            client.close();
            return true;

        } catch (Exception e){
            logger.error("BenchmarkRunner: Error downloading benchmark records: {}", e.getLocalizedMessage());
        }
        return false;
    }

    /**
     * Imports time series to all tsdbs on given server.
     *
     * @param server the server address or ip on which to import.
     * @param directory the directory containing gzipped times series files.
     * @param batchSize the batch size of how many time series should be imported per call on the server.
     * @return list of answers from the server.
     */
    public void importTimeSeriesFromDirectory(String server, File directory, int batchSize, int fromFile, List<String> tsdbImportList){
        if(directory != null && directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                List<File> fileList = new ArrayList<>();

                for (int i = fromFile; i < files.length; i++) {
                    if (i != fromFile && i % batchSize == 0) {
                        //read timeseries from json
                        List<TimeSeries> timeSeries = jsonTimeSeriesHandler.readTimeSeriesJson(fileList.toArray(new File[]{}));
                        fileList.clear();
                        String queryID = Instant.now().toString() + "_import_" + directory.getName() + "_" + i;

                        // import to tsdbs
                        logger.info("Import time series from {} to {}, {} left.", (i - batchSize), i, (files.length - i));
                        List<String> answers = this.importTimeSeries(server, timeSeries, queryID, tsdbImportList);
                        for(String answer : answers) {
                            logger.info("Import: {}", answer);
                        }
                        // generate meta data
                        jsonTimeSeriesHandler.writeTimeSeriesMetaDataJson(timeSeries);
                    }

                    fileList.add(files[i]);
                }
                //read timeseries from json
                List<TimeSeries> timeSeries = jsonTimeSeriesHandler.readTimeSeriesJson(fileList.toArray(new File[]{}));
                fileList.clear();

                // import to tsdbs
                String queryID = Instant.now().toString() + "_import_" + directory.getName() + "_" + files.length;
                List<String> answers = this.importTimeSeries(server, timeSeries, queryID, tsdbImportList);
                logger.info("Imports: \n {} \n", answers);
                logger.info("Import: {} files imported.",files.length);
                // generate meta data
                jsonTimeSeriesHandler.writeTimeSeriesMetaDataJson(timeSeries);
            }
        }
    }



    private List<String> importTimeSeries(String serverAddress, List<TimeSeries> timeSeriesList, String queryID, List<String> tsdbImportList){
        List<String> resultList = new LinkedList<>();
        if (!timeSeriesList.isEmpty()) {
            List<ImportRecord> importRecordList = benchmarkRunnerHelper.getImportRecordForTimeSeries(null, queryID, serverAddress, tsdbImportList);
            ImportRecordWrapper importRecordWrapper = new ImportRecordWrapper(timeSeriesList, importRecordList);
            logger.info("Import on: {} ...", importRecordWrapper.getAllTsdbNames());
            String[] results = queryHandler.doImportOnServer(serverAddress, importRecordWrapper);
            for(String result : results){
                resultList.add(result);
            }
            resultList.add("\n");
            Collections.addAll(resultList, results);

        }
        return resultList;
    }

    /**
     * This method is used to actually benchmark the given TSDBs on a given server.
     * If there is no benchmark generated meta data in the BenchmarkTimeSeriesMetaDataDirectoryPath
     * this method will generate random time series meta data from previously imported time series
     * which will then be stored in BenchmarkTimeSeriesMetaDataDirectoryPath.
     * So for future calls all TSDBs are measured with the same set of meta data.
     * The QueryFunction and percentile value will be selected randomly for every set of meta data
     * but will be the same per set for all TSDBs per measurement, of course.
     *
     * @param server the address or ip on which the query should be performed.
     * @param tsdbQueryList the TSDBs on which the query should be performed.
     */
    public void doBenchmarkQuery(String server, List<String> tsdbQueryList){
        if(server != null && tsdbQueryList != null && ! tsdbQueryList.isEmpty()){
            // should there be no benchmark meta data, generate it.
            if(! jsonTimeSeriesHandler.benchmarkMetaDataExists()){
                logger.info("Benchmark data does not exist -> generating ... ");
                generateBenchmarkTimeSeriesMetaDataLists(NUMBER_OF_BENCHMARK_METADATA_LISTS, BENCHMARK_TIMESERIES_METADATA_SIZE);
            }

            int listFunctionRatio = (NUMBER_OF_BENCHMARK_METADATA_LISTS / QueryFunction.values().length);
            int queryFunctionNumber = 0;
            int listCounter = 0;
            Map<Integer, List<TimeSeriesMetaData>> metaDatasToQuery = jsonTimeSeriesHandler.readBenchmarkTimeSeriesMetaDataJson();
            if( ! metaDatasToQuery.isEmpty()){
                for(Map.Entry<Integer, List<TimeSeriesMetaData>> entry : metaDatasToQuery.entrySet()){
                    if( listCounter != 0 && listCounter % listFunctionRatio == 0){
                        queryFunctionNumber++;
                        if(queryFunctionNumber > QueryFunction.values().length -1){
                            queryFunctionNumber = 0;
                        }
                    }

                    Float randomPercentile = benchmarkRunnerHelper.getRandomPercentile();
                    QueryFunction queryFunction = QueryFunction.values()[queryFunctionNumber];
                    String querID = "benchmark_randomTimeSeries:&function=" + queryFunction + "&number=" + entry.getKey();
                    //do the query
                    System.out.println();
                    logger.info("Performing query on random time series number: {} with function: {}", entry.getKey(), queryFunction);
                    List<String> results = queryWithFunction(server, querID, entry.getValue(), queryFunction, randomPercentile, tsdbQueryList);

                    listCounter++;
                    //TODO log results into log file

                }
            }
        }

    }

    /**
     * Performs queries with given meta data and function on all tsdbs on given server.
     *
     * @param server the server address or ip
     * @param metaDataList the meta data on which the queries should be performed.
     * @param function the query function that should be performed.
     * @param p the percentile value (if needed, null if not).
     * @return list of answers from the server.
     */
    public List<String> queryWithFunction(String server, String queryID, List<TimeSeriesMetaData> metaDataList, QueryFunction function, Float p, List<String> tsdbQueryList) {
        List<String> resultList = new LinkedList<>();
        if (metaDataList != null && !metaDataList.isEmpty()){

            List<QueryRecord> queryRecordList = benchmarkRunnerHelper.getQueryRecordForTimeSeriesMetaData(metaDataList,
                    queryID,
                    server,
                    function,
                    p,
                    tsdbQueryList);

            for(QueryRecord queryRecord : queryRecordList){
                logger.info("Query on: {} ... ", queryRecord.getTsdbName());
                String[] results = queryHandler.doQueryOnServer(queryRecord.getIpAddress(), queryRecord);
                for(String result : results){
                    resultList.add(queryRecord.getTsdbName() + ": " + queryRecord.getQueryID() + ": " + result);
                }
                resultList.add("\n");
            }
        }
        return resultList;
    }

    private void generateBenchmarkTimeSeriesMetaDataLists(int numberOfLists, int listSize){
        if(numberOfLists > 0 && listSize > 0){
            TimeSeriesCounter timeSeriesCounter = TimeSeriesCounter.getInstance();
            for(int i = 0; i < numberOfLists; i++){
                List<TimeSeriesMetaData> metaDataList = timeSeriesCounter.getRandomTimeSeriesMetaData(listSize);
                jsonTimeSeriesHandler.writeBenchmarkTimeSeriesMetaDataJson(metaDataList, i);
            }
        }
    }


    private void saveBenchmarkRecords(List<BenchmarkRecord> benchmarkRecords){
        ObjectMapper mapper = new ObjectMapper();
        File recordFile = new File(recordFileDirectory + File.separator + recordFileName);
        recordFile.delete();
        for(BenchmarkRecord benchmarkRecord : benchmarkRecords) {

            Long latency = queryHandler.getLatencyForQueryID(Pair.of(benchmarkRecord.getQueryID(), benchmarkRecord.getTsdbName()));
            /*if(latency == null){
                // ignore previously downloaded records. for them, no latency entry should exist
                continue;
            }*/

            benchmarkRecord.setLatency(latency);

            try {
                final String queryRecordJSON = mapper.writeValueAsString(benchmarkRecord);
                if (recordFile.exists()) {
                    Files.write(recordFile.toPath(), Arrays.asList(queryRecordJSON), StandardOpenOption.APPEND);
                } else {
                    Files.write(recordFile.toPath(), Arrays.asList(queryRecordJSON), StandardOpenOption.CREATE);
                }

            } catch (Exception e){
                logger.error("BenchmarkRunner: Error writing benchmark records to json: {}",e.getLocalizedMessage());
            }
        }
    }

    private List<BenchmarkRecord> readBenchmarkRecords(){
        List<BenchmarkRecord> benchmarkRecordList = new LinkedList<>();
        ObjectMapper mapper = new ObjectMapper();
        File benchmarkRecordFile = new File(recordFileDirectory + File.separator + recordFileName);
        if(benchmarkRecordFile.exists() && benchmarkRecordFile.isFile()){
            try {
                Stream<String> lines = Files.lines(benchmarkRecordFile.toPath());
                lines.forEach(line -> {
                    try {
                        benchmarkRecordList.add(mapper.readValue(line, BenchmarkRecord.class));
                    } catch (IOException e) {
                        logger.error("BenchmarkRunner: Error reading from json: {}",e.getLocalizedMessage());
                    }
                });
            } catch (IOException e) {
                logger.error("BenchmarkRunner: Error reading from benchmark record file: {}",e.getLocalizedMessage());
            }
        }
        return benchmarkRecordList;
    }



    public void importTimesSeriesWithUploadedFiles(String server, File directory, int batchSize, int fromFile, List<String> tsdbImportList){
        if(directory != null && directory.exists() && directory.isDirectory()) {
            try {
                File[] files = directory.listFiles();
                // delete non times series files
                if(files != null) {
                    List<File> fileList = Arrays.asList(files);
                    List<File> jsonFileList = new LinkedList<>();
                    for(File file : fileList){
                        if(file.getName().endsWith(".gz")){
                            jsonFileList.add(file);
                        }
                    }
                    files = jsonFileList.toArray(new File[]{});
                }

                if (files != null) {
                    List<File> fileList = new ArrayList<>();

                    for (int i = fromFile; i < files.length; i++) {
                        if (i != fromFile && i % batchSize == 0) {
                            //read timeseries from json
                            List<TimeSeries> timeSeries = jsonTimeSeriesHandler.readTimeSeriesJson(fileList.toArray(new File[]{}));

                            String queryID = Instant.now().toString() + "_import_" + directory.getName() + "_" + i;
                            List<ImportRecord> importRecordList = benchmarkRunnerHelper.getImportRecordForTimeSeries(null, queryID, server, tsdbImportList);
                            ImportRecordWrapper importRecordWrapper = new ImportRecordWrapper(null, importRecordList);
                            logger.info("Import time series from {} to {}, {} left.", (i - batchSize), i, (files.length - i));
                            logger.info("Import on: {} ...", importRecordWrapper.getAllTsdbNames());

                            // make multipart
                            FormDataMultiPart multiPart = new FormDataMultiPart();
                            ObjectMapper mapper = new ObjectMapper();
                            final String ImportRecordWrapperJSON = mapper.writeValueAsString(importRecordWrapper);
                            multiPart.field("ImportRecordWrapper", ImportRecordWrapperJSON, MediaType.APPLICATION_JSON_TYPE);

                            for (File file : fileList) {
                                final FileDataBodyPart filePart = new FileDataBodyPart("file", file);
                                multiPart.field("file", file, MediaType.MULTIPART_FORM_DATA_TYPE).bodyPart(filePart);
                            }

                            // do the import
                            String[] results = queryHandler.doImportOnServerWithUploadedFiles(server, multiPart);
                            for(String result : results){
                                logger.info("Imports: {}",result);
                            }

                            // generate meta data
                            jsonTimeSeriesHandler.writeTimeSeriesMetaDataJson(timeSeries);

                            fileList.clear();
                        }


                        fileList.add(files[i]);

                    }

                    //read timeseries from json
                    List<TimeSeries> timeSeries = jsonTimeSeriesHandler.readTimeSeriesJson(fileList.toArray(new File[]{}));


                    String queryID = Instant.now().toString() + "_import_" + directory.getName() + "_" + files.length;
                    List<ImportRecord> importRecordList = benchmarkRunnerHelper.getImportRecordForTimeSeries(null, queryID, server, tsdbImportList);
                    ImportRecordWrapper importRecordWrapper = new ImportRecordWrapper(null, importRecordList);
                    logger.info("Import on: {} ...", importRecordWrapper.getAllTsdbNames());

                    // make multipart
                    FormDataMultiPart multiPart = new FormDataMultiPart();
                    ObjectMapper mapper = new ObjectMapper();
                    //add importrecordwrapper json
                    final String ImportRecordWrapperJSON = mapper.writeValueAsString(importRecordWrapper);
                    multiPart.field("ImportRecordWrapper", ImportRecordWrapperJSON, MediaType.APPLICATION_JSON_TYPE);

                    //add the time series files
                    for (File file : fileList) {
                        final FileDataBodyPart filePart = new FileDataBodyPart("file", file);
                        multiPart.field("file", file, MediaType.MULTIPART_FORM_DATA_TYPE).bodyPart(filePart);
                    }

                    // do the import
                    String[] results = queryHandler.doImportOnServerWithUploadedFiles(server, multiPart);

                    // generate meta data
                    jsonTimeSeriesHandler.writeTimeSeriesMetaDataJson(timeSeries);


                    for(String result : results){
                        logger.info("Imports: {}",result);
                    }
                    logger.info("Import: {} files imported.", files.length);
                }
            } catch (Exception e){
                logger.error("Error importing: {}", e.getLocalizedMessage());
            }
        }
    }


}
