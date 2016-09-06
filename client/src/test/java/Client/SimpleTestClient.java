package Client;

import Docker.*;
import Server.GenerateServerConfigRecord;
import Server.InterfaceAndConfigUploadTest;
import de.qaware.chronix.client.benchmark.configurator.Configurator;
import de.qaware.chronix.database.TimeSeries;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mcqueen666 on 31.08.16.
 */
public class SimpleTestClient {

    public static void main(String[] args){

        Configurator configurator = Configurator.getInstance();
        String server = "localhost";
        File importFile = new File("/Users/mcqueen666/Desktop/p1/air-lasttest");
        List<TimeSeries> timeSeriesList = new LinkedList<>();

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
        //BuildDockerContainer.main(new String[]{"chronix","influxdb","kairosdb", "opentsdb"});
        //StartDockerContainer.main(new String[]{"chronix","influxdb","kairosdb", "opentsdb"});
        RunningTestDockerContainer.main(new String[]{"chronix","influxdb","kairosdb", "opentsdb"});

        //StopDockerContainer.main(new String[]{"chronix","influxdb","kairosdb"});


        if(JsonTimesSeriesTest.canImportFromJson(importFile.getName())){
            timeSeriesList = JsonTimesSeriesTest.readTest(new File(JsonTimesSeriesTest.getJsonTimesSeriesDirectory()
                    + File.separator
                    + importFile.getName()));
        } else {
            timeSeriesList = ReadCsvFilesTest.readCsv(importFile);
        }

        // write duplicate free times series to disk
        JsonTimesSeriesTest.writeTest(timeSeriesList);

        // import test
        ImportTest.importCsv(timeSeriesList);

        // query test
        //QueryTest.query(timeSeriesList);



    }
}
