import de.qaware.chronix.database.BenchmarkDataSource;
import de.qaware.chronix.database.BenchmarkQuery;
import de.qaware.chronix.database.TimeSeries;
import org.kairosdb.client.HttpClient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

/**
 * Created by mcqueen666 on 05.09.16.
 */
public class KairosDB implements BenchmarkDataSource {

    private final String KAIROSDB_STORAGE_DIRECTORY = "/var/lib/cassandra";
    private String ipAddress;
    private int portNumber;
    private boolean isSetup = false;
    HttpClient kairosdb;
    private String dbName = "chronixBenchmark";


    @Override
    public boolean setup(String ipAddress, int portNumber) {
        if(!isSetup){
            try {
                HttpClient client = new HttpClient("http://" + ipAddress + ":" + portNumber);
                // test if client is responding
                int retryCount = Integer.MIN_VALUE;
                retryCount = client.getRetryCount();
                if( retryCount != Integer.MIN_VALUE){
                    this.ipAddress = ipAddress;
                    this.portNumber = portNumber;
                    this.kairosdb = client;
                    isSetup = true;
                }

            } catch (Exception e) {
                System.err.println("KairosDB Interface: " + e.getLocalizedMessage());
                isSetup = false;
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
        try {
            kairosdb.shutdown();
        } catch (IOException e) {
            System.err.println("KairosDB Interface shutdown: " + e.getLocalizedMessage());
        }
    }

    @Override
    public String getStorageDirectoryPath() {
        return KAIROSDB_STORAGE_DIRECTORY;
    }

    @Override
    public String getQueryString(BenchmarkQuery benchmarkQuery) {
        return null;
    }

    @Override
    public String importDataPoints(TimeSeries timeSeries) {
        return null;
    }

    @Override
    public List<String> performQuery(BenchmarkQuery benchmarkQuery, String queryString) {
        return null;
    }
}
