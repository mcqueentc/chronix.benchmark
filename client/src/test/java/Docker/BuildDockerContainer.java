package Docker;

import de.qaware.chronix.client.benchmark.configurator.Configurator;
import de.qaware.chronix.shared.dockerUtil.DockerBuildOptions;

/**
 * Created by mcqueen666 on 31.08.16.
 */
public class BuildDockerContainer {

    public static void main(String[] args){

        Configurator configurator = Configurator.getInstance();
        String server = "localhost";

        System.out.println("\n###### Docker.BuildDockerContainer ######");

        if(configurator.isServerUp(server)){
            System.out.println("Server is up");
        } else {
            System.out.println("Server not responding");
            return;
        }

        // build test
        DockerBuildOptions chronix = new DockerBuildOptions("chronix","-t");
        String[] answers = configurator.buildDockerContainer(server,chronix);

        for(String s : answers){
            System.out.println("Server: " + s);
        }

    }
}
