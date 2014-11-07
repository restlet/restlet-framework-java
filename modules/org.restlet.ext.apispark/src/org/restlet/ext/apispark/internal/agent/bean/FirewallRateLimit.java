package org.restlet.ext.apispark.internal.agent.bean;

public class FirewallRateLimit {

    private String id;

    private String name;

    private String group;

    private int period;

    private int rateLimit;

    private boolean defaultRateLimit;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(int rateLimit) {
        this.rateLimit = rateLimit;
    }

    public boolean isDefaultRateLimit() {
        return defaultRateLimit;
    }

    public void setDefaultRateLimit(boolean defaultRateLimit) {
        this.defaultRateLimit = defaultRateLimit;
    }
}
