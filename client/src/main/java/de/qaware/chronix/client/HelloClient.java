package de.qaware.chronix.client;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import com.sun.org.apache.xpath.internal.operations.Mult;
import de.qaware.chronix.client.benchmark.configurator.util.Uploader;
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
        if(args.length < 3){
            System.out.println("Usage: java -jar client-<version>-all.jar [absoulutPath] [http://<serverAddress>] [portNumber]");
            return;
        }


        System.out.println(System.getProperty("user.home"));
        System.out.println(sun.awt.OSInfo.getOSType());


        // Test file upload
        Uploader uploader = Uploader.getInstance();
        //List<Response> responses = uploader.uploadDockerFiles("",System.getProperty("user.home") + "/Desktop/chronix","http://localhost","9003");
        List<Response> responses = uploader.uploadDockerFiles("",args[0],args[1],args[2]);

        if(!responses.isEmpty()){
            for(Response response : responses){
                System.out.println(response.getStatus() +" "+ response.readEntity(String.class));
            }
        } else {
            System.out.println("Nothing uploaded");
        }


    }
}
