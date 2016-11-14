package de.qaware.chronix.common.QueryUtil;

import de.qaware.chronix.database.TimeSeries;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by mcqueen666 on 29.08.16.
 */
@XmlRootElement
public class ImportRecord extends BenchmarkRecord{

    List<TimeSeries> timeSeriesList;

    public ImportRecord(){ super();}

    public ImportRecord(String queryID, String ipAddress, String portNumber, String tsdbName, List<TimeSeries> timeSeriesList) {
        super(queryID, ipAddress, portNumber, tsdbName);
        this.timeSeriesList = timeSeriesList;
    }

    public ImportRecord(BenchmarkRecord benchmarkRecord, List<TimeSeries> timeSeriesList) {
        super(benchmarkRecord);
        this.timeSeriesList = timeSeriesList;
    }

    public List<TimeSeries> getTimeSeriesList() {
        return timeSeriesList;
    }

    public void setTimeSeriesList(List<TimeSeries> timeSeriesList) {
        this.timeSeriesList = timeSeriesList;
    }
}
