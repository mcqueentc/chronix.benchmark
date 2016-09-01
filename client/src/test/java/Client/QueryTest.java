package Client;

import de.qaware.chronix.client.benchmark.configurator.Configurator;
import de.qaware.chronix.client.benchmark.queryhandler.QueryHandler;
import de.qaware.chronix.database.BenchmarkDataSource;
import de.qaware.chronix.database.BenchmarkQuery;
import de.qaware.chronix.database.TimeSeries;
import de.qaware.chronix.database.TimeSeriesMetaData;
import de.qaware.chronix.shared.QueryUtil.QueryRecord;
import de.qaware.chronix.shared.ServerConfig.ServerConfigAccessor;
import de.qaware.chronix.shared.ServerConfig.ServerConfigRecord;
import de.qaware.chronix.shared.ServerConfig.TSDBInterfaceHandler;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by mcqueen666 on 31.08.16.
 */
public class QueryTest {

    public static void query(List<TimeSeries> checktimeSeriesList) {

        if (checktimeSeriesList != null && !checktimeSeriesList.isEmpty()){
            Configurator configurator = Configurator.getInstance();
            String server = "localhost";

            System.out.println("\n###### Client.QueryTest ######");

            if (configurator.isServerUp(server)) {
                System.out.println("Server is up");
            } else {
                System.out.println("Server not responding");
            }

            // query test

            ServerConfigAccessor serverConfigAccessor = ServerConfigAccessor.getInstance();
            LinkedList<ServerConfigRecord> readRecord = serverConfigAccessor.getServerConfigRecords();
            TSDBInterfaceHandler interfaceHandler = TSDBInterfaceHandler.getInstance();
            QueryHandler queryHandler = QueryHandler.getInstance();

            for (ServerConfigRecord r : readRecord) {
                LinkedList<String> externalImpls = r.getExternalTimeSeriesDataBaseImplementations();
                for (String externalImpl : externalImpls) {
                    BenchmarkDataSource tsdb = interfaceHandler.getTSDBInstance(externalImpl);
                    String ip = r.getServerAddress();
                    String port = serverConfigAccessor.getHostPortForTSDB(ip, externalImpl);
                    String queryID = "air-lasttest:sum:1";

                    // make benchmarkquery list with entries
                    List<BenchmarkQuery> querys = new LinkedList<>();
                    // entry
                    TimeSeriesMetaData timeSeriesMetaData = new TimeSeriesMetaData(checktimeSeriesList.get(0));
                    querys.add(new BenchmarkQuery(timeSeriesMetaData, null, BenchmarkDataSource.QueryFunction.COUNT));

                    // make queryRecord with the benchmarkquery list
                    QueryRecord queryRecord = new QueryRecord(queryID, ip, port, externalImpl, querys);
                    String[] results = queryHandler.doQueryOnServer(ip, queryRecord);
                    Long latency = queryHandler.getLatencyForQueryID(queryID);
                    if (latency != null) {
                        System.out.println("\nTSDB: " + externalImpl);
                        System.out.println("QueryID: " + queryID);
                        System.out.println("Latency: " + latency + " milliseconds");
                        for (String result : results) {
                            System.out.println("Result: " + result);
                        }
                    } else {
                        System.out.println("Error: " + results[0]);
                    }

                }
            }


        }
    }
}
