package Client;

import Docker.*;
import Server.GenerateServerConfigRecord;
import Server.InterfaceAndConfigUploadTest;
import de.qaware.chronix.client.benchmark.configurator.Configurator;
import de.qaware.chronix.database.TimeSeries;

import java.io.File;
import java.util.List;

/**
 * Created by mcqueen666 on 31.08.16.
 */
public class SimpleTestClient {

    public static void main(String[] args){

        Configurator configurator = Configurator.getInstance();
        String server = "localhost";
        try {
            if (configurator.isServerUp(server)) {
                System.out.println("Server is up");
            }
        } catch (Exception e){
            System.out.println("Server not responding. Error: " + e.getLocalizedMessage());
            return;
        }


        GenerateServerConfigRecord.main(null);
        UploadDockerFiles.main(null);
        InterfaceAndConfigUploadTest.main(null);
        //BuildDockerContainer.main(new String[]{/*"chronix",*/"influxdb"});
        StartDockerContainer.main(new String[]{"chronix","influxdb"});
        RunningTestDockerContainer.main(new String[]{"chronix","influxdb"});
        //StopDockerContainer.main(new String[]{"chronix","influxdb"});



        //List<TimeSeries> timeSeriesList = ReadCsvFilesTest.readCsv(new File("/Users/mcqueen666/Desktop/p1/air-lasttest"));

        // import test
        //ImportTest.importCsv(timeSeriesList);

        // query test
        //QueryTest.query(timeSeriesList);



    }
}
