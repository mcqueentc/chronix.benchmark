package de.qaware.chronix.client.benchmark;

import de.qaware.chronix.client.benchmark.resultpresenter.ResultPresenter;

/**
 * Created by mcqueen666 on 10.10.16.
 */
public class BenchmarkStatistics {
    public static void main(String[] args){
        if(args.length < 1){
            printUsage();
            return;
        }

        String option = args[0];
        switch (option){
            case "timeseries":
                ResultPresenter.getInstance().analyzeTimeSeries();
                break;
            case "benchmark":
                //TODO
        }
    }

    public static void printUsage(){
        System.out.println("Stats usage: stats [option]");
        System.out.println("Options:\n");
        System.out.println("timeseries:     Analyzes the time series located in chronixBenchmark/timeseries_records.");
        System.out.println("                (generated from csv import)");
        System.out.println("benchmark:      Analyzes the import and benchmark records.");
    }
}
