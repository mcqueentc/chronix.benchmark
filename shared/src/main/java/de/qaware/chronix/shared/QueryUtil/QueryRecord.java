package de.qaware.chronix.shared.QueryUtil;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by mcqueen666 on 16.08.16.
 */
@XmlRootElement
public class QueryRecord {

    private String queryID;
    private String ipAddress;
    private String portNumber;
    private String tsdbName;
    private List<String> queryList;
    private String diskUsage;
    private Long queryTimeMilliseconds;
    private String MemoryUsage;
    private String cpuUsage;
    private String writtenBytes;
    private String readBytes;


    public QueryRecord(){}

    public QueryRecord(String queryID, String ipAddress, String portNumber, String tsdbName, List<String> queryList) {
        this.queryID = queryID;
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        this.tsdbName = tsdbName;
        this.queryList = queryList;
        this.diskUsage = null;
        this.queryTimeMilliseconds = null;
        this.MemoryUsage = null;
        this.cpuUsage = null;
        this.writtenBytes = null;
        this.readBytes = null;
    }

    public QueryRecord(QueryRecord queryRecord){
        this.queryID = queryRecord.getQueryID();
        this.ipAddress = queryRecord.getIpAddress();
        this.portNumber = queryRecord.getPortNumber();
        this.tsdbName = queryRecord.getTsdbName();
        this.queryList = queryRecord.getQueryList();
        this.diskUsage = queryRecord.getDiskUsage();
        this.queryTimeMilliseconds = queryRecord.getQueryTimeMilliseconds();
        this.MemoryUsage = queryRecord.getMemoryUsage();
        this.cpuUsage = queryRecord.getCpuUsage();
        this.writtenBytes = queryRecord.getWrittenBytes();
        this.readBytes = queryRecord.getReadBytes();

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

    public List<String> getQueryList() {
        return queryList;
    }

    public String getDiskUsage() {
        return diskUsage;
    }

    public Long getQueryTimeMilliseconds() {
        return queryTimeMilliseconds;
    }

    public String getMemoryUsage() {
        return MemoryUsage;
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

    public void setQueryList(List<String> queryList) {
        this.queryList = queryList;
    }

    public void setDiskUsage(String diskUsage) {
        this.diskUsage = diskUsage;
    }

    public void setQueryTimeMilliseconds(Long queryTimeMilliseconds) {
        this.queryTimeMilliseconds = queryTimeMilliseconds;
    }

    public void setMemoryUsage(String memoryUsage) {
        MemoryUsage = memoryUsage;
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
}
