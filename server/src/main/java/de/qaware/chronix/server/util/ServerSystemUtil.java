package de.qaware.chronix.server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.awt.OSInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by mcqueen666 on 17.06.16.
 */
public class ServerSystemUtil {

    static final String benchmarkUtilPath = "chronixBenchmark" + File.separator + "docker";
    private static final Logger logger = LoggerFactory.getLogger(ServerSystemUtil.class);

    private ServerSystemUtil(){
        //Avoid instances
    }

    /**
     * Returns the chronix benchmark directory for saving docker files.
     *
     * @return The directory with appending File separator. (Unix: "/", Windows: "\")
     */
    public static String getBenchmarkDockerDirectory() {
        String path = null;
        OSInfo.OSType os = sun.awt.OSInfo.getOSType();
        if (os != OSInfo.OSType.UNKNOWN) {
            path = System.getProperty("user.home") + File.separator + benchmarkUtilPath + File.separator;
        }
        return path;
    }

    /**
     * Executes the command with runtime.exec
     *
     * @param command the command string array
     * @return the result of the command
     */
    public static List<String> executeCommand(String[] command) {
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
                logger.error("Error ServerSystemUtil executeCommand: " + e.getLocalizedMessage());
            }
        }
        return result;
    }

    /**
     * Executes the command with runtime.exec without logging
     *
     * @param command the command string array
     */
    public static void executeCommandSimple(String[] command) {
        List<String> result = new LinkedList<String>();
        Process p;
        if(command != null) {
            try {
                p = Runtime.getRuntime().exec(command);
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                while ((reader.readLine()) != null) {
                }
                p.waitFor();

            } catch (Exception e) {
                logger.error("Error ServerSystemUtil executeCommand: " + e.getLocalizedMessage());
            }
        }
    }

    /**
     * Executes the command with runtime.exec
     *
     * @param command the command string
     * @return the result of the command
     */
    public static List<String> executeCommand(String command) {
        String[] newCommand = {command};
        return executeCommand(newCommand);
    }

    /**
     * Builds a OS specific command from command string
     *
     * @param command the command
     * @return the os specific command
     */
    public static String[] getOsSpecificCommand(String[] command){
        OSInfo.OSType os = OSInfo.getOSType();
        String[] specificCommand = null;
        if(os == OSInfo.OSType.MACOSX || os == OSInfo.OSType.LINUX) {

            String[] localCmd = new String[command.length + 2];
            localCmd[0] = "/bin/sh";
            localCmd[1] = "-c";
            for(int i = 0; i < command.length; i++){
                localCmd[i+2] = command[i];
            }

            /*String[] localCmd = {
                    "/bin/sh",
                    "-c",
                    command
            };*/
            specificCommand = localCmd;
        } else if (os == OSInfo.OSType.WINDOWS){
            //String[] localCmd = {command};
            specificCommand = command;

        } else if (os == OSInfo.OSType.SOLARIS){
            //TODO

        }
        return specificCommand;
    }

    public static synchronized String deleteDirectory(Path directory){
        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(java.nio.file.Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(java.nio.file.Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }

            });
            return "Direcotry " + directory.getFileName() + " has been deleted.";
        } catch (IOException e) {
            logger.error("Error ServerSystemUtil deleteDirectory: " + e.getLocalizedMessage());
            return "Directory " + directory.getFileName() + " could not be deleted.";
        }
    }

}
