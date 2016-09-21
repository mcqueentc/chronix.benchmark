package de.qaware.chronix.shared.DataModels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.qaware.chronix.database.TimeSeries;
import de.qaware.chronix.shared.QueryUtil.ImportRecord;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by mcqueen666 on 21.09.16.
 */
@XmlRootElement
public class ImportRecordWrapper {
    private List<TimeSeries> timeSeriesList;
    private List<ImportRecord> importRecordList;

    public ImportRecordWrapper() {
    }

    public ImportRecordWrapper(List<TimeSeries> timeSeriesList, List<ImportRecord> importRecordList) {
        this.timeSeriesList = timeSeriesList;
        this.importRecordList = importRecordList;
    }

    public List<TimeSeries> getTimeSeriesList() {
        return timeSeriesList;
    }

    public void setTimeSeriesList(List<TimeSeries> timeSeriesList) {
        this.timeSeriesList = timeSeriesList;
    }

    public List<ImportRecord> getImportRecordList() {
        return importRecordList;
    }

    public void setImportRecordList(List<ImportRecord> importRecordList) {
        this.importRecordList = importRecordList;
    }

    @JsonIgnore
    public String getAllTsdbNames(){
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < importRecordList.size(); i++){
            if(i != importRecordList.size() - 1){
                builder.append(importRecordList.get(i).getTsdbName()).append(",");
            } else {
                builder.append(importRecordList.get(i).getTsdbName());
            }
        }
        return builder.toString();
    }
}
