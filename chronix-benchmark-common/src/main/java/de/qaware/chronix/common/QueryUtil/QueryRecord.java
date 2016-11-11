package de.qaware.chronix.common.QueryUtil;

import de.qaware.chronix.database.BenchmarkQuery;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by mcqueen666 on 29.08.16.
 */
@XmlRootElement
public class QueryRecord extends BenchmarkRecord {

    private List<BenchmarkQuery> queryList;

    public QueryRecord(){super();}

    public QueryRecord(String queryID, String ipAddress, String portNumber, String tsdbName, List<BenchmarkQuery> queryList) {
        super(queryID, ipAddress, portNumber, tsdbName);
        this.queryList = queryList;
    }

    public QueryRecord(BenchmarkRecord benchmarkRecord, List<BenchmarkQuery> queryList) {
        super(benchmarkRecord);
        this.queryList = queryList;
    }

    public List<BenchmarkQuery> getQueryList() {
        return queryList;
    }

    public void setQueryList(List<BenchmarkQuery> queryList) {
        this.queryList = queryList;
    }
}
