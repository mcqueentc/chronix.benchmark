package de.qaware.chronix.database;

import java.util.List;

/**
 * Created by mcqueen666 on 08.08.16.
 *
 * Interface implementations have to implement this interface.
 * BenchmarkDataSource jar packed has to be rolled out to user and be used as dependency for TSDB implementations
 *
 */


public interface BenchmarkDataSource {
    public static enum QueryFunction{
        COUNT,
        MEAN,
        SUM,
        MIN,
        MAX,
        STDDEV,
        PERCENTILE
    }

    /**
     * Setup the implementation on given server (e.g. create database ...)
     *
     * @apiNote This method will NOT be part of the benchmark measurement.
     *
     * @implNote Keep track of ipAddress and portNumber!
     *
     * @param ipAddress the database server ip address.
     * @param portNumber the port number on which the database system is available. (docker container)
     * @return true if setup was successful.
     */
    boolean setup(String ipAddress,
                  int portNumber
    );

    /**
     * Cleanses the database on given server. Resets the database to the setup() condition.
     *
     * @apiNote This method will NOT be part of the benchmark measurement.
     *
     * @return true if clean-up was successful.
     */
    boolean clean();


    /**
     *
     * @return the absolute storage directory path to a TSDB implementation IN the docker container.
     */
    String getStorageDirectoryPath();


    /**
     * Generates a complete import query string to import a time series.
     *
     * @apiNote This method WILL be part of the benchmark measurement.
     *
     * @param timeSeries the time series to import.
     * @return the complete import query string.
     */
    String importDataPoints(TimeSeries timeSeries);


    /**
     * Performs the given query.
     *
     * @apiNote This method WILL be part of the benchmark measurement.
     *
     * @param benchmarkQuery the query representation which to perform.
     * @return the result strings from the database.
     */
    List<String> performQuery(BenchmarkQuery benchmarkQuery);

}
