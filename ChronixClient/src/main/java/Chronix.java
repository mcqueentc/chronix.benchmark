import de.qaware.chronix.ChronixClient;
import de.qaware.chronix.converter.KassiopeiaSimpleConverter;
import de.qaware.chronix.database.BenchmarkDataSource;
import de.qaware.chronix.database.BenchmarkQuery;
import de.qaware.chronix.database.TimeSeries;
import de.qaware.chronix.database.TimeSeriesMetaData;
import de.qaware.chronix.solr.client.ChronixSolrStorage;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;


import java.io.IOException;
import java.time.Instant;
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

    private String ipAddress;
    private int portNumber;
    private boolean isSetup = false;
    private SolrClient solrClient;
    private Function<MetricTimeSeries, String> groupBy;
    private BinaryOperator<MetricTimeSeries> reduce;


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
        return "/opt/chronix-0.1.3/";
    }

    @Override
    public String importDataPoints(TimeSeries timeSeries) {
        //TODO
        return null;
    }


    @Override
    public List<String> performQuery(BenchmarkQuery benchmarkQuery) {
        List<String> queryResults = new LinkedList<>();
        QueryFunction function = benchmarkQuery.getFunction();
        TimeSeriesMetaData timeSeriesMetaData = benchmarkQuery.getTimeSeriesMetaData();
        Map<String, String> tags = timeSeriesMetaData.getTagKey_tagValue();

        // build the query string
        String queryString = "metric:*" + timeSeriesMetaData.getMetricName() + "*"
                + " AND start:" + timeSeriesMetaData.getStart().toEpochMilli()
                + " AND end:" + timeSeriesMetaData.getEnd().toEpochMilli();

        for(Map.Entry<String, String> entry : tags.entrySet()){
            queryString += " AND " + entry.getKey() + ":" + entry.getValue();
        }

        SolrQuery query = new SolrQuery(queryString);


        switch (function){
            case COUNT: query.addFilterQuery("function=count");
                break;
            case MEAN:  query.addFilterQuery("function=avg");
                break;
            case SUM:   query.addFilterQuery("function=sum");
                break;
            case MIN:   query.addFilterQuery("function=min");
                break;
            case MAX:   query.addFilterQuery("function=max");
                break;
            case STDDEV: query.addFilterQuery("function=dev");
                break;
            case PERCENTILE: query.addFilterQuery("function=p:" + benchmarkQuery.getPercentile());
                break;
        }

        // do the query
        ChronixClient<MetricTimeSeries, SolrClient, SolrQuery> chronixClient = new ChronixClient<>(new KassiopeiaSimpleConverter(), new ChronixSolrStorage<>(200, groupBy, reduce));
        Stream<MetricTimeSeries> resultStream = chronixClient.stream(solrClient,query);
        List<MetricTimeSeries> resultList = resultStream.collect(Collectors.toList());
        if(!resultList.isEmpty()){
            resultList.forEach(ts -> queryResults.add(ts.toString()));
        }

        return queryResults;
    }
}

