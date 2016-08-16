import de.qaware.chronix.database.BenchmarkDataSource;

import java.time.Instant;
import java.util.Map;

/**
 * Created by mcqueen666 on 08.08.16.
 *
 * Interface implementation for Chronix DB
 *
 */

//TODO after testing, link this entire module as dependency to the client!!!

public class Chronix implements BenchmarkDataSource{


    @Override
    public boolean setup(String ipAddress, int portNumber, String databaseName, String measurementName) {
        return false;
    }

    @Override
    public boolean clean(String ipAddress, int portNumber) {
        return false;
    }

    @Override
    public String importDataPoint(String ipAddress,
                                  int portNumber,
                                  String databaseName,
                                  String measurementName,
                                  String metricName,
                                  Instant timestamp,
                                  double value,
                                  Map<String, String> tagKey_tagValue) {
        return null;
    }

    @Override
    public String getQueryForFunction(String databaseName,
                                      String measurementName,
                                      String metricName,
                                      Instant start,
                                      Instant end,
                                      Map<String, String> tagKey_tagValue,
                                      float percentile,
                                      QueryFunction function) {
        String query;
        switch (function){
            case COUNT: query = "count";
                break;
            case MEAN:  query = "mean";
                break;
            case SUM:   query = "sum";
                break;
            case MIN:   query = "min";
                break;
            case MAX:   query = "max";
                break;
            case STDDEV: query = "stddev";
                break;
            case PERCENTILE: query = "percentile";
                break;
            default: query = "function not implemented";
        }

        return query;
    }

    @Override
    public String performQuery(String ipAddress, String portNumber, String query) {
        //TODO
        return query;
    }


    @Override
    public Map<Instant, Double> getQueryResult(String ipAddress,
                                               int porNumber,
                                               String databaseName,
                                               String measurementName,
                                               String metricName,
                                               Instant start,
                                               Instant end,
                                               Map<String, String> tagKey_tagValue,
                                               QueryFunction function) {
        return null;
    }
}
