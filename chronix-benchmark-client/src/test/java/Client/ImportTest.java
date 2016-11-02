package Client;

import de.qaware.chronix.client.benchmark.benchmarkrunner.BenchmarkRunner;

import java.io.File;
import java.util.List;

/**
 * Created by mcqueen666 on 31.08.16.
 */
public class ImportTest {


    public static void importTimeSeriesFromDirectory(String server, File directories, int batchSize, int fromFile, List<String> tsdbImportList){
        BenchmarkRunner benchmarkRunner = BenchmarkRunner.getInstance();
        benchmarkRunner.importTimeSeriesFromDirectory(server, directories, batchSize, fromFile, tsdbImportList);

    }



}

