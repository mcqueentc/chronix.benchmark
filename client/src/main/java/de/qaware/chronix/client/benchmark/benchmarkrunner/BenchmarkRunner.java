package de.qaware.chronix.client.benchmark.benchmarkrunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;
import de.qaware.chronix.client.benchmark.benchmarkrunner.util.BenchmarkRunnerHelper;
import de.qaware.chronix.client.benchmark.configurator.Configurator;
import de.qaware.chronix.client.benchmark.queryhandler.QueryHandler;
import de.qaware.chronix.client.benchmark.util.JsonTimeSeriesHandler;
import de.qaware.chronix.database.BenchmarkDataSource.QueryFunction;
import de.qaware.chronix.database.TimeSeries;
import de.qaware.chronix.database.TimeSeriesMetaData;
import de.qaware.chronix.shared.DataModels.ImportRecordWrapper;
import de.qaware.chronix.shared.DataModels.Pair;
import de.qaware.chronix.shared.QueryUtil.BenchmarkRecord;
import de.qaware.chronix.shared.QueryUtil.ImportRecord;
import de.qaware.chronix.shared.QueryUtil.QueryRecord;
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

    private Configurator configurator;
    private final Logger logger = LoggerFactory.getLogger(BenchmarkRunner.class);
    private final String recordFileDirectory = System.getProperty("user.home")
            + File.separator
            + "chronixBenchmark"
            + File.separator
            + "downloaded_benchmark_records";
    private final String recordFileName = "benchmarkRecords.json";
    private BenchmarkRunnerHelper benchmarkRunnerHelper;
    private QueryHandler queryHandler;
    private JsonTimeSeriesHandler jsonTimeSeriesHandler;

    private BenchmarkRunner(){
        configurator = Configurator.getInstance();
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
                    + configurator.getApplicationPort()
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
     * @param directories the directory (aka measurementName) of json time series gzipped files.
     * @param batchSize the batch size of how many time series should be imported per call on the server.
     * @return list of answers from the server.
     */
    public List<String> importTimeSeriesFromDirectory(String server, List<File> directories, int batchSize){
        List<String> answers = new LinkedList<>();

        List<String> importedTimeSeriesMetaData = new LinkedList<>();
        for(File directory : directories){
            if(directory.exists()){
                File[] files = directory.listFiles();
                if(files != null) {
                    List<File> fileList = new ArrayList<>();

                    for (int i = 0; i < files.length; i++) {
                        if (i != 0 && i % batchSize == 0) {
                            //read timeseries from json
                            List<TimeSeries> timeSeries = jsonTimeSeriesHandler.readTimeSeriesJson(fileList.toArray(new File[]{}));
                            fileList.clear();
                            String queryID = Instant.now().toString() + "_import_" + directory.getName() + "_" + i;

                            // import to tsdbs
                            logger.info("Import time series from {} to {}, {} left.",(i-batchSize),i,(files.length - i));
                            answers.addAll(this.importTimeSeries(server, timeSeries, queryID));
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
                    answers.addAll(this.importTimeSeries(server, timeSeries, queryID));
                    // generate meta data
                    jsonTimeSeriesHandler.writeTimeSeriesMetaDataJson(timeSeries);
                }
            }
        }
        return answers;
    }



    private List<String> importTimeSeries(String serverAddress, List<TimeSeries> timeSeriesList, String queryID){
        List<String> resultList = new LinkedList<>();
        if (!timeSeriesList.isEmpty()) {
            //TODO change signature
            List<ImportRecord> importRecordList = benchmarkRunnerHelper.getImportRecordForTimeSeries(null, queryID, serverAddress);
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
     * Performs queries with given meta data and function on all tsdbs on given server.
     *
     * @param server the server address or ip
     * @param metaDataList the meta data on which the queries should be performed.
     * @param function the query function that should be performed.
     * @return list of answers from the server.
     */
    public List<String> queryWithFunction(String server, List<TimeSeriesMetaData> metaDataList, QueryFunction function) {
        List<String> resultList = new LinkedList<>();
        if (metaDataList != null && !metaDataList.isEmpty()){

            String queryID = Instant.now().toString() + "_query_" + function.toString() + "_" + metaDataList.size();

            List<QueryRecord> queryRecordList = benchmarkRunnerHelper.getQueryRecordForTimeSeriesMetaData(metaDataList,
                    queryID,
                    server,
                    function);

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



    private void saveBenchmarkRecords(List<BenchmarkRecord> benchmarkRecords){
        ObjectMapper mapper = new ObjectMapper();
        File recordFile = new File(recordFileDirectory + File.separator + recordFileName);
        //recordFile.delete();
        for(BenchmarkRecord benchmarkRecord : benchmarkRecords) {
            //TODO erase or implement latency measurement again
            /*Long latency = queryHandler.getLatencyForQueryID(Pair.of(benchmarkRecord.getQueryID(), benchmarkRecord.getTsdbName()));
            if(latency == null){
                // ignore previously downloaded records. for them, no latency entry should exist
                continue;
            }

            benchmarkRecord.setLatency(latency);*/

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

}
