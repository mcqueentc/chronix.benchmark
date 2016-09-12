package Client.data;

import de.qaware.chronix.client.benchmark.queryhandler.util.CsvImporter;
import de.qaware.chronix.client.benchmark.queryhandler.util.JsonTimeSeriesHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mcqueen666 on 11.09.16.
 */
public class ConvertCsvToJsonTest {

    public static void main(String[] args){

        System.out.println("\n###### Client.data.ConvertCsvToJsonTest ######");
        long startMilliseconds;
        long endMilliseconds;

        List<File> directories = new ArrayList<>();
        directories.add(new File("/Users/mcqueen666/Desktop/p1/air-lasttest"));
        directories.add(new File("/Users/mcqueen666/Desktop/p2/shd"));
        directories.add(new File("/Users/mcqueen666/Desktop/p3/promt"));

        // csv importer test
        CsvImporter csvImporter = new CsvImporter();
        startMilliseconds = System.currentTimeMillis();
        csvImporter.convertCsvToJson(directories);
        endMilliseconds = System.currentTimeMillis();
        System.out.println("Conversion time: " + (endMilliseconds - startMilliseconds) + "ms");

    }
}
