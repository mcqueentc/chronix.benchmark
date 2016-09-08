package de.qaware.chronix.database;

import java.util.List;

/**
 * Created by mcqueen666 on 08.08.16.
 *
 * Interface implementations have to implement this interface.
 * BenchmarkDataSource jar packed has to be rolled out to user and be used as dependency for TSDB implementations
 *
 */


public interface BenchmarkDataSource<T> {

    int NUMBER_OF_POINTS_PER_BATCH = 500;

    enum QueryFunction{
        COUNT,
        MEAN,
        SUM,
        MIN,
        MAX,
        STDDEV,
        PERCENTILE,
        QUERY_ONLY
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
     * Shut down established connections. (if needed)
     */
    void shutdown();


    /**
     *
     * @return the absolute storage directory path to a TSDB implementation IN the docker container.
     */
    String getStorageDirectoryPath();


    /**
     * Generates the query string that will be performed with performQuery().
     *
     * @apiNote This method will NOT be part of the benchmark measurement.
     *
     * @param benchmarkQuery the query representation.
     * @return the query object which is needed for performing the query.
     */
    T getQueryObject(BenchmarkQuery benchmarkQuery);

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
     * @param benchmarkQuery the query representation.
     * @param queryObject the query object generated from getQueryObject().
     * @return the result strings from the database.
     */
    List<String> performQuery(BenchmarkQuery benchmarkQuery, T queryObject);

}
