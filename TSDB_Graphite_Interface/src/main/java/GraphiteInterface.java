import de.qaware.chronix.database.BenchmarkDataSource;
import de.qaware.chronix.database.BenchmarkQuery;
import de.qaware.chronix.database.TimeSeries;

import java.util.List;

/**
 * Created by mcqueen666 on 08.09.16.
 */
public class GraphiteInterface implements BenchmarkDataSource {
    private final String GRAPHITE_STORAGE_DIRECTORY = "/opt/graphite/storage";

    @Override
    public boolean setup(String ipAddress, int portNumber) {
        return false;
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
        return null;
    }

    @Override
    public Object getQueryObject(BenchmarkQuery benchmarkQuery) {
        return null;
    }

    @Override
    public String importDataPoints(TimeSeries timeSeries) {
        return null;
    }

    @Override
    public List<String> performQuery(BenchmarkQuery benchmarkQuery, Object queryObject) {
        return null;
    }
}
