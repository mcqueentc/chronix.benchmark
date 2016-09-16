package de.qaware.chronix.client.benchmark.benchmarkrunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;
import de.qaware.chronix.client.benchmark.configurator.Configurator;
import de.qaware.chronix.shared.QueryUtil.BenchmarkRecord;
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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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

    private BenchmarkRunner(){
        configurator = Configurator.getInstance();
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





    private void saveBenchmarkRecords(List<BenchmarkRecord> benchmarkRecords){
        ObjectMapper mapper = new ObjectMapper();
        File recordFile = new File(recordFileDirectory + File.separator + recordFileName);
        recordFile.delete();
        for(BenchmarkRecord benchmarkRecord : benchmarkRecords) {
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
