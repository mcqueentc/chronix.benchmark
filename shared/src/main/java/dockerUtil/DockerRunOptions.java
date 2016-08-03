package dockerUtil;

/**
 * Created by mcqueen666 on 03.08.16.
 */
public class DockerRunOptions {

    private final String command;
    private final String containerName;
    private final int hostPort;
    private final int containerPort;
    private final String additionalOptions;


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

    public String getValidRunCommand(){
        if(hostPort > 0 && containerPort > 0 && !containerName.isEmpty()){
            return command + " -p " + hostPort + ":" + containerPort + " " + additionalOptions + " " + containerName;
        } else
            return null;
    }
}
