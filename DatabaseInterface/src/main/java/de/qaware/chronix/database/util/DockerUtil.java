package de.qaware.chronix.database.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.awt.OSInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mcqueen666 on 13.09.16.
 */
public class DockerUtil {

    private static final Logger logger = LoggerFactory.getLogger(DockerUtil.class);



    public List<String> executeCommandOnDockerContainer(String containerName, String commandForDockerContainer){
        List<String> result = new ArrayList<>();
        if(isDockerInstalled()){
            String containerID = getRunningContainerId(containerName);
            String[] deleteCommand = {getDockerInstallPath() + "docker exec -t " + containerID + " " + commandForDockerContainer};
            if(!containerID.isEmpty()){
                String[] command = getOsSpecificCommand(deleteCommand);
                result.addAll(executeCommand(command));

            }

        } else {
            result.add("Docker not installed!");
        }

        return result;
    }



    /**
     * Get the servers docker install path
     *
     * @return the docker install path or empty string if not installed.
     */
    private String getDockerInstallPath(){
        OSInfo.OSType os = OSInfo.getOSType();
        if(os == OSInfo.OSType.MACOSX || os == OSInfo.OSType.LINUX) {
            String[] command = {"which docker"};
            String[] localizedCommand = getOsSpecificCommand(command);
            List<String> result = executeCommand(localizedCommand);
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
    private boolean isDockerInstalled(){
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
    private String getRunningContainerId(String containerName) {
        String[] commandLine = {getDockerInstallPath() + "docker" + " ps | grep " + containerName + " | cut -d ' ' -f1"};
        String[] command = getOsSpecificCommand(commandLine);


        if(command != null){
            List<String> result = executeCommand(command);
            if(!result.isEmpty()) {
                return result.get(result.size() - 1);
            }
        }
        return "";
    }

    /**
     * Executes the command with runtime.exec
     *
     * @param command the command string array
     * @return the result of the command
     */
    private List<String> executeCommand(String[] command) {
        List<String> result = new LinkedList<String>();
        Process p;
        if(command != null) {
            try {
                p = Runtime.getRuntime().exec(command);
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String curLine;
                while ((curLine = reader.readLine()) != null) {
                    result.add(curLine);
                }
                p.waitFor();
                reader.close();

            } catch (Exception e) {
                logger.error("Error executeCommand: " + e.getLocalizedMessage());
            }
        }
        return result;
    }

    /**
     * Builds a OS specific command from command string
     *
     * @param command the command
     * @return the os specific command
     */
    private String[] getOsSpecificCommand(String[] command){
        OSInfo.OSType os = OSInfo.getOSType();
        String[] specificCommand = null;
        if(os == OSInfo.OSType.MACOSX || os == OSInfo.OSType.LINUX) {

            String[] localCmd = new String[command.length + 2];
            localCmd[0] = "/bin/sh";
            localCmd[1] = "-c";
            for(int i = 0; i < command.length; i++){
                localCmd[i + 2] = command[i];
            }

            specificCommand = localCmd;
        } else if (os == OSInfo.OSType.WINDOWS){


        } else if (os == OSInfo.OSType.SOLARIS){
            //TODO

        }
        return specificCommand;
    }

}
