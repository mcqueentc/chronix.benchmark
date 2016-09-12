package Client;

import Docker.*;
import Server.GenerateServerConfigRecord;
import Server.InterfaceAndConfigUploadTest;
import de.qaware.chronix.client.benchmark.configurator.Configurator;
import de.qaware.chronix.client.benchmark.queryhandler.util.JsonTimeSeriesHandler;
import de.qaware.chronix.database.TimeSeries;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mcqueen666 on 31.08.16.
 */
public class SimpleTestClient {

    public static void main(String[] args){

        Configurator configurator = Configurator.getInstance();

        long startMillis;
        long endMillis;
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
        //BuildDockerContainer.main(new String[]{"chronix","influxdb","kairosdb", "opentsdb", "graphite"});
        //StartDockerContainer.main(new String[]{"chronix","influxdb","kairosdb", "opentsdb", "graphite"});
        RunningTestDockerContainer.main(new String[]{"chronix","influxdb","kairosdb", "opentsdb", "graphite"});

        //StopDockerContainer.main(new String[]{"chronix","influxdb","kairosdb", "opentsdb", "graphite"});


        startMillis = System.currentTimeMillis();
        // import test
        ImportTest.importTimeSeriesHeavy();
        endMillis = System.currentTimeMillis();
        System.out.println("\nImport test total time: " + (endMillis - startMillis) + "ms");



        // query test
        //QueryTest.query(timeSeriesList);



    }
}
