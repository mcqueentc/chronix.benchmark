import de.qaware.chronix.database.*;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * Created by mcqueen666 on 01.09.16.
 */
public class InfluxDB implements BenchmarkDataSource {

    private final String INFLUXDB_STORAGE_DIRECTORY = "/var/lib/influxdb/";
    private String ipAddress;
    private int portNumber;
    private boolean isSetup = false;
    private org.influxdb.InfluxDB influxDB;
    private String dbName = "chronixBenchmark";


    @Override
    public boolean setup(String ipAddress, int portNumber) {
        if(!isSetup) {
            influxDB = InfluxDBFactory.connect("http://" + ipAddress + ":" + portNumber, "root", "root");

            do {
                Pong pong;
                try {
                    pong = influxDB.ping();
                    if (!pong.getVersion().equalsIgnoreCase("unknown")) {
                        isSetup = true;
                    } else {
                        Thread.sleep(100L);
                    }

                } catch (Exception e) {
                    isSetup = false;

                }
            } while (!isSetup);
        }

        return isSetup;
    }

    @Override
    public boolean clean() {
        influxDB.deleteDatabase(dbName);
        return true;
    }

    @Override
    public String getStorageDirectoryPath() {
        return INFLUXDB_STORAGE_DIRECTORY;
    }

    @Override
    public String importDataPoints(TimeSeries timeSeries) {
        String reply = "Import of points successful.";
        if(isSetup) {
            try {
                BatchPoints.Builder builder = BatchPoints
                        .database(dbName)
                        .retentionPolicy("default")
                        .consistency(org.influxdb.InfluxDB.ConsistencyLevel.ALL);
                for (Map.Entry<String, String> tag : timeSeries.getTagKey_tagValue().entrySet()) {
                    builder.tag(tag.getKey(), tag.getValue());
                }
                BatchPoints batchPoints = builder.build();

                for (TimeSeriesPoint timeSeriesPoint : timeSeries.getPoints()) {
                    Point point = Point
                            .measurement(timeSeries.getMeasurementName())
                            .time(timeSeriesPoint.getTimeStamp(), TimeUnit.MILLISECONDS)
                            .addField(escapeInfluxDBMetricName(timeSeries.getMetricName()), timeSeriesPoint.getValue())
                            .build();
                    batchPoints.point(point);
                }

                influxDB.write(batchPoints);

            } catch (Exception e) {
                reply = "Influx: " + e.getLocalizedMessage();
            }
        }
        return reply;
    }

    @Override
    public String getQueryString(BenchmarkQuery benchmarkQuery){
        String queryString = "";

        QueryFunction function = benchmarkQuery.getFunction();
        TimeSeriesMetaData metaData = benchmarkQuery.getTimeSeriesMetaData();

        String escapedMetricName = escapeInfluxDBMetricName(metaData.getMetricName());

        switch (function) {
            case COUNT: queryString = "SELECT COUNT(" + escapedMetricName + ")";
                break;
            case MEAN: queryString = "SELECT MEAN(" + escapedMetricName + ")";
                break;
            case SUM: queryString = "SELECT SUM(" + escapedMetricName + ")";
                break;
            case MIN: queryString = "SELECT MIN(" + escapedMetricName + ")";
                break;
            case MAX: queryString = "SELECT MAX(" + escapedMetricName + ")";
                break;
            case STDDEV: queryString = "SELECT STDDEV(" + escapedMetricName + ")";
                break;
            case PERCENTILE:
                Float p = benchmarkQuery.getPercentile();
                if (p != null) {
                    queryString = "SELECT PERCENTILE(" + escapedMetricName + "," + p + ")";
                }
                break;
        }

        queryString += " FROM " + metaData.getMeasurementName()
                + " WHERE time >= " + metaData.getStart() + "ms"
                + " AND time <= " + metaData.getEnd() + "ms";

        Map<String, String> tags = metaData.getTagKey_tagValue();
        for(Map.Entry<String, String> tag : tags.entrySet()){
            queryString += " AND " + tag.getKey() + " = \'" + tag.getValue() + "\'";
        }


        return queryString;
    }

    @Override
    public List<String> performQuery(BenchmarkQuery benchmarkQuery, String queryString) {
        List<String> queryResults = new LinkedList<>();
        if(isSetup) {

            //TODO ERASE! JUST FOR DEBUG
            queryResults.add(queryString);

        }
        return queryResults;
    }

    private String escapeInfluxDBMetricName(String metricName) {
        return metricName.replaceAll("(\\s|\\.|:|=|,|/|\\\\|\\*|\\(|\\)|_|#)","_").replaceAll("_+", "_");
    }

}
