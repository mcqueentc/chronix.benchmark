package de.qaware.chronix.client.benchmark.resultpresenter.plot;

import de.qaware.chronix.client.benchmark.resultpresenter.TsdbStatistics;
import de.qaware.chronix.client.benchmark.resultpresenter.TsdbStatisticsAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;


import java.io.File;
import java.util.List;

/**
 * Created by mcqueen666 on 17.10.16.
 */
public class StatisticsBarPlotter {

    private final Logger logger;
    private final String statisticsBarPlotDirectory;
    private final String statisticsFilename;
    private final List<TsdbStatistics> tsdbStatisticsList;

    public StatisticsBarPlotter(String statisticsDirectory, String statisticsFilename){
        logger = LoggerFactory.getLogger(StatisticsBarPlotter.class);
        this.statisticsBarPlotDirectory = statisticsDirectory + File.separator + "bar_plots";
        this.statisticsFilename = statisticsFilename;
        this.tsdbStatisticsList = new TsdbStatisticsAnalyzer(statisticsDirectory).readTsdbStatistics();
    }


}
