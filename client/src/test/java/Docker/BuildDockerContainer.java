package Docker;

import de.qaware.chronix.client.benchmark.configurator.Configurator;
import de.qaware.chronix.shared.ServerConfig.ServerConfigAccessor;
import de.qaware.chronix.shared.dockerUtil.DockerBuildOptions;

import java.util.List;

/**
 * Created by mcqueen666 on 31.08.16.
 */
public class BuildDockerContainer {

    public static void main(String[] args){

        Configurator configurator = Configurator.getInstance();
        ServerConfigAccessor serverConfigAccessor = ServerConfigAccessor.getInstance();
        String server = "localhost";
        if(args.length > 0){
            server = args[0];
        }

        System.out.println("\n###### Docker.BuildDockerContainer ######");

        if(configurator.isServerUp(server)){
            System.out.println("Server is up");
        } else {
            System.out.println("Server not responding");
            return;
        }

        // build test
        List<DockerBuildOptions> dockerBuildOptionsList = serverConfigAccessor.getServerConfigRecords().get(0).getTsdbBuildRecords();

        String[] answers = {"no container name given"};
        if(args != null && args.length > 0){
            for(int i = 1; i < args.length; i++) {
                String tsdbName = args[i];
                for(DockerBuildOptions dockerBuildOptions : dockerBuildOptionsList){
                    if(dockerBuildOptions.getContainerName().equals(tsdbName)){
                        answers = configurator.buildDockerContainer(server, dockerBuildOptions);
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
