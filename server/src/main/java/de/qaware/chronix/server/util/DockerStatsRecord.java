package de.qaware.chronix.server.util;

/**
 * Created by mcqueen666 on 11.09.16.
 */
public class DockerStatsRecord {

    private Double cpuUsage;
    private Double memoryUsage;
    private Long readBytes;
    private Long writtenBytes;
    private Long networkDownloadedBytes;
    private Long networkUploadedBytes;

    public DockerStatsRecord(Double cpuUsage, Double memoryUsage, Long readBytes, Long writtenBytes, Long networkDownloadedBytes, Long networkUploadedBytes) {
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.readBytes = readBytes;
        this.writtenBytes = writtenBytes;
    }

    //getter
    public Double getCpuUsage() {
        return cpuUsage;
    }

    public Double getMemoryUsage() {
        return memoryUsage;
    }

    public Long getReadBytes() {
        return readBytes;
    }

    public Long getWrittenBytes() {
        return writtenBytes;
    }

    public Long getNetworkDownloadedBytes() {
        return networkDownloadedBytes;
    }

    public Long getNetworkUploadedBytes() {
        return networkUploadedBytes;
    }

    //setter
    public void setCpuUsage(Double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public void setMemoryUsage(Double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public void setReadBytes(Long readBytes) {
        this.readBytes = readBytes;
    }

    public void setWrittenBytes(Long writtenBytes) {
        this.writtenBytes = writtenBytes;
    }

    public void setNetworkDownloadedBytes(Long networkDownloadedBytes) {
        this.networkDownloadedBytes = networkDownloadedBytes;
    }

    public void setNetworkUploadedBytes(Long networkUploadedBytes) {
        this.networkUploadedBytes = networkUploadedBytes;
    }
}

