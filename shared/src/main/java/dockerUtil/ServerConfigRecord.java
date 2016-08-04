package dockerUtil;


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

    public ServerConfigRecord(){}

    public ServerConfigRecord(String serverAddress){
        this.serverAddress = serverAddress;
        this.tsdbRunRecords = new LinkedList<>();
        this.tsdbBuildRecords = new LinkedList<>();
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


}
