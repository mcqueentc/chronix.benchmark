package de.qaware.chronix.database;

import java.util.HashMap;
import java.util.Map;

/**
 * A representation of the meta data of a time series. (without actual data points)
 *
 * Created by mcqueen666 on 25.08.16.
 */
public class TimeSeriesMetaData {

    private String measurementName;
    private String metricName;
    private Map<String, String> tagKey_tagValue;
    private Long start, end;

    public TimeSeriesMetaData(){}


    /**
     * @param measurementName aka table name. the measured system.
     * @param metricName aka field key, aka column name, the name of the measured metric.
     * @param tagKey_tagValue the corresponding pairs of tagKeys and tagValues of the timestamp-value entry.
     * @param start the beginning of the time series.
     * @param end the end of the time series.
     */
    public TimeSeriesMetaData(String measurementName, String metricName, Map<String, String> tagKey_tagValue, Long start, Long end) {
        this.measurementName = measurementName;
        this.metricName = metricName;
        this.tagKey_tagValue = tagKey_tagValue;
        this.start = start;
        this.end = end;
    }


    public TimeSeriesMetaData(TimeSeries timeSeries){
        this.measurementName = timeSeries.getMeasurementName();
        this.metricName = timeSeries.getMetricName();
        this.tagKey_tagValue = new HashMap<>(timeSeries.getTagKey_tagValue());
        this.start = timeSeries.getStart();
        this.end = timeSeries.getEnd();
    }

    public String getMeasurementName() {
        return measurementName;
    }

    public String getMetricName() {
        return metricName;
    }

    public Map<String, String> getTagKey_tagValue() {
        return tagKey_tagValue;
    }

    public Long getStart() {
        return start;
    }

    public Long getEnd() {
        return end;
    }

    public void setMeasurementName(String measurementName) {
        this.measurementName = measurementName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public void setTagKey_tagValue(Map<String, String> tagKey_tagValue) {
        this.tagKey_tagValue = tagKey_tagValue;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public void setEnd(Long end) {
        this.end = end;
    }
}
