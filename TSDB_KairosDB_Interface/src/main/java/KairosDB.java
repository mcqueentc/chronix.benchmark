import de.qaware.chronix.database.*;
import org.kairosdb.client.HttpClient;
import org.kairosdb.client.builder.*;
import org.kairosdb.client.builder.aggregator.SamplingAggregator;
import org.kairosdb.client.response.QueryResponse;
import org.kairosdb.client.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by mcqueen666 on 05.09.16.
 */
public class KairosDB implements BenchmarkDataSource<QueryBuilder>{

    private final String KAIROSDB_STORAGE_DIRECTORY = "/var/lib/cassandra";
    private final Logger logger = LoggerFactory.getLogger(KairosDB.class);
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
                logger.error("KairosDB Interface: " + e.getLocalizedMessage());
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
            logger.error("KairosDB Interface shutdown: " + e.getLocalizedMessage());
        }
    }

    @Override
    public String getStorageDirectoryPath() {
        return KAIROSDB_STORAGE_DIRECTORY;
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
                        logger.error("KairosDB import error: " + e.getLocalizedMessage());
                        return "KairosDB import error: " + e.getLocalizedMessage();
                    }
                }
            }

            try {
                Response response = kairosdb.pushMetrics(builder);
                status = response.getStatusCode();
            } catch (Exception e) {
                logger.error("KairosDB import error: " + e.getLocalizedMessage());
                return "KairosDB import error: " + e.getLocalizedMessage();
            }

            return "KairosDB imported points: " + count + " with metric name: " + escapedMetricName + " and status code: " + status;
        }
        return "KairosDB was not setup";

    }

    @Override
    public QueryBuilder getQueryObject(BenchmarkQuery benchmarkQuery) {
        QueryBuilder builder = null;
        if (benchmarkQuery != null) {
            TimeSeriesMetaData timeSeriesMetaData = benchmarkQuery.getTimeSeriesMetaData();
            QueryFunction function = benchmarkQuery.getFunction();

            SamplingAggregator aggregator = null;

            switch (function) {
                case COUNT:
                    aggregator = AggregatorFactory.createCountAggregator(10, TimeUnit.YEARS);
                    break;
                case MEAN:
                    aggregator = AggregatorFactory.createAverageAggregator(10, TimeUnit.YEARS);
                    break;
                case SUM:
                    aggregator = AggregatorFactory.createSumAggregator(10, TimeUnit.YEARS);
                    break;
                case MIN:
                    aggregator = AggregatorFactory.createMinAggregator(10, TimeUnit.YEARS);
                    break;
                case MAX:
                    aggregator = AggregatorFactory.createMaxAggregator(10, TimeUnit.YEARS);
                    break;
                case STDDEV:
                    aggregator = AggregatorFactory.createStandardDeviationAggregator(10, TimeUnit.YEARS);
                    break;
                case PERCENTILE:
                    Float p = benchmarkQuery.getPercentile();
                    if (p != null) {
                        aggregator = AggregatorFactory.createPercentileAggregator(p.doubleValue(), 10, TimeUnit.YEARS);
                    }
                    break;
                case QUERY_ONLY:
                    break;
            }


            builder = QueryBuilder.getInstance();
            builder.setStart(Date.from(Instant.ofEpochMilli(timeSeriesMetaData.getStart())))
                    .setEnd(Date.from(Instant.ofEpochMilli(timeSeriesMetaData.getEnd())));
            QueryMetric queryMetric = builder.addMetric(escapeKairosDB(timeSeriesMetaData.getMetricName()));
            for (Map.Entry<String, String> tag : timeSeriesMetaData.getTagKey_tagValue().entrySet()) {
                queryMetric.addTag(tag.getKey(), escapeKairosDB(tag.getValue()));
            }
            if (aggregator != null) {
                queryMetric.addAggregator(aggregator);
            }
        }
        return builder;
    }

    @Override
    public List<String> performQuery(BenchmarkQuery benchmarkQuery, QueryBuilder queryObject) {
        List<String> queryResult = new LinkedList<>();
        if(isSetup) {
            if(queryObject != null) {

                // do the query
                try {
                    QueryResponse response = kairosdb.query(queryObject);
                    queryResult.add(response.getBody());

                } catch (Exception e) {
                    logger.error("KairosDB: Error performing query: " + e.getLocalizedMessage());
                    queryResult.add("KairosDB: Error performing query: " + e.getLocalizedMessage());
                }


            }

        } else {
            queryResult.add("KairosDB was not setup!");
        }
        return queryResult;
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
