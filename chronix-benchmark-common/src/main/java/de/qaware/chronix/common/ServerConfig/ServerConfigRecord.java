package de.qaware.chronix.common.ServerConfig;


import com.fasterxml.jackson.annotation.JsonIgnore;
import de.qaware.chronix.common.dockerUtil.DockerBuildOptions;
import de.qaware.chronix.common.dockerUtil.DockerRunOptions;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by mcqueen666 on 04.08.16.
 */
@XmlRootElement
public class ServerConfigRecord {

    private String serverAddress;
    private LinkedList<DockerRunOptions> tsdbRunRecords;
    private LinkedList<DockerBuildOptions> tsdbBuildRecords;
    private LinkedList<String> externalTimeSeriesDataBaseImplementations;
    private Map<String, String> tsdbDockerFilesDirectoryMap;

    public ServerConfigRecord(){}

    public ServerConfigRecord(String serverAddress){
        this.serverAddress = serverAddress;
        this.tsdbRunRecords = new LinkedList<>();
        this.tsdbBuildRecords = new LinkedList<>();
        this.externalTimeSeriesDataBaseImplementations = new LinkedList<>();
        this.tsdbDockerFilesDirectoryMap = new HashMap<>();
    }

    public ServerConfigRecord(ServerConfigRecord otherRecord){
        this.serverAddress = otherRecord.getServerAddress();
        this.tsdbRunRecords = new LinkedList<>(otherRecord.getTsdbRunRecords());
        this.tsdbBuildRecords = new LinkedList<>(otherRecord.getTsdbBuildRecords());
        this.externalTimeSeriesDataBaseImplementations = new LinkedList<>(otherRecord.getExternalTimeSeriesDataBaseImplementations());
        this.tsdbDockerFilesDirectoryMap = new HashMap<>(otherRecord.getTsdbDockerFilesDirectoryMap());
    }

    //setter
    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public void setTsdbRunRecords(LinkedList<DockerRunOptions> tsdbRunRecords) {
        this.tsdbRunRecords = tsdbRunRecords;
    }

    public void setTsdbBuildRecords(LinkedList<DockerBuildOptions> tsdbBuildRecords) {
        this.tsdbBuildRecords = tsdbBuildRecords;
    }

    public void setExternalTimeSeriesDataBaseImplementations(LinkedList<String> externalTimeSeriesDataBaseImplementations) {
        this.externalTimeSeriesDataBaseImplementations = externalTimeSeriesDataBaseImplementations;
    }

    public void setTsdbDockerFilesDirectoryMap(Map<String, String> tsdbDockerFilesDirectoryMap) {
        this.tsdbDockerFilesDirectoryMap = tsdbDockerFilesDirectoryMap;
    }
    //getter

    public String getServerAddress() {
        return serverAddress;
    }

    public LinkedList<DockerRunOptions> getTsdbRunRecords() {
        return tsdbRunRecords;
    }

    public LinkedList<DockerBuildOptions> getTsdbBuildRecords() {
        return tsdbBuildRecords;
    }

    public LinkedList<String> getExternalTimeSeriesDataBaseImplementations() {
        return externalTimeSeriesDataBaseImplementations;
    }

    public Map<String, String> getTsdbDockerFilesDirectoryMap() {
        return tsdbDockerFilesDirectoryMap;
    }

    @JsonIgnore
    @Override
    public boolean equals(Object o){
        if(o instanceof ServerConfigRecord
                && ((ServerConfigRecord) o).getServerAddress().equals(serverAddress)){
            return true;
        } else {
            return false;
        }

    }

    @JsonIgnore
    @Override
    public int hashCode(){
        return serverAddress.hashCode();
    }
}
