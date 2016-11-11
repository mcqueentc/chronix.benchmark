package Docker;

import de.qaware.chronix.client.benchmark.configurator.Configurator;
import de.qaware.chronix.common.ServerConfig.ServerConfigAccessor;
import de.qaware.chronix.common.dockerUtil.DockerRunOptions;

import java.util.List;

/**
 * Created by mcqueen666 on 31.08.16.
 */
public class StartDockerContainer {

    public static void main(String[] args){

        Configurator configurator = Configurator.getInstance();
        ServerConfigAccessor serverConfigAccessor = ServerConfigAccessor.getInstance();
        String server = "localhost";
        if(args.length > 0){
            server = args[0];
        }

        System.out.println("\n###### Docker.StartDockerContainer ######");

        if(configurator.isServerUp(server)){
            System.out.println("Server is up");
        } else {
            System.out.println("Server not responding");
            return;
        }

        // start test

        List<DockerRunOptions> dockerRunOptionsList = serverConfigAccessor.getServerConfigRecords().get(0).getTsdbRunRecords();

        String[] answers = {"no container name given"};
        if(args != null && args.length > 0){
            for(int i = 1; i < args.length; i++) {
                String tsdbName = args[i];
                for(DockerRunOptions dockerRunOptions : dockerRunOptionsList){
                    if(dockerRunOptions.getContainerName().equals(tsdbName)){
                        answers = configurator.startDockerContainer(server, dockerRunOptions);
                        for(String s : answers){
                            System.out.println("Server: " + s);
                        }
                    }
                }
            }
        } else {
            for(String s : answers){
                System.out.println("Server: " + s);
            }
        }




    }
}
