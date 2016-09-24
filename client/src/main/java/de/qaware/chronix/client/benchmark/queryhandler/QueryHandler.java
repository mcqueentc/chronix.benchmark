package de.qaware.chronix.client.benchmark.queryhandler;

import de.qaware.chronix.client.benchmark.configurator.Configurator;
import de.qaware.chronix.shared.DataModels.ImportRecordWrapper;
import de.qaware.chronix.shared.DataModels.Pair;
import de.qaware.chronix.shared.QueryUtil.BenchmarkRecord;
import de.qaware.chronix.shared.QueryUtil.CleanCommand;
import de.qaware.chronix.shared.QueryUtil.ImportRecord;
import de.qaware.chronix.shared.QueryUtil.QueryRecord;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mcqueen666 on 20.06.16.
 */
public class QueryHandler {

    private static QueryHandler instance;
    private Configurator configurator;
    private Map<Pair<String, String>, Long> queryLatency;

    private QueryHandler(){
        configurator = Configurator.getInstance();
        queryLatency = new HashMap<>();
    }

    public static synchronized QueryHandler getInstance(){
        if(instance == null){
            instance = new QueryHandler();
        }
        return instance;
    }

    /**
     * Returns the latency of a query with given query id, tsdbName Pair.
     *
     * @param queryID_tsdb_Pair the query id, tsdbName pair
     * @return the latency in milliseconds or null if no record of given query id exists (query failed for some reason)
     */
    public Long getLatencyForQueryID(Pair<String, String> queryID_tsdb_Pair){
        return queryLatency.get(queryID_tsdb_Pair);
    }


    /**
     * Transmits the given queryRecord to given server on which the query should be performed.
     *
     * @param serverAddress the server address or ip WITHOUT http://
     * @param queryRecord the queryRecord
     * @return String starting with server status code and either the query result string or the server error message string
     *         "[StatusCode] : [QueryResult] or [error message]"
     */
    public String[] doQueryOnServer(String serverAddress, QueryRecord queryRecord) {
        final Client client = ClientBuilder.newBuilder().build();
        final WebTarget target = client.target("http://"
                + serverAddress
                + ":"
                + configurator.getApplicationPort()
                + "/queryrunner/performQuery");
        long startMillis = System.currentTimeMillis();
        final Response response = target.request().post(Entity.json(queryRecord));
        long endMillis = System.currentTimeMillis();

        int statusCode = response.getStatus();
        String[] queryResults = response.readEntity(String[].class);
        client.close();

        if(statusCode == 200){
            queryLatency.put(Pair.of(queryRecord.getQueryID(), queryRecord.getTsdbName()), (endMillis - startMillis));
            return queryResults;
        }

        return new String[]{"Server status code: " + statusCode};


    }

    public String[] doImportOnServer(String serverAddress, ImportRecordWrapper importRecordWrapper) {
        final Client client = ClientBuilder.newBuilder().build();
        final WebTarget target = client.target("http://"
                + serverAddress
                + ":"
                + configurator.getApplicationPort()
                + "/queryrunner/performImport");
        long startMillis = System.currentTimeMillis();
        final Response response = target.request().post(Entity.json(importRecordWrapper));
        long endMillis = System.currentTimeMillis();

        int statusCode = response.getStatus();
        String[] queryResults = response.readEntity(String[].class);
        client.close();

        if(statusCode == 200){
            //TODO erase or implement latency measurement again
            //queryLatency.put(Pair.of(importRecordWrapper.getQueryID(), importRecordWrapper.getTsdbName()), (endMillis - startMillis));
            return queryResults;
        }

        return new String[]{"Server status code: " + statusCode};
    }

    public String[] doImportOnServerWithUploadedFiles(String serverAddress, FormDataMultiPart multiPart){
        final Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
        final WebTarget target = client.target("http://"
                + serverAddress
                + ":"
                + configurator.getApplicationPort()
                + "/queryrunner/performImportWithFiles");


        Response response = target.request().post(Entity.entity(multiPart, multiPart.getMediaType()));
        String[] answer = response.readEntity(String[].class);
        client.close();

        return answer;
    }

    public String[] cleanDatabasesOnServer(String serverAddress, List<CleanCommand> cleanCommandList){
        final Client client = ClientBuilder.newBuilder().build();
        final WebTarget target = client.target("http://"
                + serverAddress
                + ":"
                + configurator.getApplicationPort()
                + "/queryrunner/cleanDatabases");

        final Response response = target.request().post(Entity.json(cleanCommandList));
        String[] results = response.readEntity(String[].class);
        client.close();
        return results;
    }

}
