package de.qaware.chronix.database;

import java.time.Instant;
import java.util.Map;

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
     * Creates a new database with given name on the database server.
     *
     * @apiNote This method will NOT be part of the benchmark measurement.
     *
     * @param ipAddress the database server ip address.
     * @param portNumber the port number on which the database system is available.
     * @param databaseName the database name to be created.
     * @return true if creating the database was successful.
     */
    boolean createDatabase(String ipAddress,
                           int portNumber,
                           String databaseName
    );


    /**
     * Generates a complete import query string to import a single timestamp value pair.
     *
     * @apiNote This method will NOT be part of the benchmark measurement.
     *
     * @param ipAddress the database server ip address
     * @param portNumber the port number on which the database system is available.
     * @param databaseName the database name where to import the data. (created by createDatabase(...))
     * @param measurementName aka table name. the measured system.
     * @param metricName aka field key, aka column name, the name of the measured metric.
     * @param timestamp the unix timestamp corresponding to the value.
     * @param value the value corresponding to the timestamp.
     * @param tagKey_tagValue the corresponding pairs of tagKeys and tagValues of the timestamp-value entry.
     * @return the complete import query string.
     */
    String importDataPoint(String ipAddress,
                            int portNumber,
                            String databaseName,
                            String measurementName,
                            String metricName,
                            Instant timestamp,
                            double value,
                            Map<String, String> tagKey_tagValue
    );

    /**
     * Generates the complete query string for a specified function.
     *
     * @apiNote This method will NOT be part of the benchmark measurement.
     *
     * @implNote  Example pseudo code:
     * http://[ipAddress]:[portNumber]/query [databaseName] SELECT [metricName] FROM [measurementName] WHERE
     * time >= [start] AND time <= [end] AND [tagKey_1] = [tagValue_1] AND ...
     *
     *
     *
     * @param ipAddress the database server ip address
     * @param porNumber the port number on which the database system is available.
     * @param databaseName the database name where to import the data. (created by createDatabase(...))
     * @param measurementName aka table name. the measured system. ("FROM")
     * @param metricName aka field key, aka column name, the name of the measured metric. ("SELECT")
     * @param start timestamp where the queried series begins. (>=) (null if series should not be constrained)
     * @param end timestamp where the queried series ends. (<=) (null if series should not be constrained)
     * @param tagKey_tagValue the corresponding pairs of tagKeys and tagValues of the timestamp-value entry.
     *                        (null if not constraint)
     * @param percentile the percentile to be calculated. (Example: percentile == 5.0 -> 5th percentile)
     *                   (ignore if not function.PERCENTILE)
     * @param function the query function which to perform.
     *
     * @return the complete function specific query string.
     */
    String getQueryForFunction(String ipAddress,
                                        int porNumber,
                                        String databaseName,
                                        String measurementName,
                                        String metricName,
                                        Instant start,
                                        Instant end,
                                        Map<String, String> tagKey_tagValue,
                                        float percentile,
                                        QueryFunction function
    );

    /**
     * Performs the given query.
     *
     * @apiNote This method WILL BE part of the benchmark measurement.
     *
     * @param query the query string to perform.
     * @return the result string from the database.
     */
    String performQuery(String query);




    /**
     * Query the given database on the given server.
     *
     * @apiNote This method will NOT be part of the benchmark measurement.
     *
     * @implNote  Example pseudo code:
     * http://[ipAddress]:[portNumber]/query [databaseName] SELECT [metricName] FROM [measurementName] WHERE
     * time >= [start] AND time <= [end] AND [tagKey_1] = [tagValue_1] AND ...
     *
     *
     *
     * @param ipAddress the database server ip address
     * @param porNumber the port number on which the database system is available.
     * @param databaseName the database name where to import the data. (created by createDatabase(...))
     * @param measurementName aka table name. the measured system. ("FROM")
     * @param metricName aka field key, aka column name, the name of the measured metric. ("SELECT")
     * @param start timestamp where the queried series begins. (>=) (null if series should not be constrained)
     * @param end timestamp where the queried series ends. (<=) (null if series should not be constrained)
     * @param tagKey_tagValue the corresponding pairs of tagKeys and tagValues of the timestamp-value entry.
     *                        (null if not constraint)
     *@param function the query function which to perform.
     *
     * @return Map of timestamp-value pairs.
     */
    Map<Instant, Double> getQueryResult(String ipAddress,
                                        int porNumber,
                                        String databaseName,
                                        String measurementName,
                                        String metricName,
                                        Instant start,
                                        Instant end,
                                        Map<String, String> tagKey_tagValue,
                                        QueryFunction function
    );






/* ###############################################################################################################
    *//**
     * Count the non-null fields of a given metric name of a time series query by given parameters.
     *
     * @apiNote This method WILL be part of the benchmark measurement.
     *
     *
     * @param ipAddress the database server ip address
     * @param porNumber the port number on which the database system is available.
     * @param databaseName the database name where to import the data. (created by createDatabase(...))
     * @param measurementName aka table name. the measured system. ("FROM")
     * @param metricName aka field key, aka column name, the name of the measured metric. ("SELECT")
     * @param start timestamp where the queried series begins. (>=) (null if series should not be constrained)
     * @param end timestamp where the queried series ends. (<=) (null if series should not be constrained)
     * @param tagKey_tagValue the corresponding pairs of tagKeys and tagValues of the timestamp-value entry.
     *                        (null if not constraint)
     *
     * @return the count of the non-null fields.
     *//*
    long count(String ipAddress,
               int porNumber,
               String databaseName,
               String measurementName,
               String metricName,
               Instant start,
               Instant end,
               Map<String, String> tagKey_tagValue
    );


    *//**
     * Compute the arithmetic mean (average) of all the values of a given metric name of a time series query by given parameters.
     *
     * @apiNote This method WILL be part of the benchmark measurement.
     *
     *
     * @param ipAddress the database server ip address
     * @param porNumber the port number on which the database system is available.
     * @param databaseName the database name where to import the data. (created by createDatabase(...))
     * @param measurementName aka table name. the measured system. ("FROM")
     * @param metricName aka field key, aka column name, the name of the measured metric. ("SELECT")
     * @param start timestamp where the queried series begins. (>=) (null if series should not be constrained)
     * @param end timestamp where the queried series ends. (<=) (null if series should not be constrained)
     * @param tagKey_tagValue the corresponding pairs of tagKeys and tagValues of the timestamp-value entry.
     *                        (null if not constraint)
     *
     * @return the arithmetic mean of the values.
     *//*
    double mean(String ipAddress,
               int porNumber,
               String databaseName,
               String measurementName,
               String metricName,
               Instant start,
               Instant end,
               Map<String, String> tagKey_tagValue
    );


    *//**
     * Compute the sum of all the values of a given metric name of a time series query by given parameters.
     *
     * @apiNote This method WILL be part of the benchmark measurement.
     *
     *
     * @param ipAddress the database server ip address
     * @param porNumber the port number on which the database system is available.
     * @param databaseName the database name where to import the data. (created by createDatabase(...))
     * @param measurementName aka table name. the measured system. ("FROM")
     * @param metricName aka field key, aka column name, the name of the measured metric. ("SELECT")
     * @param start timestamp where the queried series begins. (>=) (null if series should not be constrained)
     * @param end timestamp where the queried series ends. (<=) (null if series should not be constrained)
     * @param tagKey_tagValue the corresponding pairs of tagKeys and tagValues of the timestamp-value entry.
     *                        (null if not constraint)
     *
     * @return the sum of the values.
     *//*
    double sum(String ipAddress,
                int porNumber,
                String databaseName,
                String measurementName,
                String metricName,
                Instant start,
                Instant end,
                Map<String, String> tagKey_tagValue
    );


    *//**
     * Retruns the highest value of all the values of a given metric name of a time series query by given parameters.
     *
     * @apiNote This method WILL be part of the benchmark measurement.
     *
     *
     * @param ipAddress the database server ip address
     * @param porNumber the port number on which the database system is available.
     * @param databaseName the database name where to import the data. (created by createDatabase(...))
     * @param measurementName aka table name. the measured system. ("FROM")
     * @param metricName aka field key, aka column name, the name of the measured metric. ("SELECT")
     * @param start timestamp where the queried series begins. (>=) (null if series should not be constrained)
     * @param end timestamp where the queried series ends. (<=) (null if series should not be constrained)
     * @param tagKey_tagValue the corresponding pairs of tagKeys and tagValues of the timestamp-value entry.
     *                        (null if not constraint)
     *
     * @return the maximum of the values.
     *//*
    Map<Instant, Double> max(String ipAddress,
               int porNumber,
               String databaseName,
               String measurementName,
               String metricName,
               Instant start,
               Instant end,
               Map<String, String> tagKey_tagValue
    );

    *//**
     * Retruns the lowest value of all the values of a given metric name of a time series query by given parameters.
     *
     * @apiNote This method WILL be part of the benchmark measurement.
     *
     *
     * @param ipAddress the database server ip address
     * @param porNumber the port number on which the database system is available.
     * @param databaseName the database name where to import the data. (created by createDatabase(...))
     * @param measurementName aka table name. the measured system. ("FROM")
     * @param metricName aka field key, aka column name, the name of the measured metric. ("SELECT")
     * @param start timestamp where the queried series begins. (>=) (null if series should not be constrained)
     * @param end timestamp where the queried series ends. (<=) (null if series should not be constrained)
     * @param tagKey_tagValue the corresponding pairs of tagKeys and tagValues of the timestamp-value entry.
     *                        (null if not constraint)
     *
     * @return the minimum of the values.
     *//*
    Map<Instant, Double> min(String ipAddress,
               int porNumber,
               String databaseName,
               String measurementName,
               String metricName,
               Instant start,
               Instant end,
               Map<String, String> tagKey_tagValue
    );

    *//**
     * Retruns the standard deviation of all the values of a given metric name of a time series query by given parameters.
     *
     * @apiNote This method WILL be part of the benchmark measurement.
     *
     *
     * @param ipAddress the database server ip address
     * @param porNumber the port number on which the database system is available.
     * @param databaseName the database name where to import the data. (created by createDatabase(...))
     * @param measurementName aka table name. the measured system. ("FROM")
     * @param metricName aka field key, aka column name, the name of the measured metric. ("SELECT")
     * @param start timestamp where the queried series begins. (>=) (null if series should not be constrained)
     * @param end timestamp where the queried series ends. (<=) (null if series should not be constrained)
     * @param tagKey_tagValue the corresponding pairs of tagKeys and tagValues of the timestamp-value entry.
     *                        (null if not constraint)
     *
     * @return the standard deviation of the values.
     *//*
    double stddev(String ipAddress,
               int porNumber,
               String databaseName,
               String measurementName,
               String metricName,
               Instant start,
               Instant end,
               Map<String, String> tagKey_tagValue
    );


    *//**
     * Calculates the percentile of all the values of a given metric name of a time series query by given parameters.
     *
     * @apiNote This method WILL be part of the benchmark measurement.
     *
     *
     * @param ipAddress the database server ip address
     * @param porNumber the port number on which the database system is available.
     * @param databaseName the database name where to import the data. (created by createDatabase(...))
     * @param measurementName aka table name. the measured system. ("FROM")
     * @param metricName aka field key, aka column name, the name of the measured metric. ("SELECT")
     * @param start timestamp where the queried series begins. (>=) (null if series should not be constrained)
     * @param end timestamp where the queried series ends. (<=) (null if series should not be constrained)
     * @param tagKey_tagValue the corresponding pairs of tagKeys and tagValues of the timestamp-value entry.
     *                        (null if not constraint)
     * @param percentile the percentile to be calculated. (Example: percentile == 5.0 -> 5th percentile)
     *
     * @return the calculated percentile of the values.
     *//*
    Map<Instant, Double> percentile(String ipAddress,
               int porNumber,
               String databaseName,
               String measurementName,
               String metricName,
               Instant start,
               Instant end,
               Map<String, String> tagKey_tagValue,
               float percentile
    );*/

}
