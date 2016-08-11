package de.qaware.chronix.database;

/**
 * Created by mcqueen666 on 08.08.16.
 *
 * Interface implementations have to implement this interface.
 * BenchmarkDataSource jar packed has to be rolled out to user and be used as dependency for TSDB implementations
 *
 */


public interface BenchmarkDataSource {

    boolean ping();

    //Hier kommt alles rein, was man braucht um die anfragen zu formulieren (technikneutral, Strings...)
   // void setQuery(String timeSeries, TimeRange startAndEnd,...)

    //Min required functions
   // double max();


}
