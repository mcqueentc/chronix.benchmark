package de.qaware.chronix.server.benchmark.configurator;


import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import com.sun.org.apache.xpath.internal.operations.Bool;
import de.qaware.chronix.server.util.DockerCommandLineUtil;
import de.qaware.chronix.server.util.ServerSystemUtil;
import org.apache.commons.compress.utils.IOUtils;
//import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
//import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.List;

/**
 * Created by mcqueen666 on 15.06.16.
 */
@Path("/configurator")
@Produces(MediaType.APPLICATION_JSON)
public class BenchmarkConfiguratorResource {

    // JUST FOR TESTING
    @GET
    @Path("test")
    @Timed
    public String test() {
        return "Hello from Configurator Resource!";
    }


    @POST
    @Timed
    @Path("docker/upload/{name}")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    public Response uploadDockerFiles(@PathParam("name") String name,
                                      @FormDataParam("file")InputStream fileInputStream,
                                      @FormDataParam("file")FormDataContentDisposition fileMetaData) {

        String path = ServerSystemUtil.getBenchmarkDockerDirectory();
        if(path == null){
            return Response.serverError().entity("Server OS Unknown").build();
        }

        // construct directory path from name
        String[] paths = name.split("-");
        String reconstructedFilePath = "";
        for(String p : paths){
            reconstructedFilePath = reconstructedFilePath + p + File.separator;
        }


        String dirPath = path + reconstructedFilePath;
        new File(dirPath).mkdirs();
            String filename = fileMetaData.getFileName();
            String filePath = dirPath + filename;

            try {
                File newFile = new File(filePath);
                FileOutputStream outputStream = new FileOutputStream(newFile);

                IOUtils.copy(fileInputStream, outputStream);

                outputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
                return Response.serverError().entity("Server could not write file <" + reconstructedFilePath + filename + ">" ).build();
            }


       return Response.ok("Upload file <" + reconstructedFilePath + filename + "> successfull!").build();

    }

    /**
     * Starts the given docker container with given command.
     *
     * @param containerName the docker container name (e.g. folder name if your docker files upload)
     * @param commandFileName the commandFile in the uploaded docker container direcotory to start the docker container
     * @return the response from the server and the cli output (e.g. statusCode + String[])
     */
    @GET
    @Path("docker/start")
    @Timed
    public Response startDockerContainer(@QueryParam("containerName") String containerName,
                                         @QueryParam("commandFileName") String commandFileName){
        if(DockerCommandLineUtil.isDockerInstalled()){
            File directory = new File(ServerSystemUtil.getBenchmarkDockerDirectory() + containerName);
            if(directory.exists()){
                File commandFile = new File(directory.getPath() + File.separator + commandFileName);
                if (commandFile.exists()){
                    String command = "";
                    try {
                        FileReader fileReader = new FileReader(commandFile);
                        BufferedReader bufferedReader = new BufferedReader(fileReader);
                        command = bufferedReader.readLine();

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //TODO further check command for safety reasons
                    if(command.contains("docker run")
                            && !command.contains("|")
                            && !command.contains(";")){
                        String[] prepareCommand = {DockerCommandLineUtil.getDockerInstallPath() + command};
                        String[] specificCommand = ServerSystemUtil.getOsSpecificCommand(prepareCommand);
                        List<String> startResult = ServerSystemUtil.executeCommand(specificCommand);
                        if(DockerCommandLineUtil.isDockerContainerRunning(containerName)) {
                            // all went good
                            return Response.ok().entity(startResult.toArray()).build();
                        }
                        return Response.serverError().entity(startResult.toArray()).build();
                    }
                    String[] response = {"Wrong docker command."};
                    return Response.serverError().entity(response).build();
                }
                String[] response = {"docker command file missing"};
                return Response.serverError().entity(response).build();

            }
            String[] response = {"docker files missing",
                                "directory = " + ServerSystemUtil.getBenchmarkDockerDirectory() + containerName};
            return Response.serverError().entity(response).build();
        }
        String[] response = {"docker not installed or daemon not running"};
        return Response.serverError().entity(response).build();
    }

    @GET
    @Path("docker/build")
    @Timed
    public Response buildDockerContainer(@QueryParam("containerName") String containerName,
                                         @QueryParam("commandFileName") String commandFileName){
        if(DockerCommandLineUtil.isDockerInstalled()){
            File directory = new File(ServerSystemUtil.getBenchmarkDockerDirectory() + containerName);
            if(directory.exists()){
                File commandFile = new File(directory.getPath() + File.separator + commandFileName);
                if (commandFile.exists()){
                    String command = "";
                    try {
                        FileReader fileReader = new FileReader(commandFile);
                        BufferedReader bufferedReader = new BufferedReader(fileReader);
                        command = bufferedReader.readLine();

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //TODO further check command for safety reasons
                    if(command.contains("docker build")
                            && !command.contains("|")
                            && !command.contains(";")){
                        String[] prepareCommand = {"cd " + directory.getPath(),
                                                    DockerCommandLineUtil.getDockerInstallPath() + command};
                        String[] specificCommand = ServerSystemUtil.getOsSpecificCommand(prepareCommand);
                        List<String> startResult = ServerSystemUtil.executeCommand(specificCommand);
                            // all went good
                            return Response.ok().entity(startResult.toArray()).build();
                        //String[] response = specificCommand;
                        //return Response.ok().entity(response).build();

                    }
                    String[] response = {"Wrong docker command."};
                    return Response.serverError().entity(response).build();
                }
                String[] response = {"docker command file missing"};
                return Response.serverError().entity(response).build();

            }
            String[] response = {"docker files missing",
                    "directory = " + ServerSystemUtil.getBenchmarkDockerDirectory() + containerName};
            return Response.serverError().entity(response).build();
        }
        String[] response = {"docker not installed or daemon not running"};
        return Response.serverError().entity(response).build();
    }



}
