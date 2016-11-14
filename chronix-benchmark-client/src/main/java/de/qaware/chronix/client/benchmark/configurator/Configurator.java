package de.qaware.chronix.client.benchmark.configurator;

import de.qaware.chronix.client.benchmark.configurator.util.Uploader;
import de.qaware.chronix.common.ServerConfig.ServerConfigRecord;
import de.qaware.chronix.common.dockerUtil.DockerBuildOptions;
import de.qaware.chronix.common.dockerUtil.DockerRunOptions;
import de.qaware.chronix.common.ServerConfig.ServerConfigAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mcqueen666 on 20.06.16.
 */
public class Configurator {

    private static Configurator instance;
    private final Logger logger = LoggerFactory.getLogger(Configurator.class);
    private int applicationPort = 9003;
    private int adminPort = 9004;

    private Configurator(){
    }

    public static synchronized Configurator getInstance(){
        if(instance == null){
            instance = new Configurator();
        }

        return instance;
    }

    public int getApplicationPort() {
        return applicationPort;
    }

    public int getAdminPort() {
        return adminPort;
    }

    public boolean uploadServerConfig(String serverAddress){
        final Client client = ClientBuilder.newBuilder().build();
        final WebTarget target = client.target("http://"
                + serverAddress
                + ":"
                + applicationPort
                + "/configurator/upload/config");
        LinkedList<ServerConfigRecord> serverConfigRecords = new LinkedList<>();
        ServerConfigAccessor serverConfigAccessor = ServerConfigAccessor.getInstance();
        ServerConfigRecord serverConfigRecord = serverConfigAccessor.getServerConfigRecord(serverAddress);
        if(serverConfigRecord != null) {
            serverConfigRecords.add(serverConfigRecord);
            final Response response = target.request().post(Entity.json(serverConfigRecords));
            client.close();

            if (response.getStatus() == 200) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if dropwizard server is responding
     *
     * @param serverAddress the server serverAddress or ip WITHOUT http://
     * @return true if server response is http status 200
     */
    public boolean isServerUp(String serverAddress){
        final Client client = ClientBuilder.newBuilder().build();
        final WebTarget target = client.target("http://" + serverAddress + ":" + adminPort + "/ping");
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
        logger.info("Building {} on server {} ... (this may take a while)",buildOptions.getContainerName(),serverAddress);
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
     * Uploads docker files to the server
     *
     * @param serverAddress the server address or ip WITHOUT http://
     * @param directoryPath the directory to be uploaded recursively to the server.
     *                      Will be placed into servers user.home + benchmarkDirectory + docker
     * @return the server response output
     */
    public String[] uploadFiles(String serverAddress, String directoryPath){
        Uploader uploader = Uploader.getInstance();
        List<String> responses = uploader.uploadDockerFiles("",
                directoryPath,
                serverAddress,
                Integer.toString(applicationPort));

        return responses.toArray(new String[responses.size()]);
    }

    /**
     * Uploads the jar file of an interface implementation
     *
     * @param serverAddress the server address or ip WITHOUT http://
     * @param jarFile the jar file
     * @param tsdbName the implementation name
     * @return the server response output
     */
    public String[] uploadJarFile(String serverAddress, File jarFile, String tsdbName){
        Uploader uploader = Uploader.getInstance();
        String response = uploader.uploadJarFile(
                jarFile,
                tsdbName,
                serverAddress,
                Integer.toString(applicationPort));
        String[] answer = {response};
        return answer;
    }

    /**
     * Checks if given interface is available on the server.
     *
     * @param serverAddress the server address or ip WITHOUT http://
     * @param tsdbName the implementation name
     * @return the server response output
     */
    public String[] checkInterfaceStatus(String serverAddress, String tsdbName){
        final Client client = ClientBuilder.newBuilder().build();
        final WebTarget target = client.target("http://" + serverAddress + ":" + applicationPort + "/configurator/interface/running?tsdbName=" + tsdbName);
        final Response response = target.request().get();
        String[] answer = {response.readEntity(String.class)};
        client.close();
        return answer;
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
