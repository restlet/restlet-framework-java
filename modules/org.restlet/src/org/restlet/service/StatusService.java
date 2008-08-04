/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.service;

import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;

/**
 * Service to handle error statuses. If an exception is thrown within your
 * application or Restlet code, it will be intercepted by this service if it is
 * enabled.<br>
 * <br>
 * When an exception or an error is caught, the
 * {@link #getStatus(Throwable, Request, Response)} method is first invoked to
 * obtain the status that you want to set on the response. If this method isn't
 * overridden or returns null, the {@link Status#SERVER_ERROR_INTERNAL} constant
 * will be set by default.<br>
 * <br>
 * Also, when the status of a response returned is an error status (see
 * {@link Status#isError()}, the
 * {@link #getRepresentation(Status, Request, Response)} method is then invoked
 * to give your service a chance to override the default error page.<br>
 * <br>
 * If you want to customize the default behavior, you need to create a subclass
 * of StatusService that overrides some or all of the methods mentioned above.
 * Then, just create a instance of your class and set it on your Component or
 * Application via the setStatusService() methods.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class StatusService extends Service {
    /** The email address to contact in case of error. */
    private volatile String contactEmail;

    /** The home URI to propose in case of error. */
    private volatile Reference homeRef;

    /** True if an existing entity should be overwritten. */
    private volatile boolean overwrite;

    /**
     * Constructor.
     */
    public StatusService() {
        this(true);
    }

    /**
     * Constructor.
     * 
     * @param enabled
     *            True if the service has been enabled.
     */
    public StatusService(boolean enabled) {
        super(enabled);
        this.contactEmail = null;
        this.homeRef = null;
        this.overwrite = false;
    }

    /**
     * Returns the email address to contact in case of error. This is typically
     * used when creating the status representations.
     * 
     * @return The email address to contact in case of error.
     */
    public String getContactEmail() {
        return this.contactEmail;
    }

    /**
     * Returns the home URI to propose in case of error.
     * 
     * @return The home URI to propose in case of error.
     */
    public Reference getHomeRef() {
        return this.homeRef;
    }

    /**
     * Returns a representation for the given status.<br>
     * In order to customize the default representation, this method can be
     * overriden. It returns null by default.
     * 
     * @param status
     *            The status to represent.
     * @param request
     *            The request handled.
     * @param response
     *            The response updated.
     * @return The representation of the given status.
     */
    public Representation getRepresentation(Status status, Request request,
            Response response) {
        return null;
    }

    /**
     * Returns a status for a given exception or error. By default it returns an
     * {@link Status#SERVER_ERROR_INTERNAL} status and logs a severe message.<br>
     * In order to customize the default behavior, this method can be overriden.
     * 
     * @param throwable
     *            The exception or error caught.
     * @param request
     *            The request handled.
     * @param response
     *            The response updated.
     * @return The representation of the given status.
     */
    public Status getStatus(Throwable throwable, Request request,
            Response response) {
        return null;
    }

    /**
     * Indicates if an existing entity should be overwritten. False by default.
     * 
     * @return True if an existing entity should be overwritten.
     */
    public boolean isOverwrite() {
        return this.overwrite;
    }

    /**
     * Sets the email address to contact in case of error. This is typically
     * used when creating the status representations.
     * 
     * @param contactEmail
     *            The email address to contact in case of error.
     */
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    /**
     * Sets the home URI to propose in case of error.
     * 
     * @param homeRef
     *            The home URI to propose in case of error.
     */
    public void setHomeRef(Reference homeRef) {
        this.homeRef = homeRef;
    }

    /**
     * Indicates if an existing entity should be overwritten.
     * 
     * @param overwrite
     *            True if an existing entity should be overwritten.
     */
    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

}
