package de.qaware.chronix.shared.ServerConfig;


import de.qaware.chronix.shared.dockerUtil.DockerBuildOptions;
import de.qaware.chronix.shared.dockerUtil.DockerRunOptions;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedList;

/**
 * Created by mcqueen666 on 04.08.16.
 */
@XmlRootElement
public class ServerConfigRecord {

    private String serverAddress;
    private LinkedList<DockerRunOptions> tsdbRunRecords;
    private LinkedList<DockerBuildOptions> tsdbBuildRecords;
    private LinkedList<String> timeSeriesDataFolders;
    private LinkedList<String> externalTimeSeriesDataBaseImplementations;

    public ServerConfigRecord(){}

    public ServerConfigRecord(String serverAddress){
        this.serverAddress = serverAddress;
        this.tsdbRunRecords = new LinkedList<>();
        this.tsdbBuildRecords = new LinkedList<>();
        this.timeSeriesDataFolders = new LinkedList<>();
        this.externalTimeSeriesDataBaseImplementations = new LinkedList<>();
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

    public void setTimeSeriesDataFolders(LinkedList<String> timeSeriesDataFolders) {
        this.timeSeriesDataFolders = timeSeriesDataFolders;
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

    public LinkedList<String> getTimeSeriesDataFolders() {
        return timeSeriesDataFolders;
    }

    public LinkedList<String> getExternalTimeSeriesDataBaseImplementations() {
        return externalTimeSeriesDataBaseImplementations;
    }
}
