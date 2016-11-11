package de.qaware.chronix.common.DataModels;

/**
 * Created by mcqueen666 on 21.10.16.
 */
public class BenchmarkSetupVariables {

    private int importTimeSeriesBatchSize;
    private int BENCHMARK_TIMESERIES_METADATA_SIZE_QUERY_ONLY;
    private int BENCHMARK_TIMESERIES_METADATA_SIZE;
    private int NUMBER_OF_BENCHMARK_METADATA_LISTS;

    public BenchmarkSetupVariables() {
    }

    public BenchmarkSetupVariables(int importTimeSeriesBatchSize,
                                   int BENCHMARK_TIMESERIES_METADATA_SIZE_QUERY_ONLY,
                                   int BENCHMARK_TIMESERIES_METADATA_SIZE,
                                   int NUMBER_OF_BENCHMARK_METADATA_LISTS)
    {
        this.importTimeSeriesBatchSize = importTimeSeriesBatchSize;
        this.BENCHMARK_TIMESERIES_METADATA_SIZE_QUERY_ONLY = BENCHMARK_TIMESERIES_METADATA_SIZE_QUERY_ONLY;
        this.BENCHMARK_TIMESERIES_METADATA_SIZE = BENCHMARK_TIMESERIES_METADATA_SIZE;
        this.NUMBER_OF_BENCHMARK_METADATA_LISTS = NUMBER_OF_BENCHMARK_METADATA_LISTS;
    }

    public int getImportTimeSeriesBatchSize() {
        return importTimeSeriesBatchSize;
    }

    public void setImportTimeSeriesBatchSize(int importTimeSeriesBatchSize) {
        this.importTimeSeriesBatchSize = importTimeSeriesBatchSize;
    }

    public int getBENCHMARK_TIMESERIES_METADATA_SIZE_QUERY_ONLY() {
        return BENCHMARK_TIMESERIES_METADATA_SIZE_QUERY_ONLY;
    }

    public void setBENCHMARK_TIMESERIES_METADATA_SIZE_QUERY_ONLY(int BENCHMARK_TIMESERIES_METADATA_SIZE_QUERY_ONLY) {
        this.BENCHMARK_TIMESERIES_METADATA_SIZE_QUERY_ONLY = BENCHMARK_TIMESERIES_METADATA_SIZE_QUERY_ONLY;
    }

    public int getBENCHMARK_TIMESERIES_METADATA_SIZE() {
        return BENCHMARK_TIMESERIES_METADATA_SIZE;
    }

    public void setBENCHMARK_TIMESERIES_METADATA_SIZE(int BENCHMARK_TIMESERIES_METADATA_SIZE) {
        this.BENCHMARK_TIMESERIES_METADATA_SIZE = BENCHMARK_TIMESERIES_METADATA_SIZE;
    }

    public int getNUMBER_OF_BENCHMARK_METADATA_LISTS() {
        return NUMBER_OF_BENCHMARK_METADATA_LISTS;
    }

    public void setNUMBER_OF_BENCHMARK_METADATA_LISTS(int NUMBER_OF_BENCHMARK_METADATA_LISTS) {
        this.NUMBER_OF_BENCHMARK_METADATA_LISTS = NUMBER_OF_BENCHMARK_METADATA_LISTS;
    }
}
