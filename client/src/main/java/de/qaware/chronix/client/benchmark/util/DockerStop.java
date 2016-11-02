package de.qaware.chronix.client.benchmark.util;

import de.qaware.chronix.client.ClientMenu;
import de.qaware.chronix.client.benchmark.configurator.Configurator;
import de.qaware.chronix.shared.ServerConfig.ServerConfigAccessor;
import de.qaware.chronix.shared.ServerConfig.ServerConfigRecord;

import java.util.List;
import java.util.Map;

/**
 * Created by mcqueen666 on 07.10.16.
 */
public class DockerStop {
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
        ServerConfigRecord serverConfig = serverConfigAccessor.getServerConfigRecord(server); // it exists at this point for the server
        List<String> configuredTsdbImpls = serverConfig.getExternalTimeSeriesDataBaseImplementations();
        String[] answers;
        for(String tsdb : tsdbList){
            if(configuredTsdbImpls.contains(tsdb)){
                System.out.println("Stopping " + tsdb + " on server: " + server);
                try {
                    answers = configurator.stopDockerContainer(server, tsdb);
                    for (String s : answers) {
                        System.out.println("Server: " + s);
                    }
                } catch (Exception e){
                    System.err.println("Error stopping " + tsdb + " on " + server + ": " + e.getLocalizedMessage());
                }
            }
        }
    }

    private static void printUsage(){
        System.out.println("Stop usage: stop [server] [tsdbName1] [tsdbName2] ...");
    }
}
