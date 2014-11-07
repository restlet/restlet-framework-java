package org.restlet.ext.apispark.internal.agent.bean;

import java.util.List;

/**
 * @author Manuel Boillod
 */
public class FirewallIpFilter {

    List<String> ips;

    /**
     * Indicates if IpFilter is of type white list or black list
     */
    boolean whiteList;

    public List<String> getIps() {
        return ips;
    }

    public boolean isWhiteList() {
        return whiteList;
    }

    public void setIps(List<String> ips) {
        this.ips = ips;
    }

    public void setWhiteList(boolean whiteList) {
        this.whiteList = whiteList;
    }
}
