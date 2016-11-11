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
    // data set stats
    private long totalSizeInBytes;
    private long totalSizeUnzippedInBytes;
    private int numberOfTimeSeries;
    private long numberOfTotalPoints;
    private long minNumberOfPointsPerTimeSeries;
    private long maxNumberOfPointsPerTimeSeries;
    private double averagePointsPerTimeSeries;
    private double medianPointsPerTimeSeries;

    // data value stats
    private double minValueOfAllTimeSeries;
    private double maxValueOfAllTimeSeries;
    private double meanValueOfAllTimeSeries;
    private double medianValueOfAllTimeSeries;
    private double meanSampleStandardDeviationOfAllTimeSeries;
    private double medianSampleStandardDeviationOfAllTimeSeries;
    private double meanValueChangeRateOfAllTimeSeries;
    private double meanMeasurementDurationOfAllTimeSeries_inSeconds;
    private double meanSamplingIntervalOfAllTimeSeries_inMilliseconds;


    public TimeSeriesStatistics(){

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

    public long getTotalSizeInBytes() {
        return totalSizeInBytes;
    }

    public void setTotalSizeInBytes(long totalSizeInBytes) {
        this.totalSizeInBytes = totalSizeInBytes;
    }

    public long getTotalSizeUnzippedInBytes() {
        return totalSizeUnzippedInBytes;
    }

    public void setTotalSizeUnzippedInBytes(long totalSizeUnzippedInBytes) {
        this.totalSizeUnzippedInBytes = totalSizeUnzippedInBytes;
    }

    public int getNumberOfTimeSeries() {
        return numberOfTimeSeries;
    }

    public void setNumberOfTimeSeries(int numberOfTimeSeries) {
        this.numberOfTimeSeries = numberOfTimeSeries;
    }

    public long getNumberOfTotalPoints() {
        return numberOfTotalPoints;
    }

    public void setNumberOfTotalPoints(long numberOfTotalPoints) {
        this.numberOfTotalPoints = numberOfTotalPoints;
    }

    public long getMinNumberOfPointsPerTimeSeries() {
        return minNumberOfPointsPerTimeSeries;
    }

    public void setMinNumberOfPointsPerTimeSeries(long minNumberOfPointsPerTimeSeries) {
        this.minNumberOfPointsPerTimeSeries = minNumberOfPointsPerTimeSeries;
    }

    public long getMaxNumberOfPointsPerTimeSeries() {
        return maxNumberOfPointsPerTimeSeries;
    }

    public void setMaxNumberOfPointsPerTimeSeries(long maxNumberOfPointsPerTimeSeries) {
        this.maxNumberOfPointsPerTimeSeries = maxNumberOfPointsPerTimeSeries;
    }

    public double getAveragePointsPerTimeSeries() {
        return averagePointsPerTimeSeries;
    }

    public void setAveragePointsPerTimeSeries(double averagePointsPerTimeSeries) {
        this.averagePointsPerTimeSeries = averagePointsPerTimeSeries;
    }

    public double getMedianPointsPerTimeSeries() {
        return medianPointsPerTimeSeries;
    }

    public void setMedianPointsPerTimeSeries(double medianPointsPerTimeSeries) {
        this.medianPointsPerTimeSeries = medianPointsPerTimeSeries;
    }

    public double getMinValueOfAllTimeSeries() {
        return minValueOfAllTimeSeries;
    }

    public void setMinValueOfAllTimeSeries(double minValueOfAllTimeSeries) {
        this.minValueOfAllTimeSeries = minValueOfAllTimeSeries;
    }

    public double getMaxValueOfAllTimeSeries() {
        return maxValueOfAllTimeSeries;
    }

    public void setMaxValueOfAllTimeSeries(double maxValueOfAllTimeSeries) {
        this.maxValueOfAllTimeSeries = maxValueOfAllTimeSeries;
    }

    public double getMeanValueOfAllTimeSeries() {
        return meanValueOfAllTimeSeries;
    }

    public void setMeanValueOfAllTimeSeries(double meanValueOfAllTimeSeries) {
        this.meanValueOfAllTimeSeries = meanValueOfAllTimeSeries;
    }

    public double getMedianValueOfAllTimeSeries() {
        return medianValueOfAllTimeSeries;
    }

    public void setMedianValueOfAllTimeSeries(double medianValueOfAllTimeSeries) {
        this.medianValueOfAllTimeSeries = medianValueOfAllTimeSeries;
    }

    public double getMeanSampleStandardDeviationOfAllTimeSeries() {
        return meanSampleStandardDeviationOfAllTimeSeries;
    }

    public void setMeanSampleStandardDeviationOfAllTimeSeries(double meanSampleStandardDeviationOfAllTimeSeries) {
        this.meanSampleStandardDeviationOfAllTimeSeries = meanSampleStandardDeviationOfAllTimeSeries;
    }

    public double getMedianSampleStandardDeviationOfAllTimeSeries() {
        return medianSampleStandardDeviationOfAllTimeSeries;
    }

    public void setMedianSampleStandardDeviationOfAllTimeSeries(double medianSampleStandardDeviationOfAllTimeSeries) {
        this.medianSampleStandardDeviationOfAllTimeSeries = medianSampleStandardDeviationOfAllTimeSeries;
    }

    public double getMeanValueChangeRateOfAllTimeSeries() {
        return meanValueChangeRateOfAllTimeSeries;
    }

    public void setMeanValueChangeRateOfAllTimeSeries(double meanValueChangeRateOfAllTimeSeries) {
        this.meanValueChangeRateOfAllTimeSeries = meanValueChangeRateOfAllTimeSeries;
    }

    public double getMeanMeasurementDurationOfAllTimeSeries_inSeconds() {
        return meanMeasurementDurationOfAllTimeSeries_inSeconds;
    }

    public void setMeanMeasurementDurationOfAllTimeSeries_inSeconds(double meanMeasurementDurationOfAllTimeSeries_inSeconds) {
        this.meanMeasurementDurationOfAllTimeSeries_inSeconds = meanMeasurementDurationOfAllTimeSeries_inSeconds;
    }

    public double getMeanSamplingIntervalOfAllTimeSeries_inMilliseconds() {
        return meanSamplingIntervalOfAllTimeSeries_inMilliseconds;
    }

    public void setMeanSamplingIntervalOfAllTimeSeries_inMilliseconds(double meanSamplingIntervalOfAllTimeSeries_inMilliseconds) {
        this.meanSamplingIntervalOfAllTimeSeries_inMilliseconds = meanSamplingIntervalOfAllTimeSeries_inMilliseconds;
    }
}
