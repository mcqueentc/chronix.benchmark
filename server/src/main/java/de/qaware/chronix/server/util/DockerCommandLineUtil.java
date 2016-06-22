package de.qaware.chronix.server.util;

import sun.awt.OSInfo;

import java.util.List;

/**
 * Created by mcqueen666 on 21.06.16.
 */
public class DockerCommandLineUtil {

   // private static final Logger LOGGER = BenchmarkLogger.getLogger();

    private DockerCommandLineUtil() {
        //Avoid instances
    }


    /**
     * Get the servers docker install path
     *
     * @return the docker install path or empty string if not installed.
     */
    public static String getDockerInstallPath(){
        OSInfo.OSType os = OSInfo.getOSType();
        if(os == OSInfo.OSType.MACOSX || os == OSInfo.OSType.LINUX) {
            String[] command = {"which docker"};
            String[] localizedCommand = ServerSystemUtil.getOsSpecificCommand(command);
            List<String> result = ServerSystemUtil.executeCommand(localizedCommand);
            if(!result.isEmpty()){
                return result.get(0).replace("docker", "");
            }
        } else if (os == OSInfo.OSType.WINDOWS){
            //TODO

        } else if (os == OSInfo.OSType.SOLARIS){
            //TODO

        }

        return "";
    }


    /**
     * Checks if Docker is installed on the server
     *
     * @return True if Docker is installed
     */
    public static boolean isDockerInstalled(){
        String result = getDockerInstallPath();
        if(!result.isEmpty()){
            return true;
        }
        return false;
    }

    /**
     * Gets container id of the container name. Only one running instance per container is supported
     *
     * @param containerName the container name
     * @return the container name id as string
     */
    public static String getContainerId(String containerName) {
        OSInfo.OSType os = OSInfo.getOSType();
        String[] command = null;
        if(os == OSInfo.OSType.MACOSX || os == OSInfo.OSType.LINUX) {
            String[] localCmd = {
                    "/bin/sh",
                    "-c",
                    getDockerInstallPath() + "docker" + " ps | grep " + containerName + " | cut -d ' ' -f1"
            };
            command = localCmd;
        } else if (os == OSInfo.OSType.WINDOWS){
            //TODO

        } else if (os == OSInfo.OSType.SOLARIS){
            //TODO

        }

        if(command != null){
            List<String> result = ServerSystemUtil.executeCommand(command);
            if(!result.isEmpty()) {
                return result.get(result.size() - 1);
            }
        }
        return "";
    }


    /**
     * Checks if container is running. Only one running instance per container is supported
     *
     * @param containerName the container name.
     * @return true if container with containerName is running.
     */
    public static boolean isDockerContainerRunning(String containerName) {
        String result = getContainerId(containerName);
        if(result != ""){
            return true;
        }
        return false;
    }




}
