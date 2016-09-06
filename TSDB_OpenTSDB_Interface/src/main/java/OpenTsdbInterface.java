import de.qaware.chronix.database.BenchmarkDataSource;
import de.qaware.chronix.database.BenchmarkQuery;
import de.qaware.chronix.database.TimeSeries;
import de.qaware.chronix.database.TimeSeriesPoint;
import jersey.repackaged.com.google.common.collect.Sets;

import java.util.*;

/**
 * Created by mcqueen666 on 06.09.16.
 */
public class OpenTsdbInterface implements BenchmarkDataSource {
    private final String OPENTSDB_STORAGE_DIRECTORY = "/opt/data";
    private final int OPENTSDB_NUMBER_OF_POINTS_PER_BATCH = 10;
    private String ipAddress;
    private int portNumber;
    private boolean isSetup = false;
    private String dbName = "chronixBenchmark";
    OpenTsdb openTsdb;


    @Override
    public boolean setup(String ipAddress, int portNumber) {
        if(!isSetup){
            try {
                openTsdb = new OpenTsdb.Builder("http://" + ipAddress + ":" + portNumber).create();
               // openTsdb.setBatchSizeLimit(NUMBER_OF_POINTS_PER_BATCH);
                // openTsdb cannot handle many points at once (o.0)
                openTsdb.setBatchSizeLimit(OPENTSDB_NUMBER_OF_POINTS_PER_BATCH);

                this.ipAddress = ipAddress;
                this.portNumber = portNumber;

            } catch (Exception e){
                isSetup = false;
                System.err.println("Error OpenTSDB setup: " + e.getLocalizedMessage());
            }

        }
        return isSetup;
    }

    @Override
    public boolean clean() {
        return false;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public String getStorageDirectoryPath() {
        return OPENTSDB_STORAGE_DIRECTORY;
    }

    @Override
    public String importDataPoints(TimeSeries timeSeries) {
        String reply = "Error importing data points to openTsdb.";
        if(timeSeries != null) {
            Map<String, String> tags = timeSeries.getTagKey_tagValue();
            openTsdb.preAssignDimensions(tags.keySet());
            // create escapted metricName
            String metricName = openTSDBEscapeValue(timeSeries.getMetricName());

            // create escapted tags
            Map<String, String> metaData = new HashMap<>();
            for(Map.Entry<String, String> tag : tags.entrySet()){
                metaData.put(tag.getKey(), openTSDBEscapeValue(tag.getValue()));
            }

            Set<OpenTsdbMetric> openTsdbMetricSet = new HashSet<>();

            for(TimeSeriesPoint point : timeSeries.getPoints()){
                OpenTsdbMetric openTsdbMetric = OpenTsdbMetric.named(metricName)
                        .withTags(metaData)
                        .withTimestamp(point.getTimeStamp()) // TODO maybe wrong time unit
                        .withValue(point.getValue())
                        .build();

                openTsdbMetricSet.add(openTsdbMetric);

            }

            try {
                //send uses given batch point size
                if(openTsdb.send(openTsdbMetricSet)){
                    reply = "Import of " + openTsdbMetricSet.size() + " points successful. Metric name: " + metricName;
                }
            } catch (Exception e){
                reply = "Error importing data points to openTsdb: " + e.getLocalizedMessage();
            }


        }
        return reply;
    }

    @Override
    public Object getQueryObject(BenchmarkQuery benchmarkQuery) {
        return new Object();
    }

    @Override
    public List<String> performQuery(BenchmarkQuery benchmarkQuery, Object queryObject) {
        List<String> queryResults = new LinkedList<>();
        return queryResults;
    }

    public static String openTSDBEscapeValue(String value) {
        String escapedString = escape(value, ".").replaceAll("\\.\\.", ".").trim();
        escapedString = escapedString.replaceAll("%", "Percent").trim();
        escapedString = escapedString.replaceAll(":", "").trim();
        escapedString = escapedString.replaceAll("\"", "").trim();
        //Remove point if it is the first character
        if (escapedString.indexOf(".") == 0) {
            escapedString = escapedString.substring(1);
        }
        if (escapedString.lastIndexOf(".") == escapedString.length() - 1) {
            escapedString = escapedString.substring(0, escapedString.length() - 1);
        }
        escapedString = escapedString.replaceAll("\\.+", ".");
        return escapedString;
    }

    public static String escape(String metric, String replacement) {
        return metric.replaceAll("(\\s|\\.|:|=|,|/|\\\\|\\*|\\(|\\)|_|#)", replacement);
    }
}
