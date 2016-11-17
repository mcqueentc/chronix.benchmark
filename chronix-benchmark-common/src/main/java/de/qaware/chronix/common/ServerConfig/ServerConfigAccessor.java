package de.qaware.chronix.common.ServerConfig;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.qaware.chronix.common.dockerUtil.DockerRunOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by mcqueen666 on 05.08.16.
 */
public class ServerConfigAccessor {

    private static ServerConfigAccessor instance;
    private final Logger logger = LoggerFactory.getLogger(ServerConfigAccessor.class);
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

    public synchronized void addServerConfigRecord(ServerConfigRecord serverConfigRecord){
        readRecordFile();
        serverConfigRecords.add(serverConfigRecord);
        writeRecordFile();
    }

    public synchronized void removeServerConfigRecord(ServerConfigRecord serverConfigRecord){
        readRecordFile();
        if(serverConfigRecords.remove(serverConfigRecord)){
            writeRecordFile();
        }
    }

    public synchronized void updateServerConfigRecord(){
        writeRecordFile();
    }


    /**
     * Returns ServerConfigRecord for given server address.
     *
     * @param serverAddress the server address or ip.
     * @return the ServerConfigRecord or null if not contained.
     */
    public ServerConfigRecord getServerConfigRecord(String serverAddress){
        readRecordFile();
        if(!serverConfigRecords.isEmpty()){
            for(ServerConfigRecord serverConfigRecord : serverConfigRecords){
                if(serverConfigRecord.getServerAddress().equals(serverAddress)){
                    return serverConfigRecord;
                }
            }
        }
        return null;
    }


    /**
     * Returns the host port on given server where given tsdb is available.
     *
     * @param serverAddress the server address
     * @param tsdbName the tsdb
     * @return the port
     */
    public String getHostPortForTSDB(String serverAddress, String tsdbName){
        LinkedList<ServerConfigRecord> records = getServerConfigRecords();
        for(ServerConfigRecord r : records){
            if (r.getServerAddress().equals(serverAddress)){
                LinkedList<DockerRunOptions> runOptions = r.getTsdbRunRecords();
                for(DockerRunOptions o : runOptions){
                    if(o.getContainerName().equals(tsdbName)){
                        return Integer.toString(o.getHostPort());
                    }
                }
            }
        }
        return null;
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
            logger.error("Error ServerConfigAccessor: " + e.getLocalizedMessage());
        }
    }

    /**
     * Writes the server record file as json to filesystem
     */
    private synchronized void writeRecordFile(){
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(configDirectory + serverConfigFileName);
        try {
            //mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json)
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, serverConfigRecords);
        } catch (IOException e) {
            logger.error("Error ServerConfigAccessor: " + e.getLocalizedMessage());
        }
    }

}
