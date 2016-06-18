import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import com.sun.org.apache.xpath.internal.operations.Mult;
import org.apache.commons.compress.utils.IOUtils;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.media.multipart.*;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.*;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.util.List;


import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

//import com.sun.jersey.api.client.Client;
//import com.sun.jersey.api.client.ClientResponse;
//import com.sun.jersey.api.client.WebResource;

import com.google.common.base.Optional;
import sun.awt.OSInfo;

/**
 * Created by mcqueen666 on 14.06.16.
 */
public class HelloClient {



    public static void main(String[] args){
        System.out.println(System.getProperty("user.home"));
        System.out.println(sun.awt.OSInfo.getOSType());


        File file = new File("/Users/mcqueen666/Desktop/test.txt");

            final Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
            final FileDataBodyPart filepart = new FileDataBodyPart("file", file);
            FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
            final FormDataMultiPart multiPart = (FormDataMultiPart) formDataMultiPart.field("file", file, MediaType.MULTIPART_FORM_DATA_TYPE).bodyPart(filepart);
            //final FormDataMultiPart multiPart = (FormDataMultiPart) formDataMultiPart.field("file", file.getName()).bodyPart(filepart);
            final WebTarget target = client.target("http://localhost:9003/configurator/docker/upload/test");
            final Response response = target.request().post(Entity.entity(multiPart, multiPart.getMediaType()));

        System.out.println("Status: " + response.getStatus() + " " + response.readEntity(String.class));

    }
}
