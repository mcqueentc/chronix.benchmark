package de.qaware.chronix.shared.QueryUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.qaware.chronix.database.BenchmarkQuery;
import de.qaware.chronix.database.TimeSeries;

import java.util.List;

/**
 * Created by mcqueen666 on 30.08.16.
 */
public abstract class IgnoreTimesSeriesForJSON {
    @JsonIgnore List<BenchmarkQuery> queryList;
    @JsonIgnore List<TimeSeries> timeSeriesList;
}
