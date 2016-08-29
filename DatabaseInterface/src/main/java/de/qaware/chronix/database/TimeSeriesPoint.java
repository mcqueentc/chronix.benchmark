package de.qaware.chronix.database;


/**
 * A representation of a timestamp value pair.
 *
 * Created by mcqueen666 on 25.08.16.
 */
public class TimeSeriesPoint {

    private Long timeStamp;
    private Double value;

    public TimeSeriesPoint(){}

    public TimeSeriesPoint(Long timeStamp, Double value){
        this.timeStamp = timeStamp;
        this.value = value;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public Double getValue() {
        return value;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}