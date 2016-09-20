package Client;

import de.qaware.chronix.client.benchmark.benchmarkrunner.util.BenchmarkRunnerHelper;
import de.qaware.chronix.client.benchmark.configurator.Configurator;
import de.qaware.chronix.client.benchmark.queryhandler.QueryHandler;
import de.qaware.chronix.database.BenchmarkDataSource;
import de.qaware.chronix.database.TimeSeriesMetaData;
import de.qaware.chronix.shared.DataModels.Pair;
import de.qaware.chronix.shared.QueryUtil.QueryRecord;

import java.util.List;

/**
 * Created by mcqueen666 on 31.08.16.
 */
public class QueryTest {

    public static void queryCount(List<TimeSeriesMetaData> metaDataList, String server) {

        if (metaDataList != null && !metaDataList.isEmpty()){
            Configurator configurator = Configurator.getInstance();

            System.out.println("\n###### Client.QueryTest ######");

            if (configurator.isServerUp(server)) {
                System.out.println("Server is up");
            } else {
                System.out.println("Server not responding");
                return;
            }

            // query test

            BenchmarkRunnerHelper benchmarkRunnerHelper = BenchmarkRunnerHelper.getInstance();
            QueryHandler queryHandler = QueryHandler.getInstance();

            String queryID = "query_number_of_imported:count:" + metaDataList.size();

            List<QueryRecord> queryRecordList = benchmarkRunnerHelper.getQueryRecordForTimeSeriesMetaData(metaDataList,
                    queryID,
                    server,
                    BenchmarkDataSource.QueryFunction.COUNT);

            for(QueryRecord queryRecord : queryRecordList){
                String[] results = queryHandler.doQueryOnServer(queryRecord.getIpAddress(), queryRecord);
                Long latency = queryHandler.getLatencyForQueryID(Pair.of(queryID, queryRecord.getTsdbName()));
                if (latency != null) {
                    System.out.println("\nTSDB: " + queryRecord.getTsdbName());
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
