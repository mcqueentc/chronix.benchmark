package de.qaware.chronix.database;


import java.util.Objects;

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
        if(timeStamp.compareTo(o.getTimeStamp()) == 0 ){
            return value.compareTo(o.getValue());

        } else {
            return timeStamp.compareTo(o.getTimeStamp());
        }
    }

    /**
     * Compare TimeSeriesPoint with a given one.
     *
     * @param o the TimeSeriesPoint to compare
     * @return -1 or 1 if timestamp is less or greater,
     * 0 if timestamp AND value is equal,
     * -2 or 2 if timestamps are equal but value is less or greater
     */
    public int ownCompareTo(TimeSeriesPoint o){
        if(timeStamp.compareTo(o.getTimeStamp()) == 0) {

            switch (value.compareTo(o.getValue())) {
                case 1:
                    return 2;
                case 0:
                    return 0;
                case -1:
                    return -2;
            }
        }
        return timeStamp.compareTo(o.getTimeStamp());

    }

    @Override
    /**
     * only compares timestamps
     */
    public boolean equals(Object o){
        if(o instanceof TimeSeriesPoint
                && ((TimeSeriesPoint) o).getTimeStamp().equals(timeStamp)
                /*&& ((TimeSeriesPoint) o).getValue().equals(value)*/){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode(){
        return Objects.hash(timeStamp, value);
    }
}