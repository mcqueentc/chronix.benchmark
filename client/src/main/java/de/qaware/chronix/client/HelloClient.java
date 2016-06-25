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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


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


import java.util.HashMap;
import java.util.Map;

/**
 * Created by mcqueen666 on 14.06.16.
 */
public class HelloClient {



    public static void main(String[] args){
        /*if(args.length < 3){
            System.out.println("Usage: java -jar client-<version>-all.jar [absoulutPath] [http://<serverAddress>] [portNumber]");
            return;
        }

*/
/*
        System.out.println(System.getProperty("user.home"));
        System.out.println(sun.awt.OSInfo.getOSType());
        File dir = new File(System.getProperty("user.home") + "/Desktop/chronix");
        if(dir.isDirectory()){
            System.out.println("getPath() = " + dir.getPath());
        }
*/
/*
        // Test file upload
        Uploader uploader = Uploader.getInstance();
        List<Response> responses = uploader.uploadDockerFiles("",System.getProperty("user.home") + "/Documents/BA_workspace/docker/chronix","http://localhost","9003");
        //List<Response> responses = uploader.uploadDockerFiles("",args[0],args[1],args[2]);

        if(!responses.isEmpty()){
            for(Response response : responses){
                System.out.println(response.getStatus() +" "+ response.readEntity(String.class));
            }
        } else {
            System.out.println("Nothing uploaded");
        }
*/


        //test build container
        String commandFileName = "chronix.start";
        final Client client = ClientBuilder.newBuilder().build();
        //final WebTarget target = client.target("http://192.168.2.118:9003/configurator/docker/running?containerName=chronix");
        //final WebTarget target = client.target("http://192.168.2.168:9003/configurator/docker/start?containerName=chronix&commandFileName="+commandFileName);
        //final WebTarget target = client.target("http://192.168.2.168:9003/configurator/docker/build?containerName=chronix&commandFileName="+commandFileName);
        //final WebTarget target = client.target("http://192.168.2.118:9003/configurator/ping?nTimes=4");
        //final WebTarget target = client.target("http://192.168.2.118:9003/configurator/which");
        //final WebTarget target = client.target("http://192.168.2.168:9003/configurator/docker/stop?containerName=chronix");
        final WebTarget target = client.target("http://localhost:9003/configurator/booleanTest?value=yes");
        final Response response = target.request().get();
        String[] answers = response.readEntity(String[].class);
        System.out.println("Server status: " + response.getStatus());
        //System.out.println(response.readEntity(String.class));
        for(String answer : answers){
            System.out.println(answer);
        }




/*
        // test start container
        String command = "docker run -d -p 8983:8983 chronix";
        final Client client = ClientBuilder.newBuilder().build();
        final WebTarget target = client.target("http://localhost:9003/configurator/docker/start?container=chronix&command="+command);
        final Response response = target.request().get();
        String[] answers = response.readEntity(String[].class);
        for(String answer : answers){
            System.out.println(answer);
        }
*/


/*
        // test exec
        List<String> result = new LinkedList<String>();
        String[] command = {"/bin/sh","-c","ping -c 4 google.com"};
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String curLine;
            while ((curLine = reader.readLine()) != null) {
                result.add(curLine);
            }
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
            result.add(e.getLocalizedMessage());
        }
        for(String line : result){
            System.out.println(line);
        }

*/



    }
}
