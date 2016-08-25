package de.qaware.chronix.database;

import java.time.Instant;
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
    private Instant start, end;

    public TimeSeriesMetaData(){}


    /**
     * @param measurementName aka table name. the measured system.
     * @param metricName aka field key, aka column name, the name of the measured metric.
     * @param tagKey_tagValue the corresponding pairs of tagKeys and tagValues of the timestamp-value entry.
     * @param start the beginning of the time series.
     * @param end the end of the time series.
     */
    public TimeSeriesMetaData(String measurementName, String metricName, Map<String, String> tagKey_tagValue, Instant start, Instant end) {
        this.measurementName = measurementName;
        this.metricName = metricName;
        this.tagKey_tagValue = tagKey_tagValue;
        this.start = start;
        this.end = end;
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

    public Instant getStart() {
        return start;
    }

    public Instant getEnd() {
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

    public void setStart(Instant start) {
        this.start = start;
    }

    public void setEnd(Instant end) {
        this.end = end;
    }
}
