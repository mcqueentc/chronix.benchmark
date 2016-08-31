package Server;

import de.qaware.chronix.client.benchmark.configurator.Configurator;
import de.qaware.chronix.database.BenchmarkDataSource;
import de.qaware.chronix.shared.ServerConfig.ServerConfigAccessor;
import de.qaware.chronix.shared.ServerConfig.ServerConfigRecord;
import de.qaware.chronix.shared.ServerConfig.TSDBInterfaceHandler;

import java.io.File;
import java.util.LinkedList;

/**
 * Created by mcqueen666 on 31.08.16.
 */
public class InterfaceAndConfigUploadTest {

    public static void main(String[] args){

        Configurator configurator = Configurator.getInstance();
        String server = "localhost";

        System.out.println("\n###### Server.InterfaceAndConfigUploadTest ######");

        if(configurator.isServerUp(server)){
            System.out.println("Server is up");
        } else {
            System.out.println("Server not responding");
        }


        // jar interface test and upload

        TSDBInterfaceHandler interfaceHandler = TSDBInterfaceHandler.getInstance();
        ServerConfigAccessor serverConfigAccessor = ServerConfigAccessor.getInstance();
        String jarPath = "/Users/mcqueen666/Documents/BA_workspace/chronix.benchmark/ChronixClient/build/libs/ChronixClient-1.0-SNAPSHOT-all.jar";
        File jarFile = new File(jarPath);
        if (jarFile.exists()) {
            String implName = "chronix";
            interfaceHandler.copyTSDBInterface(jarFile, implName);
            BenchmarkDataSource chronix = interfaceHandler.getTSDBInstance(implName);
            if(chronix != null){
                System.out.println("Client: interface " + chronix.getClass().getName() +" is working");
                System.out.println("Client: interface " + chronix.getClass().getName() +" storage directory is: " + chronix.getStorageDirectoryPath());

                LinkedList<ServerConfigRecord> readRecord = serverConfigAccessor.getServerConfigRecords();
                for(ServerConfigRecord configRecord : readRecord){
                    LinkedList<String> externalImpls = configRecord.getExternalTimeSeriesDataBaseImplementations();
                    if(!externalImpls.contains(implName)){
                        externalImpls.add(implName);
                        configRecord.setExternalTimeSeriesDataBaseImplementations(externalImpls);
                    }

                }
                //write back to hd
                serverConfigAccessor.setServerConfigRecords(readRecord);
                // upload config to server
                if(configurator.uploadServerConfig(server)){
                    System.out.println("Config upload to server successful");
                    // upload jarFile
                    String[] answer = configurator.uploadJarFile(server,jarFile,implName);
                    System.out.println("Server: jar file " + answer[0]);
                    answer = configurator.checkInterfaceStatus(server,implName);
                    System.out.println("Server: " + answer[0]);


                } else {
                    System.out.println("Error config upload");
                }
            }

        } else {
            System.out.println("File not found!");
        }


    }

}
