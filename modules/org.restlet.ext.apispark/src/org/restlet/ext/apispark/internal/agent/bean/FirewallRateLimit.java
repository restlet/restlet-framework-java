package org.restlet.ext.apispark.internal.agent.bean;

public class FirewallRateLimit {

    private boolean defaultRateLimit;

    private String group;

    private String id;

    private String name;

    private int period;

    private int rateLimit;

    public String getGroup() {
        return group;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPeriod() {
        return period;
    }

    public int getRateLimit() {
        return rateLimit;
    }

    public boolean isDefaultRateLimit() {
        return defaultRateLimit;
    }

    public void setDefaultRateLimit(boolean defaultRateLimit) {
        this.defaultRateLimit = defaultRateLimit;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public void setRateLimit(int rateLimit) {
        this.rateLimit = rateLimit;
    }
}
