package de.qaware.chronix.common.QueryUtil;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by mcqueen666 on 13.09.16.
 */
@XmlRootElement
public class CleanCommand {
    private String tsdbName;
    private String ipAddress;
    private Integer portNumber;

    public CleanCommand() {
    }

    public CleanCommand(String tsdbName, String ipAddress, Integer portNumber) {
        this.tsdbName = tsdbName;
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
    }

    public String getTsdbName() {
        return tsdbName;
    }

    public void setTsdbName(String tsdbName) {
        this.tsdbName = tsdbName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(Integer portNumber) {
        this.portNumber = portNumber;
    }
}
