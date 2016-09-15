package de.qaware.chronix.client.benchmark.benchmarkrunner.util;

import de.qaware.chronix.client.benchmark.configurator.Configurator;
import de.qaware.chronix.client.benchmark.queryhandler.QueryHandler;
import de.qaware.chronix.database.TimeSeries;
import de.qaware.chronix.shared.QueryUtil.ImportRecord;
import de.qaware.chronix.shared.ServerConfig.ServerConfigAccessor;
import de.qaware.chronix.shared.ServerConfig.ServerConfigRecord;
import de.qaware.chronix.shared.ServerConfig.TSDBInterfaceHandler;

import java.util.LinkedList;
import java.util.List;

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
     * @param serverAddress the server address on which the import should be done.
     * @return list of ImportRecords.
     */
    public List<ImportRecord> getImportRecordForTimeSeries(List<TimeSeries> timeSeriesList, String queryID, String serverAddress) {
        List<ImportRecord> importRecordList = new LinkedList<>();
        if (!timeSeriesList.isEmpty()) {
            ServerConfigRecord serverConfigRecord = serverConfigAccessor.getServerConfigRecord(serverAddress);
            if(serverConfigRecord != null) {
                List<String> externalImpls = serverConfigRecord.getExternalTimeSeriesDataBaseImplementations();
                for (String tsdb : externalImpls) {
                    String ip = serverConfigRecord.getServerAddress();
                    String port = serverConfigAccessor.getHostPortForTSDB(ip, tsdb);
                    importRecordList.add(new ImportRecord(queryID, ip, port, tsdb, timeSeriesList));
                }
            }
        }
        return importRecordList;
    }


}
