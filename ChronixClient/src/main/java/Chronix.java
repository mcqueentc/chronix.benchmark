import de.qaware.chronix.database.BenchmarkDataSource;
import de.qaware.chronix.database.TimeSeries;
import de.qaware.chronix.database.TimeSeriesMetaData;

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
    public boolean setup(String ipAddress, int portNumber) {
        return false;
    }

    @Override
    public boolean clean() {
        return false;
    }

    @Override
    public String getStorageDirectoryPath() {
        return "/opt/chronix-0.1.3/";
    }

    @Override
    public String importDataPoints(TimeSeries timeSeries) {
        return null;
    }

    @Override
    public String getQueryForFunction(TimeSeriesMetaData timeSeriesMetaData,
                                      Float percentile,
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
    public String performQuery(String query) {
        //TODO
        return query;
    }
}

