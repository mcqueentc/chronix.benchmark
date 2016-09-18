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
        if(args.length > 0){
            server = args[0];
        }

        LinkedList<DockerBuildOptions> buildOptionses = new LinkedList<>();
        LinkedList<DockerRunOptions> runOptionses = new LinkedList<>();

        // Chronix

        buildOptionses.add(new DockerBuildOptions("chronix", "-t"));
        runOptionses.add(new DockerRunOptions("chronix", 8983, 8983, ""));

        //InfluxDB
        buildOptionses.add(new DockerBuildOptions("influxdb", "-t"));
        runOptionses.add(new DockerRunOptions("influxdb", 8086, 8086, "-p 8083:8083"));

        //KairosDB
        buildOptionses.add(new DockerBuildOptions("kairosdb", "-t"));
        runOptionses.add(new DockerRunOptions("kairosdb", 8080, 8080, ""));

        //OpenTSDB
        buildOptionses.add(new DockerBuildOptions("opentsdb", "-t"));
        runOptionses.add(new DockerRunOptions("opentsdb", 4242, 4242, "-h localHost"));

        //Graphite
        buildOptionses.add(new DockerBuildOptions("graphite", "-t"));
        runOptionses.add(new DockerRunOptions("graphite", 2003, 2003, "-p 8888:8888"));



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

        // write the server config record
        LinkedList<ServerConfigRecord> records = new LinkedList<>();
        records.add(serverConfigRecord);
        //records.add(serverConfigRecord2);

        ServerConfigAccessor serverConfigAccessor = ServerConfigAccessor.getInstance();
        serverConfigAccessor.setServerConfigRecords(records);

    }



}
