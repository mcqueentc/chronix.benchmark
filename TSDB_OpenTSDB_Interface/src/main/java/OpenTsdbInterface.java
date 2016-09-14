import de.qaware.chronix.database.*;
import de.qaware.chronix.database.util.DockerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Created by mcqueen666 on 06.09.16.
 */
public class OpenTsdbInterface implements BenchmarkDataSource<OpenTsdbQuery> {
    private final String OPENTSDB_STORAGE_DIRECTORY = "/tmp/hadoop-root/dfs/";
    private final int OPENTSDB_NUMBER_OF_POINTS_PER_BATCH = 10;
    private final int WAIT_TIME_SLICE = 250;
    private final int MAX_WAIT_TIME = 180_000;
    private final Logger logger = LoggerFactory.getLogger(OpenTsdbInterface.class);
    private String ipAddress;
    private int portNumber;
    private boolean isSetup = false;
    private String dbName = "chronixBenchmark";
    OpenTsdb openTsdb;
    private boolean dimensionsSet = false;


    @Override
    public boolean setup(String ipAddress, int portNumber) {
        if(!isSetup){
            try {
                openTsdb = new OpenTsdb.Builder("http://" + ipAddress + ":" + portNumber).create();

                int elapsedTime = 0;
                //wait until openTsdb is up and ready
                while(!openTsdb.isResponding() && elapsedTime < MAX_WAIT_TIME){
                    if(elapsedTime % 10000 == 0) {
                        logger.info("OpenTsdb is setting up ... waiting ... {}s", elapsedTime / 1000);
                    }

                    Thread.sleep(WAIT_TIME_SLICE);
                    elapsedTime += WAIT_TIME_SLICE;
                }


               // openTsdb.setBatchSizeLimit(NUMBER_OF_POINTS_PER_BATCH);
                // openTsdb cannot handle many points at once (o.0)
                openTsdb.setBatchSizeLimit(OPENTSDB_NUMBER_OF_POINTS_PER_BATCH);

                this.ipAddress = ipAddress;
                this.portNumber = portNumber;
                if(elapsedTime < MAX_WAIT_TIME) {
                    isSetup = true;
                } else {
                    isSetup = false;
                    logger.error("OpenTsdb max setup time exceeded.");
                }

            } catch (Exception e){
                isSetup = false;
                logger.error("Error OpenTSDB setup: " + e.getLocalizedMessage());
            }

        }
        return isSetup;
    }

    @Override
    public boolean clean() {
        DockerUtil dockerUtil = new DockerUtil();
        List<String> result = dockerUtil.executeCommandOnDockerContainer("opentsdb", "./opt/cleanseDatabase.sh");
        if(!result.isEmpty()){
            logger.info("OpenTsdb: Performing command on docker container: {}", result);
            return true;
        }
        logger.error("OpenTsdb: Error performing command on docker.");
        return false;
    }

    @Override
    public void shutdown() {
        openTsdb.close();
    }

    @Override
    public String getStorageDirectoryPath() {
        return OPENTSDB_STORAGE_DIRECTORY;
    }

    @Override
    public void writeCachesToDisk(){
       /* logger.info("OpenTsdb: writing caches to disk ...");
        DockerUtil dockerUtil = new DockerUtil();
        List<String> result = dockerUtil.executeCommandOnDockerContainer("opentsdb", "./opt/forceWritingOnDisk.sh");
        logger.info("OpenTsdb: Result for writing caches to disk: {}",result);*/

    }

    @Override
    public String importDataPoints(TimeSeries timeSeries) {
        String reply = "Error importing data points to openTsdb.";
        if(isSetup && timeSeries != null) {
            Map<String, String> tags = timeSeries.getTagKey_tagValue();
            if(!dimensionsSet) {
                try {
                    openTsdb.preAssignDimensions(tags.keySet());
                    dimensionsSet = true;
                } catch (Exception e) {
                    logger.info("OpenTSDB: Dimensions already set.");
                }
            }
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
                logger.error("Error importing data points to openTsdb: " + e.getLocalizedMessage());
                // TODO DEBUG remove!
                reply = "Error importing data points to openTsdb: " + e.getLocalizedMessage();
            }


        }
        return reply;
    }

    @Override
    public OpenTsdbQuery getQueryObject(BenchmarkQuery benchmarkQuery) {
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

        // millisecond precision
        String startDate = openTsdbTimeString(timeSeriesMetaData.getStart());
        String endDate = openTsdbTimeString(timeSeriesMetaData.getEnd());

/*
        // Downsampling
        long timespan = Duration.between(Instant.ofEpochMilli(timeSeriesMetaData.getStart()), Instant.ofEpochMilli(timeSeriesMetaData.getEnd())).toDays();
        String aggregatedTimeSpan = timespan + "d";

        //if equals or less zero we try hours
        if(timespan <= 0){
            timespan = Duration.between(Instant.ofEpochMilli(timeSeriesMetaData.getStart()), Instant.ofEpochMilli(timeSeriesMetaData.getEnd())).toHours();
            aggregatedTimeSpan = timespan + "h";
        }

        //if equals or less zero we try minutes
        if(timespan <= 0){
            timespan = Duration.between(Instant.ofEpochMilli(timeSeriesMetaData.getStart()), Instant.ofEpochMilli(timeSeriesMetaData.getEnd())).toMinutes();
            aggregatedTimeSpan = timespan + "m";
        }

        //if equals or less zero we try millis
        if(timespan <= 0){
            timespan = Duration.between(Instant.ofEpochMilli(timeSeriesMetaData.getStart()), Instant.ofEpochMilli(timeSeriesMetaData.getEnd())).toMillis();
            aggregatedTimeSpan = timespan + "ms";
        }
*/

        String aggregatedTimeSpan = "1ms"; // full resolution
        String defaultAggregatedMetric = "";
        switch (function) {
            case COUNT:     defaultAggregatedMetric = "count:" + aggregatedTimeSpan + "-count";
                break;
            case MEAN:      defaultAggregatedMetric = "avg:" + aggregatedTimeSpan + "-avg";
                break;
            case SUM:       defaultAggregatedMetric = "sum:" + aggregatedTimeSpan + "-sum";
                break;
            case MIN:       defaultAggregatedMetric = "min:" + aggregatedTimeSpan + "-min";
                break;
            case MAX:       defaultAggregatedMetric = "max:" + aggregatedTimeSpan + "-max";
                break;
            case STDDEV:    defaultAggregatedMetric = "dev:" + aggregatedTimeSpan + "-dev";
                break;
            case PERCENTILE:
                Float p = benchmarkQuery.getPercentile();
                if (p != null) {
                    if(p <= 0.5){
                        defaultAggregatedMetric = "p50:" + aggregatedTimeSpan + "-p50";
                    } else if(p > 0.5 && p <= 0.75){
                        defaultAggregatedMetric = "p75:" + aggregatedTimeSpan + "-p75";
                    } else if(p > 0.75 && p <= 0.9){
                        defaultAggregatedMetric = "p90:" + aggregatedTimeSpan + "-p90";
                    } else if(p > 0.9 && p <= 0.95){
                        defaultAggregatedMetric = "p95:" + aggregatedTimeSpan + "-p95";
                    } else {
                        defaultAggregatedMetric = "p99:" + aggregatedTimeSpan + "-p99";
                    }
                }
                break;
            case QUERY_ONLY: defaultAggregatedMetric = "sum:" + aggregatedTimeSpan;
        }

        defaultAggregatedMetric = defaultAggregatedMetric + ":" + escapedMetricName + "{tags}";


        return new OpenTsdbQuery(startDate, endDate, defaultAggregatedMetric, tagString.toString());
    }

    @Override
    public List<String> performQuery(BenchmarkQuery benchmarkQuery, OpenTsdbQuery queryObject) {
        List<String> queryResults = new LinkedList<>();
        if(isSetup) {
            try {
                String result = openTsdb.query(queryObject.getStartDate(), queryObject.getEndDate(), queryObject.getAggregatedMetric(), queryObject.getTagString());
                queryResults.add(result);

                // TODO erase, only for debug
                queryResults.add("OpenTsdb aggregatedMetric: " + queryObject.getAggregatedMetric());
                queryResults.add("OpenTsdb tagString: " + queryObject.getTagString());
                queryResults.add("OpenTsdb time range: " + queryObject.getStartDate() + " -> " + queryObject.getEndDate());
                queryResults.add("OpenTsdb number of data points: " + getDataPointCount(result) + "\n");
            } catch (Exception e) {
                logger.error("OpenTSDB error performing query: " + e.getLocalizedMessage());
                // TODO erase, only for debug
                queryResults.add("OpenTSDB error performing query: " + e.getLocalizedMessage());
            }

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

    private String openTsdbTimeString(Long epochMillis){
        // first 10 or less digits are treated as seconds, milliseconds only with 3 digits precision.
        String result = epochMillis.toString();
        if(result.length() >= 10){
            String seconds = result.substring(0, 10);
            String millis = result.substring(10);

            if(millis.length() > 0 && millis.length() <= 3){
                result = seconds + "." + millis;
            }
            else if (millis.length() > 3){
                result = seconds + "." + millis.substring(0,3);
            }
        }

        return result;
    }

    private String addDateSplit(int value) {
        if (value < 10) {
            return "0" + value;
        } else {
            return "" + value;
        }
    }

    private int getDataPointCount(String openTsdbResultString){
        String result = openTsdbResultString.substring(openTsdbResultString.indexOf("dps"));
        String[] splits = result.split(",");
        return splits.length;
    }
}
