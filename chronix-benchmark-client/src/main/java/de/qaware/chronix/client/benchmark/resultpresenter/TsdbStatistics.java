package de.qaware.chronix.client.benchmark.resultpresenter;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by mcqueen666 on 15.10.16.
 */
@XmlRootElement
public class TsdbStatistics {

    private String tsdbName;
    private int numberOfTimeSeriesPerQuery;
    private int numberOfTimeSeriesPer_QUERY_ONLY_Function;
    private int numberOfQueriesPerQueryFunction;
    private int totalNumberOfPerformedQueries;
    private int totalNumberOfPerformedImports;

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

    public int getNumberOfTimeSeriesPerQuery() {
        return numberOfTimeSeriesPerQuery;
    }

    public void setNumberOfTimeSeriesPerQuery(int numberOfTimeSeriesPerQuery) {
        this.numberOfTimeSeriesPerQuery = numberOfTimeSeriesPerQuery;
    }

    public int getNumberOfQueriesPerQueryFunction() {
        return numberOfQueriesPerQueryFunction;
    }

    public void setNumberOfQueriesPerQueryFunction(int numberOfQueriesPerQueryFunction) {
        this.numberOfQueriesPerQueryFunction = numberOfQueriesPerQueryFunction;
    }

    public int getTotalNumberOfPerformedQueries() {
        return totalNumberOfPerformedQueries;
    }

    public void setTotalNumberOfPerformedQueries(int totalNumberOfPerformedQueries) {
        this.totalNumberOfPerformedQueries = totalNumberOfPerformedQueries;
    }

    public int getTotalNumberOfPerformedImports() {
        return totalNumberOfPerformedImports;
    }

    public void setTotalNumberOfPerformedImports(int totalNumberOfPerformedImports) {
        this.totalNumberOfPerformedImports = totalNumberOfPerformedImports;
    }

    public int getNumberOfTimeSeriesPer_QUERY_ONLY_Function() {
        return numberOfTimeSeriesPer_QUERY_ONLY_Function;
    }

    public void setNumberOfTimeSeriesPer_QUERY_ONLY_Function(int numberOfTimeSeriesPer_QUERY_ONLY_Function) {
        this.numberOfTimeSeriesPer_QUERY_ONLY_Function = numberOfTimeSeriesPer_QUERY_ONLY_Function;
    }
}
