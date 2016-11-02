package de.qaware.chronix.shared.dockerUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * Created by mcqueen666 on 03.08.16.
 */
@XmlRootElement
public class DockerRunOptions {

    private String command;
    private String restartCommand;
    private String containerName;
    private int hostPort;
    private int containerPort;
    private String additionalOptions;


    public DockerRunOptions(){
    }

    public DockerRunOptions(String containerName, int hostPort, int containerPort, String additionalOptions){
        this.command = "docker run -d";
        this.restartCommand = "docker restart";
        this.hostPort = hostPort;
        this.containerPort = containerPort;
        this.containerName = containerName;
        this.additionalOptions = additionalOptions;
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

    public String getRestartCommand() {
        return restartCommand;
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

    public void setRestartCommand(String restartCommand) {
        this.restartCommand = restartCommand;
    }

    @JsonIgnore
    public String getValidRunCommand(boolean shouldRestart){
        if(hostPort > 0 && containerPort > 0 && !containerName.isEmpty()){
            if(shouldRestart){
                if (isHarmlessString(restartCommand) && isHarmlessString(containerName)) {
                    return restartCommand + "  " + containerName;
                }
            } else {
                if (isHarmlessString(command) && isHarmlessString(containerName) && isHarmlessString(additionalOptions)) {
                    if (additionalOptions.isEmpty()) {
                        return command + " -p " + hostPort + ":" + containerPort + " " + containerName;
                    } else {
                        return command + " -p " + hostPort + ":" + containerPort + " " + additionalOptions + " " + containerName;
                    }
                }
            }
        }

        return null;
    }

    @JsonIgnore
    private boolean isHarmlessString(String string){
        if(string.contains("|") || string.contains(";")) {
            return false;
        } else {
            return true;
        }
    }

    @JsonIgnore
    @Override
    public boolean equals(Object o){
        if(o instanceof DockerRunOptions
                && ((DockerRunOptions) o).getContainerName().equals(containerName)){
            return true;
        } else {
            return false;
        }

    }

    @JsonIgnore
    @Override
    public int hashCode(){
        return containerName.hashCode();
    }
}
