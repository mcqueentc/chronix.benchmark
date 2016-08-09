package de.qaware.chronix.shared.ServerConfig;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by mcqueen666 on 05.08.16.
 */
public class ServerConfigAccessor {

    private static ServerConfigAccessor instance;
    private final String configDirectory = System.getProperty("user.home") + File.separator + ".chronixBenchmark_conf" + File.separator;
    private final String serverConfigFileName = "serverConfig.json";
    private LinkedList<ServerConfigRecord> serverConfigRecords;


    private ServerConfigAccessor(){
        new File(configDirectory).mkdir();
        if((new File(configDirectory + serverConfigFileName).exists())) {
            readRecordFile();
        }
        if(serverConfigRecords == null){
            serverConfigRecords =  new LinkedList<ServerConfigRecord>();
            writeRecordFile();
        }
    }

    public static synchronized ServerConfigAccessor getInstance(){
        if(instance == null){
            instance = new ServerConfigAccessor();
        }

        return instance;
    }

    public String getConfigDirectory(){
        return configDirectory;
    }

    public String getServerConfigFileName(){
        return serverConfigFileName;
    }

    public LinkedList<ServerConfigRecord> getServerConfigRecords(){
        readRecordFile();
        return serverConfigRecords;
    }

    public synchronized void setServerConfigRecords(LinkedList<ServerConfigRecord> serverConfigRecords){
        this.serverConfigRecords = serverConfigRecords;
        writeRecordFile();
    }


    /**
     * Reads the server record file from json file in filesystem
     */
    private void readRecordFile(){
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(configDirectory + serverConfigFileName);
        try {
            serverConfigRecords = mapper.readValue(file, new TypeReference<LinkedList<ServerConfigRecord>>() {});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes the server record file as json to filesystem
     */
    private synchronized void writeRecordFile(){
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(configDirectory + serverConfigFileName);
        try {
            mapper.writeValue(file, serverConfigRecords);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
