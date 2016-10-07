package de.qaware.chronix.client;

import de.qaware.chronix.client.benchmark.benchmarkrunner.BenchmarkRunner;
import de.qaware.chronix.database.BenchmarkDataSource;
import de.qaware.chronix.shared.ServerConfig.TSDBInterfaceHandler;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by mcqueen666 on 07.10.16.
 */
public class BenchmarkQueryMeasurement {
    public static void main(String[] args){
        if(args.length < 1) {
            printUsage();
            return;
        }

        String server = args[0];
        List<String> tsdbList = new LinkedList<>();

        TSDBInterfaceHandler tsdbInterfaceHandler = TSDBInterfaceHandler.getInstance();
        // print info for user
        System.out.println("Server: " + server);
        for(int i = 1; i < args.length; i++){
            BenchmarkDataSource<Object> tsdbInterface = tsdbInterfaceHandler.getTSDBInstance(args[i]);
            if(tsdbInterface != null){
                System.out.println("TSDB:   " + args[i]);
                tsdbList.add(args[i]);
            } else {
                System.err.println("No interface found for: " + args[i] + " -> rejected!");
            }
        }

        if(tsdbList.isEmpty()){
            System.err.println("No implemented tsdb interfaces found for your entries -> aborting");
            return;
        }

        BenchmarkRunner benchmarkRunner = BenchmarkRunner.getInstance();
        long startMillis = System.currentTimeMillis();
        benchmarkRunner.doBenchmarkQuery(server, tsdbList);
        long endMillis = System.currentTimeMillis();
        System.out.println("Query test total time: " + (endMillis - startMillis) + "ms\n");


    }

    public static void printUsage(){
        System.out.println("Benchmark usage: benchmark [server] [tsdbName1] [tsdbName2] ...");
    }
}
