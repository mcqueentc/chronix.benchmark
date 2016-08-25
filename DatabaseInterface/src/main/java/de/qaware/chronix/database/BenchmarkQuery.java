package de.qaware.chronix.database;

import de.qaware.chronix.database.BenchmarkDataSource.QueryFunction;

/**
 * A representation of a time series query.
 *
 * Created by mcqueen666 on 25.08.16.
 */
public class BenchmarkQuery {

    private TimeSeriesMetaData timeSeriesMetaData;
    private Float percentile;
    private QueryFunction function;

    public BenchmarkQuery(){}


    /**
     *
     *
     * @param timeSeriesMetaData the time series meta data.
     * @param percentile the percentile to be calculated. (Example: percentile == 5.0 -> 5th percentile)
     *                   (ignore if not function.PERCENTILE)
     * @param function the query function which to perform.
     */
    public BenchmarkQuery(TimeSeriesMetaData timeSeriesMetaData, Float percentile, QueryFunction function) {
        this.timeSeriesMetaData = timeSeriesMetaData;
        this.percentile = percentile;
        this.function = function;
    }

    public TimeSeriesMetaData getTimeSeriesMetaData() {
        return timeSeriesMetaData;
    }

    public Float getPercentile() {
        return percentile;
    }

    public QueryFunction getFunction() {
        return function;
    }

    public void setTimeSeriesMetaData(TimeSeriesMetaData timeSeriesMetaData) {
        this.timeSeriesMetaData = timeSeriesMetaData;
    }

    public void setPercentile(Float percentile) {
        this.percentile = percentile;
    }

    public void setFunction(QueryFunction function) {
        this.function = function;
    }
}
