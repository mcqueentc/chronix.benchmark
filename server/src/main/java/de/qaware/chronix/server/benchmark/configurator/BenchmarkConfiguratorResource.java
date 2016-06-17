package de.qaware.chronix.server.benchmark.configurator;


import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import de.qaware.chronix.server.util.ServerSystemUtil;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.*;
import java.nio.channels.FileChannel;
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
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadDockerFiles(@PathParam("name") String name, FormDataMultiPart multiPart) {

        Optional<String> path = ServerSystemUtil.getBenchmarkDockerDirectory();
        if(!path.isPresent()){
            return Response.serverError().build();
        }

        String dirPath = path + name + File.separator;
        new File(dirPath).mkdirs();

        List<FormDataBodyPart> bodyParts = multiPart.getFields("file");

        for(FormDataBodyPart part : bodyParts) {
            String filename = part.getFormDataContentDisposition().getFileName();
            String filePath = dirPath + filename;

            try {
                File newFile = new File(filePath);
                FileOutputStream outputStream = new FileOutputStream(newFile);
                FileChannel outChannel = outputStream.getChannel();

                FileInputStream inputStream = part.getValueAs(FileInputStream.class);
                FileChannel inChannel = inputStream.getChannel();

                //InputStream is = part.getValueAs(InputStream.class);

                inChannel.transferTo(0, part.getFormDataContentDisposition().getSize(), outChannel);

                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                return Response.serverError().build();
            }

        }

        return Response.ok().build();



    }



}
