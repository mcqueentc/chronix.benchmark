import de.qaware.chronix.database.*;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by mcqueen666 on 08.09.16.
 */
public class GraphiteInterface implements BenchmarkDataSource<GraphiteQuery>{
    private final String GRAPHITE_STORAGE_DIRECTORY = "/opt/graphite/storage";
    private final Logger logger = LoggerFactory.getLogger(GraphiteInterface.class);
    private final int WAIT_TIME_SLICE = 250;
    private final int MAX_WAIT_TIME = 180_000;
    private String ipAddress;
    private int portNumber;
    private boolean isSetup = false;
    private WebTarget graphiteClient;
    private Socket socket;
    private Writer writer;
    private Client client;

    @Override
    public boolean setup(String ipAddress, int portNumber) {
        if(!isSetup){
            int elapsedTime = 0;
            while(!isSetup && elapsedTime < MAX_WAIT_TIME) {
                try {
                    client = ClientBuilder.newBuilder()
                            .register(JacksonFeature.class)
                            .property(ClientProperties.CONNECT_TIMEOUT, 30_000)
                            .property(ClientProperties.READ_TIMEOUT, 30_000)
                            .build();
                    graphiteClient = client.target("http://" + ipAddress + "/");
                    socket = new Socket(ipAddress, portNumber);
                    writer = new OutputStreamWriter(socket.getOutputStream());
                    this.ipAddress = ipAddress;
                    this.portNumber = portNumber;

                    isSetup = true;

                } catch (Exception e) {
                    logger.error("Error initializing graphite interface: " + e.getLocalizedMessage());
                    isSetup = false;
                    try {
                        Thread.sleep(WAIT_TIME_SLICE);
                    } catch (InterruptedException e1) {
                        //
                    }
                    elapsedTime += WAIT_TIME_SLICE;
                }
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
        if(isSetup){
            try {
                writer.close();
                socket.close();
                client.close();
                isSetup = false;

            } catch (IOException e) {
                logger.error("Error shutting down graphite interface: " + e.getLocalizedMessage());
            }
        }
    }

    @Override
    public String getStorageDirectoryPath() {
        return GRAPHITE_STORAGE_DIRECTORY;
    }

    @Override
    public String importDataPoints(TimeSeries timeSeries) {
        String reply = "Error: graphite was not setup!";
        if(isSetup && timeSeries != null){

            String metric = getGraphiteMetricWithTags(timeSeries.getMetricName(), timeSeries.getTagKey_tagValue());

            // write data
            int count = 0;
            int counter = 0;
            for(TimeSeriesPoint point : timeSeries.getPoints()){
                // the graphite metric string to be send
                String metricToSend = metric + " " + point.getValue() + " " + (point.getTimeStamp() / 1000L) + " \n";

                try {
                    writer.write(metricToSend);
                    count++;
                    counter++;

                    if(counter == NUMBER_OF_POINTS_PER_BATCH){
                        writer.flush();
                        counter = 0;
                    }
                } catch (IOException e) {
                    logger.error("Error importing points to graphite: " + e.getLocalizedMessage());
                    return "Error importing points to graphite: " + e.getLocalizedMessage();
                }

            }
            reply = "Import of " + count + " points successful. Metric name: " + metric;

        }
        return reply;
    }

    @Override
    public GraphiteQuery getQueryObject(BenchmarkQuery benchmarkQuery) {
        GraphiteQuery graphiteQuery = null;
        if(isSetup){
            TimeSeriesMetaData metaData = benchmarkQuery.getTimeSeriesMetaData();
            QueryFunction function = benchmarkQuery.getFunction();
            String metric = getGraphiteMetricWithTags(metaData.getMetricName(), metaData.getTagKey_tagValue());

            String startDate = graphiteDateQuery(Instant.ofEpochMilli(metaData.getStart()));
            String endDate = graphiteDateQuery(Instant.ofEpochMilli(metaData.getEnd()));

            // Downsampling
            long timespan = Duration.between(Instant.ofEpochMilli(metaData.getStart()), Instant.ofEpochMilli(metaData.getEnd())).toDays() + 1;
            String aggregatedTimeSpan = timespan + "d";
/*
            //if equals or less zero we try hours
            if(timespan <= 0){
                timespan = Duration.between(Instant.ofEpochMilli(metaData.getStart()), Instant.ofEpochMilli(metaData.getEnd())).toHours() + 1;
                aggregatedTimeSpan = timespan + "h";
            }

            //if equals or less zero we try minutes
            if(timespan <= 0){
                timespan = Duration.between(Instant.ofEpochMilli(metaData.getStart()), Instant.ofEpochMilli(metaData.getEnd())).toMinutes() + 1;
                aggregatedTimeSpan = timespan + "m";
            }

            //if equals or less zero we try millis
            if(timespan <= 0){
                timespan = Duration.between(Instant.ofEpochMilli(metaData.getStart()), Instant.ofEpochMilli(metaData.getEnd())).toMillis() + 1;
                aggregatedTimeSpan = timespan + "ms";
            }

*/

            //[{"target": "summarize(cache.database.Global.win.global.metrics.srv.mmm.Prozessor.Total.Prozessorzeit.Percent.metric, \"1y\", \"stddev\")", "datapoints": [[113266.18400000047, 1419120000]]}]

            String query = "";
            String summerizeMetric = "summarize(" + metric + ",\"" + aggregatedTimeSpan + "\"";
            //String interval = "\"1y\"";

            switch (function) {
                case COUNT: query = "alias(sumSeries(offset(scale(" + metric + ", 0),1)), \"points\")";   //"integral(" + metric + ")";
                    break;
                case MEAN:  query = summerizeMetric + ", \"avg\", \"true\")";
                    break;
                case SUM:   query = summerizeMetric + ", \"sum\", \"true\")";
                    break;
                case MIN:   query = summerizeMetric +  ", \"min\", \"true\")";
                    break;
                case MAX:   query = summerizeMetric +  ", \"max\", \"true\")";
                    break;
                case STDDEV: query = "stddevSeries(" + metric + ")";
                    break;
                case PERCENTILE:
                    Float p = benchmarkQuery.getPercentile();
                    if (p != null) {
                            query = "nPercentile(" + metric + ", " + (int)(p * 100) + ")";
                    }
                    break;
                case QUERY_ONLY:
            }
            graphiteQuery = new GraphiteQuery(query, startDate, endDate);

        }
        return graphiteQuery;
    }

    @Override
    public List<String> performQuery(BenchmarkQuery benchmarkQuery, GraphiteQuery queryObject) {
        List<String> queryResults = new LinkedList<>();
        if(isSetup && queryObject != null){
            try {
                String result = graphiteClient.path("/render")
                        .queryParam("target", queryObject.getQuery())
                        .queryParam("from", queryObject.getStartDate())
                        .queryParam("until", queryObject.getEndDate())
                        .queryParam("format", "json")
                        .request(MediaType.TEXT_PLAIN)
                        .get(String.class);

                queryResults.add(result);

                //TODO EREASE: DEBUG ONLY
                queryResults.add("Graphite query string: " + queryObject.getQuery());
                queryResults.add("Graphite query startDate: " + queryObject.getStartDate());
                queryResults.add("Graphite query endDate: " + queryObject.getEndDate());

            } catch (Exception e){
                logger.error("Error performing graphite query: " + e.getLocalizedMessage());
                queryResults.add("Error performing graphite query: " + e.getLocalizedMessage());
            }
        }

        return queryResults;
    }

    private String getGraphiteMetricWithTags(String metricName, Map<String, String> tags){
        String escapedMetric = escapeGraphiteMetricName(metricName);
        StringBuilder metricBuilder = new StringBuilder();

        //add tags
        metricBuilder.append(escapeGraphiteMetricName(tags.get("host"))).append(".")
                .append(escapeGraphiteMetricName(tags.get("process"))).append(".")
                .append(escapeGraphiteMetricName(tags.get("metricGroup"))).append(".");

   /*     //add tags
        for(Map.Entry<String, String> tag : tags.entrySet()){
            metricBuilder.append(escapeGraphiteMetricName(tag.getValue())).append(".");
        }
   */
        // add metricName
        return metricBuilder.append(escapedMetric).toString();
    }


    private String escapeGraphiteMetricName(String metricName) {
        //String prefix = escape(metadata.joinWithoutMetric(), ".");
        String metric = escape(metricName, ".").replaceAll("%", "Percent").replaceAll("-", ".").replaceAll("\\.+", ".");

        //String escapedMetric = (prefix + "." + metric).replaceAll("-", ".").replaceAll("\\.+", ".");
        return metric;
    }

    private String escape(String metric, String replacement) {
        return metric.replaceAll("(\\s|\\.|:|=|,|/|\\\\|\\*|\\(|\\)|_|#)", replacement);
    }

    private String graphiteDateQuery(Instant date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date, ZoneId.systemDefault());

        StringBuilder dateString = new StringBuilder();

        dateString.append(addDateSplit(localDateTime.getHour()))
                .append(":").
                append(addDateSplit(localDateTime.getMinute())).
                append("_").
                append(localDateTime.getYear()).
                append(addDateSplit(localDateTime.getMonthValue())).
                append(addDateSplit(localDateTime.getDayOfMonth()));


        return dateString.toString();
        //localDateTime.getHour() + ":" + localDateTime.getMinute() + "_" + localDateTime.getYear() + localDateTime.getMonthValue() + localDateTime.getDayOfMonth();
    }

    private String addDateSplit(int value) {
        if (value < 10) {
            return "0" + value;
        } else {
            return "" + value;
        }
    }

    private boolean isResponding(){
        try {
            Response response = graphiteClient.path("/").request().get();
            if(response.getStatus() == 200){
                return true;
            }

        } catch (Exception e){
            logger.error("GraphiteClient not responding!");
            return false;
        }
        return false;
    }
}
