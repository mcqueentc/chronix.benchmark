package de.qaware.chronix.database;


/**
 * A representation of a timestamp value pair.
 *
 * Created by mcqueen666 on 25.08.16.
 */
public class TimeSeriesPoint implements Comparable<TimeSeriesPoint> {

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

    @Override
    public int compareTo(TimeSeriesPoint o) {
        return timeStamp.compareTo(o.getTimeStamp());
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof TimeSeriesPoint
                && ((TimeSeriesPoint) o).getTimeStamp().equals(timeStamp)
                && ((TimeSeriesPoint) o).getValue().equals(value)){
            return true;
        } else {
            return false;
        }
    }
}