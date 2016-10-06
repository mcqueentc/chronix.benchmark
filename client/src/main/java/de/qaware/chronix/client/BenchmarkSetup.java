package de.qaware.chronix.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.qaware.chronix.client.benchmark.configurator.Configurator;
import de.qaware.chronix.database.BenchmarkDataSource;
import de.qaware.chronix.shared.ServerConfig.ServerConfigAccessor;
import de.qaware.chronix.shared.ServerConfig.ServerConfigRecord;
import de.qaware.chronix.shared.ServerConfig.TSDBInterfaceHandler;
import de.qaware.chronix.shared.dockerUtil.DockerBuildOptions;
import de.qaware.chronix.shared.dockerUtil.DockerRunOptions;

import java.io.File;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by mcqueen666 on 06.10.16.
 */
public class BenchmarkSetup {
    public static void main(String[] args){
        if(args.length == 0){
            printUsage();
            printHelp();
            return;
        }

        switch (args[0]){
            case "add": {
                if (args.length != 7) {
                    printAddHelp();
                    break;
                }
                String server = args[1];
                String tsdbName = args[2];
                String additionalOptions = args[4];
                String jarFilePath = args[5];
                String dockerFilesDirectory = args[6];
                int hostPort;
                int containerPort;
                try {
                    String[] ports = args[3].split(":");
                    hostPort = Integer.valueOf(ports[0]);
                    containerPort = Integer.valueOf(ports[1]);
                } catch (Exception e) {
                    System.err.println(e.getLocalizedMessage());
                    printAddHelp();
                    return;
                }

                System.out.println("command:        " + args[0]);
                System.out.println("server:         " + server);
                System.out.println("tsdbName:       " + tsdbName);
                System.out.println("hostPort:       " + hostPort);
                System.out.println("containerPort:  " + containerPort);
                System.out.println("additopts:      " + additionalOptions);
                System.out.println("jar file:       " + jarFilePath);
                System.out.println("dockerDir:      " + dockerFilesDirectory);

                //docker files
                File dockerDirectory = new File(dockerFilesDirectory);
                if (!dockerDirectory.exists() || !dockerDirectory.isDirectory()) {
                    System.out.println("Docker files directory does not exist or is not a directory");
                    return;
                }

                // jar file
                File jarFile = new File(jarFilePath);
                if (!jarFile.exists() || !jarFilePath.endsWith(".jar")) {
                    System.out.println("Given jar file does not exist.");
                    return;
                }
                TSDBInterfaceHandler interfaceHandler = TSDBInterfaceHandler.getInstance();
                interfaceHandler.copyTSDBInterface(jarFile, tsdbName);
                BenchmarkDataSource tsdbInstance = interfaceHandler.getTSDBInstance(tsdbName);
                if (tsdbInstance == null) {
                    System.out.println("BenchmarkDataSource interface was not implemented properly");
                    return;
                }

                // add or update server config
                DockerBuildOptions buildOption = new DockerBuildOptions(tsdbName, "-t");
                DockerRunOptions runOption = new DockerRunOptions(tsdbName, hostPort, containerPort, additionalOptions + " --name " + tsdbName);

                ServerConfigAccessor serverConfigAccessor = ServerConfigAccessor.getInstance();
                ServerConfigRecord serverConfigRecord = serverConfigAccessor.getServerConfigRecord(server);
                if (serverConfigRecord == null) {
                    //generate new record
                    LinkedList<DockerBuildOptions> buildOptions = new LinkedList<>();
                    LinkedList<DockerRunOptions> runOptions = new LinkedList<>();
                    LinkedList<String> tsdbImpls = new LinkedList<>();

                    buildOptions.add(buildOption);
                    runOptions.add(runOption);
                    tsdbImpls.add(tsdbName);

                    serverConfigRecord = new ServerConfigRecord(server);
                    serverConfigRecord.setTsdbBuildRecords(buildOptions);
                    serverConfigRecord.setTsdbRunRecords(runOptions);
                    serverConfigRecord.setExternalTimeSeriesDataBaseImplementations(tsdbImpls);

                    //save new record
                    serverConfigAccessor.addServerConfigRecord(serverConfigRecord);
                    System.out.println("New record saved to config.");
                    printHintUpload();

                } else {
                    // update existing record
                    if (!serverConfigRecord.getTsdbBuildRecords().contains(buildOption)) {
                        serverConfigRecord.getTsdbBuildRecords().add(buildOption);
                    }

                    if (!serverConfigRecord.getTsdbRunRecords().contains(runOption)) {
                        serverConfigRecord.getTsdbRunRecords().add(runOption);
                    } else {
                        //remove the options with same containerName and add updated options
                        serverConfigRecord.getTsdbRunRecords().remove(runOption);
                        serverConfigRecord.getTsdbRunRecords().add(runOption);
                    }
                    if (!serverConfigRecord.getExternalTimeSeriesDataBaseImplementations().contains(tsdbName)) {
                        serverConfigRecord.getExternalTimeSeriesDataBaseImplementations().add(tsdbName);
                    }

                    serverConfigAccessor.updateServerConfigRecord();
                    System.out.println("Config updated.");
                    printHintUpload();
                    break;
                }
            }

            case "remove":{
                if(args.length != 2){
                    printRemoveHelp();
                    return;
                }

                String server = args[1];
                System.out.println("server:     " + server);


                ServerConfigAccessor serverConfigAccessor = ServerConfigAccessor.getInstance();
                ServerConfigRecord serverConfigRecord = serverConfigAccessor.getServerConfigRecord(server);
                if(serverConfigRecord != null){
                    serverConfigAccessor.removeServerConfigRecord(serverConfigRecord);
                    System.out.println("Server removed from config.");
                    printHintUpload();
                } else {
                    System.out.println("No record with given server found -> nothing was removed");
                }
                break;
            }

            case "print":{
                ObjectMapper mapper = new ObjectMapper();
                ServerConfigAccessor serverConfigAccessor = ServerConfigAccessor.getInstance();
                LinkedList<ServerConfigRecord> serverConfigRecords = serverConfigAccessor.getServerConfigRecords();
                for(ServerConfigRecord record : serverConfigRecords){
                    try {
                        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(record));
                        System.out.println("");
                    } catch (JsonProcessingException e) {
                        System.err.println(e.getLocalizedMessage());
                    }
                }
                printHintUpload();
                break;
            }

            case "upload":{
                if(args.length != 2){
                    printUploadHelp();
                    return;
                }

                String server = args[1];

                try {
                    Configurator configurator = Configurator.getInstance();
                    if (configurator.uploadServerConfig(server)) {
                        ServerConfigAccessor serverConfigAccessor = ServerConfigAccessor.getInstance();
                        ServerConfigRecord serverConfigRecord = serverConfigAccessor.getServerConfigRecord(server);
                        TSDBInterfaceHandler tsdbInterfaceHandler = TSDBInterfaceHandler.getInstance();
                        Map<String, File> interfaces = tsdbInterfaceHandler.getInterfaces();
                        for (Map.Entry<String, File> entry : interfaces.entrySet()) {
                            if (serverConfigRecord.getExternalTimeSeriesDataBaseImplementations().contains(entry.getKey())) {
                                String[] answers = configurator.uploadJarFile(server, entry.getValue(), entry.getKey());
                                for(String answer : answers){
                                    System.out.println("Server: " + answer);
                                }
                                answers = configurator.checkInterfaceStatus(server, entry.getKey());
                                for(String answer : answers){
                                    System.out.println("Server: " + answer);
                                }
                            }
                        }
                    } else {
                        System.out.println("Error config upload.");
                    }
                } catch (Exception e) {
                    System.err.println("Error connecting to server: " + e.getLocalizedMessage());
                }
                break;
            }
        }
    }



    private static void printUsage(){
        System.out.println("Usage: setup [command] [server] [option1] [option2] ... ");
        System.out.println("Call command without option to get more help for each command.\n");
    }

    private static void printHelp(){
        System.out.println("Options: \n");
        System.out.println("add:    Add a TSDB interface and the corresponding docker files to a server.");
        System.out.println("remove: Remove a TSDB interface and the corresponding docker files from a server.");
        System.out.println("upload: Upload the configuration to the server.");
        System.out.println("print:  Print the current configuration.");
    }

    private static void printAddHelp(){
        System.out.println("Add Usage: add [server] [tsdbName] [hostPort:containerPort] [\"additionalDockerOptionsString\"] [<TSDB>.jar] [dockerFilesFolder]");
        System.out.println("NOTICE: \" --name [tsdbName]\" will be added to additionalOptions automatically");
    }

    private static void printHintUpload(){
        System.out.println("If you are done with config, don't forget to upload the config to the server");
        printUploadHelp();
    }

    private static void printUploadHelp(){
        System.out.println("Upload usage: upload [server]");
    }

    private static void printRemoveHelp(){
        System.out.println("Remove usage: remove [server]");
    }
}
