package Client;

import de.qaware.chronix.client.benchmark.queryhandler.util.JsonTimeSeriesHandler;
import de.qaware.chronix.database.TimeSeries;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mcqueen666 on 03.09.16.
 */
public class JsonTimesSeriesTest {

    private static JsonTimeSeriesHandler jsonTimeSeriesHandler = JsonTimeSeriesHandler.getInstance();

    public static void main(String[] args){
        System.out.println("\n###### Client.JsonTimeSeriesTest.readTest ######");

        List<File> directories = new ArrayList<>();
        directories.add(new File(jsonTimeSeriesHandler.getTimeSeriesJsonRecordDirectoryPath() + File.separator + "air-lasttest"));
        directories.add(new File(jsonTimeSeriesHandler.getTimeSeriesJsonRecordDirectoryPath() + File.separator + "shd"));
        directories.add(new File(jsonTimeSeriesHandler.getTimeSeriesJsonRecordDirectoryPath() + File.separator + "promt"));

        for(File directory : directories) {
            if (directory != null) {
                if(directory.exists() && directory.isDirectory() && canImportFromJson(directory.getName())) {
                    File[] files = directory.listFiles();
                    if (files != null) {
                        List<TimeSeries> timeSeriesList = new ArrayList<>();
                        System.out.println("\nNumber of Files: " + files.length);
                        long startMilliseconds = System.currentTimeMillis();
                        List<File> fileParts = new ArrayList<>();
                        for (int i = 0; i < files.length; i++) {
                            fileParts.add(files[i]);
                            if (i != 0 && i % 500 == 0) {
                                jsonTimeSeriesHandler.readTimeSeriesJson(fileParts.toArray(new File[]{}));
                                fileParts.clear();
                                System.out.println(directory.getName() + ": " + i + " files read.");
                            }
                        }
                        jsonTimeSeriesHandler.readTimeSeriesJson(fileParts.toArray(new File[]{}));
                        System.out.println(directory.getName() + ": " + files.length + " files read.");
                        long endMilliseconds = System.currentTimeMillis();
                        System.out.println("JSON read time: " + (endMilliseconds - startMilliseconds) + "ms");
                    }
                } else {
                    System.out.println(directory.getName() + " could not be imported with json importer");
                }
            }

        }

    }

    public static boolean canImportFromJson(String measurement){
        return jsonTimeSeriesHandler.isMeasurementImportedAsJson(measurement);
    }

    public static String getJsonTimesSeriesDirectory(){
        return jsonTimeSeriesHandler.getTimeSeriesJsonRecordDirectoryPath();
    }
}
