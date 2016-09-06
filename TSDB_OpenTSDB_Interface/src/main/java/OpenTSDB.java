import de.qaware.chronix.database.BenchmarkDataSource;
import de.qaware.chronix.database.BenchmarkQuery;
import de.qaware.chronix.database.TimeSeries;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by mcqueen666 on 06.09.16.
 */
public class OpenTSDB implements BenchmarkDataSource {


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
        return new Object();
    }

    @Override
    public String importDataPoints(TimeSeries timeSeries) {
        return "";
    }

    @Override
    public List<String> performQuery(BenchmarkQuery benchmarkQuery, Object queryObject) {
        List<String> queryResults = new LinkedList<>();
        return queryResults;
    }

    public static String openTSDBEscapeValue(String value) {
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

    public static String escape(String metric, String replacement) {
        return metric.replaceAll("(\\s|\\.|:|=|,|/|\\\\|\\*|\\(|\\)|_|#)", replacement);
    }
}
