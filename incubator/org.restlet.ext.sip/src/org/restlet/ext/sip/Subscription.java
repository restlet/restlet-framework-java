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
 * Describes the state of a subscription. Used by the SIP "Subscription-State"
 * header.
 * 
 * @author Thierry Boileau
 */
public class Subscription {

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

    /** The subscription parameters. */
    private Series<Parameter> parameters;

    /** The subscription reason. */
    private String reason;

    /** How long the service is expected to be unavailable. */
    private long retryAfter;

    /** The subscription value. */
    private String value;

    /**
     * Constructor.
     * 
     * @param value
     *            The subscription value.
     */
    public Subscription(String value) {
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
     * Returns the subscription parameters.
     * 
     * @return The subscription parameters.
     */
    public Series<Parameter> getParameters() {
        if (parameters == null) {
            parameters = new Form();
        }
        return parameters;
    }

    /**
     * Returns the subscription reason.
     * 
     * @return The subscription reason.
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
     * Returns the subscription value.
     * 
     * @return The subscription value.
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
     * Sets the subscription parameters.
     * 
     * @param parameters
     *            The subscription parameters.
     */
    public void setParameters(Series<Parameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * Sets the subscription reason.
     * 
     * @param reason
     *            The subscription reason.
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
