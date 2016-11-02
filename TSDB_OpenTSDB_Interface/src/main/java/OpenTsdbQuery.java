/**
 * Created by mcqueen666 on 07.09.16.
 */
public class OpenTsdbQuery {
    private final String startDate;
    private final String endDate;
    private final String aggregatedMetric;
    private final String tagString;

    public OpenTsdbQuery(String startDate, String endDate, String aggregatedMetric, String tagString) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.aggregatedMetric = aggregatedMetric;
        this.tagString = tagString;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getAggregatedMetric() {
        return aggregatedMetric;
    }

    public String getTagString() {
        return tagString;
    }
}
