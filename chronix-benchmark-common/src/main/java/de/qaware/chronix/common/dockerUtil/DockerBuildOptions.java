package de.qaware.chronix.common.dockerUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.xml.bind.annotation.XmlRootElement;
/**
 * Created by mcqueen666 on 03.08.16.
 */
@XmlRootElement
public class DockerBuildOptions {
    private String command;
    private String containerName;
    private String options;

    public DockerBuildOptions(){
    }

    public DockerBuildOptions(String containerName, String options){
        this.command = "docker build";
        this.containerName = containerName;
        this.options = options;
    }

    //getter
    public String getCommand() {
        return command;
    }

    public String getContainerName() {
        return containerName;
    }

    public String getOptions() {
        return options;
    }


    //setter
    public void setCommand(String command) {
        this.command = command;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    @JsonIgnore
    public String getValidBuildCommand(){
        if(!containerName.isEmpty()){
            if(isHarmlessString(command) && isHarmlessString(containerName) && isHarmlessString(options)){
                return command + " " + options + " " + containerName + " . ";
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
        if(o instanceof DockerBuildOptions
                && ((DockerBuildOptions) o).getContainerName().equals(containerName)){
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
