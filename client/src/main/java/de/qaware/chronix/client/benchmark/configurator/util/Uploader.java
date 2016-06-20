package de.qaware.chronix.client.benchmark.configurator.util;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mcqueen666 on 20.06.16.
 */
public class Uploader {

    private static final String SERVER_UPLOAD_DOCKER_COMMAND_STRING = "/configurator/docker/upload/";
    private static Uploader instance;

    private Uploader(){

    }

    public static synchronized Uploader getInstance() {
        if (instance == null) {
            instance = new Uploader();
        }
        return instance;
    }


    /**
     * Uploads all docker files under given directory path.
     * NOTE: Subfolders will be ignored. All files necessary for the docker container must be first level under directory path.
     *
     * @param dirPath The path to the docker file to upload as String. Will be used as folder name on the server.
     * @param httpServerAddress The http or ip address as String. (e.g. "http://some.server.com)
     * @param portNumber The port number on which the server is listening as String. (e.g. "66666")
     *
     * @return List of Responses. (check e.g. response.getStatus() +" "+ response.readEntity(String.class)
     */
    public List<Response> uploadDockerFiles(String dirPath, String httpServerAddress, String portNumber) {
        List<Response> responses = new LinkedList<Response>();
        File directory = new File(dirPath);
        if(directory.exists()){
            File[] fList = directory.listFiles();

            for(File file : fList){
                // ignore subfolders at this moment
                if (file.isDirectory()) {
                    continue;
                }

                final FileDataBodyPart filepart = new FileDataBodyPart("file", file);
                FormDataMultiPart multiPart = new FormDataMultiPart();
                multiPart.field("file", file, MediaType.MULTIPART_FORM_DATA_TYPE).bodyPart(filepart);
                final Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
                // final WebTarget target = client.target("http://localhost:9003/configurator/docker/upload/test");
                final WebTarget target = client.target(
                        httpServerAddress +":"
                                + portNumber
                                + SERVER_UPLOAD_DOCKER_COMMAND_STRING
                                + directory.getName());
                final Response response = target.request().post(Entity.entity(multiPart, multiPart.getMediaType()));
                //System.out.println("Status: " + response.getStatus() + " " + response.readEntity(String.class));

                responses.add(response);

            }
        }

        return responses;
    }


}
