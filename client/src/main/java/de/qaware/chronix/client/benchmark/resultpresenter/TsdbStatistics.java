package de.qaware.chronix.client.benchmark.resultpresenter;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by mcqueen666 on 15.10.16.
 */
@XmlRootElement
public class TsdbStatistics {

    private String tsdbName;
    private List<QueryFunctionStatistics> queryFunctionStatisticsList;

    public TsdbStatistics(String tsdbName, List<QueryFunctionStatistics> queryFunctionStatisticsList) {
        this.tsdbName = tsdbName;
        this.queryFunctionStatisticsList = queryFunctionStatisticsList;
    }

    public TsdbStatistics() {
    }

    public String getTsdbName() {
        return tsdbName;
    }

    public void setTsdbName(String tsdbName) {
        this.tsdbName = tsdbName;
    }

    public List<QueryFunctionStatistics> getQueryFunctionStatisticsList() {
        return queryFunctionStatisticsList;
    }

    public void setQueryFunctionStatisticsList(List<QueryFunctionStatistics> queryFunctionStatisticsList) {
        this.queryFunctionStatisticsList = queryFunctionStatisticsList;
    }
}
