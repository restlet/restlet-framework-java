/**
 * Copyright 2005-2010 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.sip;

import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.util.Series;

/**
 * Used by the SIP "Subscription-State" header.
 * 
 * @author Thierry Boileau
 */
public class SubscriptionState {

    public static final String REASON_DEACTIVATED = "deactivated";

    public static final String REASON_GIVEUP = "giveup";

    public static final String REASON_NORESOURCE = "noresource";

    public static final String REASON_PROBATION = "probation";

    public static final String REASON_REJECTED = "rejected";

    public static final String REASON_TIMEOUT = "timeout";

    public static final String STATE_ACTIVE = "active";

    public static final String STATE_PENDING = "pending";

    public static final String STATE_TERMINATED = "terminated";

    /** The expiration delay in seconds. */
    private long expires;

    /** The subscription state parameters. */
    private Series<Parameter> parameters;

    /** The subscription state reason. */
    private String reason;

    /** How long the service is expected to be unavailable. */
    private long retryAfter;

    /** The subscription state value. */
    private String value;

    /**
     * Constructor.
     * 
     * @param value
     *            The subscription state value.
     */
    public SubscriptionState(String value) {
        super();
        this.value = value;
    }

    /**
     * Returns the expiration delay in seconds.
     * 
     * @return The expiration delay in seconds.
     */
    public long getExpires() {
        return expires;
    }

    /**
     * Returns the subscription state parameters.
     * 
     * @return The subscription state parameters.
     */
    public Series<Parameter> getParameters() {
        if (parameters == null) {
            parameters = new Form();
        }
        return parameters;
    }

    /**
     * Returns the subscription state reason.
     * 
     * @return The subscription state reason.
     */
    public String getReason() {
        return reason;
    }

    /**
     * Returns how long the service is expected to be unavailable.
     * 
     * @return How long the service is expected to be unavailable.
     */
    public long getRetryAfter() {
        return retryAfter;
    }

    /**
     * Returns the subscription state value.
     * 
     * @return The subscription state value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the expiration delay in seconds.
     * 
     * @param expires
     *            The expiration delay in seconds.
     */
    public void setExpires(long expires) {
        this.expires = expires;
    }

    /**
     * Sets the subscription state parameters.
     * 
     * @param parameters
     *            The subscription state parameters.
     */
    public void setParameters(Series<Parameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * Sets the subscription state reason.
     * 
     * @param reason
     *            The subscription state reason.
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Sets how long the service is expected to be unavailable.
     * 
     * @param retryAfter
     *            How long the service is expected to be unavailable
     */
    public void setRetryAfter(long retryAfter) {
        this.retryAfter = retryAfter;
    }

    /**
     * Sets the subscription state value.
     * 
     * @param value
     *            The subscription state value.
     */
    public void setValue(String value) {
        this.value = value;
    }

}
