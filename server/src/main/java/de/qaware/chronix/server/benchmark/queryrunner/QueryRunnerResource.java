package de.qaware.chronix.server.benchmark.queryrunner;


import de.qaware.chronix.database.BenchmarkDataSource;
import de.qaware.chronix.database.BenchmarkQuery;
import de.qaware.chronix.server.benchmark.collector.StatsCollector;
import de.qaware.chronix.server.util.DockerCommandLineUtil;
import de.qaware.chronix.server.util.DockerStatsUtil;
import de.qaware.chronix.shared.DataModels.Tuple;
import de.qaware.chronix.shared.QueryUtil.BenchmarkRecord;
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

        List<BenchmarkQuery> queryList = queryRecord.getQueryList();
        List<String> queryResults = new LinkedList<>();

        //start threaded background measurement
        Long startDiskUsage = dockerStatsUtil.estimateStorageSize(queryRecord.getTsdbName(), tsdb.getStorageDirectoryPath());
        dockerStatsUtil.startDockerContainerMeasurement(DockerCommandLineUtil.getRunningContainerId(queryRecord.getTsdbName()));
        long startMilliseconds = System.currentTimeMillis();


        //perform the query mix
        for(BenchmarkQuery query : queryList){
            queryResults.addAll(tsdb.performQuery(query));
        }


        // for testing
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        // end measurement
        long endMilliseconds = System.currentTimeMillis();
        List<Tuple<Double,Double,Long,Long>> dockerMeasurement = dockerStatsUtil.stopDockerContainerMeasurement();
        Long endDiskUsage = dockerStatsUtil.estimateStorageSize(queryRecord.getTsdbName(), tsdb.getStorageDirectoryPath());


        // edit and write the queryrecord to json file (threaded)
        queryRecord.setQueryTimeMilliseconds(endMilliseconds - startMilliseconds);
        if(startDiskUsage != -1 && endDiskUsage != -1){
            queryRecord.setDiskUsage(String.valueOf((endDiskUsage - startDiskUsage)));
            queryRecord.setDiskUsageTotal(String.valueOf(endDiskUsage));
        }
        statsCollector.addQueryRecordEditJob(queryRecord, dockerMeasurement);

        if (!queryResults.isEmpty()) {
            return Response.ok().entity(queryResults.toArray(new String[]{})).build();
        }

        return Response.serverError().entity(new String[]{"Error performing query!"}).build();

    }
    
}
