package de.qaware.chronix.client.benchmark;

import de.qaware.chronix.client.benchmark.resultpresenter.ResultPresenter;

import java.io.File;

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
                if(args.length > 1){
                    File dir = new File(args[1]);
                    ResultPresenter.getInstance().analyzeTimeSeries(dir);
                } else {
                    ResultPresenter.getInstance().analyzeTimeSeries();
                }
                break;
            case "benchmark":
                ResultPresenter.getInstance().doBenchmarkRecordsAnalysis();
                break;
            case "plot":
                ResultPresenter.getInstance().plotBenchmarkStatistics();
                break;
        }
    }

    public static void printUsage(){
        System.out.println("Stats usage: stats [option]");
        System.out.println("Options:\n");
        System.out.println("timeseries:     [optionalDirectory] Analyzes the time series located in chronixBenchmark/timeseries_records (generated from csv import)");
        System.out.println("                if no optionalDirectory is given. Otherwise the time series in optionalDirectory is analyzed.");
        System.out.println("benchmark:      Analyzes the import and benchmark records.");
        System.out.println("plot:           Plots bar diagrams from import and benchmark records to jpg file in chronixBenchmark/statistics/bar_plots");
    }
}
