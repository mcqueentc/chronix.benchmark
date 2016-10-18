package de.qaware.chronix.client.benchmark.resultpresenter.plot;

import de.qaware.chronix.client.benchmark.BenchmarkImport;
import de.qaware.chronix.client.benchmark.benchmarkrunner.BenchmarkRunner;
import de.qaware.chronix.client.benchmark.resultpresenter.QueryFunctionStatistics;
import de.qaware.chronix.client.benchmark.resultpresenter.TsdbStatistics;
import de.qaware.chronix.client.benchmark.resultpresenter.TsdbStatisticsAnalyzer;
import de.qaware.chronix.database.BenchmarkDataSource;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jfree.data.category.DefaultCategoryDataset;


import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by mcqueen666 on 17.10.16.
 */
public class StatisticsBarPlotter {

    private final Logger logger;
    private final int CHART_WIDTH = 1920;
    private final int CHART_HEIGHT = 1080;
    private final String statisticsBarPlotDirectory;
    private final List<TsdbStatistics> tsdbStatisticsList;

    public StatisticsBarPlotter(String statisticsDirectory){
        logger = LoggerFactory.getLogger(StatisticsBarPlotter.class);
        this.statisticsBarPlotDirectory = statisticsDirectory + File.separator + "bar_plots";
        this.tsdbStatisticsList = new TsdbStatisticsAnalyzer(statisticsDirectory).readTsdbStatistics();

        File dir = new File(statisticsBarPlotDirectory);
        if(!dir.exists()){
            dir.mkdirs();
        }
    }

    public void plotTsdbStatisticsForQueryFunctions(List<String> includeQueryFunctions){
        if(includeQueryFunctions != null && !includeQueryFunctions.isEmpty()) {
            logger.info("Plotting for " + String.join(",", includeQueryFunctions) + " ...");
            Map<String, List<PlotData>> plotDataPerMeasurement = new HashMap<>();
            // query time
            plotDataPerMeasurement.put("mean query time", new LinkedList<>());
            plotDataPerMeasurement.put("median query time", new LinkedList<>());
            plotDataPerMeasurement.put("total query time", new LinkedList<>());

            // cpu usage
            plotDataPerMeasurement.put("mean total cpu usage", new LinkedList<>());
            plotDataPerMeasurement.put("median total cpu usage", new LinkedList<>());
            plotDataPerMeasurement.put("maximum recorded cpu usage", new LinkedList<>());

            // disk usage
            plotDataPerMeasurement.put("mean disk usage", new LinkedList<>());
            plotDataPerMeasurement.put("median disk usage", new LinkedList<>());
            plotDataPerMeasurement.put("maximum recorded disk usage", new LinkedList<>());

            // memory usage
            plotDataPerMeasurement.put("mean total memory usage", new LinkedList<>());
            plotDataPerMeasurement.put("median total memory usage", new LinkedList<>());
            plotDataPerMeasurement.put("maximum recorded memory usage", new LinkedList<>());

            // disk write
            plotDataPerMeasurement.put("mean disk write", new LinkedList<>());
            plotDataPerMeasurement.put("median disk write", new LinkedList<>());
            plotDataPerMeasurement.put("total disk write", new LinkedList<>());

            // disk read
            plotDataPerMeasurement.put("mean disk read", new LinkedList<>());
            plotDataPerMeasurement.put("median disk read", new LinkedList<>());
            plotDataPerMeasurement.put("total disk read", new LinkedList<>());

            // network download
            plotDataPerMeasurement.put("mean network download", new LinkedList<>());
            plotDataPerMeasurement.put("median network download", new LinkedList<>());
            plotDataPerMeasurement.put("total total network download", new LinkedList<>());

            // network upload
            plotDataPerMeasurement.put("mean network upload", new LinkedList<>());
            plotDataPerMeasurement.put("median network upload", new LinkedList<>());
            plotDataPerMeasurement.put("total total network upload", new LinkedList<>());

            // letency
            plotDataPerMeasurement.put("mean latency", new LinkedList<>());
            plotDataPerMeasurement.put("median latency", new LinkedList<>());

            // build up data for plotting
            for (TsdbStatistics tsdbStatistics : tsdbStatisticsList) {
                for (QueryFunctionStatistics queryFunctionStatistics : tsdbStatistics.getQueryFunctionStatisticsList()) {
                    if (includeQueryFunctions.contains(queryFunctionStatistics.getQueryFunction())) {
                        // query time
                        plotDataPerMeasurement.get("mean query time").add(new PlotData(tsdbStatistics.getTsdbName(), queryFunctionStatistics.getQueryFunction(), queryFunctionStatistics.getMeanQueryTime_inMilliseconds() / 1000, "s"));
                        plotDataPerMeasurement.get("median query time").add(new PlotData(tsdbStatistics.getTsdbName(), queryFunctionStatistics.getQueryFunction(), queryFunctionStatistics.getMedianQueryTime_inMilliseconds() / 1000, "s"));
                        plotDataPerMeasurement.get("total query time").add(new PlotData(tsdbStatistics.getTsdbName(), queryFunctionStatistics.getQueryFunction(), queryFunctionStatistics.getTotalQueryTimePerQueryFunction_inMilliseconds() / (1000*60), "min"));

                        // cpu usage
                        plotDataPerMeasurement.get("mean total cpu usage").add(new PlotData(tsdbStatistics.getTsdbName(), queryFunctionStatistics.getQueryFunction(), queryFunctionStatistics.getMeanTotalCpuUsagePerQuery_inPercent(), "%"));
                        plotDataPerMeasurement.get("median total cpu usage").add(new PlotData(tsdbStatistics.getTsdbName(), queryFunctionStatistics.getQueryFunction(), queryFunctionStatistics.getMedianTotalCpuUsagePerQuery_inPercent(), "%"));
                        plotDataPerMeasurement.get("maximum recorded cpu usage").add(new PlotData(tsdbStatistics.getTsdbName(), queryFunctionStatistics.getQueryFunction(), queryFunctionStatistics.getMaximumCpuUsageRecorded_inPercent(), "%"));

                        // disk usage
                        plotDataPerMeasurement.get("mean disk usage").add(new PlotData(tsdbStatistics.getTsdbName(), queryFunctionStatistics.getQueryFunction(), queryFunctionStatistics.getMeanDiskUsagePerQuery_inBytes() / (1024*1024), "MiB"));
                        plotDataPerMeasurement.get("median disk usage").add(new PlotData(tsdbStatistics.getTsdbName(), queryFunctionStatistics.getQueryFunction(), queryFunctionStatistics.getMedianDiskUsagePerQuery_inBytes()/ (1024*1024), "MiB"));
                        plotDataPerMeasurement.get("maximum recorded disk usage").add(new PlotData(tsdbStatistics.getTsdbName(), queryFunctionStatistics.getQueryFunction(), queryFunctionStatistics.getMaximumDiskUsageRecorded_inBytes() / (1024*1024*1024), "GiB"));

                        // memory usage
                        plotDataPerMeasurement.get("mean total memory usage").add(new PlotData(tsdbStatistics.getTsdbName(), queryFunctionStatistics.getQueryFunction(), queryFunctionStatistics.getMeanTotalMemoryUsage_inPercent(), "%"));
                        plotDataPerMeasurement.get("median total memory usage").add(new PlotData(tsdbStatistics.getTsdbName(), queryFunctionStatistics.getQueryFunction(), queryFunctionStatistics.getMedianTotalMemoryUsage_inPercent(), "%"));
                        plotDataPerMeasurement.get("maximum recorded memory usage").add(new PlotData(tsdbStatistics.getTsdbName(), queryFunctionStatistics.getQueryFunction(), queryFunctionStatistics.getMaximumMemoryUsageRecorded_inPercent(), "%"));

                        // disk write
                        plotDataPerMeasurement.get("mean disk write").add(new PlotData(tsdbStatistics.getTsdbName(), queryFunctionStatistics.getQueryFunction(), queryFunctionStatistics.getMeanDiskWrite_inBytes() / (1024*1024), "MiB"));
                        plotDataPerMeasurement.get("median disk write").add(new PlotData(tsdbStatistics.getTsdbName(), queryFunctionStatistics.getQueryFunction(), queryFunctionStatistics.getMedianDiskWrite_inBytes() / (1024*1024), "MiB"));
                        plotDataPerMeasurement.get("total disk write").add(new PlotData(tsdbStatistics.getTsdbName(), queryFunctionStatistics.getQueryFunction(), queryFunctionStatistics.getTotalDiskWrite_inBytes() / (1024*1024), "MiB"));

                        // disk write
                        plotDataPerMeasurement.get("mean disk read").add(new PlotData(tsdbStatistics.getTsdbName(), queryFunctionStatistics.getQueryFunction(), queryFunctionStatistics.getMeanDiskRead_inBytes() / (1024*1024), "MiB"));
                        plotDataPerMeasurement.get("median disk read").add(new PlotData(tsdbStatistics.getTsdbName(), queryFunctionStatistics.getQueryFunction(), queryFunctionStatistics.getMedianDiskRead_inBytes() / (1024*1024), "MiB"));
                        plotDataPerMeasurement.get("total disk read").add(new PlotData(tsdbStatistics.getTsdbName(), queryFunctionStatistics.getQueryFunction(), queryFunctionStatistics.getTotalDiskRead_inBytes() / (1024*1024), "MiB"));

                        // network download
                        plotDataPerMeasurement.get("mean network download").add(new PlotData(tsdbStatistics.getTsdbName(), queryFunctionStatistics.getQueryFunction(), queryFunctionStatistics.getMeanNetworkDownload_inBytes() / (1024*1024), "MiB"));
                        plotDataPerMeasurement.get("median network download").add(new PlotData(tsdbStatistics.getTsdbName(), queryFunctionStatistics.getQueryFunction(), queryFunctionStatistics.getMedianNetworkDownload_inBytes() / (1024*1024), "MiB"));
                        plotDataPerMeasurement.get("total total network download").add(new PlotData(tsdbStatistics.getTsdbName(), queryFunctionStatistics.getQueryFunction(), queryFunctionStatistics.getTotalNetworkDownload_inBytes() / (1024*1024), "MiB"));

                        // network upload
                        plotDataPerMeasurement.get("mean network upload").add(new PlotData(tsdbStatistics.getTsdbName(), queryFunctionStatistics.getQueryFunction(), queryFunctionStatistics.getMeanNetworkUpload_inBytes() / (1024*1024), "MiB"));
                        plotDataPerMeasurement.get("median network upload").add(new PlotData(tsdbStatistics.getTsdbName(), queryFunctionStatistics.getQueryFunction(), queryFunctionStatistics.getMedianNetworkUpload_inBytes() / (1024*1024), "MiB"));
                        plotDataPerMeasurement.get("total total network upload").add(new PlotData(tsdbStatistics.getTsdbName(), queryFunctionStatistics.getQueryFunction(), queryFunctionStatistics.getTotalNetworkUpload_inBytes() / (1024*1024), "MiB"));

                        // latency
                        plotDataPerMeasurement.get("mean latency").add(new PlotData(tsdbStatistics.getTsdbName(), queryFunctionStatistics.getQueryFunction(), queryFunctionStatistics.getMeanLatency_inMilliseconds() / 1000, "s"));
                        plotDataPerMeasurement.get("median latency").add(new PlotData(tsdbStatistics.getTsdbName(), queryFunctionStatistics.getQueryFunction(), queryFunctionStatistics.getMedianLatency_inMilliseconds() / 1000, "s"));

                    }
                }
            }

            //plot the data per measurement
            BenchmarkRunner benchmarkRunner = BenchmarkRunner.getInstance();
            int normalTsQuerySize = benchmarkRunner.getBENCHMARK_TIMESERIES_METADATA_SIZE();
            int rangeQueryTsSize = benchmarkRunner.getBENCHMARK_TIMESERIES_METADATA_SIZE_QUERY_ONLY();
            String importBatchSize = BenchmarkImport.readBatchSize();
            for (Map.Entry<String, List<PlotData>> entry : plotDataPerMeasurement.entrySet()) {
                if(includeQueryFunctions.size() == 1 && includeQueryFunctions.get(0).equals("import")){
                    plotTsdbStatisticsForMeasurement(entry.getKey(), "query [TS count = " + importBatchSize + "]", entry.getValue().get(0).getUnit(), entry.getValue());

                } else if(includeQueryFunctions.size() == 1 && includeQueryFunctions.get(0).equals(BenchmarkDataSource.QueryFunction.QUERY_ONLY.toString())){
                    plotTsdbStatisticsForMeasurement(entry.getKey(), "query [TS count = " + rangeQueryTsSize + "]", entry.getValue().get(0).getUnit(), entry.getValue());

                } else {
                    plotTsdbStatisticsForMeasurement(entry.getKey(), "query [TS count = " + normalTsQuerySize + "]", entry.getValue().get(0).getUnit(), entry.getValue());
                }
            }

            logger.info("Plots for " + String.join(",", includeQueryFunctions) + " done.");
        } else {
            logger.error("No query functions given to include in bar plots");
        }

    }

    private void plotTsdbStatisticsForMeasurement(String measurement, String xAxisName, String yAxisName, List<PlotData> plotDataList){
        if(plotDataList != null && ! plotDataList.isEmpty()){
            // Prepare the data set /* barDataset.setValue(26, "Chronix", "count"); */
            List<String> functionNames = new LinkedList<>();
            DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
            for(PlotData data : plotDataList){
                barDataset.setValue(data.getValue(), data.getTsdbName(), data.getQueryFunction());
                if( ! functionNames.contains(data.getQueryFunction()))
                functionNames.add(data.getQueryFunction());
            }

            //Create the chart
            JFreeChart chart = ChartFactory.createBarChart(
                    measurement,
                    xAxisName,
                    yAxisName,
                    barDataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );

            //save to file
            try {
                String functions = String.join("_", functionNames);
                String fileName = measurement.replaceAll(" ", "_") + "_" + functions +  ".jpg";
                ChartUtilities.saveChartAsJPEG(
                        new File(statisticsBarPlotDirectory + File.separator + fileName),
                        chart,
                        CHART_WIDTH,
                        CHART_HEIGHT);

            } catch (Exception e){
                logger.error("Error saving chart to file: " + e.getLocalizedMessage());
            }
        }
    }


}
