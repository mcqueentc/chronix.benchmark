import de.qaware.chronix.database.BenchmarkDataSource;
import de.qaware.chronix.database.BenchmarkQuery;
import de.qaware.chronix.database.TimeSeries;
import org.influxdb.InfluxDBFactory;

import java.util.List;

/**
 * Created by mcqueen666 on 01.09.16.
 */
public class InfluxDB implements BenchmarkDataSource {

    private final String INFLUXDB_STORAGE_DIRECTORY = "/var/lib/influxdb/";
    private String ipAddress;
    private int portNumber;
    private boolean isSetup = false;
    private org.influxdb.InfluxDB influxDB;
    private String dbName = "chronixBenchmark";


    @Override
    public boolean setup(String ipAddress, int portNumber) {
        influxDB = InfluxDBFactory.connect("http://" + ipAddress + ":" + portNumber, "root", "root");
        if(influxDB != null){
            influxDB.createDatabase(dbName);
            isSetup = true;
            return isSetup;
        }
        return false;
    }

    @Override
    public boolean clean() {
        influxDB.deleteDatabase(dbName);
        return true;
    }

    @Override
    public String getStorageDirectoryPath() {
        return INFLUXDB_STORAGE_DIRECTORY;
    }

    @Override
    public String importDataPoints(TimeSeries timeSeries) {

        return null;
    }

    @Override
    public List<String> performQuery(BenchmarkQuery benchmarkQuery) {
        return null;
    }
}
