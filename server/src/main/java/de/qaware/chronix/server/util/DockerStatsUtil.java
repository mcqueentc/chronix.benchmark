package de.qaware.chronix.server.util;

import com.sun.management.OperatingSystemMXBean;
import de.qaware.chronix.shared.DataModels.Pair;
import de.qaware.chronix.shared.DataModels.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mcqueen666 on 19.08.16.
 */
public class DockerStatsUtil {
    private final Logger logger = LoggerFactory.getLogger(DockerStatsUtil.class);
    private final long MEASURE_INTERVAL_MILLISECONDS = 100;
    private final long DOCKER_STATS_REACTION_MILLISECONDS = 1000;
    private final int NUMBER_OF_THREADS = 4;

    private static DockerStatsUtil instance;
    private volatile MeasureRunner[] threads;


    private DockerStatsUtil(){
       // OperatingSystemMXBean oSMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        threads = new MeasureRunner[NUMBER_OF_THREADS];

    }

    public static synchronized DockerStatsUtil getInstance(){
        if(instance == null){
            instance = new DockerStatsUtil();
        }
        return instance;
    }


    /**
     * Starts a threaded docker stats measurement of given container id.
     *
     * @param containerID the containerID of the container to be measured.
     */
    public synchronized void startDockerContainerMeasurement(String containerID){
        if(containerID != null && !containerID.isEmpty()) {
            for(int i = 0; i < threads.length; i++) {
                threads[i] = new MeasureRunner(containerID, true);
                threads[i].start();
                try {
                    Thread.sleep(DOCKER_STATS_REACTION_MILLISECONDS / threads.length);
                } catch (InterruptedException e) {
                    logger.error("Error DockerStatsUtil startDockerContainerMeasurement: " + e.getLocalizedMessage());
                }

            }
        }
    }

    /**
     * Ends the previously started docker stats measurement.
     *
     * @return A list of Pairs containing as Pair.first the cpu usage in % and as Pair.second the memory usage in %
     */
    public synchronized List<DockerStatsRecord> stopDockerContainerMeasurement(){
        List<DockerStatsRecord> completeMeasures = new LinkedList<>();
        for(int i = 0; i < threads.length; i++){
            threads[i].stopRunning();
            try {
                threads[i].join(2 * DOCKER_STATS_REACTION_MILLISECONDS);
                completeMeasures.addAll(threads[i].getMeasures());

            } catch (Exception e) {
               logger.error("Error DockerStatsUtil stopDockerContainerMeasurement: " + e.getLocalizedMessage());
            }
        }

        return completeMeasures;
    }


    /**
     * Estimates the storage size of the given container.
     *
     * @param containerName the name of the docker container
     * @param mappedStorageDirectoryPath the path to the mapped storage directory on the host.
     * @return the size of the storage directory mapped from container to host in bytes.
     */
    public Long estimateStorageSize(String containerName, String mappedStorageDirectoryPath){
        Long resultBytes = -1L;
        String containerID = DockerCommandLineUtil.getRunningContainerId(containerName);
        if(!containerID.isEmpty()){
            String[] command = ServerSystemUtil.getOsSpecificCommand(new String[]{
                    "/usr/bin/du -c -b --max=1 "
                    + mappedStorageDirectoryPath
                    + " | awk '{print $1}'"});

            List<String> answers = ServerSystemUtil.executeCommand(command);
            if(!answers.isEmpty()){
                try {
                    resultBytes = Long.valueOf(answers.get(answers.size() - 1));
                } catch (Exception e){
                    logger.error("Error estimating storage size. Result is not a number: {}",e.getLocalizedMessage());
                    resultBytes = -1L;
                }
            }
        }

        return resultBytes;
    }


    private class MeasureRunner extends Thread{
        private List<DockerStatsRecord> measures = new LinkedList<>();
        private String containerID;
        private String[] command;
        private volatile boolean running;

        public MeasureRunner(String containerID, boolean running){
            this.containerID = containerID;
            command = ServerSystemUtil.getOsSpecificCommand(new String[]{DockerCommandLineUtil.getDockerInstallPath()
                    + "docker stats "
                    + containerID
                    + " --no-stream | grep "
                    + containerID
                    + " | awk '{print $2 $8 $14$15\"%\" $17$18\"%\" $9$10\"%\" $12$13\"%\"}'"}); // cpu%mem%readblock%writeblock%netDOWN%netUP
            this.running = running;
        }

        public List<DockerStatsRecord> getMeasures(){
            return measures;
        }

        public synchronized void stopRunning(){
            this.running = false;
        }

        public void run(){
            while(this.running) {
                try {
                    List<String> answers = ServerSystemUtil.executeCommand(command);
                    String[] splits = answers.get(0).split("%");
                    if (splits.length == 6) {
                        // cpu%mem%readblock%writeblock%netDOWN%netUP
                        Double cpuUsage = Double.valueOf(splits[0]);
                        Double memoryUsage = Double.valueOf(splits[1]);
                        Long readBytes = getBytesCountFromString(splits[2]);
                        Long writtenBytes = getBytesCountFromString(splits[3]);
                        Long networkDownloadedBytes = getBytesCountFromString(splits[4]);
                        Long networkUploadedBytes = getBytesCountFromString(splits[5]);

                        //logger.info("MeasureRunner: cpu: {}, mem: {}, readBytes: {}, writtenBytes: {} netDown: {}, netUp: {}",cpuUsage,memoryUsage,readBytes,writtenBytes,networkDownloadedBytes,networkUploadedBytes);
                        measures.add(new DockerStatsRecord(cpuUsage, memoryUsage, readBytes, writtenBytes, networkDownloadedBytes, networkUploadedBytes));
                    }
                } catch (Exception e){
                    logger.error("Error doing docker measurement: {}", e.getLocalizedMessage());
                    break;
                }
            }

        }

        private Long getBytesCountFromString(String s){
            Double result = new Double(-1);
            Character last = s.charAt(s.length()-1);
            Character secondLast = s.charAt(s.length()-2);

            if(last == 'B') {
                if (Character.getType(secondLast) == Character.LOWERCASE_LETTER || Character.getType(secondLast) == Character.UPPERCASE_LETTER) {
                    result = Double.valueOf(s.substring(0, s.length()-2));
                    switch (secondLast){
                        case 'k': result *= 1024;
                            break;
                        case 'M': result *= 1024 * 1024;
                            break;
                        case 'G': result *= 1024 * 1024 * 1024;
                            break;
                    }
                } else {
                    // secondLast is not a letter -> only Bytes here
                    result = Double.valueOf(s.substring(0, s.length()-1));
                }
            }
            return result.longValue();
        }


    }

}
