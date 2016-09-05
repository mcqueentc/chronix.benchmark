package Server;

import de.qaware.chronix.client.benchmark.configurator.Configurator;
import de.qaware.chronix.database.BenchmarkDataSource;
import de.qaware.chronix.shared.ServerConfig.ServerConfigAccessor;
import de.qaware.chronix.shared.ServerConfig.ServerConfigRecord;
import de.qaware.chronix.shared.ServerConfig.TSDBInterfaceHandler;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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
        ServerConfigAccessor serverConfigAccessor = ServerConfigAccessor.getInstance();//String jarPath = "/Users/mcqueen666/Documents/BA_workspace/chronix.benchmark/TSDB_Chronix_Interface/build/libs/TSDB_Chronix_Interface-1.0-SNAPSHOT-all.jar";

        Map<String,String> tsdbImpls = new HashMap<>();
        tsdbImpls.put("chronix", "/Users/mcqueen666/Documents/BA_workspace/chronix.benchmark/TSDB_Chronix_Interface/build/libs/TSDB_Chronix_Interface-1.0-SNAPSHOT-all.jar");
        tsdbImpls.put("influxdb", "/Users/mcqueen666/Documents/BA_workspace/chronix.benchmark/TSDB_InfluxDB_Interface/build/libs/TSDB_InfluxDB_Interface-1.0-SNAPSHOT-all.jar");
        tsdbImpls.put("kairosdb", "/Users/mcqueen666/Documents/BA_workspace/chronix.benchmark/TSDB_KairosDB_Interface/build/libs/TSDB_KairosDB_Interface-1.0-SNAPSHOT-all.jar");

        for(Map.Entry<String, String> entry : tsdbImpls.entrySet()){
            File jarFile = new File(entry.getValue());
            String implName = entry.getKey();

            if (jarFile.exists()) {
                interfaceHandler.copyTSDBInterface(jarFile, implName);
                BenchmarkDataSource tsdbInstance = interfaceHandler.getTSDBInstance(implName);
                if (tsdbInstance != null) {
                    System.out.println("Client: interface " + tsdbInstance.getClass().getName() + " is working");
                    System.out.println("Client: interface " + tsdbInstance.getClass().getName() + " storage directory is: " + tsdbInstance.getStorageDirectoryPath());

                    LinkedList<ServerConfigRecord> readRecord = serverConfigAccessor.getServerConfigRecords();
                    for (ServerConfigRecord configRecord : readRecord) {
                        LinkedList<String> externalImpls = configRecord.getExternalTimeSeriesDataBaseImplementations();
                        if (!externalImpls.contains(implName)) {
                            externalImpls.add(implName);
                            configRecord.setExternalTimeSeriesDataBaseImplementations(externalImpls);
                        }

                    }
                    //write back to hd
                    serverConfigAccessor.setServerConfigRecords(readRecord);
                    // upload config to server
                    if (configurator.uploadServerConfig(server)) {
                        System.out.println("Config upload to server successful");
                        // upload jarFile
                        String[] answer = configurator.uploadJarFile(server, jarFile, implName);
                        System.out.println("Server: jar file " + answer[0]);
                        answer = configurator.checkInterfaceStatus(server, implName);
                        System.out.println("Server: " + answer[0] + "\n");


                    } else {
                        System.out.println("Error config upload\n");
                    }
                }

            } else {
                System.out.println("File not found!\n");

            }
        }
    }

}
