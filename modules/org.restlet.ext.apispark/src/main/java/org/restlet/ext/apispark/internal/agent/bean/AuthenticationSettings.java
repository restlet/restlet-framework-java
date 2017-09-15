/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

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
