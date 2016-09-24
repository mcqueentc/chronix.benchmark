package de.qaware.chronix.client.benchmark.benchmarkrunner.util;

import de.qaware.chronix.database.BenchmarkDataSource.QueryFunction;
import de.qaware.chronix.database.BenchmarkQuery;
import de.qaware.chronix.database.TimeSeries;
import de.qaware.chronix.database.TimeSeriesMetaData;
import de.qaware.chronix.shared.QueryUtil.ImportRecord;
import de.qaware.chronix.shared.QueryUtil.QueryRecord;
import de.qaware.chronix.shared.ServerConfig.ServerConfigAccessor;
import de.qaware.chronix.shared.ServerConfig.ServerConfigRecord;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by mcqueen666 on 15.09.16.
 */
public class BenchmarkRunnerHelper {

    private static BenchmarkRunnerHelper instance;

    private ServerConfigAccessor serverConfigAccessor;

    private BenchmarkRunnerHelper(){
        serverConfigAccessor = ServerConfigAccessor.getInstance();
    }

    public static BenchmarkRunnerHelper getInstance() {
        if(instance == null){
            instance = new BenchmarkRunnerHelper();
        }
        return instance;
    }

    /**
     * Generate a list of ImportRecords containing one ImportRecord per tsdb implementation on given server for a given time series.
     *
     * @param timeSeriesList the time series to be imported.
     * @param queryID the query id
     * @param serverAddress the server address or ip on which the import should be done.
     * @param tsdbImportList a list of tsdb names on which the import should be done. null == all available on server
     * @return list of ImportRecords.
     */
    public List<ImportRecord> getImportRecordForTimeSeries(List<TimeSeries> timeSeriesList,
                                                           String queryID,
                                                           String serverAddress, List<String> tsdbImportList) {

        List<ImportRecord> importRecordList = new LinkedList<>();

            ServerConfigRecord serverConfigRecord = serverConfigAccessor.getServerConfigRecord(serverAddress);
            if(serverConfigRecord != null) {
                List<String> externalImpls = serverConfigRecord.getExternalTimeSeriesDataBaseImplementations();
                for (String tsdb : externalImpls) {
                    if(tsdbImportList != null){
                        if(tsdbImportList.contains(tsdb)){
                            String ip = serverConfigRecord.getServerAddress();
                            String port = serverConfigAccessor.getHostPortForTSDB(ip, tsdb);
                            importRecordList.add(new ImportRecord(queryID, ip, port, tsdb, timeSeriesList));
                        }
                    } else {
                        //if tsdbImportList is null, take all available tsdbs saved in server record.
                        String ip = serverConfigRecord.getServerAddress();
                        String port = serverConfigAccessor.getHostPortForTSDB(ip, tsdb);
                        importRecordList.add(new ImportRecord(queryID, ip, port, tsdb, timeSeriesList));
                    }
                }
            }
        
        return importRecordList;
    }

    /**
     * Generate a list of QueryRecords containing one QueryRecord per tsdb implementation on given server for given times seres meta data.
     *
     * @param timeSeriesMetaDataList the time series meta data which should be queried.
     * @param queryID the query id
     * @param serverAddress the server address or ip on which the queries should be done.
     * @param function the query function which should be performed.
     * @return list of QueryRecords.
     */
    public List<QueryRecord> getQueryRecordForTimeSeriesMetaData(List<TimeSeriesMetaData> timeSeriesMetaDataList,
                                                                 String queryID,
                                                                 String serverAddress,
                                                                 QueryFunction function){

        List<QueryRecord> queryRecordList = new LinkedList<>();
        if(!timeSeriesMetaDataList.isEmpty()){
            ServerConfigRecord serverConfigRecord = serverConfigAccessor.getServerConfigRecord(serverAddress);
            if(serverConfigRecord != null){
                // make benchmarkquery list with entries
                List<BenchmarkQuery> querys = new LinkedList<>();
                for(TimeSeriesMetaData metaData : timeSeriesMetaDataList) {
                    querys.add(new BenchmarkQuery(metaData, null, function));
                }

                List<String> externalImpls = serverConfigRecord.getExternalTimeSeriesDataBaseImplementations();
                //generate queryRecord for every tsdb
                for(String tsdb : externalImpls){
                    String ip = serverConfigRecord.getServerAddress();
                    String port = serverConfigAccessor.getHostPortForTSDB(ip, tsdb);

                 queryRecordList.add(new QueryRecord(queryID, serverAddress, port, tsdb, querys));

                }
            }
        }
        return queryRecordList;
    }

    /**
     * Selects a random QueryFunction
     *
     * @return a QueryFunction
     */
    public QueryFunction getRandomQueryFunction(){
        Random random = new Random();
        int functionCount = QueryFunction.values().length - 1; // - 1 to disable QUERY_ONLY at the moment

        return QueryFunction.values()[random.nextInt(functionCount)];
    }

}
