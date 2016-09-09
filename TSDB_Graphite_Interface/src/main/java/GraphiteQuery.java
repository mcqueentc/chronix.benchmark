/**
 * Created by mcqueen666 on 09.09.16.
 */
public class GraphiteQuery {

    private final String query;
    private final String startDate;
    private final String endDate;

    public GraphiteQuery(String query, String startDate, String endDate) {
        this.query = query;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getQuery() {
        return query;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }
}
