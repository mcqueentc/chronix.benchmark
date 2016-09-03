package Client;

import de.qaware.chronix.client.benchmark.queryhandler.util.JsonTimeSeriesHandler;
import de.qaware.chronix.database.TimeSeries;

import java.util.List;

/**
 * Created by mcqueen666 on 03.09.16.
 */
public class JsonTimesSeriesTest {

    private static JsonTimeSeriesHandler jsonTimeSeriesHandler = JsonTimeSeriesHandler.getInstance();

    public static void writeTest(List<TimeSeries> timeSeriesList){
        System.out.println("\n###### Client.JsonTimeSeriesTest ######");
        System.out.println("Number of TimeSeries: " + timeSeriesList.size());
        List<String> results = jsonTimeSeriesHandler.writeTimeSeriesJson(timeSeriesList);
        //results.forEach(System.out::println);

    }
}
