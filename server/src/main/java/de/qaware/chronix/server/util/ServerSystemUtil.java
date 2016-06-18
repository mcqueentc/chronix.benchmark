package de.qaware.chronix.server.util;

import sun.awt.OSInfo;
import java.io.File;

/**
 * Created by mcqueen666 on 17.06.16.
 */
public class ServerSystemUtil {

    static final String benchmarkUtilPath = "chronixBenchmark" + File.separator + "docker";

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
}
