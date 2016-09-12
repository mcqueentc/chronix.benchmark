package Client.data;

import de.qaware.chronix.client.benchmark.queryhandler.util.JsonTimeSeriesHandler;
import de.qaware.chronix.database.TimeSeriesMetaData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mcqueen666 on 12.09.16.
 */
public class ReadMetaDataFromJsonTest {
    private static JsonTimeSeriesHandler jsonTimeSeriesHandler = JsonTimeSeriesHandler.getInstance();

    public static void main(String[] args) {
        System.out.println("\n###### Client.data.ReadMetaDataFromJsonTest ######");


        List<String> measurements = new ArrayList<>();
        measurements.add("air-lasttest");
        measurements.add("shd");
        measurements.add("promt");

        for(String measurement : measurements){
            List<TimeSeriesMetaData> timeSeriesMetaDataList = jsonTimeSeriesHandler.readTimeSeriesMetaDatafromJson(measurement);
            System.out.println("\nMeasurement: " + measurement);
            System.out.println("Number of TimesSeriesMetaData: " + timeSeriesMetaDataList.size());
            int count = 10;
            System.out.println("First " + count + " metricNames: ...");
            for(int i = 0; i < count; i++){
                System.out.println(timeSeriesMetaDataList.get(i).getMetricName());
            }
        }
    }

}
