package Docker;

import de.qaware.chronix.client.benchmark.configurator.Configurator;

/**
 * Created by mcqueen666 on 31.08.16.
 */
public class UploadDockerFiles {

    public static void main(String[] args){

        Configurator configurator = Configurator.getInstance();
        String server = "localhost";

        System.out.println("\n###### Docker.UploadDockerFiles ######");

        if(configurator.isServerUp(server)){
            System.out.println("Server is up");
        } else {
            System.out.println("Server not responding");
            return;
        }

        // Chronix
        String path = "/Documents/BA_workspace/docker/chronix";
        System.out.println("Uploading: " + path);
        String[] uploadAnswers = configurator.uploadFiles(server,System.getProperty("user.home") + path);
        for(String answer : uploadAnswers){
            System.out.println(answer);
        }

        // InfluxDB
        path = "/Documents/BA_workspace/docker/influxdb";
        System.out.println("\nUploading: " + path);
        uploadAnswers = configurator.uploadFiles(server,System.getProperty("user.home") + path);
        for(String answer : uploadAnswers){
            System.out.println(answer);
        }

        // KairosDB
        path = "/Documents/BA_workspace/docker/kairosdb";
        System.out.println("\nUploading: " + path);
        uploadAnswers = configurator.uploadFiles(server, System.getProperty("user.home") + path);
        for(String answer : uploadAnswers){
            System.out.println(answer);
        }

    }
}
