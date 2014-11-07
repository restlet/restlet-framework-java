package org.restlet.ext.apispark.internal.agent.bean;

import java.util.List;

public class FirewallSettings {

    private List<FirewallIpFilter> ipFilters;

    private List<FirewallRateLimit> rateLimits;

    public List<FirewallIpFilter> getIpFilters() {
        return ipFilters;
    }

    public List<FirewallRateLimit> getRateLimits() {
        return rateLimits;
    }

    public void setIpFilters(List<FirewallIpFilter> ipFilters) {
        this.ipFilters = ipFilters;
    }

    public void setRateLimits(List<FirewallRateLimit> rateLimits) {
        this.rateLimits = rateLimits;
    }
}
