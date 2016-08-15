package de.qaware.chronix.client;


import de.qaware.chronix.client.benchmark.configurator.Configurator;
import de.qaware.chronix.client.benchmark.configurator.util.Uploader;
import de.qaware.chronix.database.BenchmarkDataSource;
import de.qaware.chronix.shared.ServerConfig.TSDBInterfaceHandler;
import de.qaware.chronix.shared.ServerConfig.ServerConfigAccessor;
import de.qaware.chronix.shared.ServerConfig.ServerConfigRecord;
import de.qaware.chronix.shared.dockerUtil.DockerBuildOptions;
import de.qaware.chronix.shared.dockerUtil.DockerRunOptions;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.LinkedList;


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

        Configurator configurator = Configurator.getInstance();

        //json to file test (server record test)


            LinkedList<DockerBuildOptions> buildOptionses = new LinkedList<>();
            buildOptionses.add(new DockerBuildOptions("chronix", "-t"));
            buildOptionses.add(new DockerBuildOptions("kairosdb", "-t"));

            LinkedList<DockerRunOptions> runOptionses = new LinkedList<>();
            runOptionses.add(new DockerRunOptions("chronix", 8983, 8983, "-v"));
            runOptionses.add(new DockerRunOptions("kairos", 2003, 2003, "-v"));

            ServerConfigRecord serverConfigRecord = new ServerConfigRecord("192.168.2.100");
            serverConfigRecord.setTsdbBuildRecords(buildOptionses);
            serverConfigRecord.setTsdbRunRecords(runOptionses);
            LinkedList<String> tsdata = new LinkedList<>();
            tsdata.add("p1");
            tsdata.add("p2");
            serverConfigRecord.setTimeSeriesDataFolders(tsdata);


            ServerConfigRecord serverConfigRecord2 = new ServerConfigRecord("www.fau.cs.de");
            serverConfigRecord2.setTsdbBuildRecords(buildOptionses);
            serverConfigRecord2.setTsdbRunRecords(runOptionses);
            LinkedList<String> tsdata2 = new LinkedList<>();
            tsdata2.add("p3");
            tsdata2.add("p4");
            serverConfigRecord2.setTimeSeriesDataFolders(tsdata2);

            LinkedList<ServerConfigRecord> records = new LinkedList<>();
            records.add(serverConfigRecord);
            records.add(serverConfigRecord2);


            ServerConfigAccessor serverConfigAccessor = ServerConfigAccessor.getInstance();
            serverConfigAccessor.setServerConfigRecords(records);



       // jar interface test

            TSDBInterfaceHandler interfaceHandler = TSDBInterfaceHandler.getInstance();
            File jarFile = new File("/Users/mcqueen666/Documents/BA_workspace/chronix.benchmark/DBClient/build/libs/DBClient-1.0-SNAPSHOT.jar");
            if (jarFile.exists()) {
                String implName = "Solr";
                interfaceHandler.copyTSDBInterface(jarFile, implName);
                BenchmarkDataSource chronix = interfaceHandler.getTSDBInstance(implName);
                if(chronix.ping()){
                    LinkedList<ServerConfigRecord> readRecord = serverConfigAccessor.getServerConfigRecords();
                    for(ServerConfigRecord configRecord : readRecord){
                        LinkedList<String> externalImpls = configRecord.getExternalTimeSeriesDataBaseImplementations();
                        externalImpls.add(implName);
                        configRecord.setExternalTimeSeriesDataBaseImplementations(externalImpls);
                    }
                    //write back to hd
                    serverConfigAccessor.setServerConfigRecords(readRecord);
                    // upload config to server
                    if(configurator.uploadServerConfig("localhost")){
                        System.out.println("Config upload to server successful");
                        // upload jarFile
                        String[] answer = configurator.uploadJarFile(jarFile,implName,"192.168.2.108");
                        System.out.println("Server: " + answer[0]);


                    } else {
                        System.out.println("Error config upload");
                    }
                }

            } else {
                System.out.println("File not found!");
            }




        // show server record

        LinkedList<ServerConfigRecord> readRecord = serverConfigAccessor.getServerConfigRecords();

        for(ServerConfigRecord r : readRecord){


            System.out.println("Serveraddress: " + r.getServerAddress());
            LinkedList<DockerRunOptions> newRunList = r.getTsdbRunRecords();
            LinkedList<DockerBuildOptions> newBuildList = r.getTsdbBuildRecords();
            LinkedList<String> tsfolders = r.getTimeSeriesDataFolders();
            LinkedList<String> externalImpls = r.getExternalTimeSeriesDataBaseImplementations();

            for (DockerRunOptions op : newRunList) {
                System.out.println(op.getValidRunCommand());
            }

            for (DockerBuildOptions op : newBuildList) {
                System.out.println(op.getValidBuildCommand());
            }

            for (String s : tsfolders){
                System.out.println("TS Data Folder: " + s);
            }

            for (String s : externalImpls){
                System.out.println(s + " implemented");
                BenchmarkDataSource impl = interfaceHandler.getTSDBInstance(s);
                System.out.println(s + " interface "+ impl.getClass().getName() +" is working: " + impl.ping());
                System.out.println("Query is: " + impl.getQueryForFunction(null,0,null,null,null,null,null,null,0.f,
                                                                            BenchmarkDataSource.QueryFunction.COUNT));
            }


        }




        // Server is up test

        if(configurator.isServerUp("localhost")){
            System.out.println("Server is up");
        } else {
            System.out.println("Server not responding");
        }


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
            List<Response> responses = uploader.uploadDockerFiles("", System.getProperty("user.home") + "/Documents/BA_workspace/docker/chronix", "http://192.168.2.108", "9003");
            //List<Response> responses = uploader.uploadDockerFiles("",args[0],args[1],args[2]);

            if (!responses.isEmpty()) {
                for (Response response : responses) {
                    System.out.println(response.getStatus() + " " + response.readEntity(String.class));
                }
            } else {
                System.out.println("Nothing uploaded");
            }

*/

///*
        //test build container
        //String commandFileName = "chronix.build";
        //final Client client = ClientBuilder.newBuilder().build();
        //final WebTarget target = client.target("http://192.168.2.100:9003/configurator/test?name=chronix.jar");
        //final Response response = target.request().get();
        //System.out.println(response.readEntity(String.class));

       // final WebTarget target = client.target("http://192.168.2.100:9003/configurator/docker/build?containerName=chronix&commandFileName="+commandFileName);
        //final WebTarget target = client.target("http://192.168.2.100:9003/configurator/ping?nTimes=4");
        //final WebTarget target = client.target("http://192.168.2.100:9003/configurator/which");
        //final WebTarget target = client.target("http://192.168.2.100:9003/configurator/docker/stop?containerName=chronix");
        //final WebTarget target = client.target("http://localhost:9003/configurator/booleanTest?value=yes");
        //final WebTarget target = client.target("http://192.168.2.100:9003/configurator/docker/remove?imageName=chronix&removeFiles=yes");

 /*
        // start test
        DockerRunOptions chronix = new DockerRunOptions("chronix",8983,8983,"");
        String[] answers = configurator.startDockerContainer("localhost",chronix);

        //final WebTarget target = client.target("http://192.168.2.100:9003/configurator/docker/start");
        //final Response response = target.request().post(Entity.json(chronix));
 */
///*
        //running test
        String[] answers = null;
        if(configurator.isDockerContainerRunning("localhost","chronix")){
            String[] s = {"container is running"};
            answers = s;
        } else {
            String[] s = {"container is not running"};
            answers = s;
        }
        for(String answer : answers){
            System.out.println(answer);
        }


//*/


        //stop test
        //String[] answers = configurator.stopDockerContainer("localhost","chronix");

/*
        // build test
        DockerBuildOptions chronix = new DockerBuildOptions("chronix","-t");
        String[] answers = configurator.buildDockerContainer("localhost",chronix);

        //final WebTarget target = client.target("http://192.168.2.100:9003/configurator/docker/build");
        //final Response response = target.request().post(Entity.json(chronix));
*/

        // upload test
        //String[] answers = configurator.uploadFiles("localhost",System.getProperty("user.home") + "/Documents/BA_workspace/docker/chronix");

        // remove test
        //String[] answers = configurator.removeDockerContainer("localhost","chronix",true);


   //     final Response response = target.request().get();

        //DockerRunOptions op = response.readEntity(DockerRunOptions.class);
        //String op = response.readEntity(String.class);
        //System.out.println(response.getStatus() + " : " + op);


///*
        //String[] answers = response.readEntity(String[].class);
        //System.out.println("Server status: " + response.getStatus());
        //System.out.println(response.readEntity(String.class));

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
