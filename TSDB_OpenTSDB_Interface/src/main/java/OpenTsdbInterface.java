import de.qaware.chronix.database.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
                //TODO wait until openTsdb is up and ready
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
        TimeSeriesMetaData timeSeriesMetaData = benchmarkQuery.getTimeSeriesMetaData();
        QueryFunction function = benchmarkQuery.getFunction();
        Map<String, String> tags = timeSeriesMetaData.getTagKey_tagValue();

        String escapedMetricName = openTSDBEscapeValue(timeSeriesMetaData.getMetricName());

        StringBuilder tagString = new StringBuilder();
        tagString.append("{");
        for(Map.Entry<String, String> tag : tags.entrySet()){
            tagString.append(tag.getKey() + "=").append(openTSDBEscapeValue(tag.getValue())).append(",");
        }
        tagString.deleteCharAt(tagString.length()-1);
        tagString.append("}");

        String startDate = opentTSDBDate(Instant.ofEpochMilli(timeSeriesMetaData.getStart()));
        String endDate = opentTSDBDate(Instant.ofEpochMilli(timeSeriesMetaData.getEnd()));

        long timespan = Duration.between(Instant.ofEpochMilli(timeSeriesMetaData.getStart()), Instant.ofEpochMilli(timeSeriesMetaData.getEnd())).toMillis();
        String aggregatedTimeSpan = timespan + "ms";


        String defaultAggregatedMetric = "";
        switch (function) {
            case COUNT:     defaultAggregatedMetric = "sum:" + aggregatedTimeSpan + "-count";
                break;
            case MEAN:      defaultAggregatedMetric = "sum:" + aggregatedTimeSpan + "-avg";
                break;
            case SUM:       defaultAggregatedMetric = "sum:" + aggregatedTimeSpan + "-sum";
                break;
            case MIN:       defaultAggregatedMetric = "sum:" + aggregatedTimeSpan + "-min";
                break;
            case MAX:       defaultAggregatedMetric = "sum:" + aggregatedTimeSpan + "-max";
                break;
            case STDDEV:    defaultAggregatedMetric = "sum:" + aggregatedTimeSpan + "-dev";
                break;
            case PERCENTILE:
                Float p = benchmarkQuery.getPercentile();
                if (p != null) {
                    if(p <= 0.5){
                        defaultAggregatedMetric = "sum:" + aggregatedTimeSpan + "-p50";
                    } else if(p > 0.5 && p <= 0.75){
                        defaultAggregatedMetric = "sum:" + aggregatedTimeSpan + "-p75";
                    } else if(p > 0.75 && p <= 0.9){
                        defaultAggregatedMetric = "sum:" + aggregatedTimeSpan + "-p90";
                    } else if(p > 0.9 && p <= 0.95){
                        defaultAggregatedMetric = "sum:" + aggregatedTimeSpan + "-p95";
                    } else {
                        defaultAggregatedMetric = "sum:" + aggregatedTimeSpan + "-p99";
                    }
                }
                break;
            case QUERY_ONLY: defaultAggregatedMetric = "sum:" + aggregatedTimeSpan;
        }

        defaultAggregatedMetric = defaultAggregatedMetric + ":" + escapedMetricName + "{tags}";


        return new OpenTsdbQuery(startDate, endDate, defaultAggregatedMetric, tagString.toString());
    }

    @Override
    public List<String> performQuery(BenchmarkQuery benchmarkQuery, Object queryObject) {
        List<String> queryResults = new LinkedList<>();
        try{
            OpenTsdbQuery query = ((OpenTsdbQuery) queryObject);
            queryResults.add(openTsdb.query(query.getStartDate(),query.getEndDate(),query.getAggregatedMetric(),query.getTagString()));

        } catch (Exception e){
            queryResults.add("OpenTSDB error performing query: " + e.getLocalizedMessage());
        }

        return queryResults;
    }

    public String openTSDBEscapeValue(String value) {
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

    public String escape(String metric, String replacement) {
        return metric.replaceAll("(\\s|\\.|:|=|,|/|\\\\|\\*|\\(|\\)|_|#)", replacement);
    }

    private String opentTSDBDate(Instant date) {
        // "2014/12/27-12:48:20";
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date, ZoneId.systemDefault());

        StringBuilder sb = new StringBuilder();

        sb.append(localDateTime.getYear())
                .append("/")
                .append(addDateSplit(localDateTime.getMonthValue()))
                .append("/")
                .append(addDateSplit(localDateTime.getDayOfMonth()))
                .append("-")
                .append(addDateSplit(localDateTime.getHour()))
                .append(":")
                .append(addDateSplit(localDateTime.getMinute()))
                .append(":")
                .append(addDateSplit(localDateTime.getSecond()));

        return sb.toString();
    }

    private String addDateSplit(int value) {
        if (value < 10) {
            return "0" + value;
        } else {
            return "" + value;
        }
    }
}
