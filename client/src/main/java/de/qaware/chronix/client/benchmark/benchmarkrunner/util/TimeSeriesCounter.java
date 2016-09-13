package de.qaware.chronix.client.benchmark.benchmarkrunner.util;

/**
 * Created by mcqueen666 on 13.09.16.
 */
public class TimeSeriesCounter {

    private static TimeSeriesCounter instance;



    private TimeSeriesCounter(){

    }

    public TimeSeriesCounter getInstance(){
        if(instance != null){
            instance = new TimeSeriesCounter();
        }
        return instance;
    }


}
