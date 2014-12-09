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

package org.restlet.engine.application;

import org.restlet.data.Status;

import java.io.Serializable;

/**
 * 
 * Representation of a {@link Status}.
 * 
 * @author Manuel Boillod
 */
public class StatusInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The specification code. */
    private volatile int code;

    /** The longer description. */
    private volatile String description;

    /** The short reason phrase. */
    private volatile String reasonPhrase;

    /**
     * Empty Constructor
     */
    public StatusInfo() {
    }

    /**
     * Constructor.
     * 
     * @param code
     *            The specification code.
     * @param description
     *            The longer description.
     * @param reasonPhrase
     *            The short reason phrase.
     */
    public StatusInfo(int code, String description, String reasonPhrase) {
        super();
        this.code = code;
        this.description = description;
        this.reasonPhrase = reasonPhrase;
    }

    /**
     * Constructor.
     * 
     * @param status
     *            The represented status.
     */
    public StatusInfo(Status status) {
        this(status.getCode(), status.getDescription(), status
                .getReasonPhrase());
    }

    /**
     * Returns the code of the status.
     * 
     * @return The code of the status.
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the description of the status.
     *
     * @return The description of the status.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the short description of the status.
     *
     * @return The short description of the status.
     */
    public String getReasonPhrase() {
        return reasonPhrase;
    }

    /**
     * Sets the code of the status.
     * 
     * @param code
     *            The code of the status.
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * Sets the description of the status.
     *
     * @param description
     *            The description of the status.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the short description of the status.
     *
     * @param reasonPhrase
     *            The short description of the status.
     */
    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }
}