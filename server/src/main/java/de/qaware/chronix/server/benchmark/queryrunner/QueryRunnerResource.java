package de.qaware.chronix.server.benchmark.queryrunner;


import com.fasterxml.jackson.databind.ObjectMapper;
import de.qaware.chronix.database.BenchmarkDataSource;
import de.qaware.chronix.database.BenchmarkQuery;
import de.qaware.chronix.database.TimeSeries;
import de.qaware.chronix.server.benchmark.collector.StatsCollector;
import de.qaware.chronix.server.util.DockerCommandLineUtil;
import de.qaware.chronix.server.util.DockerStatsRecord;
import de.qaware.chronix.server.util.DockerStatsUtil;
import de.qaware.chronix.server.util.ServerSystemUtil;
import de.qaware.chronix.shared.DataModels.ImportRecordWrapper;
import de.qaware.chronix.shared.DataModels.Tuple;
import de.qaware.chronix.shared.QueryUtil.*;
import de.qaware.chronix.shared.ServerConfig.TSDBInterfaceHandler;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;
import java.util.*;

/**
 * Created by mcqueen666 on 15.06.16.
 */
@Path("/queryrunner")
@Produces(MediaType.APPLICATION_JSON)
public class QueryRunnerResource {

    private final Logger logger = LoggerFactory.getLogger(QueryRunnerResource.class);
    private final StatsCollector statsCollector = StatsCollector.getInstance();

    @POST
    @Path("performQuery")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response performQuery(QueryRecord queryRecord){
        if(queryRecord != null) {
            TSDBInterfaceHandler tsdbInterfaceHandler = TSDBInterfaceHandler.getInstance();
            BenchmarkDataSource<Object> tsdb = tsdbInterfaceHandler.getTSDBInstance(queryRecord.getTsdbName());
            if (tsdb == null) {
                logger.error("No TSDB implementation with name " + queryRecord.getTsdbName() + " found on.");
                return Response.serverError().entity(new String[]{"No TSDB implementation with name " + queryRecord.getTsdbName() + " found on server!"}).build();
            }

            if(tsdb.setup(queryRecord.getIpAddress(),Integer.valueOf(queryRecord.getPortNumber()))) {
                logger.info("Performing query on {}",tsdb.getClass().getName());
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
                DockerStatsUtil dockerStatsUtil = DockerStatsUtil.getInstance();
                Long startDiskUsage = dockerStatsUtil.estimateStorageSize(queryRecord.getTsdbName(), tsdb.getMappedStorageDirectoryPath());
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
                tsdb.writeCachesToDisk();
                List<DockerStatsRecord> dockerMeasurement = dockerStatsUtil.stopDockerContainerMeasurement();
                Long endDiskUsage = dockerStatsUtil.estimateStorageSize(queryRecord.getTsdbName(), tsdb.getMappedStorageDirectoryPath());


                // edit and write the queryrecord to json file (threaded)
                queryRecord.setQueryTimeMilliseconds(endMilliseconds - startMilliseconds);
                if (startDiskUsage != -1 && endDiskUsage != -1) {
                    queryRecord.setDiskUsage(String.valueOf((endDiskUsage - startDiskUsage)));
                    queryRecord.setDiskUsageTotal(String.valueOf(endDiskUsage));
                } else {
                    queryRecord.setDiskUsage("-1");
                    queryRecord.setDiskUsageTotal("-1");
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
    public Response performImport(ImportRecordWrapper importRecordWrapper){
        List<String> importResults = new LinkedList<>();
        for(ImportRecord importRecord : importRecordWrapper.getImportRecordList()) {
            if (importRecord != null) {
                TSDBInterfaceHandler tsdbInterfaceHandler = TSDBInterfaceHandler.getInstance();
                //importRecord.setTimeSeriesList(importRecordWrapper.getTimeSeriesList());
                BenchmarkDataSource<Object> tsdb = tsdbInterfaceHandler.getTSDBInstance(importRecord.getTsdbName());
                if (tsdb == null) {
                    logger.error("No TSDB implementation with name " + importRecord.getTsdbName() + " found on.");
                    return Response.serverError().entity(new String[]{"No TSDB implementation with name " + importRecord.getTsdbName() + " found on server!"}).build();
                }

                if (tsdb.setup(importRecord.getIpAddress(), Integer.valueOf(importRecord.getPortNumber()))) {
                    logger.info("Performing import on {}", tsdb.getClass().getName());
                    //List<TimeSeries> importList = importRecord.getTimeSeriesList();


                    //start threaded background measurement
                    DockerStatsUtil dockerStatsUtil = DockerStatsUtil.getInstance();
                    Long startDiskUsage = dockerStatsUtil.estimateStorageSize(importRecord.getTsdbName(), tsdb.getMappedStorageDirectoryPath());
                    dockerStatsUtil.startDockerContainerMeasurement(DockerCommandLineUtil.getRunningContainerId(importRecord.getTsdbName()));
                    long startMilliseconds = System.currentTimeMillis();

                    //preform the import
                    for (TimeSeries timeSeries : importRecordWrapper.getTimeSeriesList()) {
                        String answer = tsdb.importDataPoints(timeSeries);
                        if(!answer.isEmpty()) {
                            importResults.add(tsdb.getClass().getName() + ": " + answer);
                        } else {
                            importResults.add(tsdb.getClass().getName() + ": Nothing imported");
                        }
                    }

                    // end measurement
                    long endMilliseconds = System.currentTimeMillis();
                    tsdb.writeCachesToDisk();
                    List<DockerStatsRecord> dockerMeasurement = dockerStatsUtil.stopDockerContainerMeasurement();
                    Long endDiskUsage = dockerStatsUtil.estimateStorageSize(importRecord.getTsdbName(), tsdb.getMappedStorageDirectoryPath());

                    // edit and write the queryrecord to json file (threaded)
                    importRecord.setQueryTimeMilliseconds(endMilliseconds - startMilliseconds);
                    if (startDiskUsage != -1 && endDiskUsage != -1) {
                        importRecord.setDiskUsage(String.valueOf((endDiskUsage - startDiskUsage)));
                        importRecord.setDiskUsageTotal(String.valueOf(endDiskUsage));
                    } else {
                        importRecord.setDiskUsage("-1");
                        importRecord.setDiskUsageTotal("-1");
                    }
                    statsCollector.addQueryRecordEditJob(importRecord, dockerMeasurement);

                    tsdb.shutdown();
                } else {
                    logger.error("Error performing import, tsdb: " + tsdb.getClass().getName());
                    return Response.serverError().entity(new String[]{"Error performing import! " + tsdb.getClass().getName()}).build();
                }

            }
        }

        return Response.ok().entity(importResults.toArray(new String[]{})).build();

    }

    @POST
    @Path("performImportWithFiles")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    public Response performImportWithFiles(@Context HttpServletRequest request) {
        Response response = Response.serverError().entity(new String[]{"Error performing import."}).build();
        if(ServletFileUpload.isMultipartContent(request)){
            final FileItemFactory factory = new DiskFileItemFactory();
            final ServletFileUpload fileUpload = new ServletFileUpload(factory);
            File[] filesToImport = null;
            File tempDirectory = new File(ServerSystemUtil.getBenchmarkUtilPath() + "tmp");
            if (!tempDirectory.exists()){
                tempDirectory.mkdir();
            }

            try{
                final List items = fileUpload.parseRequest(request);
                if(items != null){
                    ImportRecordWrapper importRecordWrapper = null;

                    // read the files from multipart
                    final Iterator iter = items.iterator();
                    while (iter.hasNext()){
                        final FileItem item = (FileItem) iter.next();
                        final String itemName = item.getName();
                        final String fieldName = item.getFieldName();
                        final String fieldValue = item.getString();


                        if(fieldName.equals("ImportRecordWrapper")){
                            // json string
                            ObjectMapper mapper = new ObjectMapper();
                            String jsonString = new String(fieldValue.getBytes());
                            importRecordWrapper = mapper.readValue(jsonString, ImportRecordWrapper.class);
                        }
                        if(!item.isFormField()){
                            // write file data to file
                            final File savedFile = new File(tempDirectory.getPath() + File.separator + itemName);
                            item.write(savedFile);
                        }
                    }


                    // only proceed if reading importRecordWrapper was successful.
                    if(importRecordWrapper != null) {
                        // read time series from json files
                        filesToImport = tempDirectory.listFiles();

                        if (filesToImport != null && filesToImport.length > 0) {
                            //delete non .gz (even if all files were .gz files, there is still a "null" file from the iterator)
                            List<File> fileList = Arrays.asList(filesToImport);
                            List<File> importFileList = new LinkedList<>();
                            for(File file : fileList){
                                if(file.getName().endsWith(".gz")){
                                    importFileList.add(file);
                                }
                            }

                            JsonTimeSeriesHandler jsonTimeSeriesHandler = JsonTimeSeriesHandler.getInstance();
                            List<TimeSeries> timeSeriesList = jsonTimeSeriesHandler.readTimeSeriesJson(importFileList.toArray(new File[]{}));
                            importRecordWrapper.setTimeSeriesList(timeSeriesList);


                            logger.info("pre import: timeSeriesSize: {} and importRecordSize: {}",
                                    importRecordWrapper.getTimeSeriesList().size(),
                                    importRecordWrapper.getImportRecordList().size());
                            // do the import
                            response = this.performImport(importRecordWrapper);
                        }
                    }


                }

            } catch (Exception e){
                logger.error("Error handling files: {}",e.getLocalizedMessage());
                response = Response.serverError().entity(new String[]{"Error performing import: " + e.getLocalizedMessage()}).build();
            }


            // clean tmp dir
            if(filesToImport != null) {
                for (File file : filesToImport) {
                    if( ! file.delete()){
                        logger.error("Could not delete tmp file: {}",file.getName());
                    }
                }
            }
            if( ! tempDirectory.delete()){
                logger.error("Could not delete tmp directory: {}", tempDirectory.getAbsolutePath());
            }
        }

        return response;

    }



    @POST
    @Path("cleanDatabases")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cleanDatabases(List<CleanCommand> cleanCommandList){
        List<String> results = new ArrayList<>();
        if(cleanCommandList != null && !cleanCommandList.isEmpty()){
            TSDBInterfaceHandler tsdbInterfaceHandler = TSDBInterfaceHandler.getInstance();
            for(CleanCommand cleanCommand : cleanCommandList){
                BenchmarkDataSource<Object> tsdb = tsdbInterfaceHandler.getTSDBInstance(cleanCommand.getTsdbName());
                if(tsdb != null){
                    if(tsdb.setup(cleanCommand.getIpAddress(), cleanCommand.getPortNumber())){
                        if(tsdb.clean()){
                            results.add(cleanCommand.getTsdbName() + " was cleaned.");
                        }
                        else {
                            results.add(cleanCommand.getTsdbName() + " was not cleaned.");
                        }
                    }
                    else {
                        results.add(cleanCommand.getTsdbName() + " could not be setup.");
                    }
                    tsdb.shutdown();
                }
                else {
                    results.add(cleanCommand.getTsdbName() + " not found on server.");
                }

            }
        }
        else {
            results.add("No instructions given!");
        }

        return Response.ok().entity(results.toArray(new String[]{})).build();
    }
    
}
