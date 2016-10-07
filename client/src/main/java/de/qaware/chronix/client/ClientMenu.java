package de.qaware.chronix.client;


import de.qaware.chronix.client.benchmark.util.CsvConverter;
import de.qaware.chronix.client.benchmark.util.DockerBuild;
import de.qaware.chronix.shared.ServerConfig.ServerConfigAccessor;
import de.qaware.chronix.shared.ServerConfig.ServerConfigRecord;

import java.util.*;

/**
 * Created by mcqueen666 on 06.10.16.
 */
public class ClientMenu {
    public static void main(String[] args){
        if(args.length == 0){
            printUsage();
            printFunctions();
            return;
        }

        String function = args[0];
        List<String> arguments = new ArrayList<String>(Arrays.asList(args));
        arguments.remove(0);
        args = arguments.toArray(new String[]{});

        switch (function){
            case "setup": {
                BenchmarkSetup.main(args);
                break;
            }

            case "import": {
                BenchmarkImport.main(args);
                break;
            }
            case "convert": {
                CsvConverter.main(args);
                break;
            }
            case "benchmark": {
                BenchmarkQueryMeasurement.main(args);
                break;
            }
            case "build": {
                DockerBuild.main(args);
                break;
            }
            case "start":
            case "stop":
            case "clean":
            default:
                printUsage();
                printFunctions();

        }

    }

    private static void printUsage(){
        System.out.println("Usage: java -jar chronixClient.jar [function]");
        System.out.println("For more help per function call with function only.\n");
    }

    private static void printFunctions(){
        System.out.println("Functions: \n");
        System.out.println("setup:      configure client and server.");
        System.out.println("import:     import json time series.");
        System.out.println("convert:    convert .csv to json time series.");
        System.out.println("benchmark:  run the benchmark (after you have imported something)");
        System.out.println("build:      builds docker containers on the server");
        System.out.println("start:      starts docker containers on the server");
        System.out.println("stop:       stops docker containers on the server");
        System.out.println("clean:      purge all data from TSDBs on the server");
    }

    public static Map<String, List<String>> getConfiguredServerAndTSDBs(String[] args){
        Map<String, List<String>> serverTsdbMap = new HashMap<>();
        List<String> tsdbList = new LinkedList<>();
        //check if server is configured
        if(args.length > 1) {
            String server = args[0];
            ServerConfigAccessor serverConfigAccessor = ServerConfigAccessor.getInstance();
            ServerConfigRecord serverConfig = serverConfigAccessor.getServerConfigRecord(server);
            if (serverConfig != null) {
                // check if tsdb interface is imported.
                LinkedList<String> configuredTsdbImpls = serverConfig.getExternalTimeSeriesDataBaseImplementations();
                for (int i = 1; i < args.length; i++) {
                    if (configuredTsdbImpls.contains(args[i])) {
                        tsdbList.add(args[i]);
                    } else {
                        System.err.println("No interface found for: " + args[i] + " -> rejected!");
                    }
                }
                serverTsdbMap.put(server, tsdbList);
            }
        }
        return serverTsdbMap;
    }


}
