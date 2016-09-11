package de.qaware.chronix.server.benchmark.queryrunner;


import de.qaware.chronix.database.BenchmarkDataSource;
import de.qaware.chronix.database.BenchmarkQuery;
import de.qaware.chronix.database.TimeSeries;
import de.qaware.chronix.server.benchmark.collector.StatsCollector;
import de.qaware.chronix.server.util.DockerCommandLineUtil;
import de.qaware.chronix.server.util.DockerStatsRecord;
import de.qaware.chronix.server.util.DockerStatsUtil;
import de.qaware.chronix.shared.DataModels.Tuple;
import de.qaware.chronix.shared.QueryUtil.BenchmarkRecord;
import de.qaware.chronix.shared.QueryUtil.ImportRecord;
import de.qaware.chronix.shared.QueryUtil.QueryRecord;
import de.qaware.chronix.shared.ServerConfig.TSDBInterfaceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by mcqueen666 on 15.06.16.
 */
@Path("/queryrunner")
@Produces(MediaType.APPLICATION_JSON)
public class QueryRunnerResource {

    private final Logger logger = LoggerFactory.getLogger(QueryRunnerResource.class);
    private final TSDBInterfaceHandler tsdbInterfaceHandler = TSDBInterfaceHandler.getInstance();
    private final DockerStatsUtil dockerStatsUtil = DockerStatsUtil.getInstance();
    private final StatsCollector statsCollector = StatsCollector.getInstance();

    @POST
    @Path("performQuery")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response performQuery(QueryRecord queryRecord){
        if(queryRecord != null) {
            BenchmarkDataSource<Object> tsdb = tsdbInterfaceHandler.getTSDBInstance(queryRecord.getTsdbName());
            if (tsdb == null) {
                logger.error("No TSDB implementation with name " + queryRecord.getTsdbName() + " found on.");
                return Response.serverError().entity(new String[]{"No TSDB implementation with name " + queryRecord.getTsdbName() + " found on server!"}).build();
            }

            if(tsdb.setup(queryRecord.getIpAddress(),Integer.valueOf(queryRecord.getPortNumber()))) {
                List<BenchmarkQuery> queryList = queryRecord.getQueryList();
                List<String> queryResults = new LinkedList<>();

                //get the query strings
                Map<Object, BenchmarkQuery> queryBenchmarkQueryMap = new HashMap<>();
                for (BenchmarkQuery benchmarkQuery : queryList) {
                    if (benchmarkQuery != null) {
                        queryBenchmarkQueryMap.put(tsdb.getQueryObject(benchmarkQuery), benchmarkQuery);
                    }
                }

                //start threaded background measurement
                Long startDiskUsage = dockerStatsUtil.estimateStorageSize(queryRecord.getTsdbName(), tsdb.getStorageDirectoryPath());
                dockerStatsUtil.startDockerContainerMeasurement(DockerCommandLineUtil.getRunningContainerId(queryRecord.getTsdbName()));
                long startMilliseconds = System.currentTimeMillis();

                //perform the query mix
                for (Map.Entry<Object, BenchmarkQuery> entry : queryBenchmarkQueryMap.entrySet()) {

                    List<String> results = tsdb.performQuery(entry.getValue(), entry.getKey());
                    if (results != null && !results.isEmpty()) {
                        queryResults.addAll(results);
                    }
                }

                // end measurement
                long endMilliseconds = System.currentTimeMillis();
                List<DockerStatsRecord> dockerMeasurement = dockerStatsUtil.stopDockerContainerMeasurement();
                Long endDiskUsage = dockerStatsUtil.estimateStorageSize(queryRecord.getTsdbName(), tsdb.getStorageDirectoryPath());


                // edit and write the queryrecord to json file (threaded)
                queryRecord.setQueryTimeMilliseconds(endMilliseconds - startMilliseconds);
                if (startDiskUsage != -1 && endDiskUsage != -1) {
                    queryRecord.setDiskUsage(String.valueOf((endDiskUsage - startDiskUsage)));
                    queryRecord.setDiskUsageTotal(String.valueOf(endDiskUsage));
                }
                statsCollector.addQueryRecordEditJob(queryRecord, dockerMeasurement);

                tsdb.shutdown();
                if (!queryResults.isEmpty()) {
                    return Response.ok().entity(queryResults.toArray(new String[]{})).build();
                }
            }

        }
        logger.error("Error performing queries!");
        return Response.serverError().entity(new String[]{"Error performing queries!"}).build();

    }

    @POST
    @Path("performImport")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response performImport(ImportRecord importRecord){
        if(importRecord != null) {
            BenchmarkDataSource tsdb = tsdbInterfaceHandler.getTSDBInstance(importRecord.getTsdbName());
            if (tsdb == null) {
                logger.error("No TSDB implementation with name " + importRecord.getTsdbName() + " found on.");
                return Response.serverError().entity(new String[]{"No TSDB implementation with name " + importRecord.getTsdbName() + " found on server!"}).build();
            }

            if(tsdb.setup(importRecord.getIpAddress(),Integer.valueOf(importRecord.getPortNumber()))) {
                List<TimeSeries> importList = importRecord.getTimeSeriesList();
                List<String> importResults = new LinkedList<>();

                //start threaded background measurement
                Long startDiskUsage = dockerStatsUtil.estimateStorageSize(importRecord.getTsdbName(), tsdb.getStorageDirectoryPath());
                dockerStatsUtil.startDockerContainerMeasurement(DockerCommandLineUtil.getRunningContainerId(importRecord.getTsdbName()));
                long startMilliseconds = System.currentTimeMillis();

                //preform the import
                for (TimeSeries timeSeries : importList) {
                    importResults.add(tsdb.importDataPoints(timeSeries));
                }

                // end measurement
                long endMilliseconds = System.currentTimeMillis();
                List<DockerStatsRecord> dockerMeasurement = dockerStatsUtil.stopDockerContainerMeasurement();
                Long endDiskUsage = dockerStatsUtil.estimateStorageSize(importRecord.getTsdbName(), tsdb.getStorageDirectoryPath());

                // edit and write the queryrecord to json file (threaded)
                importRecord.setQueryTimeMilliseconds(endMilliseconds - startMilliseconds);
                if (startDiskUsage != -1 && endDiskUsage != -1) {
                    importRecord.setDiskUsage(String.valueOf((endDiskUsage - startDiskUsage)));
                    importRecord.setDiskUsageTotal(String.valueOf(endDiskUsage));
                }
                statsCollector.addQueryRecordEditJob(importRecord, dockerMeasurement);

                tsdb.shutdown();
                if (!importResults.isEmpty()) {
                    return Response.ok().entity(importResults.toArray(new String[]{})).build();
                }
            }


        }
        logger.error("Error performing import!");
        return Response.serverError().entity(new String[]{"Error performing import!"}).build();
    }

    
}
