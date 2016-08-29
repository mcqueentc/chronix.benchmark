package de.qaware.chronix.client.benchmark.queryhandler;

import de.qaware.chronix.client.benchmark.configurator.Configurator;
import de.qaware.chronix.shared.QueryUtil.BenchmarkRecord;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mcqueen666 on 20.06.16.
 */
public class QueryHandler {

    private static QueryHandler instance;
    private Configurator configurator;
    private Map<String, Long> queryLatency;

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
     * Returns the latency of a query with given query id.
     *
     * @param queryID the query id
     * @return the latency in milliseconds or null if no record of given query id exists (query failed for some reason)
     */
    public Long getLatencyForQueryID(String queryID){
        return queryLatency.get(queryID);
    }


    /**
     * Transmits the given benchmarkRecord to given server on which the query should be performed.
     *
     * @param serverAddress the server address or ip WITHOUT http://
     * @param benchmarkRecord the benchmarkRecord
     * @return String starting with server status code and either the query result string or the server error message string
     *         "[StatusCode] : [QueryResult] or [error message]"
     */
    public String[] doQueryOnServer(String serverAddress, BenchmarkRecord benchmarkRecord) {
        final Client client = ClientBuilder.newBuilder().build();
        final WebTarget target = client.target("http://"
                + serverAddress
                + ":"
                + configurator.getApplicationPort()
                + "/queryrunner/performQuery");
        long startMillis = System.currentTimeMillis();
        final Response response = target.request().post(Entity.json(benchmarkRecord));
        long endMillis = System.currentTimeMillis();

        int statusCode = response.getStatus();
        String[] queryResults = response.readEntity(String[].class);
        client.close();

        if(statusCode == 200){
            queryLatency.put(benchmarkRecord.getQueryID(), (endMillis - startMillis));
            return queryResults;
        }

        return new String[]{"Server status code: " + statusCode};


    }

    //test
    public String[] getMeasurement(String serverAddress){
        final Client client = ClientBuilder.newBuilder().build();
        final WebTarget target = client.target("http://"
                + serverAddress
                + ":"
                + configurator.getApplicationPort()
                + "/queryrunner/measurement");
        final Response response = target.request().get();
        return  response.readEntity(String[].class);
    }

}
