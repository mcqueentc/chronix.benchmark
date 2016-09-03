package Client;

import de.qaware.chronix.client.benchmark.queryhandler.util.JsonTimeSeriesHandler;
import de.qaware.chronix.database.TimeSeries;

import java.io.File;
import java.util.List;

/**
 * Created by mcqueen666 on 03.09.16.
 */
public class JsonTimesSeriesTest {

    private static JsonTimeSeriesHandler jsonTimeSeriesHandler = JsonTimeSeriesHandler.getInstance();

    public static void writeTest(List<TimeSeries> timeSeriesList){
        System.out.println("\n###### Client.JsonTimeSeriesTest.writeTest ######");
        System.out.println("Number of TimeSeries: " + timeSeriesList.size());
        List<String> results = jsonTimeSeriesHandler.writeTimeSeriesJson(timeSeriesList);
        //results.forEach(System.out::println);

    }

    public static List<TimeSeries> readTest(File directory){
        System.out.println("\n###### Client.JsonTimeSeriesTest.readTest ######");

        if(directory != null && directory.exists() && directory.isDirectory()){
            File[] files = directory.listFiles();
            if(files != null) {
                System.out.println("Number of Files: " + files.length);
                long startMilliseconds = System.currentTimeMillis();
                List<TimeSeries> timeSeriesList = jsonTimeSeriesHandler.readTimeSeriesJson(files);
                long endMilliseconds = System.currentTimeMillis();
                if(timeSeriesList.get(0).getPoints().size() >= 20) {
                    for (int i = 0; i <= 20; i++) {

                        System.out.println("JSON TimeSeries: " + timeSeriesList.get(0).getPoints().get(i));

                    }
                    System.out.println("Number of points in " + timeSeriesList.get(0).getMetricName() + " = " + timeSeriesList.get(0).getPoints().size());
                }

                System.out.println("Number of TimeSeries: " + timeSeriesList.size());
                System.out.println("JSON read time: " + (endMilliseconds - startMilliseconds) + "ms");
                return timeSeriesList;
            }
        }

        return null;
    }

    public static boolean canImportFromJson(String measurement){
        return jsonTimeSeriesHandler.isMeasurementImportedAsJson(measurement);
    }

    public static String getJsonTimesSeriesDirectory(){
        return jsonTimeSeriesHandler.getTimeSeriesJsonRecordDirectoryPath();
    }
}
