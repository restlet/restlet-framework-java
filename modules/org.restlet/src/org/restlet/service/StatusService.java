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

package org.restlet.service;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Resource;

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
 * @see <a href="http://wiki.restlet.org/docs_2.1/202-restlet.html">User
 *      Guide</a>
 * @author Jerome Louvel
 */
public class StatusService extends Service {
    /** The email address to contact in case of error. */
    private volatile String contactEmail;

    /** The home URI to propose in case of error. */
    private volatile Reference homeRef;

    /** True if an existing entity should be overwritten. */
    private volatile boolean overwriting;

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
        this.homeRef = new Reference("/");
        this.overwriting = false;
    }

    // [ifndef gwt] method
    @Override
    public org.restlet.routing.Filter createInboundFilter(Context context) {
        return new org.restlet.engine.application.StatusFilter(context, this);
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
     * overridden. It returns null by default.
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
     * Returns a status for a given exception or error. By default it unwraps
     * the status of {@link ResourceException}. For other exceptions or errors,
     * it returns an {@link Status#SERVER_ERROR_INTERNAL} status.<br>
     * <br>
     * In order to customize the default behavior, this method can be
     * overridden.
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
        Status result = null;

        if (throwable instanceof ResourceException) {
            ResourceException re = (ResourceException) throwable;

            if (re.getCause() != null) {
                // What is most interesting is the embedded cause
                result = new Status(re.getStatus(), re.getCause());
            } else {
                result = re.getStatus();
            }
        } else {
            result = new Status(Status.SERVER_ERROR_INTERNAL, throwable);
        }

        return result;
    }

    /**
     * Returns a status for a given exception or error. By default it returns an
     * {@link Status#SERVER_ERROR_INTERNAL} status and logs a severe message.<br>
     * In order to customize the default behavior, this method can be
     * overridden.
     * 
     * @param throwable
     *            The exception or error caught.
     * @param resource
     *            The parent resource.
     * @return The representation of the given status.
     */
    public Status getStatus(Throwable throwable, Resource resource) {
        return getStatus(throwable,
                (resource == null) ? null : resource.getRequest(),
                (resource == null) ? null : resource.getResponse());
    }

    /**
     * Indicates if an existing entity should be overwritten. False by default.
     * 
     * @return True if an existing entity should be overwritten.
     */
    public boolean isOverwriting() {
        return this.overwriting;
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
     * @param overwriting
     *            True if an existing entity should be overwritten.
     */
    public void setOverwriting(boolean overwriting) {
        this.overwriting = overwriting;
    }

}
