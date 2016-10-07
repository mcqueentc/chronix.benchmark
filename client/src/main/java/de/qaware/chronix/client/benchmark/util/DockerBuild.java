package de.qaware.chronix.client.benchmark.util;

import de.qaware.chronix.client.ClientMenu;
import de.qaware.chronix.client.benchmark.configurator.Configurator;
import de.qaware.chronix.shared.ServerConfig.ServerConfigAccessor;
import de.qaware.chronix.shared.dockerUtil.DockerBuildOptions;

import java.util.List;
import java.util.Map;

/**
 * Created by mcqueen666 on 07.10.16.
 */
public class DockerBuild {
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
        List<DockerBuildOptions> dockerBuildOptionsList = serverConfigAccessor.getServerConfigRecord(server).getTsdbBuildRecords();
        String[] answers;
        for(String tsdb : tsdbList){
            for(DockerBuildOptions buildOption : dockerBuildOptionsList){
                if(buildOption.getContainerName().equals(tsdb)){
                    System.out.println("Building " + tsdb + " on server: " + server);
                    answers = configurator.buildDockerContainer(server, buildOption);
                    for(String s : answers){
                        System.out.println("Server: " + s);
                    }
                }
            }
        }
    }

    private static void printUsage(){
        System.out.println("Build usage: build [server] [tsdbName1] [tsdbName2] ...");
    }
}
