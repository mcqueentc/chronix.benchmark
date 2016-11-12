package de.qaware.chronix.common.math;

import de.qaware.chronix.database.TimeSeriesPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by mcqueen666 on 11.11.16.
 */
public class ChronixMath {

    public static Double calcMedianFromDouble(List<Double> collection){
        Double median = Double.NaN;
        try {
            Collections.sort(collection);
            int n = collection.size();
            if (n % 2 == 0) {
                median = (collection.get((n / 2)-1) + collection.get((n / 2 + 1)-1)) / 2;
            } else {
                median = collection.get(((n + 1) / 2)-1);
            }
        } catch (Exception e){
            //
        }

        return median;
    }

    public static Double calcMedianFromLong(List<Long> collection){
        Double median = Double.NaN;
        try {
            Collections.sort(collection);
            int n = collection.size();
            if (n % 2 == 0) {
                median = ((double)(collection.get((n / 2)-1) + collection.get((n / 2 + 1)-1))) / 2;
            } else {
                median = (double)(collection.get(((n + 1) / 2)-1));
            }
        } catch (Exception e){
            //
        }

        return median;
    }

    public static Double calcMedianFromTimeSeriesPoints(List<TimeSeriesPoint> points){
        Double median = Double.NaN;
        try {
            List<Double> collection = new ArrayList<>(points.size());
            for(TimeSeriesPoint point : points){
                collection.add(point.getValue());
            }

            Collections.sort(collection);
            int n = collection.size();
            if (n % 2 == 0) {
                median = ((collection.get((n / 2)-1) + collection.get((n / 2 + 1)-1))) / 2;
            } else {
                median = (collection.get(((n + 1) / 2)-1));
            }
        } catch (Exception e){
            //
        }

        return median;
    }

    public static Double calcStddev(List<Double> collection){
        Double result = Double.NaN;
        try {
            // calc mean
            double mean = 0.0;
            for (Double d : collection) {
                mean += d;
            }
            mean /= collection.size();

            //calc stddev
            double s2 = 0.0;
            for (Double x : collection) {
                s2 += Math.pow((x - mean), 2);
            }
            s2 /= (collection.size() - 1);
            result = Math.sqrt(s2);
        } catch (Exception e){
            //
        }

        return result;
    }

    public static Double calcStddev(List<Double> collection, double mean){
        Double result = Double.NaN;
        try {
            //calc stddev
            double s2 = 0.0;
            for (Double x : collection) {
                s2 += Math.pow((x - mean), 2);
            }
            s2 /= (collection.size() - 1);
            result = Math.sqrt(s2);
        } catch (Exception e){
            //
        }

        return result;
    }


}
