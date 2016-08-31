package Client;

import de.qaware.chronix.client.benchmark.configurator.Configurator;
import de.qaware.chronix.client.benchmark.queryhandler.QueryHandler;
import de.qaware.chronix.database.BenchmarkDataSource;
import de.qaware.chronix.database.TimeSeries;
import de.qaware.chronix.shared.QueryUtil.ImportRecord;
import de.qaware.chronix.shared.ServerConfig.ServerConfigAccessor;
import de.qaware.chronix.shared.ServerConfig.ServerConfigRecord;
import de.qaware.chronix.shared.ServerConfig.TSDBInterfaceHandler;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by mcqueen666 on 31.08.16.
 */
public class ImportTest {

    public static void importCsv(List<TimeSeries> checktimeSeriesList) {

        Configurator configurator = Configurator.getInstance();
        String server = "localhost";

        System.out.println("\n###### Client.ImportTest ######");

        if (configurator.isServerUp(server)) {
            System.out.println("Server is up");
        } else {
            System.out.println("Server not responding");
        }
        // import test


        ServerConfigAccessor serverConfigAccessor = ServerConfigAccessor.getInstance();
        LinkedList<ServerConfigRecord> readRecord = serverConfigAccessor.getServerConfigRecords();
        TSDBInterfaceHandler interfaceHandler = TSDBInterfaceHandler.getInstance();
        QueryHandler queryHandler = QueryHandler.getInstance();

        if (!checktimeSeriesList.isEmpty()) {

            for (ServerConfigRecord r : readRecord) {
                LinkedList<String> externalImpls = r.getExternalTimeSeriesDataBaseImplementations();
                for (String s : externalImpls) {
                        BenchmarkDataSource tsdb = interfaceHandler.getTSDBInstance(s);
                        String ip = r.getServerAddress();
                        String port = serverConfigAccessor.getHostPortForTSDB(ip, s);
                        String queryID = "import_air-lasttest:1";

                        ImportRecord importRecord = new ImportRecord(queryID, ip, port, s, checktimeSeriesList.subList(0, 1));
                        String[] results = queryHandler.doImportOnServer(ip, importRecord);
                        Long latency = queryHandler.getLatencyForQueryID(queryID);
                        if (latency != null) {
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

