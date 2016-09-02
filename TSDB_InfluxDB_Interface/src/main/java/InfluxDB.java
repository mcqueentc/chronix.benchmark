import de.qaware.chronix.database.*;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.*;

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
                        influxDB.createDatabase(dbName);
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
        String reply = "";
        long count = 0L;
        if(isSetup) {
            try {
                BatchPoints.Builder builder = BatchPoints
                        .database(dbName)
                        .retentionPolicy("default")
                        .consistency(org.influxdb.InfluxDB.ConsistencyLevel.ALL);
                for (Map.Entry<String, String> tag : timeSeries.getTagKey_tagValue().entrySet()) {
                    builder.tag(tag.getKey(), escapeInfluxDBMetricName(tag.getValue()));
                }
                BatchPoints batchPoints = builder.build();

                int counter = 0;
                for (TimeSeriesPoint timeSeriesPoint : timeSeries.getPoints()) {
                    Point point = Point
                            .measurement(escapeInfluxDBMetricName(timeSeries.getMeasurementName()))
                            .time(timeSeriesPoint.getTimeStamp(), TimeUnit.MILLISECONDS)
                            .addField(escapeInfluxDBMetricName(timeSeries.getMetricName()), timeSeriesPoint.getValue())
                            .build();
                    batchPoints.point(point);
                    count++;

                    if (++counter == 5000) {
                        counter = 0;
                        influxDB.write(batchPoints);
                        batchPoints.getPoints().clear();
                    }
                }


                influxDB.write(batchPoints);
                reply = "Import of "+ count + " points successful.";

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
            case QUERY_ONLY: queryString = "SELECT " + escapedMetricName;
        }

        queryString += " FROM " + escapeInfluxDBMetricName(metaData.getMeasurementName())
                + " WHERE time >= " + metaData.getStart() + "ms"
                + " AND time <= " + metaData.getEnd() + "ms";

        Map<String, String> tags = metaData.getTagKey_tagValue();
        for(Map.Entry<String, String> tag : tags.entrySet()){
            queryString += " AND " + escapeInfluxDBMetricName(tag.getKey()) + " = \'" + escapeInfluxDBMetricName(tag.getValue()) + "\'";
        }


        return queryString;
    }

    @Override
    public List<String> performQuery(BenchmarkQuery benchmarkQuery, String queryString) {
        List<String> queryResults = new LinkedList<>();
        if(isSetup) {

            try {
                Query query = new Query(queryString, dbName);
                QueryResult influxdbQueryResult = influxDB.query(query);

                if(influxdbQueryResult != null){
                    queryResults.add(influxdbQueryResult.toString());
                }

            } catch (Exception e){
                queryResults.add("Error performing query: " + e.getLocalizedMessage());
            }

            //TODO ERASE! JUST FOR DEBUG
            queryResults.add(queryString);

        }
        return queryResults;
    }

    private String escapeInfluxDBMetricName(String metricName) {
        return metricName.replaceAll("(\\s|\\.|:|=|,|/|\\\\|\\*|\\(|\\)|_|#)","_").replaceAll("(-)", "_");
    }

}
