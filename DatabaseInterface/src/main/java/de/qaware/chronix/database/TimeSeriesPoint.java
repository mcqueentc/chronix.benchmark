package de.qaware.chronix.database;

import java.time.Instant;

/**
 * A representation of a timestamp value pair.
 *
 * Created by mcqueen666 on 25.08.16.
 */
public class TimeSeriesPoint {

    private Instant timeStamp;
    private Double value;

    public TimeSeriesPoint(){}

    public TimeSeriesPoint(Instant timeStamp, Double value){
        this.timeStamp = timeStamp;
        this.value = value;
    }

    public Instant getTimeStamp() {
        return timeStamp;
    }

    public Double getValue() {
        return value;
    }

    public void setTimeStamp(Instant timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}