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
        String tsdbName = "chronix";
        buildOptionses.add(new DockerBuildOptions(tsdbName, "-t"));                        //   host/dir:/container/dir
        //runOptionses.add(new DockerRunOptions(tsdbName, 8983, 8983, "--name " + tsdbName));
        runOptionses.add(new DockerRunOptions(tsdbName, 8983, 8983, "-v /opt/chronix-0.3/chronix-solr-6.1.0/server/solr/chronix/data --name " + tsdbName));


        //InfluxDB
        tsdbName = "influxdb";
        buildOptionses.add(new DockerBuildOptions(tsdbName, "-t"));
        //runOptionses.add(new DockerRunOptions(tsdbName, 8086, 8086, "-p 8083:8083 --name " + tsdbName));
        runOptionses.add(new DockerRunOptions(tsdbName, 8086, 8086, "-p 8083:8083 -v /var/lib/influxdb --name " + tsdbName));

        //KairosDB
        tsdbName = "kairosdb";
        buildOptionses.add(new DockerBuildOptions(tsdbName, "-t"));
        //runOptionses.add(new DockerRunOptions(tsdbName, 8080, 8080, "--name " + tsdbName));
        runOptionses.add(new DockerRunOptions(tsdbName, 8080, 8080, "-v /var/lib/cassandra --name " + tsdbName));

        //OpenTSDB
        tsdbName = "opentsdb";
        buildOptionses.add(new DockerBuildOptions(tsdbName, "-t"));
        //runOptionses.add(new DockerRunOptions(tsdbName, 4242, 4242, "--name " + tsdbName));
        runOptionses.add(new DockerRunOptions(tsdbName, 4242, 4242, "-v /data/hbase --name " + tsdbName));


        //Graphite
        tsdbName = "graphite";
        buildOptionses.add(new DockerBuildOptions(tsdbName, "-t"));
        runOptionses.add(new DockerRunOptions(tsdbName, 2003, 2003, "-p 80:80 -v /opt/graphite/storage --name " + tsdbName));
        //runOptionses.add(new DockerRunOptions(tsdbName, 2003, 2003, "-p 80:80 --name " + tsdbName));
        /*runOptionses.add(new DockerRunOptions(tsdbName, 2003, 2003, "-p 80:80 -p 2004:2004 -p 2023:2023 -p 2024:2024 -p 8125:8125 -p 8126:8126"
                                                                            + " -v /mnt/tsdb-benchmark-data/graphite/storage:/opt/graphite/storage"
                                                                            + " -v /mnt/tsdb-benchmark-data/graphite/nginx:/etc/nginx"
                                                                            + " -v /mnt/tsdb-benchmark-data/graphite/log:/var/log"
                                                                            + " -v /mnt/tsdb-benchmark-data/graphite/conf:/opt/graphite/conf"
                                                                            + " -v /mnt/tsdb-benchmark-data/graphite/statsd:/opt/statsd"
                                                                            + " -v /mnt/tsdb-benchmark-data/graphite/logrotate:/etc/logrotate.d"
                                                                            + " --name " + tsdbName));*/



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
