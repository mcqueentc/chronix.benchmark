package Server;

import de.qaware.chronix.client.benchmark.configurator.Configurator;
import de.qaware.chronix.client.benchmark.queryhandler.QueryHandler;
import de.qaware.chronix.shared.QueryUtil.CleanCommand;
import de.qaware.chronix.shared.ServerConfig.ServerConfigAccessor;
import de.qaware.chronix.shared.ServerConfig.ServerConfigRecord;
import de.qaware.chronix.shared.ServerConfig.TSDBInterfaceHandler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mcqueen666 on 13.09.16.
 */
public class CleanDatabasesOnServerTest {

    public static void main(String[] args){
        Configurator configurator = Configurator.getInstance();
        String server = "localhost";
        if(args.length > 0){
            server = args[0];
        }

        System.out.println("\n###### Client.CleanDatabasesOnServerTest ######");

        if (configurator.isServerUp(server)) {
            System.out.println("Server is up");
        } else {
            System.out.println("Server not responding");
            return;
        }


        ServerConfigAccessor serverConfigAccessor = ServerConfigAccessor.getInstance();
        LinkedList<ServerConfigRecord> readRecord = serverConfigAccessor.getServerConfigRecords();
        TSDBInterfaceHandler interfaceHandler = TSDBInterfaceHandler.getInstance();
        QueryHandler queryHandler = QueryHandler.getInstance();

        // generate clean commands
        List<CleanCommand> cleanCommandList = new ArrayList<>();
        for (ServerConfigRecord r : readRecord) {
            LinkedList<String> externalImpls = r.getExternalTimeSeriesDataBaseImplementations();
            for (String externalImpl : externalImpls) {
                String ip = r.getServerAddress();
                Integer port = Integer.valueOf(serverConfigAccessor.getHostPortForTSDB(ip, externalImpl));
                cleanCommandList.add(new CleanCommand(externalImpl, ip, port));

            }
        }

        String[] results = queryHandler.cleanDatabasesOnServer(server, cleanCommandList);

        for(String s : results){
            System.out.println(s);
        }

    }
}
