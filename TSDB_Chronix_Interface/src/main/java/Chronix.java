import de.qaware.chronix.ChronixClient;
import de.qaware.chronix.converter.KassiopeiaSimpleConverter;
import de.qaware.chronix.database.*;
import de.qaware.chronix.solr.client.ChronixSolrStorage;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by mcqueen666 on 08.08.16.
 *
 * Interface implementation for Chronix DB
 *
 */

public class Chronix implements BenchmarkDataSource<SolrQuery>{

    private final String CHRONIX_STORAGE_DIRECTORY = "/opt/chronix-0.3/chronix-solr-6.1.0/server/solr/chronix/data";
    private final String CHRONIX_HOST_MAPPED_STORAGE_DIRECTORY = "/mnt/tsdb-benchmark-data/chronix";
    private final Logger logger = LoggerFactory.getLogger(Chronix.class);
    private final int WAIT_TIME_SLICE = 250;
    private final int MAX_WAIT_TIME = 180_000;
    private String ipAddress;
    private int portNumber;
    private boolean isSetup = false;
    private SolrClient solrClient;
    private Function<MetricTimeSeries, String> groupBy;
    private BinaryOperator<MetricTimeSeries> reduce;
    private ChronixClient<MetricTimeSeries, SolrClient, SolrQuery> chronixClient;


    @Override
    public boolean setup(String ipAddress, int portNumber) {
        if(!isSetup) {
            SolrClient solrClient = new HttpSolrClient.Builder("http://" + ipAddress + ":" + portNumber + "/solr/chronix/").build();
            try {
                //solrClient.ping();
                this.ipAddress = ipAddress;
                this.portNumber = portNumber;
                this.solrClient = solrClient;

                int elapsedMillis = 0;
                while(!isSolrResponding()){
                    if(elapsedMillis < MAX_WAIT_TIME) {
                        Thread.sleep(WAIT_TIME_SLICE);
                        elapsedMillis += WAIT_TIME_SLICE;
                    } else {
                        return false;
                    }
                }

                //Define a group by function for the time series records
                groupBy = MetricTimeSeries::getMetric;

                //Define a reduce function for the grouped time series records
                reduce = (ts1, ts2) -> {
                    ts1.addAll(ts2.getTimestampsAsArray(), ts2.getValuesAsArray());
                    return ts1;
                };


                chronixClient = new ChronixClient<>(new KassiopeiaSimpleConverter(), new ChronixSolrStorage<>(200, groupBy, reduce));


                isSetup = true;

            } catch (Exception e) {
                logger.error("Chronix setup: " + e.getLocalizedMessage());
                isSetup = false;
            }
        }

        return isSetup;
    }

    @Override
    public boolean clean() {
        try {
            solrClient.deleteByQuery("*:*");
            UpdateResponse response = solrClient.commit();
            logger.info("chronix cleaning: status = {} ", response.getStatus());
            return true;

        } catch (Exception e) {
           logger.error("Chronix: Could not clean database: {}", e.getLocalizedMessage());
        }

        return false;
    }

    @Override
    public void shutdown() {
        try {
            solrClient.close();
            isSetup = false;
        } catch (IOException e) {
            logger.error("Chronix Interface shutdown: " + e.getLocalizedMessage());
        }
    }

    @Override
    public boolean ableToMeasureExternalDirectory(){
        return false;
    }

    @Override
    public String getStorageDirectoryPath() {
        return CHRONIX_STORAGE_DIRECTORY;
    }

    @Override
    public String getMappedStorageDirectoryPath(){
        return CHRONIX_HOST_MAPPED_STORAGE_DIRECTORY;
    }

    @Override
    public void writeCachesToDisk(){

    }

    @Override
    public String importDataPoints(TimeSeries timeSeries) {
        long count = 0L;
        if(isSetup){

            String escapedMetricName = escape(timeSeries.getMetricName());
            MetricTimeSeries.Builder builder = new MetricTimeSeries.Builder(escapedMetricName);
            for(Map.Entry<String, String> entry : timeSeries.getTagKey_tagValue().entrySet()){
                builder.attribute(entry.getKey(), escape(entry.getValue()));
                //logger.info("chronix: unescaped tag: {}, escaped tag: {}",entry.getValue(), escape(entry.getValue()));
            }

            List<TimeSeriesPoint> pointList = timeSeries.getPoints();
            int counter = 0;
            for(TimeSeriesPoint point : pointList){
                builder.point(point.getTimeStamp(), point.getValue());
                counter++;
                count++;

                if(counter == NUMBER_OF_POINTS_PER_BATCH){
                    List<MetricTimeSeries> pointsToAdd = new ArrayList<>();
                    pointsToAdd.add(builder.build());

                    try{
                        if(chronixClient.add(pointsToAdd, solrClient)){
                            solrClient.commit();
                            builder = new MetricTimeSeries.Builder(escapedMetricName);
                            for(Map.Entry<String, String> entry : timeSeries.getTagKey_tagValue().entrySet()){
                                builder.attribute(entry.getKey(),escape(entry.getValue()));
                            }
                        } else {
                            return "Error importing data points on chronix.";
                        }

                    } catch (Exception e){
                        logger.error("Error importing data points: " + e.getLocalizedMessage());
                        return "Error importing data points: " + e.getLocalizedMessage();
                    }
                }
            }

            try {
                List<MetricTimeSeries> pointsToAdd = new ArrayList<>();
                pointsToAdd.add(builder.build());
                if(chronixClient.add(pointsToAdd, solrClient)) {
                    solrClient.commit();
                } else {
                    return "Error importing data points on chronix.";
                }
            } catch (Exception e) {
                logger.error("Error importing data points: " + e.getLocalizedMessage());
                return "Error importing data points: " + e.getLocalizedMessage();
            }

            return "Import of " + count +" points successful. Metric name: " + escapedMetricName;
        }

        return "Chronix was not setup";
    }

    @Override
    public SolrQuery getQueryObject(BenchmarkQuery benchmarkQuery){
        String queryString = "";
        SolrQuery query = null;
        if(isSetup) {
            if (benchmarkQuery != null) {
                QueryFunction function = benchmarkQuery.getFunction();
                TimeSeriesMetaData timeSeriesMetaData = benchmarkQuery.getTimeSeriesMetaData();
                Map<String, String> tags = timeSeriesMetaData.getTagKey_tagValue();

                if (timeSeriesMetaData != null && function != null) {
                    // build the query string

                    //host _ process _ metricGroup _ metric
                    for (Map.Entry<String, String> entry : tags.entrySet()) {
                        queryString += entry.getKey() + ":\"" + escape(entry.getValue()) + "\" AND ";
                    }
                    queryString += "metric:\"" + escape(timeSeriesMetaData.getMetricName())
                            + "\" AND start:" + timeSeriesMetaData.getStart()
                            + " AND end:" + timeSeriesMetaData.getEnd() + 1;


                    query = new SolrQuery(queryString);
                    //query.setRows(Integer.MAX_VALUE);
                    //logger.info("SolrQueryString: {}", query.getQuery());


                    switch (function) {
                        case COUNT:
                            query.addFilterQuery("function=count");
                            break;
                        case MEAN:
                            query.addFilterQuery("function=avg");
                            break;
                        case SUM:
                            query.addFilterQuery("function=sum");
                            break;
                        case MIN:
                            query.addFilterQuery("function=min");
                            break;
                        case MAX:
                            query.addFilterQuery("function=max");
                            break;
                        case STDDEV:
                            query.addFilterQuery("function=dev");
                            break;
                        case PERCENTILE:
                            Float p = benchmarkQuery.getPercentile();
                            if (p != null) {
                                query.addFilterQuery("function=p:" + p);
                            }
                            break;
                        case QUERY_ONLY:
                            // nothing to do here for chronix
                    }
                }
            }
        }
        return query;
    }

    @Override
    public List<String> performQuery(BenchmarkQuery benchmarkQuery, SolrQuery queryObject) {
        List<String> queryResults = new LinkedList<>();
        if(isSetup) {
            if (benchmarkQuery != null) {
                QueryFunction function = benchmarkQuery.getFunction();
                TimeSeriesMetaData timeSeriesMetaData = benchmarkQuery.getTimeSeriesMetaData();


                    // do the query
                    if (chronixClient != null && queryObject != null) {
                        try{
                            Stream<MetricTimeSeries> resultStream = chronixClient.stream(solrClient, queryObject);
                            List<MetricTimeSeries> resultList = resultStream.collect(Collectors.toList());
                            if (!resultList.isEmpty()) {
                                resultList.forEach(ts -> queryResults.add(ts.toString() + ", Points:" + ts.points().collect(Collectors.toList())      ));

                                //resultList.forEach(ts -> queryResults.add(ts.toString() + "; Points: " + String.join(", ", ts.points().collect(Collectors.toList()).toString())));
                                //resultList.forEach(ts -> logger.info("Points: {}", ts.points().collect(Collectors.toList())));
                                // debug //TODO erase
                                //queryResults.add("Chronix time range millis: " + timeSeriesMetaData.getStart() + " -> " + timeSeriesMetaData.getEnd() + "\n");
                                //queryResults.add("Query: " + queryObject);
                                //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                //queryResults.add("Chronix Solr Server date: " + formatter.format(new Date(Instant.now().toEpochMilli())));


                            }
                        } catch (Exception e){
                            logger.error("Error Chronix performing query: " + e.getLocalizedMessage());
                            queryResults.add("Error Chronix performing query: " + e.getLocalizedMessage());
                        }

                    } else {
                        queryResults.add("no connection to the Solr ChronixClient");
                    }
            }

        } else {
            queryResults.add("the interface is not setup properly");
        }

        return queryResults;
    }


    public static String escape(String metric) {
        String result = metric.replaceAll("(\\s|\\.|:|=|,|/|\\\\|\\*|\\(|\\)|_|#|-)", "_").replaceAll("@","_at_");
        result = ClientUtils.escapeQueryChars(result);
        result = result.replaceAll("\\\\\\*", "*");
        if(result.charAt(result.length() -1 ) == '_'){
            result = result.substring(0, result.length() -1 );
        }
        if(result.charAt(0) == '_'){
            result = result.substring(1, result.length());
        }
        return result;
    }

    private boolean isSolrResponding(){
        try {
            SolrPingResponse pingResponse = solrClient.ping();
            return true;

        } catch (Exception e){
            logger.info("Solr not responding");
            return false;
        }
    }
}

