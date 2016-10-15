package de.qaware.chronix.client.benchmark.resultpresenter;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by mcqueen666 on 15.10.16.
 */
@XmlRootElement
public class QueryFunctionStatistics {

    private String queryFunction;

    private long meanQueryTime_inMilliseconds;
    private long medianQueryTime_inMilliseconds;

    private double meanTotalCpuUsagePerQuery_inPercent;
    private double medianTotalCpuUsagePerQuery_inPercent;
    private double maximumCpuUsageRecorded_inPercent;

    private long meanDiskUsagePerQuery_inBytes;
    private long medianDiskUsagePerQuery_inBytes;
    private long maximumDiskUsageRecorded_inBytes;

    private double meanMemoryUsagePerQuery_inPercent;
    private double medianMemoryUsagePerQuery_inPercent;
    private double meanTotalMemoryUsage_inPercent;
    private double medianTotalMemoryUsage_inPercent;
    private double maximumMemoryUsageRecorded_inPercent;

    private long meanDiskWrite_inBytes;
    private long medianDiskWrite_inBytes;
    private long totalDiskWrite_inBytes;
    private long meanDiskRead_inBytes;
    private long medianDiskRead_inBytes;
    private long totalDiskRead_inBytes;

    private long meanNetworkDownload_inBytes;
    private long medianNetworkDownload_inBytes;
    private long totalNetworkDownload_inBytes;
    private long meanNetworkUpload_inBytes;
    private long medianNetworkUpload_inBytes;
    private long totalNetworkUpload_inBytes;

    private long meanLatency_inMilliseconds;
    private long medianLatency_inMilliseconds;

    public QueryFunctionStatistics() {
    }

    public QueryFunctionStatistics(String queryFunction) {
        this.queryFunction = queryFunction;
    }

    public String getQueryFunction() {
        return queryFunction;
    }

    public void setQueryFunction(String queryFunction) {
        this.queryFunction = queryFunction;
    }

    public long getMeanQueryTime_inMilliseconds() {
        return meanQueryTime_inMilliseconds;
    }

    public void setMeanQueryTime_inMilliseconds(long meanQueryTime_inMilliseconds) {
        this.meanQueryTime_inMilliseconds = meanQueryTime_inMilliseconds;
    }

    public long getMedianQueryTime_inMilliseconds() {
        return medianQueryTime_inMilliseconds;
    }

    public void setMedianQueryTime_inMilliseconds(long medianQueryTime_inMilliseconds) {
        this.medianQueryTime_inMilliseconds = medianQueryTime_inMilliseconds;
    }

    public double getMeanTotalCpuUsagePerQuery_inPercent() {
        return meanTotalCpuUsagePerQuery_inPercent;
    }

    public void setMeanTotalCpuUsagePerQuery_inPercent(double meanTotalCpuUsagePerQuery_inPercent) {
        this.meanTotalCpuUsagePerQuery_inPercent = meanTotalCpuUsagePerQuery_inPercent;
    }

    public double getMedianTotalCpuUsagePerQuery_inPercent() {
        return medianTotalCpuUsagePerQuery_inPercent;
    }

    public void setMedianTotalCpuUsagePerQuery_inPercent(double medianTotalCpuUsagePerQuery_inPercent) {
        this.medianTotalCpuUsagePerQuery_inPercent = medianTotalCpuUsagePerQuery_inPercent;
    }

    public double getMaximumCpuUsageRecorded_inPercent() {
        return maximumCpuUsageRecorded_inPercent;
    }

    public void setMaximumCpuUsageRecorded_inPercent(double maximumCpuUsageRecorded_inPercent) {
        this.maximumCpuUsageRecorded_inPercent = maximumCpuUsageRecorded_inPercent;
    }

    public long getMeanDiskUsagePerQuery_inBytes() {
        return meanDiskUsagePerQuery_inBytes;
    }

    public void setMeanDiskUsagePerQuery_inBytes(long meanDiskUsagePerQuery_inBytes) {
        this.meanDiskUsagePerQuery_inBytes = meanDiskUsagePerQuery_inBytes;
    }

    public long getMedianDiskUsagePerQuery_inBytes() {
        return medianDiskUsagePerQuery_inBytes;
    }

    public void setMedianDiskUsagePerQuery_inBytes(long medianDiskUsagePerQuery_inBytes) {
        this.medianDiskUsagePerQuery_inBytes = medianDiskUsagePerQuery_inBytes;
    }

    public long getMaximumDiskUsageRecorded_inBytes() {
        return maximumDiskUsageRecorded_inBytes;
    }

    public void setMaximumDiskUsageRecorded_inBytes(long maximumDiskUsageRecorded_inBytes) {
        this.maximumDiskUsageRecorded_inBytes = maximumDiskUsageRecorded_inBytes;
    }

    public double getMeanMemoryUsagePerQuery_inPercent() {
        return meanMemoryUsagePerQuery_inPercent;
    }

    public void setMeanMemoryUsagePerQuery_inPercent(double meanMemoryUsagePerQuery_inPercent) {
        this.meanMemoryUsagePerQuery_inPercent = meanMemoryUsagePerQuery_inPercent;
    }

    public double getMedianMemoryUsagePerQuery_inPercent() {
        return medianMemoryUsagePerQuery_inPercent;
    }

    public void setMedianMemoryUsagePerQuery_inPercent(double medianMemoryUsagePerQuery_inPercent) {
        this.medianMemoryUsagePerQuery_inPercent = medianMemoryUsagePerQuery_inPercent;
    }

    public double getMeanTotalMemoryUsage_inPercent() {
        return meanTotalMemoryUsage_inPercent;
    }

    public void setMeanTotalMemoryUsage_inPercent(double meanTotalMemoryUsage_inPercent) {
        this.meanTotalMemoryUsage_inPercent = meanTotalMemoryUsage_inPercent;
    }

    public double getMedianTotalMemoryUsage_inPercent() {
        return medianTotalMemoryUsage_inPercent;
    }

    public void setMedianTotalMemoryUsage_inPercent(double medianTotalMemoryUsage_inPercent) {
        this.medianTotalMemoryUsage_inPercent = medianTotalMemoryUsage_inPercent;
    }

    public double getMaximumMemoryUsageRecorded_inPercent() {
        return maximumMemoryUsageRecorded_inPercent;
    }

    public void setMaximumMemoryUsageRecorded_inPercent(double maximumMemoryUsageRecorded_inPercent) {
        this.maximumMemoryUsageRecorded_inPercent = maximumMemoryUsageRecorded_inPercent;
    }

    public long getMeanDiskWrite_inBytes() {
        return meanDiskWrite_inBytes;
    }

    public void setMeanDiskWrite_inBytes(long meanDiskWrite_inBytes) {
        this.meanDiskWrite_inBytes = meanDiskWrite_inBytes;
    }

    public long getMedianDiskWrite_inBytes() {
        return medianDiskWrite_inBytes;
    }

    public void setMedianDiskWrite_inBytes(long medianDiskWrite_inBytes) {
        this.medianDiskWrite_inBytes = medianDiskWrite_inBytes;
    }

    public long getTotalDiskWrite_inBytes() {
        return totalDiskWrite_inBytes;
    }

    public void setTotalDiskWrite_inBytes(long totalDiskWrite_inBytes) {
        this.totalDiskWrite_inBytes = totalDiskWrite_inBytes;
    }

    public long getMeanDiskRead_inBytes() {
        return meanDiskRead_inBytes;
    }

    public void setMeanDiskRead_inBytes(long meanDiskRead_inBytes) {
        this.meanDiskRead_inBytes = meanDiskRead_inBytes;
    }

    public long getMedianDiskRead_inBytes() {
        return medianDiskRead_inBytes;
    }

    public void setMedianDiskRead_inBytes(long medianDiskRead_inBytes) {
        this.medianDiskRead_inBytes = medianDiskRead_inBytes;
    }

    public long getTotalDiskRead_inBytes() {
        return totalDiskRead_inBytes;
    }

    public void setTotalDiskRead_inBytes(long totalDiskRead_inBytes) {
        this.totalDiskRead_inBytes = totalDiskRead_inBytes;
    }

    public long getMeanNetworkDownload_inBytes() {
        return meanNetworkDownload_inBytes;
    }

    public void setMeanNetworkDownload_inBytes(long meanNetworkDownload_inBytes) {
        this.meanNetworkDownload_inBytes = meanNetworkDownload_inBytes;
    }

    public long getMedianNetworkDownload_inBytes() {
        return medianNetworkDownload_inBytes;
    }

    public void setMedianNetworkDownload_inBytes(long medianNetworkDownload_inBytes) {
        this.medianNetworkDownload_inBytes = medianNetworkDownload_inBytes;
    }

    public long getTotalNetworkDownload_inBytes() {
        return totalNetworkDownload_inBytes;
    }

    public void setTotalNetworkDownload_inBytes(long totalNetworkDownload_inBytes) {
        this.totalNetworkDownload_inBytes = totalNetworkDownload_inBytes;
    }

    public long getMeanNetworkUpload_inBytes() {
        return meanNetworkUpload_inBytes;
    }

    public void setMeanNetworkUpload_inBytes(long meanNetworkUpload_inBytes) {
        this.meanNetworkUpload_inBytes = meanNetworkUpload_inBytes;
    }

    public long getMedianNetworkUpload_inBytes() {
        return medianNetworkUpload_inBytes;
    }

    public void setMedianNetworkUpload_inBytes(long medianNetworkUpload_inBytes) {
        this.medianNetworkUpload_inBytes = medianNetworkUpload_inBytes;
    }

    public long getTotalNetworkUpload_inBytes() {
        return totalNetworkUpload_inBytes;
    }

    public void setTotalNetworkUpload_inBytes(long totalNetworkUpload_inBytes) {
        this.totalNetworkUpload_inBytes = totalNetworkUpload_inBytes;
    }

    public long getMeanLatency_inMilliseconds() {
        return meanLatency_inMilliseconds;
    }

    public void setMeanLatency_inMilliseconds(long meanLatency_inMilliseconds) {
        this.meanLatency_inMilliseconds = meanLatency_inMilliseconds;
    }

    public long getMedianLatency_inMilliseconds() {
        return medianLatency_inMilliseconds;
    }

    public void setMedianLatency_inMilliseconds(long medianLatency_inMilliseconds) {
        this.medianLatency_inMilliseconds = medianLatency_inMilliseconds;
    }
}
