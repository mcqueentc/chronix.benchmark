package de.qaware.chronix.client;


import dockerUtil.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;


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
        List<Response> responses = uploader.uploadDockerFiles("",System.getProperty("user.home") + "/Documents/BA_workspace/docker/chronix","http://192.168.2.108","9003");
        //List<Response> responses = uploader.uploadDockerFiles("",args[0],args[1],args[2]);

        if(!responses.isEmpty()){
            for(Response response : responses){
                System.out.println(response.getStatus() +" "+ response.readEntity(String.class));
            }
        } else {
            System.out.println("Nothing uploaded");
        }
*/

///*
        //test build container
        //String commandFileName = "chronix.build";
        final Client client = ClientBuilder.newBuilder().build();
        //final WebTarget target = client.target("http://192.168.2.168:9003/configurator/docker/running?containerName=chronix");

       // final WebTarget target = client.target("http://192.168.2.100:9003/configurator/docker/build?containerName=chronix&commandFileName="+commandFileName);
        //final WebTarget target = client.target("http://192.168.2.118:9003/configurator/ping?nTimes=4");
        //final WebTarget target = client.target("http://192.168.2.118:9003/configurator/which");
        final WebTarget target = client.target("http://192.168.2.100:9003/configurator/docker/stop?containerName=chronix");
        //final WebTarget target = client.target("http://localhost:9003/configurator/booleanTest?value=yes");
        //final WebTarget target = client.target("http://192.168.2.168:9003/configurator/docker/remove?imageName=chronix&removeFiles=yes");

 /*
        // start test
        DockerRunOptions chronix = new DockerRunOptions("chronix",8983,8983,"");
        final WebTarget target = client.target("http://192.168.2.100:9003/configurator/docker/start");
        final Response response = target.request().post(Entity.json(chronix));
 */

/*
        // build test
        DockerBuildOptions chronix = new DockerBuildOptions("chronix","-t");
        final WebTarget target = client.target("http://192.168.2.100:9003/configurator/docker/build");
        final Response response = target.request().post(Entity.json(chronix));
*/

        final Response response = target.request().get();

        //DockerRunOptions op = response.readEntity(DockerRunOptions.class);
        //String op = response.readEntity(String.class);
        //System.out.println(response.getStatus() + " : " + op);



        String[] answers = response.readEntity(String[].class);
        System.out.println("Server status: " + response.getStatus());
        //System.out.println(response.readEntity(String.class));
        for(String answer : answers){
            System.out.println(answer);
        }
//*/



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
