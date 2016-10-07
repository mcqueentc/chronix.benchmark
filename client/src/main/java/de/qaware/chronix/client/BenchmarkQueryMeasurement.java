package de.qaware.chronix.client;

import de.qaware.chronix.client.benchmark.benchmarkrunner.BenchmarkRunner;
import java.util.List;
import java.util.Map;

/**
 * Created by mcqueen666 on 07.10.16.
 */
public class BenchmarkQueryMeasurement {
    public static void main(String[] args){
        if(args.length < 2) {
            printUsage();
            return;
        }

        String server = args[0];
        List<String> tsdbList;

        Map<String, List<String>> serverTsdbMap = ClientMenu.getConfiguredServerAndTSDBs(args);
        if(serverTsdbMap.isEmpty()){
            System.err.println("Server: " + server + " was not configured!");
            return;
        }

        tsdbList = serverTsdbMap.get(server);
        if(tsdbList.isEmpty()){
            System.err.println("No implemented tsdb interfaces found for your entries -> aborting");
            return;
        }

        BenchmarkRunner benchmarkRunner = BenchmarkRunner.getInstance();
        long startMillis = System.currentTimeMillis();
        benchmarkRunner.doBenchmarkQuery(server, tsdbList);
        long endMillis = System.currentTimeMillis();
        System.out.println("Query test total time: " + (endMillis - startMillis) + "ms");

        System.out.println("\nDownloading benchmark records from server successful: " +  benchmarkRunner.getBenchmarkRecordsFromServer(server));



    }

    public static void printUsage(){
        System.out.println("Benchmark usage: benchmark [server] [tsdbName1] [tsdbName2] ...");
    }
}
