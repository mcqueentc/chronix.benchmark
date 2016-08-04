package de.qaware.chronix.client.benchmark.configurator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.qaware.chronix.client.benchmark.configurator.util.Uploader;
import dockerUtil.DockerBuildOptions;
import dockerUtil.DockerRunOptions;
import dockerUtil.ServerConfigRecord;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mcqueen666 on 20.06.16.
 */
public class Configurator {

    private final String configDirectory = System.getProperty("user.home") + File.separator + ".chronixBenchmark_conf" + File.separator;
    private final String serverConfigFileName = "serverConfig.json";

    private static Configurator instance;
    private int applicationPort = 9003;
    private int adminPort = 9004;
    private LinkedList<ServerConfigRecord> serverConfigRecords;



    private Configurator(){
        new File(configDirectory).mkdir();
        if((new File(configDirectory + serverConfigFileName).exists())) {
            readRecordFile();
        }
        if(serverConfigRecords == null){
            serverConfigRecords =  new LinkedList<ServerConfigRecord>();
            writeRecordFile();
        }

    }

    public static synchronized Configurator getInstance(){
        if(instance == null){
            instance = new Configurator();
        }

        return instance;
    }

    public String getConfigDirectory(){
        return configDirectory;
    }

    public String getServerConfigFileName(){
        return serverConfigFileName;
    }

    public LinkedList<ServerConfigRecord> getServerConfigRecords(){
        readRecordFile();
        return serverConfigRecords;
    }

    public synchronized void setServerConfigRecords(LinkedList<ServerConfigRecord> serverConfigRecords){
        this.serverConfigRecords = serverConfigRecords;
        writeRecordFile();
    }

    /**
     * Reads the server record file from json file in filesystem
     */
    private void readRecordFile(){
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(configDirectory + serverConfigFileName);
        try {
            serverConfigRecords = mapper.readValue(file, new TypeReference<LinkedList<ServerConfigRecord>>() {});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes the server record file as json to filesystem
     */
    private void writeRecordFile(){
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(configDirectory + serverConfigFileName);
        try {
            mapper.writeValue(file, serverConfigRecords);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //TODO upload server record file to server

    /**
     * Checks if dropwizard server is responding
     *
     * @param address the server address or ip WITHOUT http://
     * @return true if server response is http status 200
     */
    public boolean isServerUp(String address){
        final Client client = ClientBuilder.newBuilder().build();
        final WebTarget target = client.target("http://" + address + ":" + adminPort + "/ping");
        final Response response = target.request().get();
        int status = response.getStatus();
        client.close();

        if(status == 200) {
            return true;
        }

        return false;
    }

    /**
     * Checks if docker container is running on server
     *
     * @param serverAddress the server address or ip WITHOUT http://
     * @param containerName the name of the container to be checked(the name the container was build with)
     * @return true if docker container is running on server
     */
    public boolean isDockerContainerRunning(String serverAddress, String containerName){
        final Client client = ClientBuilder.newBuilder().build();
        final WebTarget target = client.target("http://"
                + serverAddress
                +
                ":"
                + applicationPort +
                "/configurator/docker/running?containerName="
                +containerName);
        final Response response = target.request().get();
        String[] answers = response.readEntity(String[].class);
        client.close();

        for(String answer : answers){
            if(answer.equals("true")){
                return true;
            }
        }

        return false;
    }



    /**
     * Builds the docker container on the server.
     *
     * @param serverAddress the server address or ip WITHOUT http://
     * @param buildOptions the build options
     * @return the docker build command output
     */
    public String[] buildDockerContainer(String serverAddress, DockerBuildOptions buildOptions){
        final Client client = ClientBuilder.newBuilder().build();
        final WebTarget target = client.target("http://"
                + serverAddress
                + ":"
                + applicationPort
                + "/configurator/docker/build");
        final Response response = target.request().post(Entity.json(buildOptions));
        String[] answers = response.readEntity(String[].class);
        client.close();

        return answers;

    }

    /**
     * Starts the docker container on the server.
     *
     * @param serverAddress the server address or ip WITHOUT http://
     * @param runOptions the run options
     * @return the docker start command output
     */
    public String[] startDockerContainer(String serverAddress, DockerRunOptions runOptions){
        final Client client = ClientBuilder.newBuilder().build();
        final WebTarget target = client.target("http://"
                + serverAddress
                + ":"
                + applicationPort
                + "/configurator/docker/start");
        final Response response = target.request().post(Entity.json(runOptions));
        String[] answers = response.readEntity(String[].class);
        client.close();

        return answers;
    }

    /**
     * Stops the docker container on the server.
     *
     * @param serverAddress the server address or ip WITHOUT http://
     * @param containerName the name of the container to be stopped(the name the container was build with)
     * @return the docker stop command output
     */
    public String[] stopDockerContainer(String serverAddress, String containerName){
        final Client client = ClientBuilder.newBuilder().build();
        final WebTarget target = client.target("http://"
                + serverAddress +
                ":"
                + applicationPort
                + "/configurator/docker/stop?containerName=" + containerName);
        final Response response = target.request().get();
        String[] answers = response.readEntity(String[].class);
        client.close();

        return answers;
    }

    /**
     * Uploads files to the server
     *
     * @param serverAddress the server address or ip WITHOUT http://
     * @param directoryPath the directory to be uploaded recursively to the server.
     *                      Will be placed into servers user.home + benchmarkDirectory
     * @return the server response output
     */
    public String[] uploadFiles(String serverAddress, String directoryPath){
        Uploader uploader = Uploader.getInstance();
        List<Response> responses = uploader.uploadDockerFiles("",
                directoryPath,
                "http://"+serverAddress,
                Integer.toString(applicationPort));

        LinkedList<String> answers = new LinkedList<String>();
        if(!responses.isEmpty()){
            for(Response response : responses){
                 answers.add(response.readEntity(String.class));
            }
        }

        return answers.toArray(new String[answers.size()]);
    }


    /**
     * Removes the docker image and all corresponding containers from the server.
     *
     * @param serverAddress the server address or ip WITHOUT http://
     * @param imageName the docker image name to be removed.
     * @param alsoRemoveBuildFiles true if the related build files should also be removed.
     * @return the server response output
     */
    public String[] removeDockerContainer(String serverAddress, String imageName, boolean alsoRemoveBuildFiles){
        final Client client = ClientBuilder.newBuilder().build();
        final WebTarget target = client.target("http://"
                + serverAddress +
                ":"
                + applicationPort
                + "/configurator/docker/remove?imageName="
                + imageName
                +"&removeFiles="
                +alsoRemoveBuildFiles);
        final Response response = target.request().get();
        String[] answers = response.readEntity(String[].class);
        client.close();

        return answers;
    }




}
