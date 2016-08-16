package de.qaware.chronix.server.benchmark.queryrunner;


import de.qaware.chronix.database.BenchmarkDataSource;
import de.qaware.chronix.shared.QueryUtil.QueryRecord;
import de.qaware.chronix.shared.ServerConfig.TSDBInterfaceHandler;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by mcqueen666 on 15.06.16.
 */
@Path("/queryrunner")
@Produces(MediaType.APPLICATION_JSON)
public class QueryRunnerResource {

    private final TSDBInterfaceHandler tsdbInterfaceHandler = TSDBInterfaceHandler.getInstance();

    @POST
    @Path("performQuery")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response performQuery(QueryRecord queryRecord){
        BenchmarkDataSource tsdb = tsdbInterfaceHandler.getTSDBInstance(queryRecord.getTsdbName());
        if(tsdb == null){
            return Response.serverError().entity("No TSDB implementation with name " + queryRecord.getTsdbName() + " found on server!").build();
        }

        //TODO log RAM-, disk- and cpu usage

        long startMilliseconds = System.currentTimeMillis();
        String queryResult = tsdb.performQuery(queryRecord.getIpAddress(), queryRecord.getPortNumber(), queryRecord.getQuery());
        long endMilliseconds = System.currentTimeMillis();

        queryRecord.setQueryTimeMilliseconds(endMilliseconds - startMilliseconds);
        //TODO give measurements to threaded StatsCollector(set queryRecord with RAM-, disk- and cpu usage | write queryRecord to file)


        if (queryResult != null) {
            return Response.ok().entity(queryResult).build();
        }

        return Response.serverError().entity("Error performing query!").build();
    }
}
