package Client;

import de.qaware.chronix.client.benchmark.benchmarkrunner.BenchmarkRunner;

import java.io.File;
import java.util.List;

/**
 * Created by mcqueen666 on 31.08.16.
 */
public class ImportTest {


    public static void importTimeSeriesFromDirectory(String server, List<File> directories){
        BenchmarkRunner benchmarkRunner = BenchmarkRunner.getInstance();
        List<String> answers = benchmarkRunner.importTimeSeriesFromDirectory(server, directories, 50);

        answers.forEach(System.out::println);

    }



}

