package de.qaware.chronix.client.benchmark.resultpresenter.plot;

import de.qaware.chronix.client.benchmark.resultpresenter.QueryFunctionStatistics;
import de.qaware.chronix.client.benchmark.resultpresenter.TsdbStatistics;
import de.qaware.chronix.client.benchmark.resultpresenter.TsdbStatisticsAnalyzer;
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

    public void plotTsdbStatisticsForQueryFunctions(){
        Map<String, List<PlotData>> plotDataPerMeasurement = new HashMap<>();
        plotDataPerMeasurement.put("mean query time", new LinkedList<>());
        plotDataPerMeasurement.put("median query time", new LinkedList<>());

        // build up data for plotting
        for(TsdbStatistics tsdbStatistics : tsdbStatisticsList){
            for(QueryFunctionStatistics queryFunctionStatistics : tsdbStatistics.getQueryFunctionStatisticsList()){
                if(! queryFunctionStatistics.getQueryFunction().equals("import")) {
                    plotDataPerMeasurement.get("mean query time").add(new PlotData(tsdbStatistics.getTsdbName(), queryFunctionStatistics.getQueryFunction(), queryFunctionStatistics.getMeanQueryTime_inMilliseconds(), "ms"));
                    plotDataPerMeasurement.get("median query time").add(new PlotData(tsdbStatistics.getTsdbName(), queryFunctionStatistics.getQueryFunction(), queryFunctionStatistics.getMedianQueryTime_inMilliseconds(), "ms"));
                }
            }
        }

        //plot the data per measurement
        for(Map.Entry<String, List<PlotData>> entry : plotDataPerMeasurement.entrySet()){
            plotTsdbStatisticsForMeasurement(entry.getKey(), "query function", entry.getValue().get(0).getUnit(), entry.getValue());
        }

        logger.info("Done!");

    }

    private void plotTsdbStatisticsForMeasurement(String measurement, String xAxisName, String yAxisName, List<PlotData> plotDataList){
        if(plotDataList != null && ! plotDataList.isEmpty()){
            // Prepare the data set /* barDataset.setValue(26, "Chronix", "count"); */
            DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
            for(PlotData data : plotDataList){
                barDataset.setValue(data.getValue(), data.getTsdbName(), data.getQueryFunction());
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
                String fileName = measurement.replaceAll(" ", "_") + ".jpg";
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
