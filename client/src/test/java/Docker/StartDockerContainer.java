package Docker;

import de.qaware.chronix.client.benchmark.configurator.Configurator;
import de.qaware.chronix.shared.dockerUtil.DockerRunOptions;

/**
 * Created by mcqueen666 on 31.08.16.
 */
public class StartDockerContainer {

    public static void main(String[] args){

        Configurator configurator = Configurator.getInstance();
        String server = "localhost";

        System.out.println("\n###### Docker.StartDockerContainer ######");

        if(configurator.isServerUp(server)){
            System.out.println("Server is up");
        } else {
            System.out.println("Server not responding");
            return;
        }

        // start test
        DockerRunOptions chronix = new DockerRunOptions("chronix",8983,8983,"");
        String[] start = configurator.startDockerContainer(server,chronix);
        for(String s : start){
            System.out.println("Server: " + s);
        }


    }
}
