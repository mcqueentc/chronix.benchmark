package dockerUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * Created by mcqueen666 on 03.08.16.
 */
@XmlRootElement
public class DockerRunOptions {

    private String command;
    private String containerName;
    private int hostPort;
    private int containerPort;
    private String additionalOptions;


    public DockerRunOptions(){
        this.command = "";
        this.containerName = "";
        this.hostPort = -1;
        this.containerPort = -1;
        this.additionalOptions = "";

    }

    public DockerRunOptions(String containerName, int hostPort, int containerPort, String additionalOptions){
        this.command = "docker run -d";
        this.hostPort = hostPort;
        this.containerPort = containerPort;

        //TODO do more security checking
        if(!containerName.contains("|") || !containerName.contains(";")){
            this.containerName = containerName;
        } else {
            this.containerName = "";
        }

        if(!additionalOptions.contains("|") || !additionalOptions.contains(";")){
            this.additionalOptions = additionalOptions;
        } else {
            this.additionalOptions = "";
        }

    }

    //GET
    public String getCommand(){
        return this.command;
    }

    public String getContainerName(){
        return this.containerName;
    }

    public int getHostPort(){
        return this.hostPort;
    }

    public int getContainerPort(){
        return this.containerPort;
    }

    public String getAdditionalOptions(){
        return this.additionalOptions;
    }

    // SET
    public void setCommand(String command){
        this.command = command;
    }

    public void setContainerName(String containerName){
        this.containerName = containerName;
    }

    public void setHostPort(int hostPort){
        this.hostPort = hostPort;
    }

    public void setContainerPort(int containerPort){
        this.containerPort = containerPort;
    }

    public void setAdditionalOptions(String additionalOptions){
        this.additionalOptions = additionalOptions;
    }

    @JsonIgnore
    public String getValidRunCommand(){
        if(hostPort > 0 && containerPort > 0 && !containerName.isEmpty()){
            if(additionalOptions.isEmpty()){
                return command + " -p " + hostPort + ":" + containerPort + " " + containerName;
            } else {
                return command + " -p " + hostPort + ":" + containerPort + " " + additionalOptions + " " + containerName;
            }

        } else
            return null;
    }
}
