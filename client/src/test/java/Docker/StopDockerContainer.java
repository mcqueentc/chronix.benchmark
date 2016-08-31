package Docker;

import de.qaware.chronix.client.benchmark.configurator.Configurator;

/**
 * Created by mcqueen666 on 31.08.16.
 */
public class StopDockerContainer {

    public static void main(String[] args){

        Configurator configurator = Configurator.getInstance();
        String server = "localhost";

        System.out.println("\n###### Docker.StopDockerContainer ######");

        if(configurator.isServerUp(server)){
            System.out.println("Server is up");
        } else {
            System.out.println("Server not responding");
            return;
        }


        //stop test
        String[] answers = configurator.stopDockerContainer(server,"chronix");
        for(String s : answers){
            System.out.println("Server: " + s);
        }


    }
}
