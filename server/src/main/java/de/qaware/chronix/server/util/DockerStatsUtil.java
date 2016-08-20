package de.qaware.chronix.server.util;

import com.sun.management.OperatingSystemMXBean;
import de.qaware.chronix.shared.DataModels.Pair;

import java.lang.management.ManagementFactory;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mcqueen666 on 19.08.16.
 */
public class DockerStatsUtil {
    private static final long MEASURE_INTERVAL_MILLISECONDS = 100;
    private static final long DOCKER_STATS_REACTION_MILLISECONDS = 2000;

    private static DockerStatsUtil instance;
    private MeasureRunner[] threads;


    private DockerStatsUtil(){
        OperatingSystemMXBean oSMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        threads = new MeasureRunner[oSMXBean.getAvailableProcessors()];

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
    public void startDockerContainerMeasurement(String containerID){
        if(containerID != null && !containerID.isEmpty()) {
            for(int i = 0; i < threads.length; i++) {
                threads[i] = new MeasureRunner(containerID, true);
                threads[i].start();
                try {
                    Thread.sleep(DOCKER_STATS_REACTION_MILLISECONDS / threads.length);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     * Ends the previously started docker stats measurement.
     *
     * @return A list of Pairs containing as Pair.first the cpu usage in % and as Pair.second the memory usage in %
     */
    public List<Pair<Double,Double>> stopDockerContainerMeasurement(){
        List<Pair<Double,Double>> completeMeasures = new LinkedList<>();
        for(int i = 0; i < threads.length; i++){
            threads[i].stopRunning();
            try {
                threads[i].join(2 * DOCKER_STATS_REACTION_MILLISECONDS);
                completeMeasures.addAll(threads[i].getMeasures());

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return completeMeasures;
    }


    private class MeasureRunner extends Thread{
        private List<Pair<Double,Double>> measures = new LinkedList<>();
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
                    + " | awk '{print $2 $8}'"});
            this.running = running;
        }

        public List<Pair<Double,Double>> getMeasures(){
            return measures;
        }

        public synchronized void stopRunning(){
            this.running = false;
        }

        public void run(){
            while(this.running) {
                List<String> answers = ServerSystemUtil.executeCommand(command);
                String[] splits = answers.get(0).split("%");
                Pair<Double, Double> record = Pair.of(Double.valueOf(splits[0]), Double.valueOf(splits[1]));
                measures.add(record);
            }

        }


    }

}
