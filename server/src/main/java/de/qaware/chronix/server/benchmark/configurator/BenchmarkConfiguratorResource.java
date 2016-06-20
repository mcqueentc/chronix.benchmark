package de.qaware.chronix.server.benchmark.configurator;


import com.codahale.metrics.annotation.Timed;
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

        String dirPath = path + name + File.separator;
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
                return Response.serverError().entity("Server could not write file <" + filename + ">" ).build();
            }


        return Response.ok("Upload file <" + filename + "> successfull!").build();

    }

}
