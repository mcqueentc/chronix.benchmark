package de.qaware.chronix.shared.ServerConfig;

import de.qaware.chronix.database.BenchmarkDataSource;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.nio.file.StandardCopyOption.*;

/**
 * Created by mcqueen666 on 08.08.16.
 *
 */
public class TSDBInterfaceHandler {

    private static TSDBInterfaceHandler instance;
    private final Logger logger = LoggerFactory.getLogger(TSDBInterfaceHandler.class);
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

    public String getInterfaceDirectory() {
        return interfaceDirectory;
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
     * Get all full class names contained in given jar file.
     *
     * @param file the jar file
     * @return List of all contained full class names.
     */
    public List<String> getClassesFromJarFile(File file){
        if(file != null && file.exists()){
            if(file.getName().endsWith(".jar")){
                List<String> classes = new LinkedList<>();
                try{
                    JarFile jarFile = new JarFile(file);
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()){
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if(name.endsWith(".class")){
                            if(name.lastIndexOf(".class") != -1) {
                                name = name.substring(0, name.lastIndexOf(".class"));
                            }
                            if (name.indexOf("/")!= -1) {
                                name = name.replaceAll("/", ".");
                            }
                            classes.add(name);
                        }
                    }

                } catch (IOException e) {
                    logger.error("Error TSDBInterfaceHandler: " + e.getLocalizedMessage());
                }
                return classes;
            }
        }

        return null;
    }

    /**
     * Get the class name of the the implementation of given interface.
     * NOTE: Only one implementation per jar file is supported.
     *
     * @param file the jar file
     * @param iface the interface for the implementation
     * @return the full class name of the implementation.
     */
    public String getImplementingClassNameFromJarFile(File file, Class<?> iface){
        List<String> classes = getClassesFromJarFile(file);
        if(classes != null && !classes.isEmpty()){
            for(String className : classes){
                Class<?> clazz;
                try {
                    // it is clear at this point, that file is indeed a jar file
                    URLClassLoader classLoader = new URLClassLoader(new URL[] {file.toURI().toURL()},TSDBInterfaceHandler.class.getClassLoader());
                    clazz = classLoader.loadClass(className);

                    if(iface.isAssignableFrom(clazz) && !clazz.equals(iface)){
                        return className;
                    }

                } catch (Exception e) {
                    logger.error("Error TSDBInterfaceHandler: " + e.getLocalizedMessage());
                }
            }
        }

        return null;
    }




    /**
     * Copys the given interface implementation jar file to config directory and renames it to given class name.
     *
     * @param jarFile the interface implementation jar file
     * @param tsdbName the time series database name which was implemented
     * @return true if copy was successful
     */
    public boolean copyTSDBInterface(File jarFile, String tsdbName){
        if(jarFile.exists()){
            File target = new File(interfaceDirectory + tsdbName + ".jar");
            try {
                Files.copy(jarFile.toPath(), target.toPath(), REPLACE_EXISTING);

            } catch (IOException e) {
                logger.error("Error TSDBInterfaceHandler: " + e.getLocalizedMessage());
                return false;
            }

            if(target.exists()){
                interfaces.put(tsdbName, target);
                return true;
            }
        }
        return false;
    }

    /**
     * Copys the given interface implementation jar file to config directory and renames it to given class name.
     *
     * @param fileInputStream the file input stream of the interface implementation jar file
     * @param tsdbName the time series database name which was implemented
     * @return true if copy was successful
     */
    public boolean copyTSDBInterface(InputStream fileInputStream, String tsdbName){
        if(fileInputStream != null){
            File target = new File(interfaceDirectory + tsdbName + ".jar");
            try {
                FileOutputStream outputStream = new FileOutputStream(target);
                IOUtils.copy(fileInputStream, outputStream);
                outputStream.close();

            } catch (Exception e) {
                logger.error("Error TSDBInterfaceHandler: " + e.getLocalizedMessage());
                return false;
            }

            if(target.exists()){
                interfaces.put(tsdbName, target);
                return true;
            }
        }

        return false;
    }


    /**
     * Get an instance of the implemented interface given by class name
     *
     * @param tsdbName the name of the implemented time series database
     * @return instance of implemented interface as BenchmarkDataSource
     */
    public BenchmarkDataSource getTSDBInstance(String tsdbName){
        if(interfaces.containsKey(tsdbName)){
            File classFile = interfaces.get(tsdbName);
            if(classFile.exists()){
                try {

                    String className = getImplementingClassNameFromJarFile(classFile, BenchmarkDataSource.class);
                    if(className != null){
                        URLClassLoader classLoader = new URLClassLoader(new URL[] {classFile.toURI().toURL()},TSDBInterfaceHandler.class.getClassLoader());
                        Class<?> tsdbInterface = classLoader.loadClass(className);
                        Class<? extends BenchmarkDataSource> castedTsdbInterface = tsdbInterface.asSubclass(BenchmarkDataSource.class);
                        BenchmarkDataSource castedTsdbInterfaceInstance = castedTsdbInterface.newInstance();
                        return  castedTsdbInterfaceInstance;
                    }

                } catch (Exception e) {
                    logger.error("Error TSDBInterfaceHandler: " + e.getLocalizedMessage());
                    return null;
                }
            }
        }

        return null;
    }

}
