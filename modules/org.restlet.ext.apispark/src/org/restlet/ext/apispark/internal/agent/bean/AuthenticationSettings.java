package org.restlet.ext.apispark.internal.agent.bean;

import java.util.concurrent.TimeUnit;

/**
 * @author Manuel Boillod
 */
@SuppressWarnings("ALL")
public class AuthenticationSettings {

    /**
     * Indicates if the authentication is optional
     */
    private boolean optional = false;

    private long cacheTimeToLiveSeconds = TimeUnit.MINUTES.toSeconds(5);

    private int cacheSize = 1000;

    public long getCacheTimeToLiveSeconds() {
        return cacheTimeToLiveSeconds;
    }

    public void setCacheTimeToLiveSeconds(long cacheTimeToLiveSeconds) {
        this.cacheTimeToLiveSeconds = cacheTimeToLiveSeconds;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }
}
