package de.qaware.chronix.client.benchmark.configurator.util;

import ServerConfig.ServerConfigAccessor;
import database.BenchmarkDataSource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardCopyOption.*;

/**
 * Created by mcqueen666 on 08.08.16.
 *
 */
public class TSDBInterfaceHandler {

    private static TSDBInterfaceHandler instance;
    private String interfaceDirectoryName = "interfaces";
    private String interfaceDirectory;
    private ServerConfigAccessor serverConfigAccessor;
    private Map<String, File> interfaces;

    private TSDBInterfaceHandler(){
        serverConfigAccessor = ServerConfigAccessor.getInstance();
        interfaceDirectory = serverConfigAccessor.getConfigDirectory() + interfaceDirectoryName + File.separator;
        makeInterfaceDirectory();
        interfaces = new HashMap<>();
        readInterfaceDirectory();

    }

    public static synchronized TSDBInterfaceHandler getInstance(){
        if(instance == null){
            instance = new TSDBInterfaceHandler();
        }
        return instance;
    }

    private void makeInterfaceDirectory(){
        File directory = new File(interfaceDirectory);
        if(!directory.exists()){
            directory.mkdir();
        }
    }

    private void readInterfaceDirectory(){
        File directory = new File(interfaceDirectory);
        if(directory.exists()){
            File[] fileList = directory.listFiles();
            for(File file : fileList){
                interfaces.put(file.getName().replaceAll(".jar", ""), file);
            }
        }
    }

    /**
     * Copys the given interface implementation jar file to config directory and renames it to given class name.
     *
     * @param jarFile the interface implementation jar file
     * @param className the class name which was implemented
     * @return true if copy was successful
     */
    public boolean copyTSDBInterface(File jarFile, String className){
        if(jarFile.exists()){
            File target = new File(interfaceDirectory + className + ".jar");
            try {
                Files.copy(jarFile.toPath(), target.toPath(), REPLACE_EXISTING);

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            if(target.exists()){
                interfaces.put(className, target);
                return true;
            }
        }
        return false;
    }

    /**
     * Get an instance of the implemented interface given by class name
     *
     * @param className the name of the implemented class
     * @return instance of implemented interface as BenchmarkDataSource
     */
    public BenchmarkDataSource getTSDBInstance(String className){
        if(interfaces.containsKey(className)){
            File classFile = interfaces.get(className);
            if(classFile.exists()){
                try {

                    URLClassLoader classLoader = new URLClassLoader(new URL[] {classFile.toURI().toURL()},TSDBInterfaceHandler.class.getClassLoader());
                    Class tsdbInterface = classLoader.loadClass(className);
                    Class<? extends BenchmarkDataSource> castedTsdbInterface = tsdbInterface.asSubclass(BenchmarkDataSource.class);
                    BenchmarkDataSource castedTsdbInterfaceInstance = castedTsdbInterface.newInstance();
                    return  castedTsdbInterfaceInstance;


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        return null;
    }

}
