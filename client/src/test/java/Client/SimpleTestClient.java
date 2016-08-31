package Client;

import Docker.BuildDockerContainer;
import Docker.RunningTestDockerContainer;
import Docker.StartDockerContainer;
import Docker.UploadDockerFiles;
import Server.GenerateServerConfigRecord;
import Server.InterfaceAndConfigUploadTest;
import de.qaware.chronix.database.TimeSeries;

import java.io.File;
import java.util.List;

/**
 * Created by mcqueen666 on 31.08.16.
 */
public class SimpleTestClient {

    public static void main(String[] args){

        GenerateServerConfigRecord.main(null);


        //UploadDockerFiles.main(null);
        //BuildDockerContainer.main(null);
        StartDockerContainer.main(null);
        RunningTestDockerContainer.main(null);

        InterfaceAndConfigUploadTest.main(null);

        List<TimeSeries> timeSeriesList = ReadCsvFilesTest.readCsv(new File("/Users/mcqueen666/Desktop/p1/air-lasttest"));

        // import test
        ImportTest.importCsv(timeSeriesList);

        // query test
        QueryTest.query(timeSeriesList);



    }
}
