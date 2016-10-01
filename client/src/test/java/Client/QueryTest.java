package Client;

import de.qaware.chronix.client.benchmark.benchmarkrunner.BenchmarkRunner;
import de.qaware.chronix.database.BenchmarkDataSource.QueryFunction;
import de.qaware.chronix.database.TimeSeriesMetaData;

import java.util.List;

/**
 * Created by mcqueen666 on 31.08.16.
 */
public class QueryTest {

    public static void queryTest(String server, List<TimeSeriesMetaData> metaDataList, QueryFunction function, Float p, List<String> tsdbImportList) {

        BenchmarkRunner benchmarkRunner = BenchmarkRunner.getInstance();
        List<String> answers = benchmarkRunner.queryWithFunction(server, metaDataList, function, p, tsdbImportList);

        answers.forEach(System.out::println);
    }
}
