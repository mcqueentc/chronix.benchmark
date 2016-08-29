package de.qaware.chronix.shared.QueryUtil;

import de.qaware.chronix.database.BenchmarkQuery;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by mcqueen666 on 16.08.16.
 */
@XmlRootElement
public abstract class BenchmarkRecord {

    private String queryID;
    private String ipAddress;
    private String portNumber;
    private String tsdbName;
    private String diskUsage;
    private Long queryTimeMilliseconds;
    private String memoryUsage;
    private String cpuUsage;
    private String writtenBytes;
    private String readBytes;
    private String cpuUsageTotal;
    private String diskUsageTotal;
    private String memoryUsageTotal;


    public BenchmarkRecord(String queryID, String ipAddress, String portNumber, String tsdbName) {
        this.queryID = queryID;
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        this.tsdbName = tsdbName;
        this.diskUsage = null;
        this.queryTimeMilliseconds = null;
        this.memoryUsage = null;
        this.cpuUsage = null;
        this.writtenBytes = null;
        this.readBytes = null;
        this.cpuUsageTotal = null;
        this.diskUsageTotal = null;
        this.memoryUsageTotal = null;
    }

    public BenchmarkRecord(BenchmarkRecord benchmarkRecord){
        this.queryID = benchmarkRecord.getQueryID();
        this.ipAddress = benchmarkRecord.getIpAddress();
        this.portNumber = benchmarkRecord.getPortNumber();
        this.tsdbName = benchmarkRecord.getTsdbName();
        this.diskUsage = benchmarkRecord.getDiskUsage();
        this.queryTimeMilliseconds = benchmarkRecord.getQueryTimeMilliseconds();
        this.memoryUsage = benchmarkRecord.getMemoryUsage();
        this.cpuUsage = benchmarkRecord.getCpuUsage();
        this.writtenBytes = benchmarkRecord.getWrittenBytes();
        this.readBytes = benchmarkRecord.getReadBytes();
        this.cpuUsageTotal = benchmarkRecord.getCpuUsageTotal();
        this.diskUsageTotal = benchmarkRecord.getDiskUsageTotal();
        this.memoryUsageTotal = benchmarkRecord.getMemoryUsageTotal();

    }

    //getter
    public String getQueryID() {
        return queryID;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getPortNumber() {
        return portNumber;
    }

    public String getTsdbName() {
        return tsdbName;
    }

    public String getDiskUsage() {
        return diskUsage;
    }

    public Long getQueryTimeMilliseconds() {
        return queryTimeMilliseconds;
    }

    public String getMemoryUsage() {
        return memoryUsage;
    }

    public String getCpuUsage() {
        return cpuUsage;
    }

    public String getWrittenBytes() {
        return writtenBytes;
    }

    public String getReadBytes() {
        return readBytes;
    }

    public String getCpuUsageTotal() {
        return cpuUsageTotal;
    }

    public String getDiskUsageTotal() {
        return diskUsageTotal;
    }

    public String getMemoryUsageTotal() {
        return memoryUsageTotal;
    }

    //setter
    public void setQueryID(String queryID) {
        this.queryID = queryID;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setPortNumber(String portNumber) {
        this.portNumber = portNumber;
    }

    public void setTsdbName(String tsdbName) {
        this.tsdbName = tsdbName;
    }

    public void setDiskUsage(String diskUsage) {
        this.diskUsage = diskUsage;
    }

    public void setQueryTimeMilliseconds(Long queryTimeMilliseconds) {
        this.queryTimeMilliseconds = queryTimeMilliseconds;
    }

    public void setMemoryUsage(String memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public void setCpuUsage(String cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public void setWrittenBytes(String writtenBytes) {
        this.writtenBytes = writtenBytes;
    }

    public void setReadBytes(String readBytes) {
        this.readBytes = readBytes;
    }

    public void setCpuUsageTotal(String cpuUsageTotal) {
        this.cpuUsageTotal = cpuUsageTotal;
    }

    public void setDiskUsageTotal(String diskUsageTotal) {
        this.diskUsageTotal = diskUsageTotal;
    }

    public void setMemoryUsageTotal(String memoryUsageTotal) {
        this.memoryUsageTotal = memoryUsageTotal;
    }
}
