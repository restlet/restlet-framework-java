/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.sip;

import org.restlet.data.Parameter;
import org.restlet.util.Series;

/**
 * Describes the state of a subscription. Used by the SIP "Subscription-State"
 * header.
 * 
 * @author Thierry Boileau
 */
public class Subscription {

    /**
     * The subscription has been terminated, but the subscriber SHOULD retry
     * immediately with a new subscription.
     * 
     * @see <a
     *      http://tools.ietf.org/html/rfc3265#section-7.2.3">Subscription-State
     *      header</a>
     */
    public static final String REASON_DEACTIVATED = "deactivated";

    /**
     * The subscription has been terminated because the notifier could not
     * obtain authorization in a timely fashion.
     * 
     * @see <a
     *      http://tools.ietf.org/html/rfc3265#section-7.2.3">Subscription-State
     *      header</a>
     */
    public static final String REASON_GIVEUP = "giveup";

    /**
     * The subscription has been terminated because the resource state which was
     * being monitored no longer exists.
     * 
     * @see <a
     *      http://tools.ietf.org/html/rfc3265#section-7.2.3">Subscription-State
     *      header</a>
     */
    public static final String REASON_NORESOURCE = "noresource";

    /**
     * The subscription has been terminated, but the client SHOULD retry at some
     * later time.
     * 
     * @see <a
     *      http://tools.ietf.org/html/rfc3265#section-7.2.3">Subscription-State
     *      header</a>
     */
    public static final String REASON_PROBATION = "probation";

    /**
     * The subscription has been terminated due to change in authorization
     * policy.
     * 
     * @see <a
     *      http://tools.ietf.org/html/rfc3265#section-7.2.3">Subscription-State
     *      header</a>
     */
    public static final String REASON_REJECTED = "rejected";

    /**
     * The subscription has been terminated because it was not refreshed before
     * it expired.
     * 
     * @see <a
     *      http://tools.ietf.org/html/rfc3265#section-7.2.3">Subscription-State
     *      header</a>
     */
    public static final String REASON_TIMEOUT = "timeout";

    /**
     * The subscription has been accepted and (in general) has been authorized.
     * 
     * @see <a
     *      http://tools.ietf.org/html/rfc3265#section-7.2.3">Subscription-State
     *      header</a>
     */
    public static final String STATE_ACTIVE = "active";

    /**
     * The subscription has been received by the notifier, but there is
     * insufficient policy information to grant or deny the subscription yet.
     * 
     * @see <a
     *      http://tools.ietf.org/html/rfc3265#section-7.2.3">Subscription-State
     *      header</a>
     */
    public static final String STATE_PENDING = "pending";

    /**
     * The subscription has been terminated.
     * 
     * @see <a
     *      http://tools.ietf.org/html/rfc3265#section-7.2.3">Subscription-State
     *      header</a>
     */
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
     * Constructor.
     * 
     * @param value
     *            The subscription value.
     * @param reason
     *            The subscription reason.
     */
    public Subscription(String value, String reason) {
        super();
        this.value = value;
        this.reason = reason;
    }

    /**
     * Constructor.
     * 
     * @param value
     *            The subscription value.
     * @param reason
     *            The subscription reason.
     * @param expires
     *            The expiration delay in seconds.
     */
    public Subscription(String value, String reason, int expires) {
        super();
        this.value = value;
        this.reason = reason;
        this.expires = expires;
    }

    /**
     * Constructor.
     * 
     * @param value
     *            The subscription value.
     * @param reason
     *            The subscription reason.
     * @param expires
     *            The expiration delay in seconds.
     */
    public Subscription(String value, String reason, int expires, int retryAfter) {
        this(value, reason, expires);
        this.retryAfter = retryAfter;
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
            parameters = new Series<Parameter>(Parameter.class);
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
