import de.qaware.chronix.database.BenchmarkDataSource;
import de.qaware.chronix.database.BenchmarkQuery;
import de.qaware.chronix.database.TimeSeries;
import de.qaware.chronix.database.TimeSeriesPoint;
import org.kairosdb.client.HttpClient;
import org.kairosdb.client.builder.Metric;
import org.kairosdb.client.builder.MetricBuilder;
import org.kairosdb.client.response.Response;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * Created by mcqueen666 on 05.09.16.
 */
public class KairosDB implements BenchmarkDataSource {

    private final String KAIROSDB_STORAGE_DIRECTORY = "/var/lib/cassandra";
    private String ipAddress;
    private int portNumber;
    private boolean isSetup = false;
    HttpClient kairosdb;
    private String dbName = "chronixBenchmark";


    @Override
    public boolean setup(String ipAddress, int portNumber) {
        if(!isSetup){
            try {
                HttpClient client = new HttpClient("http://" + ipAddress + ":" + portNumber);
                // test if client is responding
                int retryCount = Integer.MIN_VALUE;
                retryCount = client.getRetryCount();
                if( retryCount != Integer.MIN_VALUE){
                    this.ipAddress = ipAddress;
                    this.portNumber = portNumber;
                    this.kairosdb = client;
                    isSetup = true;
                }

            } catch (Exception e) {
                System.err.println("KairosDB Interface: " + e.getLocalizedMessage());
                isSetup = false;
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
        try {
            kairosdb.shutdown();
        } catch (IOException e) {
            System.err.println("KairosDB Interface shutdown: " + e.getLocalizedMessage());
        }
    }

    @Override
    public String getStorageDirectoryPath() {
        return KAIROSDB_STORAGE_DIRECTORY;
    }

    @Override
    public String getQueryString(BenchmarkQuery benchmarkQuery) {
        return null;
    }

    @Override
    public String importDataPoints(TimeSeries timeSeries) {
        long count = 0L;
        int status = 0;
        if(isSetup) {

            String escapedMetricName = escapeKairosDB(timeSeries.getMetricName());

            MetricBuilder builder = MetricBuilder.getInstance();
            Metric kairosdbMetric = builder.addMetric(escapedMetricName);
            for(Map.Entry<String, String> tags : timeSeries.getTagKey_tagValue().entrySet()){
                kairosdbMetric.addTag(tags.getKey(), escapeKairosDB(tags.getValue()));
            }

            int counter = 0;
            for(TimeSeriesPoint point : timeSeries.getPoints()){
                kairosdbMetric.addDataPoint(point.getTimeStamp().longValue(), point.getValue().doubleValue());
                counter++;
                count++;

                if(counter == NUMBER_OF_POINTS_PER_BATCH){
                    try {
                        Response response = kairosdb.pushMetrics(builder);
                        if(response.getStatusCode() >= 200 && response.getStatusCode() < 300){
                            builder.getMetrics().remove(kairosdbMetric);
                            kairosdbMetric = builder.addMetric(escapedMetricName);
                            for(Map.Entry<String, String> tags : timeSeries.getTagKey_tagValue().entrySet()){
                                kairosdbMetric.addTag(tags.getKey(), escapeKairosDB(tags.getValue()));
                            }
                            counter = 0;

                        } else {
                            return "KairosDB import error: Status code: " + response.getStatusCode();
                        }

                    } catch (Exception e) {
                        return "KairosDB import error: " + e.getLocalizedMessage();
                    }

                }
            }

            try {
                Response response = kairosdb.pushMetrics(builder);
                status = response.getStatusCode();
            } catch (Exception e) {
                return "KairosDB import error: " + e.getLocalizedMessage();
            }

        }
        return "KairosDB imported points: " + count + " with status code: " + status;
    }

    @Override
    public List<String> performQuery(BenchmarkQuery benchmarkQuery, String queryString) {
        return null;
    }



    public static String escape(String metric, String replacement) {
        return metric.replaceAll("(\\s|\\.|:|=|,|/|\\\\|\\*|\\(|\\)|_|#)", replacement);
    }

    public static String escapeKairosDB(String metric) {
        String escapedString = escape(metric, ".").replaceAll("\\.\\.", ".");
        escapedString = escapedString.replaceAll("%", "Percent");
        //Remove point if it is the first character
        if (escapedString.indexOf(".") == 0) {
            escapedString = escapedString.substring(1);
        }
        return escapedString;
    }
}
