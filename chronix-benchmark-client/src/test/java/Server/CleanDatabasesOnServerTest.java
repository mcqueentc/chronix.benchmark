package Server;

import de.qaware.chronix.client.benchmark.configurator.Configurator;
import de.qaware.chronix.client.benchmark.queryhandler.QueryHandler;
import de.qaware.chronix.common.QueryUtil.CleanCommand;
import de.qaware.chronix.common.ServerConfig.ServerConfigAccessor;
import de.qaware.chronix.common.ServerConfig.ServerConfigRecord;

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
        QueryHandler queryHandler = QueryHandler.getInstance();

        ServerConfigRecord serverConfigRecord = serverConfigAccessor.getServerConfigRecord(server);
        String ip = serverConfigRecord.getServerAddress();

        // generate clean commands
        List<CleanCommand> cleanCommandList = new ArrayList<>();
        LinkedList<String> externalImpls = serverConfigRecord.getExternalTimeSeriesDataBaseImplementations();
        for (int i = 1; i < args.length; i++) {
            if(externalImpls.contains(args[i])) {
                Integer port = Integer.valueOf(serverConfigAccessor.getHostPortForTSDB(ip, args[i]));
                cleanCommandList.add(new CleanCommand(args[i], ip, port));
            }
        }

        String[] results = queryHandler.cleanDatabasesOnServer(server, cleanCommandList);

        for(String s : results){
            System.out.println(s);
        }

    }
}
