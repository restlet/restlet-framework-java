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
 * Provides availability details on a SIP agent. Used by the SIP "Retry-After"
 * header.
 * 
 * @author Thierry Boileau
 */
public class Availability {

    /** Indicates additional information about the time of callback. */
    private String comment;

    /** The time the service is expected to be unavailable. */
    private int delay;

    /** The time the service will be available. */
    private int duration;

    /** Other parameters. */
    private Series<Parameter> parameters;

    /**
     * Constructor.
     * 
     * @param delay
     *            The time the service is expected to be unavailable.
     */
    public Availability(int delay) {
        super();
        this.delay = delay;
    }

    /**
     * Returns additional information about the time of callback.
     * 
     * @return Additional information about the time of callback.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Returns the time the service is expected to be unavailable.
     * 
     * @return The time the service is expected to be unavailable.
     */
    public int getDelay() {
        return delay;
    }

    /**
     * Returns the time the service will be available.
     * 
     * @return The time the service will be available.
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Returns the list of other parameters.
     * 
     * @return The list of other parameters.
     */
    public Series<Parameter> getParameters() {
        if (parameters == null) {
            parameters = new Form();
        }
        return parameters;
    }

    /**
     * Sets additional information about the time of callback.
     * 
     * @param comment
     *            Additional information about the time of callback.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Sets the time the service is expected to be unavailable.
     * 
     * @param delay
     *            The time the service is expected to be unavailable.
     */
    public void setDelay(int delay) {
        this.delay = delay;
    }

    /**
     * Sets the time the service will be available.
     * 
     * @param duration
     *            The time the service will be available.
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * Sets the list of other parameters.
     * 
     * @param parameters
     *            The list of other parameters.
     */
    public void setParameters(Series<Parameter> parameters) {
        this.parameters = parameters;
    }
}
