package Docker;

import de.qaware.chronix.client.benchmark.configurator.Configurator;

/**
 * Created by mcqueen666 on 31.08.16.
 */
public class RunningTestDockerContainer {

    public static void main(String[] args){

        Configurator configurator = Configurator.getInstance();
        String server = "localhost";

        System.out.println("\n###### Docker.RunningTestDockerContainer ######");

        if(configurator.isServerUp(server)){
            System.out.println("Server is up");
        } else {
            System.out.println("Server not responding");
            return;
        }


        //running test
        String[] answers = {"no container name given"};
        if(args != null && args.length > 0){
            for(String containerName : args){
                boolean isDockerContainerRunning = configurator.isDockerContainerRunning(server, containerName);
                if(isDockerContainerRunning){
                    String[] s = {"container " + containerName + " is running"};
                    answers = s;
                } else {
                    String[] s = {"container " + containerName + " is not running"};
                    answers = s;
                }
                for(String answer : answers){
                    System.out.println(answer);
                }
            }
        } else {
            for(String answer : answers){
                System.out.println(answer);
            }
        }



    }

}
