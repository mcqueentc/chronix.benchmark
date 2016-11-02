package Client;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.File;
import java.io.IOException;

/**
 * Created by mcqueen666 on 17.10.16.
 */
public class HorizontalBarChart {

    public static void main(String arg[]) {

        // Prepare the data set
        DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
        barDataset.setValue(26, "Chronix", "count");
        barDataset.setValue(20, "InfluxDB", "count");
        barDataset.setValue(12, "KairosDB", "count");
        barDataset.setValue(14, "Graphite", "count");
        barDataset.setValue(18, "OpenTSDB", "count");

        barDataset.setValue(21, "Chronix", "max");
        barDataset.setValue(12, "InfluxDB", "max");
        barDataset.setValue(15, "KairosDB", "max");
        barDataset.setValue(18, "Graphite", "max");
        barDataset.setValue(10, "OpenTSDB", "max");

        //Create the chart
        JFreeChart chart = ChartFactory.createBarChart(
                "mean query time", "query function", "ms", barDataset,
                PlotOrientation.VERTICAL, true, true, false);

        try {
            ChartUtilities.saveChartAsPNG(new File(System.getProperty("user.home") + File.separator + "testplot.png"), chart, 560, 350);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Render the frame
        ChartFrame chartFrame = new ChartFrame("Horizontal Bar Chart", chart);
        chartFrame.setVisible(true);
        chartFrame.setSize(560, 350);


    }
}
