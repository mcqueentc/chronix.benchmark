package Server;

import de.qaware.chronix.shared.ServerConfig.ServerConfigAccessor;
import de.qaware.chronix.shared.ServerConfig.ServerConfigRecord;
import de.qaware.chronix.shared.dockerUtil.DockerBuildOptions;
import de.qaware.chronix.shared.dockerUtil.DockerRunOptions;

import java.util.LinkedList;

/**
 * Created by mcqueen666 on 31.08.16.
 */
public class GenerateServerConfigRecord {

    public static void main(String[] args){


        //server record test


        String server = "localhost";
        LinkedList<DockerBuildOptions> buildOptionses = new LinkedList<>();
        buildOptionses.add(new DockerBuildOptions("chronix", "-t"));
        //buildOptionses.add(new DockerBuildOptions("kairosdb", "-t"));

        LinkedList<DockerRunOptions> runOptionses = new LinkedList<>();
        runOptionses.add(new DockerRunOptions("chronix", 8983, 8983, ""));
        //runOptionses.add(new DockerRunOptions("kairos", 2003, 2003, "-v"));

        ServerConfigRecord serverConfigRecord = new ServerConfigRecord(server);
        serverConfigRecord.setTsdbBuildRecords(buildOptionses);
        serverConfigRecord.setTsdbRunRecords(runOptionses);
        LinkedList<String> tsdata = new LinkedList<>();
        tsdata.add("p1");
        //tsdata.add("p2");
        serverConfigRecord.setTimeSeriesDataFolders(tsdata);

/*
            ServerConfigRecord serverConfigRecord2 = new ServerConfigRecord("www.fau.cs.de");
            serverConfigRecord2.setTsdbBuildRecords(buildOptionses);
            serverConfigRecord2.setTsdbRunRecords(runOptionses);
            LinkedList<String> tsdata2 = new LinkedList<>();
            tsdata2.add("p3");
            tsdata2.add("p4");
            serverConfigRecord2.setTimeSeriesDataFolders(tsdata2);
*/
        LinkedList<ServerConfigRecord> records = new LinkedList<>();
        records.add(serverConfigRecord);
        //records.add(serverConfigRecord2);


        ServerConfigAccessor serverConfigAccessor = ServerConfigAccessor.getInstance();
        serverConfigAccessor.setServerConfigRecords(records);

    }



}
