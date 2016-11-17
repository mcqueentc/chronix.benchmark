package de.qaware.chronix.client.benchmark;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.qaware.chronix.client.benchmark.benchmarkrunner.BenchmarkRunner;
import de.qaware.chronix.common.DataModels.BenchmarkSetupVariables;
import de.qaware.chronix.common.QueryUtil.JsonTimeSeriesHandler;
import de.qaware.chronix.common.ServerConfig.ServerConfigAccessor;
import de.qaware.chronix.common.ServerConfig.ServerConfigRecord;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mcqueen666 on 07.10.16.
 */
public class BenchmarkImport {
    public static void main(String[] args){
        if(args.length < 7){
            printImportUsage();
            return;
        }

        List<String> tsdbImportList = new LinkedList<>();
        List<File> importDirectories = new LinkedList<>();
        String server = args[0];
        int batchSize;
        int fromFile;
        try{
            batchSize = Integer.valueOf(args[1]);
            fromFile = Integer.valueOf(args[2]);

            for(int i = 3; i < args.length - 1; i = i + 2 ){
                if(args[i].equals("-t")){
                    tsdbImportList.add(args[i+1]);
                }

                if(args[i].equals("-d")){
                    File dir = new File(args[i+1]);
                    if(dir.exists() && dir.isDirectory()){
                        importDirectories.add(dir);
                    }
                }
            }
        } catch (Exception e){
            System.err.println("Error processing your entries: " + e.getLocalizedMessage());
            return;
        }

        // print info for user
        System.out.println("Recognized entries: \n");
        System.out.println("Server:     " + server);
        System.out.println("BatchSize:  " + batchSize);
        System.out.println("fromFile:   " + fromFile);
        tsdbImportList.forEach(s -> System.out.println("TSDB:       " + s));
        importDirectories.forEach(dir -> System.out.println("Directory: " + dir.getAbsolutePath()));

        //check if server is configured
        ServerConfigAccessor serverConfigAccessor = ServerConfigAccessor.getInstance();
        ServerConfigRecord serverConfig = serverConfigAccessor.getServerConfigRecord(server);
        if(serverConfig == null){
            System.err.println("Server: " + server + " was not configured.");
            return;
        }

        // check if tsdb interface is imported.
        LinkedList<String> configuredTsdbImpls = serverConfig.getExternalTimeSeriesDataBaseImplementations();
        List<String> removeTsdbList = new LinkedList<>();
        for(String tsdb : tsdbImportList) {
            if(!configuredTsdbImpls.contains(tsdb)){
                removeTsdbList.add(tsdb);
                System.err.println("No interface found for: " + tsdb + " -> rejected!");
            }
        }
        tsdbImportList.removeAll(removeTsdbList);

        if(tsdbImportList.isEmpty()){
            System.out.println("No Tsdb given for import");
            return;
        }
        if(importDirectories.isEmpty()){
            System.out.println("No directory given for import");
            return;
        }

        try {
            saveBatchSize(batchSize);
            BenchmarkRunner benchmarkRunner = BenchmarkRunner.getInstance();
            for (File directory : importDirectories) {
                JsonTimeSeriesHandler.getInstance().deleteTimeSeriesMetaDataJsonFile(directory.getName());
                System.out.println("\nImporting directory: " + directory);
                //benchmarkRunner.importTimeSeriesFromDirectory(server, directory, batchSize, fromFile, tsdbImportList);
                benchmarkRunner.importTimesSeriesWithUploadedFiles(server, directory, batchSize, fromFile, tsdbImportList);
            }

            if(benchmarkRunner.getBenchmarkRecordsFromServer(server)){
                System.out.println("\nDownloading benchmark records from server successful.");
                benchmarkRunner.deleteBenchmarkRecordsOnServer(server);
            } else {
                System.out.println("\nDownloading benchmark records from server was not successful.");
            }



        } catch (Exception e){
            System.err.println("Error importing on " + server + ": " + e.getLocalizedMessage());
        }

    }

    private static void printImportUsage(){
        System.out.println("Import usage:   import [server] [batchSize] [fromFile] -t [tsdbName1] -t [tsdbName2] ... -d [directoryToImport1] -d [directoryToImport2] ...");
        System.out.println("Example:        import localhost 25 0 -t someTsdb -d /home/someUser/chronixBenchmark/timeseries_records/someFolder");
        System.out.println("NOTICE: paths have to be absolute paths!");
    }

    private static synchronized void saveBatchSize(int batchSize){
        ServerConfigAccessor serverConfigAccessor = ServerConfigAccessor.getInstance();
        ObjectMapper mapper = new ObjectMapper();
        File setupFile = new File(serverConfigAccessor.getConfigDirectory() + "benchmark_setup.json");

        try {
            BenchmarkSetupVariables benchmarkSetupVariables = new BenchmarkSetupVariables();
            if(setupFile.exists()){
                benchmarkSetupVariables = mapper.readValue(setupFile, new TypeReference<BenchmarkSetupVariables>(){});
            }
            benchmarkSetupVariables.setImportTimeSeriesBatchSize(batchSize);

            mapper.writerWithDefaultPrettyPrinter().writeValue(setupFile, benchmarkSetupVariables);
        } catch (Exception e) {
            System.err.println("Error saving file for batch size.");
        }
    }

    public static String readBatchSize(){
        String batchSize = "";
        ServerConfigAccessor serverConfigAccessor = ServerConfigAccessor.getInstance();
        ObjectMapper mapper = new ObjectMapper();
        File setupFile = new File(serverConfigAccessor.getConfigDirectory() + "benchmark_setup.json");
        try {
            if(setupFile.exists()){
                BenchmarkSetupVariables benchmarkSetupVariables = mapper.readValue(setupFile, new TypeReference<BenchmarkSetupVariables>(){});
                batchSize = String.valueOf(benchmarkSetupVariables.getImportTimeSeriesBatchSize());
            }
        } catch (Exception e) {
            System.err.println("Error reading file for batch size.");
        }
        return batchSize;
    }

}
