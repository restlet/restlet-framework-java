package org.restlet.ext.apispark.internal.agent.bean;

import java.util.List;

/**
 * @author Manuel Boillod
 */
public class FirewallIpFilter {

    /**
     * Indicates if IpFilter is of type white list or black list
     */
    boolean whiteList;

    List<String> ips;

    public boolean isWhiteList() {
        return whiteList;
    }

    public void setWhiteList(boolean whiteList) {
        this.whiteList = whiteList;
    }

    public List<String> getIps() {
        return ips;
    }

    public void setIps(List<String> ips) {
        this.ips = ips;
    }
}
