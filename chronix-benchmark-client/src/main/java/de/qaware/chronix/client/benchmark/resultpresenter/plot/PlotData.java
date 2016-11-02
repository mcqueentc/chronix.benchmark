package de.qaware.chronix.client.benchmark.resultpresenter.plot;

/**
 * Created by mcqueen666 on 17.10.16.
 */
public class PlotData {
    private String tsdbName;
    private String queryFunction;
    private Number value;
    private String unit;

    public PlotData() {
    }

    public PlotData(String tsdbName, String queryFunction, Number value, String unit) {
        this.tsdbName = tsdbName;
        this.queryFunction = queryFunction;
        this.value = value;
        this.unit = unit;
    }

    public String getTsdbName() {
        return tsdbName;
    }

    public void setTsdbName(String tsdbName) {
        this.tsdbName = tsdbName;
    }

    public String getQueryFunction() {
        return queryFunction;
    }

    public void setQueryFunction(String queryFunction) {
        this.queryFunction = queryFunction;
    }

    public Number getValue() {
        return value;
    }

    public void setValue(Number value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
