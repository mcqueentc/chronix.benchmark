package de.qaware.chronix.client.benchmark.util;

import de.qaware.chronix.client.ClientMenu;
import de.qaware.chronix.client.benchmark.configurator.Configurator;
import de.qaware.chronix.common.ServerConfig.ServerConfigAccessor;
import de.qaware.chronix.common.dockerUtil.DockerRunOptions;

import java.util.List;
import java.util.Map;

/**
 * Created by mcqueen666 on 07.10.16.
 */
public class DockerStart {
    public static void main(String[] args){
        if(args.length < 2){
            printUsage();
            return;
        }

        String server = args[0];
        List<String> tsdbList;

        Map<String, List<String>> serverTsdbMap = ClientMenu.getConfiguredServerAndTSDBs(args);
        if(serverTsdbMap.isEmpty()){
            System.err.println("Server: " + server + " was not configured!");
            return;
        }

        tsdbList = serverTsdbMap.get(server);
        if(tsdbList.isEmpty()){
            System.err.println("No implemented tsdb interfaces found for your entries -> aborting");
            return;
        }

        Configurator configurator = Configurator.getInstance();
        ServerConfigAccessor serverConfigAccessor = ServerConfigAccessor.getInstance();
        List<DockerRunOptions> dockerRunOptionsList = serverConfigAccessor.getServerConfigRecord(server).getTsdbRunRecords();
        String[] answers;
        for(String tsdb : tsdbList){
            for(DockerRunOptions runOptions : dockerRunOptionsList){
                if(runOptions.getContainerName().equals(tsdb)){
                    System.out.println("Starting " + tsdb + " on server: " + server);
                    try {
                        answers = configurator.startDockerContainer(server, runOptions);
                        for (String s : answers) {
                            System.out.println("Server: " + s);
                        }
                    } catch (Exception e){
                        System.err.println("Error starting " + tsdb + " on " + server + ": " + e.getLocalizedMessage());
                    }
                }
            }
        }
    }

    private static void printUsage(){
        System.out.println("Start usage: start [server] [tsdbName1] [tsdbName2] ...");
    }
}
