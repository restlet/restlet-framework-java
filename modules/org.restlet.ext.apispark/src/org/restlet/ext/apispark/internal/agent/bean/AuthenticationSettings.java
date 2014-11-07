package org.restlet.ext.apispark.internal.agent.bean;

import java.util.concurrent.TimeUnit;

/**
 * @author Manuel Boillod
 */
public class AuthenticationSettings {

    private int cacheSize = 1000;

    private long cacheTimeToLiveSeconds = TimeUnit.MINUTES.toSeconds(5);

    /**
     * Indicates if the authentication is optional.
     */
    private boolean optional = false;

    public int getCacheSize() {
        return cacheSize;
    }

    public long getCacheTimeToLiveSeconds() {
        return cacheTimeToLiveSeconds;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public void setCacheTimeToLiveSeconds(long cacheTimeToLiveSeconds) {
        this.cacheTimeToLiveSeconds = cacheTimeToLiveSeconds;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }
}
