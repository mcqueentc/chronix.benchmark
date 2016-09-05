import de.qaware.chronix.ChronixClient;
import de.qaware.chronix.converter.KassiopeiaSimpleConverter;
import de.qaware.chronix.database.*;
import de.qaware.chronix.solr.client.ChronixSolrStorage;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
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

public class Chronix implements BenchmarkDataSource{

    private final String CHRONIX_STORAGE_DIRECTORY = "/opt/chronix-0.3/";
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
            SolrClient solrClient = new HttpSolrClient("http://" + ipAddress + ":" + portNumber + "/solr/chronix/");
            try {
                solrClient.ping();
                this.ipAddress = ipAddress;
                this.portNumber = portNumber;
                this.solrClient = solrClient;

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
                System.err.println("Chronix setup: " + e.getLocalizedMessage());
                isSetup = false;
            }
        }

        return isSetup;
    }

    @Override
    public boolean clean() {
        //TODO

        return false;
    }

    @Override
    public void shutdown() {
        try {
            solrClient.close();
        } catch (IOException e) {
            System.err.println("Chronix Interface shutdown: " + e.getLocalizedMessage());
        }
    }

    @Override
    public String getStorageDirectoryPath() {
        return CHRONIX_STORAGE_DIRECTORY;
    }

    @Override
    public String importDataPoints(TimeSeries timeSeries) {
        long count = 0L;
        if(isSetup){

            MetricTimeSeries.Builder builder = new MetricTimeSeries.Builder(escape(timeSeries.getMetricName()));
            for(Map.Entry<String, String> entry : timeSeries.getTagKey_tagValue().entrySet()){
                builder.attribute(entry.getKey(), escape(entry.getValue()));
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
                            builder = new MetricTimeSeries.Builder(escape(timeSeries.getMetricName()));
                            for(Map.Entry<String, String> entry : timeSeries.getTagKey_tagValue().entrySet()){
                                builder.attribute(entry.getKey(),escape(entry.getValue()));
                            }
                        } else {
                            return "Error importing data points on chronix.";
                        }

                    } catch (Exception e){
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
                return "Error importing data points: " + e.getLocalizedMessage();
            }

            return "Import of " + count +" points successful.";
        }

        return "Chronix was not setup";
    }

    @Override
    public String getQueryString(BenchmarkQuery benchmarkQuery){
        String queryString = "";
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
                            + " AND end:" + timeSeriesMetaData.getEnd();
                }
            }
        }
        return queryString;
    }

    @Override
    public List<String> performQuery(BenchmarkQuery benchmarkQuery, String queryString) {
        List<String> queryResults = new LinkedList<>();
        if(isSetup) {
            if (benchmarkQuery != null) {
                QueryFunction function = benchmarkQuery.getFunction();
                TimeSeriesMetaData timeSeriesMetaData = benchmarkQuery.getTimeSeriesMetaData();


                    SolrQuery query = new SolrQuery(queryString);
                    //query.setRows(Integer.MAX_VALUE);


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
                            break;
                    }


                    // do the query
                    if (chronixClient != null) {
                        try{
                            Stream<MetricTimeSeries> resultStream = chronixClient.stream(solrClient, query);
                            List<MetricTimeSeries> resultList = resultStream.collect(Collectors.toList());
                            if (!resultList.isEmpty()) {
                                resultList.forEach(ts -> queryResults.add(ts.toString()));
                                // debug //TODO erase
                                queryResults.add("start: " + timeSeriesMetaData.getStart());
                                queryResults.add("end: " + timeSeriesMetaData.getEnd());
                                queryResults.add("Query: " + queryString);
                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                queryResults.add("Chronix Solr Server date: " + formatter.format(new Date(Instant.now().toEpochMilli())));

                                // debug // TODO erase
                                queryResults.add("Fields: " + query.getFields());
                                queryResults.add("SolrQuery: " + query.toQueryString());

                            }
                        } catch (Exception e){
                            queryResults.add(e.getLocalizedMessage());
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
        return metric.replaceAll("(\\s|\\.|:|=|,|/|\\\\|\\*|\\(|\\)|_|#)", "_");
    }
}

