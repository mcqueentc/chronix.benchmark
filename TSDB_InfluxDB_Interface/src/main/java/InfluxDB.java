import de.qaware.chronix.database.*;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * Created by mcqueen666 on 01.09.16.
 */
public class InfluxDB implements BenchmarkDataSource<String> {

    private final String INFLUXDB_STORAGE_DIRECTORY = "/var/lib/influxdb/";
    private final String INFLUXDB_HOST_MAPPED_STORAGE_DIRECTORY = "/mnt/tsdb-benchmark-data/influxdb";
    private final Logger logger = LoggerFactory.getLogger(InfluxDB.class);
    private final int WAIT_TIME_SLICE = 250;
    private final int MAX_WAIT_TIME = 180_000;
    private String ipAddress;
    private int portNumber;
    private boolean isSetup = false;
    private org.influxdb.InfluxDB influxDB;
    private String dbName = "chronixBenchmark";


    @Override
    public boolean setup(String ipAddress, int portNumber) {
        if(!isSetup) {
            influxDB = InfluxDBFactory.connect("http://" + ipAddress + ":" + portNumber, "root", "root");

            int elapsedTime = 0;
            do {
                Pong pong;
                try {
                    pong = influxDB.ping();
                    if (!pong.getVersion().equalsIgnoreCase("unknown")) {
                        influxDB.createDatabase(dbName);
                        isSetup = true;
                    } else {
                        logger.info("Influx not responding -> waiting...");
                        Thread.sleep(WAIT_TIME_SLICE);
                        elapsedTime += WAIT_TIME_SLICE;
                    }

                } catch (Exception e) {
                    logger.error("Error InfluxDB setup: " + e.getLocalizedMessage());
                    try {
                        Thread.sleep(WAIT_TIME_SLICE);
                    } catch (InterruptedException e1) {
                        //
                    }
                    elapsedTime += WAIT_TIME_SLICE;
                    isSetup = false;

                }
            } while (!isSetup && elapsedTime < MAX_WAIT_TIME);
        }

        return isSetup;
    }

    @Override
    public boolean clean() {
        if(isSetup){
            influxDB.deleteDatabase(dbName);
            influxDB.createDatabase(dbName);
            return true;
        }
        return false;
    }

    @Override
    public void shutdown() {
        // nothing to do for influxdb
        isSetup = false;
    }

    @Override
    public boolean ableToMeasureExternalDirectory(){
        return false;
    }

    @Override
    public String getStorageDirectoryPath() {
        return INFLUXDB_STORAGE_DIRECTORY;
    }

    @Override
    public String getMappedStorageDirectoryPath(){
        return INFLUXDB_HOST_MAPPED_STORAGE_DIRECTORY;
    }

    @Override
    public void writeCachesToDisk(){

    }

    @Override
    public String importDataPoints(TimeSeries timeSeries) {
        String reply = "Influxdb was not setup";
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
                String escapedMetricName = escapeInfluxDBMetricName(timeSeries.getMetricName());
                for (TimeSeriesPoint timeSeriesPoint : timeSeries.getPoints()) {
                    Point point = Point
                            .measurement(escapeInfluxDBMetricName(timeSeries.getMeasurementName()))
                            .time(timeSeriesPoint.getTimeStamp(), TimeUnit.MILLISECONDS)
                            .addField(escapedMetricName, timeSeriesPoint.getValue())
                            .build();
                    batchPoints.point(point);
                    count++;

                    if (++counter == NUMBER_OF_POINTS_PER_BATCH) {
                        counter = 0;
                        influxDB.write(batchPoints);
                        batchPoints.getPoints().clear();
                    }
                }

                influxDB.write(batchPoints);
                reply = "Import of "+ count + " points successful. Metric Name: " + escapedMetricName;

            } catch (Exception e) {
                logger.error("Error Influx: " + e.getLocalizedMessage());
                reply = "Error Influx: " + e.getLocalizedMessage();
            }
        }
        return reply;
    }

    @Override
    public String getQueryObject(BenchmarkQuery benchmarkQuery){
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
    public List<String> performQuery(BenchmarkQuery benchmarkQuery, String queryObject) {
        List<String> queryResults = new LinkedList<>();
        if(isSetup) {


            try {
                Query query = new Query(queryObject, dbName);
                QueryResult influxdbQueryResult = influxDB.query(query);

                if(influxdbQueryResult != null){
                    queryResults.add(influxdbQueryResult.toString());
                }

                //TODO ERASE! JUST FOR DEBUG
                queryResults.add(queryObject + "\n");

            } catch (Exception e){
                logger.error("Error performing InfluxDB query: " + e.getLocalizedMessage());
                queryResults.add("Error performing query: " + e.getLocalizedMessage());
            }

        } else {
            queryResults.add("InfluxDB was not setup!");
        }
        return queryResults;
    }

    private String escapeInfluxDBMetricName(String metricName) {
        String result = metricName.replaceAll("(\\s|\\.|:|=|,|/|\\\\|\\*|\\(|\\)|_|#)","_").replaceAll("(-)", "_").replaceAll("%","Percent");
        if(result.charAt(result.length() -1 ) == '_'){
            result = result.substring(0, result.length() -1 );
        }
        if(result.charAt(0) == '_'){
            result = result.substring(1, result.length());
        }
        return result;
    }

}
