package de.qaware.chronix.server.benchmark.queryrunner;


import de.qaware.chronix.database.BenchmarkDataSource;
import de.qaware.chronix.server.benchmark.collector.StatsCollector;
import de.qaware.chronix.server.util.DockerCommandLineUtil;
import de.qaware.chronix.server.util.DockerStatsUtil;
import de.qaware.chronix.shared.DataModels.Pair;
import de.qaware.chronix.shared.QueryUtil.QueryRecord;
import de.qaware.chronix.shared.ServerConfig.TSDBInterfaceHandler;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mcqueen666 on 15.06.16.
 */
@Path("/queryrunner")
@Produces(MediaType.APPLICATION_JSON)
public class QueryRunnerResource {

    private final TSDBInterfaceHandler tsdbInterfaceHandler = TSDBInterfaceHandler.getInstance();
    private final DockerStatsUtil dockerStatsUtil = DockerStatsUtil.getInstance();
    private final StatsCollector statsCollector = StatsCollector.getInstance();

    @POST
    @Path("performQuery")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response performQuery(QueryRecord queryRecord){
        BenchmarkDataSource tsdb = tsdbInterfaceHandler.getTSDBInstance(queryRecord.getTsdbName());
        if(tsdb == null){
            return Response.serverError().entity("No TSDB implementation with name " + queryRecord.getTsdbName() + " found on server!").build();
        }

        //TODO log disk usage





        //start threaded backround measurement
        dockerStatsUtil.startDockerContainerMeasurement(DockerCommandLineUtil.getRunningContainerId(queryRecord.getTsdbName()));
        long startMilliseconds = System.currentTimeMillis();

        //the query
        String queryResult = tsdb.performQuery(queryRecord.getIpAddress(), queryRecord.getPortNumber(), queryRecord.getQuery());

        // for testing
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endMilliseconds = System.currentTimeMillis();
        List<Pair<Double,Double>> dockerMeasurement = dockerStatsUtil.stopDockerContainerMeasurement();


        //TODO set queryRecord with diskUsage
        // edit and write the queryrecord to json file (threaded)
        queryRecord.setQueryTimeMilliseconds(endMilliseconds - startMilliseconds);
        statsCollector.addQueryRecordEditJob(queryRecord, dockerMeasurement);

        if (queryResult != null) {
            return Response.ok().entity(queryResult).build();
        }

        return Response.serverError().entity("Error performing query!").build();

    }

/*
    //tesing
    @GET
    @Path("measurement")
    public Response getMeasurement(){
        if(dockerMeasurement == null || dockerMeasurement.isEmpty()){
            return Response.serverError().entity(new String[]{"no measurement available"}).build();
        }
        List<String> answers = new LinkedList<>();
        for (Pair<Double, Double> pair : dockerMeasurement){
            answers.add("Cpu: " + pair.getFirst() + "% with Memory: " + pair.getSecond()+"%");
        }
        return Response.ok().entity(answers.toArray(new String[]{})).build();
    }
 */
}
