package de.qaware.chronix.database;

import java.time.Instant;
import java.util.Map;
import de.qaware.chronix.database.TimeSeriesPoint;
import de.qaware.chronix.database.TimeSeries;
import de.qaware.chronix.database.TimeSeriesMetaData;

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
     * Generates the complete query string for a specified function.
     *
     * @apiNote This method WILL be part of the benchmark measurement.
     *
     * @implNote  Example pseudo code:
     * query = [databaseName] SELECT [metricName] FROM [measurementName] WHERE
     * time >= [start] AND time <= [end] AND [tagKey_1] = [tagValue_1] AND ...
     *
     *
     * @param percentile the percentile to be calculated. (Example: percentile == 5.0 -> 5th percentile)
     *                   (ignore if not function.PERCENTILE)
     * @param function the query function which to perform.
     *
     * @return the complete function specific query string.
     */
    String getQueryForFunction(TimeSeriesMetaData timeSeriesMetaData,
                               Float percentile,
                               QueryFunction function
    );

    /**
     * Performs the given query.
     *
     * @apiNote This method WILL be part of the benchmark measurement.
     *
     * @implNote  Example pseudo code:
     * http://[ipAddress]:[portNumber]/[query]
     *
     * @param query the query string to perform.
     * @return the result string from the database.
     */
    String performQuery(String query);

}
