import de.qaware.chronix.ChronixClient;
import de.qaware.chronix.converter.KassiopeiaSimpleConverter;
import de.qaware.chronix.solr.client.ChronixSolrStorage;
import de.qaware.chronix.database.BenchmarkDataSource;
import de.qaware.chronix.database.BenchmarkQuery;
import de.qaware.chronix.database.TimeSeries;
import de.qaware.chronix.database.TimeSeriesMetaData;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import de.qaware.chronix.timeseries.dt.Point;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;


import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

    private final String CHRONIX_STORAGE_DIRECTORY = "/opt/chronix-0.1.3/";
    private String ipAddress;
    private int portNumber;
    private boolean isSetup = false;
    private SolrClient solrClient;
    private Function<MetricTimeSeries, String> groupBy;
    private BinaryOperator<MetricTimeSeries> reduce;
    private ChronixClient<MetricTimeSeries, SolrClient, SolrQuery> chronixClient;


    @Override
    public boolean setup(String ipAddress, int portNumber) {
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
            return true;

        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean clean() {
        //TODO

        return false;
    }

    @Override
    public String getStorageDirectoryPath() {
        return CHRONIX_STORAGE_DIRECTORY;
    }

    @Override
    public String importDataPoints(TimeSeries timeSeries) {
        String reply = "Error importing data points";
        if(timeSeries != null){
            MetricTimeSeries.Builder builder = new MetricTimeSeries.Builder(timeSeries.getMetricName());
            for(Map.Entry<String, String> entry : timeSeries.getTagKey_tagValue().entrySet()){
                builder.attribute(entry.getKey(),entry.getValue());
            }

            //Convert points
            timeSeries.getPoints().forEach(point -> builder.point(point.getTimeStamp(), point.getValue()));

            List<MetricTimeSeries> pointsToAdd = new ArrayList<>();
            pointsToAdd.add(builder.build());

            if(chronixClient != null) {
                try{
                    chronixClient.add(pointsToAdd, solrClient);
                    reply =  "Import of points successfull.";
                } catch (Exception e){
                    reply = "Error importing data points: " + e.getLocalizedMessage();
                }
            }
        }

        return reply;
    }


    @Override
    public List<String> performQuery(BenchmarkQuery benchmarkQuery) {
        List<String> queryResults = new LinkedList<>();
        if(isSetup) {
            if (benchmarkQuery != null) {
                QueryFunction function = benchmarkQuery.getFunction();
                TimeSeriesMetaData timeSeriesMetaData = benchmarkQuery.getTimeSeriesMetaData();
                Map<String, String> tags = timeSeriesMetaData.getTagKey_tagValue();

                if (timeSeriesMetaData != null && function != null) {
                    // build the query string
                    String queryString = "";

                    //host _ process _ group _ metric
                    for (Map.Entry<String, String> entry : tags.entrySet()) {
                        queryString += entry.getKey() + ":" + entry.getValue() + " AND ";
                    }
                    queryString += "metric:" + timeSeriesMetaData.getMetricName()
                                + " AND start:" + timeSeriesMetaData.getStart()
                                + " AND end:" + timeSeriesMetaData.getEnd();

                    SolrQuery query = new SolrQuery(queryString);
                    query.setRows(Integer.MAX_VALUE);


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
                    }


                    // do the query
                    if (chronixClient != null) {
                        try{
                            Stream<MetricTimeSeries> resultStream = chronixClient.stream(solrClient, query);
                            List<MetricTimeSeries> resultList = resultStream.collect(Collectors.toList());
                            if (!resultList.isEmpty()) {
                                resultList.forEach(ts -> queryResults.add(ts.toString()));
                            }
                        } catch (Exception e){
                            queryResults.add(e.getLocalizedMessage());
                        }

                    } else {
                        queryResults.add("no connection to the Solr ChronixClient");
                    }

                }
            }
        } else {
            queryResults.add("the interface is not setup properly");
        }
        return queryResults;
    }
}

