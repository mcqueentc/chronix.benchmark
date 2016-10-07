package de.qaware.chronix.client.benchmark.util;

import de.qaware.chronix.client.ClientMenu;
import de.qaware.chronix.client.benchmark.queryhandler.QueryHandler;
import de.qaware.chronix.shared.QueryUtil.CleanCommand;
import de.qaware.chronix.shared.ServerConfig.ServerConfigAccessor;
import de.qaware.chronix.shared.ServerConfig.ServerConfigRecord;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by mcqueen666 on 07.10.16.
 */
public class CleanDatabase {
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

        ServerConfigAccessor serverConfigAccessor = ServerConfigAccessor.getInstance();
        ServerConfigRecord serverConfigRecord = serverConfigAccessor.getServerConfigRecord(server); // it exists at this point for the server
        List<CleanCommand> cleanCommandList = new LinkedList<>();
        List<String> configuredTsdbImpls = serverConfigRecord.getExternalTimeSeriesDataBaseImplementations();
        for(String tsdb : tsdbList){
            if(configuredTsdbImpls.contains(tsdb)){
                System.out.println("Cleaning " + tsdb + " on server: " + server);
                Integer port = Integer.valueOf(serverConfigAccessor.getHostPortForTSDB(server, tsdb));
                cleanCommandList.add(new CleanCommand(tsdb, server, port));
            }
        }

        String[] results = QueryHandler.getInstance().cleanDatabasesOnServer(server, cleanCommandList);
        for(String s : results){
            System.out.println(s);
        }
    }

    private static void printUsage(){
        System.out.println("Clean usage: clean [server] [tsdbName1] [tsdbName2] ...");
    }
}
