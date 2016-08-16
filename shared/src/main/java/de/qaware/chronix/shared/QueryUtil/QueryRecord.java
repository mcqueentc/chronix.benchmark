package de.qaware.chronix.shared.QueryUtil;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by mcqueen666 on 16.08.16.
 */
@XmlRootElement
public class QueryRecord {

    private String queryID;
    private String ipAddress;
    private String portNumber;
    private String tsdbName;
    private String query;
    private String diskUsage;
    private Long queryTimeMilliseconds;
    private String MemoryUsage;
    private String cpuUsage;


    public QueryRecord(){}

    public QueryRecord(String queryID, String ipAddress, String portNumber, String tsdbName, String query) {
        this.queryID = queryID;
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        this.tsdbName = tsdbName;
        this.query = query;
        this.diskUsage = null;
        this.queryTimeMilliseconds = null;
        this.MemoryUsage = null;
        this.cpuUsage = null;
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

    public String getQuery() {
        return query;
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

    public void setQuery(String query) {
        this.query = query;
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
}
