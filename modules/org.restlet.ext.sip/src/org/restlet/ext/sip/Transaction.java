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

/**
 * Represent a client-side or server-side SIP transaction composed of a request
 * and a series of associated responses.
 * 
 * @author Jerome Louvel
 */
public class Transaction {

    /** The default time out (32s). */
    private final static int DEFAULT_TIMEOUT = 32000;

    /** The creation time of this transaction object. */
    private final long creation;

    /** The time of last activity on this transaction object. */
    private volatile long lastActivity;

    /** The timeout in milliseconds. */
    private final int maxIdleTimeMs;

    /** The initiating request. */
    private final SipRequest request;

    /**
     * Constructor.
     * 
     * @param request
     *            The initiating request.
     */
    public Transaction(SipRequest request) {
        this(request, DEFAULT_TIMEOUT);
    }

    /**
     * Constructor.
     * 
     * @param request
     *            The initiating request.
     */
    public Transaction(SipRequest request, int timeout) {
        this.request = request;
        this.creation = System.currentTimeMillis();
        this.lastActivity = this.creation;
        this.maxIdleTimeMs = timeout;
    }

    public void abort() {
        // TODO
    }

    /**
     * Returns the creation date of this transaction object.
     * 
     * @return The creation date of this transaction object.
     */
    protected long getCreation() {
        return creation;
    }

    /**
     * Returns the date of last activity on this transaction object.
     * 
     * @return The date of last activity on this transaction object.
     */
    protected long getLastActivity() {
        return lastActivity;
    }

    /**
     * Returns the timeout in milliseconds.
     * 
     * @return The timeout in milliseconds.
     */
    protected int getMaxIdleTimeMs() {
        return maxIdleTimeMs;
    }

    /**
     * Returns the initiating request.
     * 
     * @return The initiating request.
     */
    protected SipRequest getRequest() {
        return request;
    }

    /**
     * Indicates if the transaction has timed out due to lack of activity.
     * 
     * @return True if the transaction has timed out due to lack of activity.
     */
    public boolean hasTimedout() {
        return (System.currentTimeMillis() - this.lastActivity) >= getMaxIdleTimeMs();
    }

    /**
     * Indicates that a new activity on this transaction has been detected.
     */
    public void notifyActivity() {
        setLastActivity(System.currentTimeMillis());
    }

    /**
     * Sets the date of last activity on this transaction object.
     * 
     * @param lastActivityTime
     *            The date of last activity on this transaction object.
     */
    private void setLastActivity(long lastActivityTime) {
        this.lastActivity = lastActivityTime;
    }

}
