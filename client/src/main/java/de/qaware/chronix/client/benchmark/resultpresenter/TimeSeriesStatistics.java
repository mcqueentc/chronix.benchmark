package de.qaware.chronix.client.benchmark.resultpresenter;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by mcqueen666 on 10.10.16.
 */
@XmlRootElement
public class TimeSeriesStatistics {

    private String date;
    private List<String> measurements;
    private long totalSizeInBytes;
    private int numberOfTimeSeries;
    private long numberOfTotalPoints;
    private int minNumberOfPointsPerTimeSeries;
    private int maxNumberOfPointsPerTimeSeries;
    private int averagePointsPerTimeSeries;
    private int medianPointsPerTimeSeries;


    public TimeSeriesStatistics(){

    }

    public long getTotalSizeInBytes() {
        return totalSizeInBytes;
    }

    public int getNumberOfTimeSeries() {
        return numberOfTimeSeries;
    }

    public long getNumberOfTotalPoints() {
        return numberOfTotalPoints;
    }

    public int getMinNumberOfPointsPerTimeSeries() {
        return minNumberOfPointsPerTimeSeries;
    }

    public int getMaxNumberOfPointsPerTimeSeries() {
        return maxNumberOfPointsPerTimeSeries;
    }

    public int getAveragePointsPerTimeSeries() {
        return averagePointsPerTimeSeries;
    }

    public int getMedianPointsPerTimeSeries() {
        return medianPointsPerTimeSeries;
    }

    public void setTotalSizeInBytes(long totalSizeInBytes) {
        this.totalSizeInBytes = totalSizeInBytes;
    }

    public void setNumberOfTimeSeries(int numberOfTimeSeries) {
        this.numberOfTimeSeries = numberOfTimeSeries;
    }

    public void setNumberOfTotalPoints(long numberOfTotalPoints) {
        this.numberOfTotalPoints = numberOfTotalPoints;
    }

    public void setMinNumberOfPointsPerTimeSeries(int minNumberOfPointsPerTimeSeries) {
        this.minNumberOfPointsPerTimeSeries = minNumberOfPointsPerTimeSeries;
    }

    public void setMaxNumberOfPointsPerTimeSeries(int maxNumberOfPointsPerTimeSeries) {
        this.maxNumberOfPointsPerTimeSeries = maxNumberOfPointsPerTimeSeries;
    }

    public void setAveragePointsPerTimeSeries(int averagePointsPerTimeSeries) {
        this.averagePointsPerTimeSeries = averagePointsPerTimeSeries;
    }

    public void setMedianPointsPerTimeSeries(int medianPointsPerTimeSeries) {
        this.medianPointsPerTimeSeries = medianPointsPerTimeSeries;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<String> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<String> measurements) {
        this.measurements = measurements;
    }
}
