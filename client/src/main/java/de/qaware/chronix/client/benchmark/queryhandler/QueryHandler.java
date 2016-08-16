package de.qaware.chronix.client.benchmark.queryhandler;

import de.qaware.chronix.client.benchmark.configurator.Configurator;
import de.qaware.chronix.shared.QueryUtil.QueryRecord;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 * Created by mcqueen666 on 20.06.16.
 */
public class QueryHandler {

    private static QueryHandler instance;
    private Configurator configurator;

    private QueryHandler(){
        configurator = Configurator.getInstance();
    }

    public static synchronized QueryHandler getInstance(){
        if(instance == null){
            instance = new QueryHandler();
        }
        return instance;
    }


    /**
     * Transmits the given queryRecord to given server on which the query should be performed.
     *
     * @param serverAddress the server address or ip WITHOUT http://
     * @param queryRecord the queryRecord
     * @return String starting with server status code and either the query result string or the server error message string
     *         "[StatusCode] : [QueryResult] or [error message]"
     */
    public String doQueryOnServer(String serverAddress, QueryRecord queryRecord) {
        final Client client = ClientBuilder.newBuilder().build();
        final WebTarget target = client.target("http://"
                + serverAddress
                + ":"
                + configurator.getApplicationPort()
                + "/queryrunner/performQuery");
        final Response response = target.request().post(Entity.json(queryRecord));

        return response.getStatus() + " : " + response.readEntity(String.class);

    }

}
