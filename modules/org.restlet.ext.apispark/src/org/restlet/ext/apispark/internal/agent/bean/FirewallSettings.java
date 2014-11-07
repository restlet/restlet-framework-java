package org.restlet.ext.apispark.internal.agent.bean;

import java.util.List;

public class FirewallSettings {

    private List<FirewallRateLimit> rateLimits;

    private List<FirewallIpFilter> ipFilters;

    public List<FirewallRateLimit> getRateLimits() {
        return rateLimits;
    }

    public void setRateLimits(List<FirewallRateLimit> rateLimits) {
        this.rateLimits = rateLimits;
    }

    public List<FirewallIpFilter> getIpFilters() {
        return ipFilters;
    }

    public void setIpFilters(List<FirewallIpFilter> ipFilters) {
        this.ipFilters = ipFilters;
    }
}
