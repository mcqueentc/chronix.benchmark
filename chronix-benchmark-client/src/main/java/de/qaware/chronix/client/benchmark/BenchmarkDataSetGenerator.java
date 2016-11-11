package de.qaware.chronix.client.benchmark;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.qaware.chronix.client.benchmark.benchmarkrunner.util.TimeSeriesCounter;
import de.qaware.chronix.database.TimeSeriesMetaData;
import de.qaware.chronix.common.DataModels.BenchmarkSetupVariables;
import de.qaware.chronix.common.QueryUtil.JsonTimeSeriesHandler;
import de.qaware.chronix.common.ServerConfig.ServerConfigAccessor;

import java.io.File;
import java.util.List;

/**
 * Created by mcqueen666 on 21.10.16.
 */
public class BenchmarkDataSetGenerator {
    public static void main(String[] args){
        if(args.length < 3){
            printUsage();
            return;
        }

        int BENCHMARK_TIMESERIES_METADATA_SIZE_QUERY_ONLY = 0;
        int BENCHMARK_TIMESERIES_METADATA_SIZE = 0;
        int NUMBER_OF_BENCHMARK_METADATA_LISTS = 0;

        try{
            BENCHMARK_TIMESERIES_METADATA_SIZE = Integer.valueOf(args[0]);
            NUMBER_OF_BENCHMARK_METADATA_LISTS = Integer.valueOf(args[1]);
            BENCHMARK_TIMESERIES_METADATA_SIZE_QUERY_ONLY = Integer.valueOf(args[2]);
            if(BENCHMARK_TIMESERIES_METADATA_SIZE < BENCHMARK_TIMESERIES_METADATA_SIZE_QUERY_ONLY){
                System.err.println("[timeSeries_count_per_query] must not be less than [timeSeries_count_per_rangeQuery] ");
                return;
            }
        } catch (Exception e){
            System.err.println("Not all entries are numbers!");
            return;
        }

        JsonTimeSeriesHandler jsonTimeSeriesHandler = JsonTimeSeriesHandler.getInstance();
        // should there be no benchmark meta data, generate it.
            if(! jsonTimeSeriesHandler.benchmarkMetaDataExists()){
                System.out.println("Benchmark data does not exist -> generating ... ");

            } else {
                if( ! jsonTimeSeriesHandler.deleteBenchmarkTimeSeriesMetaDataJson()){
                    System.err.println("Could not delete old benchmark data.");
                    return;
                }
                System.out.println("Existing benchmark data deleted -> generating new benchmark data ... ");
            }

        generateBenchmarkTimeSeriesMetaDataLists(NUMBER_OF_BENCHMARK_METADATA_LISTS, BENCHMARK_TIMESERIES_METADATA_SIZE);

        BenchmarkSetupVariables benchmarkSetupVariables = readBenchmarkSetupVariables();
        if(benchmarkSetupVariables != null){
            benchmarkSetupVariables.setBENCHMARK_TIMESERIES_METADATA_SIZE(BENCHMARK_TIMESERIES_METADATA_SIZE);
            benchmarkSetupVariables.setBENCHMARK_TIMESERIES_METADATA_SIZE_QUERY_ONLY(BENCHMARK_TIMESERIES_METADATA_SIZE_QUERY_ONLY);
            benchmarkSetupVariables.setNUMBER_OF_BENCHMARK_METADATA_LISTS(NUMBER_OF_BENCHMARK_METADATA_LISTS);
        } else {
            benchmarkSetupVariables = new BenchmarkSetupVariables(
                    0,
                    BENCHMARK_TIMESERIES_METADATA_SIZE_QUERY_ONLY,
                    BENCHMARK_TIMESERIES_METADATA_SIZE,
                    NUMBER_OF_BENCHMARK_METADATA_LISTS
            );
        }

        saveBenchmarkSetupVariables(benchmarkSetupVariables);
        System.out.println("Done! You may now run the benchmark.");
    }

    public static void printUsage(){
        System.out.println("Generate usage: generate [timeSeries_count_per_query] [total_count_of_queries] [timeSeries_count_per_rangeQuery]");
        System.out.println("Example: generate 400 1600 50");
    }

    private static void generateBenchmarkTimeSeriesMetaDataLists(int numberOfLists, int listSize){
        if(numberOfLists > 0 && listSize > 0){
            TimeSeriesCounter timeSeriesCounter = TimeSeriesCounter.getInstance();
            JsonTimeSeriesHandler jsonTimeSeriesHandler = JsonTimeSeriesHandler.getInstance();
            for(int i = 0; i < numberOfLists; i++){
                List<TimeSeriesMetaData> metaDataList = timeSeriesCounter.getRandomTimeSeriesMetaData(listSize);
                jsonTimeSeriesHandler.writeBenchmarkTimeSeriesMetaDataJson(metaDataList, i);
            }
        }
    }

    private static synchronized void saveBenchmarkSetupVariables(BenchmarkSetupVariables newBenchmarkSetupVariables){
        ServerConfigAccessor serverConfigAccessor = ServerConfigAccessor.getInstance();
        ObjectMapper mapper = new ObjectMapper();
        File setupFile = new File(serverConfigAccessor.getConfigDirectory() + "benchmark_setup.json");
        try {
            if(setupFile.exists()){
                BenchmarkSetupVariables benchmarkSetupVariables = mapper.readValue(setupFile, new TypeReference<BenchmarkSetupVariables>(){});
                newBenchmarkSetupVariables.setImportTimeSeriesBatchSize(benchmarkSetupVariables.getImportTimeSeriesBatchSize());
            }

            mapper.writerWithDefaultPrettyPrinter().writeValue(setupFile, newBenchmarkSetupVariables);
        } catch (Exception e) {
            System.err.println("Error saving benchmark setup file.");
        }
    }

    public static BenchmarkSetupVariables readBenchmarkSetupVariables(){
        BenchmarkSetupVariables benchmarkSetupVariables = null;
        ServerConfigAccessor serverConfigAccessor = ServerConfigAccessor.getInstance();
        ObjectMapper mapper = new ObjectMapper();
        File setupFile = new File(serverConfigAccessor.getConfigDirectory() + "benchmark_setup.json");
        try {
            if(setupFile.exists()){
                benchmarkSetupVariables = mapper.readValue(setupFile, new TypeReference<BenchmarkSetupVariables>(){});
            }
        } catch (Exception e) {
            System.err.println("Error reading benchmark setup file.");
        }
        return benchmarkSetupVariables;
    }

}