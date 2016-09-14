package Client;

import de.qaware.chronix.client.benchmark.configurator.Configurator;
import de.qaware.chronix.client.benchmark.queryhandler.QueryHandler;
import de.qaware.chronix.client.benchmark.queryhandler.util.JsonTimeSeriesHandler;
import de.qaware.chronix.database.BenchmarkDataSource;
import de.qaware.chronix.database.TimeSeries;
import de.qaware.chronix.database.TimeSeriesMetaData;
import de.qaware.chronix.shared.QueryUtil.ImportRecord;
import de.qaware.chronix.shared.ServerConfig.ServerConfigAccessor;
import de.qaware.chronix.shared.ServerConfig.ServerConfigRecord;
import de.qaware.chronix.shared.ServerConfig.TSDBInterfaceHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mcqueen666 on 31.08.16.
 */
public class ImportTest {

    public static void importTimeSeries(List<TimeSeries> checktimeSeriesList, String queryID) {

        Configurator configurator = Configurator.getInstance();
        String server = "localhost";

        System.out.println("\n###### Client.ImportTest.importTimesSeries ######");

        if (configurator.isServerUp(server)) {
            System.out.println("Server is up");
        } else {
            System.out.println("Server not responding");
        }
        // import test
        ServerConfigAccessor serverConfigAccessor = ServerConfigAccessor.getInstance();
        LinkedList<ServerConfigRecord> readRecord = serverConfigAccessor.getServerConfigRecords();
        TSDBInterfaceHandler interfaceHandler = TSDBInterfaceHandler.getInstance();
        QueryHandler queryHandler = QueryHandler.getInstance();

        if (!checktimeSeriesList.isEmpty()) {

            for (ServerConfigRecord r : readRecord) {
                LinkedList<String> externalImpls = r.getExternalTimeSeriesDataBaseImplementations();
                for (String externalImpl : externalImpls) {
                        String ip = r.getServerAddress();
                        String port = serverConfigAccessor.getHostPortForTSDB(ip, externalImpl);

                        ImportRecord importRecord = new ImportRecord(queryID, ip, port, externalImpl, checktimeSeriesList);
                        String[] results = queryHandler.doImportOnServer(ip, importRecord);
                        Long latency = queryHandler.getLatencyForQueryID(queryID);
                        if (latency != null) {
                            System.out.println("\nTSDB: " + externalImpl);
                            System.out.println("QueryID: " + queryID);
                            System.out.println("Latency: " + latency + " milliseconds");
                            for (String result : results) {
                                System.out.println("Result: " + result);
                            }
                        } else {
                            System.out.println("Error: " + results[0]);
                        }
                }
            }
        }
    }

    public static void importTimeSeriesHeavy(){
        JsonTimeSeriesHandler jsonTimeSeriesHandler = JsonTimeSeriesHandler.getInstance();
        List<File> directories = new ArrayList<>();
        directories.add(new File("/Users/mcqueen666/chronixBenchmark/timeseries_records/air-lasttest"));
        //directories.add(new File("/Users/mcqueen666/chronixBenchmark/timeseries_records/shd"));
        //directories.add(new File("/Users/mcqueen666/chronixBenchmark/timeseries_records/promt"));


        for(File directory : directories){
            if(directory.exists()){
                File[] files = directory.listFiles();
                if(files != null) {
                    List<File> fileList = new ArrayList<>();
                    // as if was not imported previously
                    jsonTimeSeriesHandler.deleteTimeSeriesMetaDataJsonFile(directory.getName());

                    for (int i = 0; i < files.length; i++) {
                        if (i != 0 && i % 500 == 0) {
                            //read timeseries from json
                            List<TimeSeries> timeSeries = jsonTimeSeriesHandler.readTimeSeriesJson(fileList.toArray(new File[]{}));
                            fileList.clear();
                            System.out.println("Number of TimeSeries to import: " + timeSeries.size());
                            System.out.println("Number of TimeSeries left: " + (files.length - i));
                            // import to tsdbs
                            String queryID = directory.getName() + ":" + i;
                            ImportTest.importTimeSeries(timeSeries, queryID);
                            // generate meta data
                            jsonTimeSeriesHandler.writeTimeSeriesMetaDataJson(timeSeries);
                        }

                        fileList.add(files[i]);
                    }
                    //read timeseries from json
                    List<TimeSeries> timeSeries = jsonTimeSeriesHandler.readTimeSeriesJson(fileList.toArray(new File[]{}));
                    fileList.clear();
                    System.out.println("Number of TimesSeries to import: " + timeSeries.size());
                    // import to tsdbs
                    String queryID = directory.getName() + ":" + files.length;
                    ImportTest.importTimeSeries(timeSeries, queryID);
                    // generate meta data
                    jsonTimeSeriesHandler.writeTimeSeriesMetaDataJson(timeSeries);
                }
            }
        }
    }

    public static List<TimeSeriesMetaData> importNumberOfTimeSeries(int number){
        JsonTimeSeriesHandler jsonTimeSeriesHandler = JsonTimeSeriesHandler.getInstance();
        List<File> directories = new ArrayList<>();
        directories.add(new File("/Users/mcqueen666/chronixBenchmark/timeseries_records/air-lasttest"));
        //directories.add(new File("/Users/mcqueen666/chronixBenchmark/timeseries_records/shd"));
        //directories.add(new File("/Users/mcqueen666/chronixBenchmark/timeseries_records/promt"));

        List<TimeSeriesMetaData> importedTimeSeriesMetaData = new ArrayList<>();
        for(File directory : directories){
            if(directory.exists()){
                File[] files = directory.listFiles();
                if(files != null) {
                    List<File> fileList = new ArrayList<>();
                    // as if was not imported previously
                    jsonTimeSeriesHandler.deleteTimeSeriesMetaDataJsonFile(directory.getName());

                    for (int i = 0; i < number; i++) {
                        if (i != 0 && i % 500 == 0) {
                            //read timeseries from json
                            List<TimeSeries> timeSeries = jsonTimeSeriesHandler.readTimeSeriesJson(fileList.toArray(new File[]{}));
                            fileList.clear();
                            System.out.println("Number of TimeSeries to import: " + timeSeries.size());
                            System.out.println("Number of TimeSeries left: " + (files.length - i));
                            // import to tsdbs
                            String queryID = "import:" + directory.getName() + ":" + i;
                            ImportTest.importTimeSeries(timeSeries, queryID);
                            // generate meta data
                            importedTimeSeriesMetaData.addAll(jsonTimeSeriesHandler.writeTimeSeriesMetaDataJson(timeSeries));
                        }

                        fileList.add(files[i]);
                    }
                    //read timeseries from json
                    List<TimeSeries> timeSeries = jsonTimeSeriesHandler.readTimeSeriesJson(fileList.toArray(new File[]{}));
                    fileList.clear();
                    System.out.println("Number of TimesSeries to import: " + timeSeries.size());
                    for(TimeSeries ts : timeSeries){
                        System.out.println("Measurement: "+ ts.getMeasurementName() + " -> Metric name to be imported: " + ts.getMetricName());
                    }
                    // import to tsdbs
                    String queryID = "import:" + directory.getName() + ":" + number;
                    ImportTest.importTimeSeries(timeSeries, queryID);
                    // generate meta data
                    importedTimeSeriesMetaData.addAll(jsonTimeSeriesHandler.writeTimeSeriesMetaDataJson(timeSeries));
                }
            }
        }
        return importedTimeSeriesMetaData;
    }



}

