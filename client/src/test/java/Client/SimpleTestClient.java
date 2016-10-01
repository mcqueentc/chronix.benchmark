package Client;

import Docker.UploadDockerFiles;
import Server.GenerateServerConfigRecord;
import Server.InterfaceAndConfigUploadTest;
import de.qaware.chronix.client.benchmark.benchmarkrunner.BenchmarkRunner;
import de.qaware.chronix.client.benchmark.benchmarkrunner.util.BenchmarkRunnerHelper;
import de.qaware.chronix.client.benchmark.benchmarkrunner.util.TimeSeriesCounter;
import de.qaware.chronix.client.benchmark.configurator.Configurator;
import de.qaware.chronix.database.BenchmarkDataSource;
import de.qaware.chronix.database.TimeSeriesMetaData;
import de.qaware.chronix.shared.QueryUtil.JsonTimeSeriesHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mcqueen666 on 31.08.16.
 */
public class SimpleTestClient {

    public static void main(String[] args){

        Configurator configurator = Configurator.getInstance();
        JsonTimeSeriesHandler jsonTimeSeriesHandler = JsonTimeSeriesHandler.getInstance();
        BenchmarkRunner benchmarkRunner = BenchmarkRunner.getInstance();

        long startMillis;
        long endMillis;

        String server = "192.168.2.123";
        //String server = "localhost";
        List<String> tsdbImportList = new ArrayList<>();
        tsdbImportList.add("chronix");
        tsdbImportList.add("influxdb");
        tsdbImportList.add("kairosdb");
        tsdbImportList.add("graphite");
        tsdbImportList.add("opentsdb");

        try {
            if (configurator.isServerUp(server)) {
                System.out.println("Server is up");
            }
        } catch (Exception e){
            System.out.println("Server not responding. Error: " + e.getLocalizedMessage());
            return;
        }


        GenerateServerConfigRecord.main(new String[]{server});
        UploadDockerFiles.main(new String[]{server});
        InterfaceAndConfigUploadTest.main(new String[]{server});
        //BuildDockerContainer.main(new String[]{server,"chronix","influxdb","kairosdb", "opentsdb", "graphite"});
        //StartDockerContainer.main(new String[]{server,"chronix","influxdb","kairosdb", "opentsdb", "graphite"});
        //RunningTestDockerContainer.main(new String[]{server,"chronix","influxdb","kairosdb", "opentsdb", "graphite"});
        //StopDockerContainer.main(new String[]{server,"chronix","influxdb","kairosdb", "opentsdb", "graphite"});
        //CleanDatabasesOnServerTest.main(new String[]{server});




/*

        // import test

        List<File> directories = new ArrayList<>();
        directories.add(new File("/Users/mcqueen666/chronixBenchmark/timeseries_records/air-lasttest_small"));
        //directories.add(new File("/Users/mcqueen666/chronixBenchmark/timeseries_records/air-lasttest"));
        //directories.add(new File("/Users/mcqueen666/chronixBenchmark/timeseries_records/shd"));
        //directories.add(new File("/Users/mcqueen666/chronixBenchmark/timeseries_records/promt"));
        // as if was not imported previously
        for(File directory : directories){
            jsonTimeSeriesHandler.deleteTimeSeriesMetaDataJsonFile(directory.getName());
        }
        for(File directory : directories) {
            startMillis = System.currentTimeMillis();
            //multiple file upload and import test
            benchmarkRunner.importTimesSeriesWithUploadedFiles(server, directory, 25, 0, tsdbImportList);

            //ImportTest.importTimeSeriesFromDirectory(server, directory, 5 , 0, tsdbImportList);
            endMillis = System.currentTimeMillis();
            System.out.println("Import test total time: " + (endMillis - startMillis) + "ms\n");

       }

*/

        // query test
        TimeSeriesCounter timeSeriesCounter = TimeSeriesCounter.getInstance();
        List<TimeSeriesMetaData> randomTimeSeries = timeSeriesCounter.getRandomTimeSeriesMetaData(10);
        BenchmarkRunnerHelper benchmarkRunnerHelper = BenchmarkRunnerHelper.getInstance();
        BenchmarkDataSource.QueryFunction function = BenchmarkDataSource.QueryFunction.STDDEV;
        Float p = 0.5f;
        //function = benchmarkRunnerHelper.getRandomQueryFunction();

        startMillis = System.currentTimeMillis();
        QueryTest.queryTest(server, randomTimeSeries, function, p, tsdbImportList);
        endMillis = System.currentTimeMillis();
        System.out.println("Query test total time: " + (endMillis - startMillis) + "ms\n");


        //get benchmark query record test
        benchmarkRunner = BenchmarkRunner.getInstance();
        System.out.println("Downloading benchmark records from server successful: " +  benchmarkRunner.getBenchmarkRecordsFromServer(server));

    }
}
