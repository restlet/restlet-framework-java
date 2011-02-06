/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.engine.application;

import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.routing.Filter;
import org.restlet.service.StatusService;

// [excludes gwt]
/**
 * Filter associating a response entity based on the status. In order to
 * customize the default representation, just subclass this class and override
 * the "getRepresentation" method.<br>
 * If any exception occurs during the call handling, a "server internal error"
 * status is automatically associated to the call. Of course, you can
 * personalize the representation of this error. Also, if no status is set
 * (null), then the "success OK" status is assumed.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Jerome Louvel
 */
public class StatusFilter extends Filter {
    /** The email address of the administrator to contact in case of error. */
    private volatile String contactEmail;

    /** The home URI to propose in case of error. */
    private volatile Reference homeRef;

    /** Indicates if existing representations should be overwritten. */
    private volatile boolean overwriting;

    /** The helped status service. */
    private volatile StatusService statusService;

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param overwriting
     *            Indicates whether an existing representation should be
     *            overwritten.
     * @param email
     *            Email address of the administrator to contact in case of
     *            error.
     * @param homeRef
     *            The home URI to propose in case of error.
     */
    public StatusFilter(Context context, boolean overwriting, String email,
            Reference homeRef) {
        super(context);
        this.overwriting = overwriting;
        this.contactEmail = email;
        this.homeRef = homeRef;
        this.statusService = null;
    }

    /**
     * Constructor from a status service.
     * 
     * @param context
     *            The context.
     * @param statusService
     *            The helped status service.
     */
    public StatusFilter(Context context, StatusService statusService) {
        this(context, statusService.isOverwriting(), statusService
                .getContactEmail(), statusService.getHomeRef());
        this.statusService = statusService;
    }

    /**
     * Allows filtering after its handling by the target Restlet. Does nothing
     * by default.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    @Override
    public void afterHandle(Request request, Response response) {
        // If no status is set, then the "success ok" status is assumed.
        if (response.getStatus() == null) {
            response.setStatus(Status.SUCCESS_OK);
        }

        // Do we need to get a representation for the current status?
        if (response.getStatus().isError()
                && ((response.getEntity() == null) || isOverwriting())) {
            response.setEntity(getRepresentation(response.getStatus(), request,
                    response));
        }
    }

    /**
     * Handles the call by distributing it to the next Restlet. If a throwable
     * is caught, the {@link #getStatus(Throwable, Request, Response)} method is
     * invoked.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @return The continuation status.
     */
    @Override
    protected int doHandle(Request request, Response response) {
        // Normally handle the call
        try {
            super.doHandle(request, response);
        } catch (Throwable t) {
            getLogger().log(Level.WARNING,
                    "Exception or error caught in status service", t);
            response.setStatus(getStatus(t, request, response));
        }

        return CONTINUE;
    }

    /**
     * Returns the email address of the administrator to contact in case of
     * error.
     * 
     * @return The email address.
     */
    public String getContactEmail() {
        return contactEmail;
    }

    /**
     * Returns a representation for the given status.<br>
     * In order to customize the default representation, this method can be
     * overridden.
     * 
     * @param status
     *            The status to represent.
     * @param request
     *            The request handled.
     * @param response
     *            The response updated.
     * @return The representation of the given status.
     */
    protected Representation getDefaultRepresentation(Status status,
            Request request, Response response) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<html>\n");
        sb.append("<head>\n");
        sb.append("   <title>Status page</title>\n");
        sb.append("</head>\n");
        sb.append("<body style=\"font-family: sans-serif;\">\n");

        sb.append("<p style=\"font-size: 1.2em;font-weight: bold;margin: 1em 0px;\">");
        sb.append(getStatusInfo(status));
        sb.append("</p>\n");
        if (status.getDescription() != null) {
            sb.append("<p>");
            sb.append(status.getDescription());
            sb.append("</p>\n");
        }

        sb.append("<p>You can get technical details <a href=\"");
        sb.append(status.getUri());
        sb.append("\">here</a>.<br>\n");

        if (getContactEmail() != null) {
            sb.append("For further assistance, you can contact the <a href=\"mailto:");
            sb.append(getContactEmail());
            sb.append("\">administrator</a>.<br>\n");
        }

        if (getHomeRef() != null) {
            sb.append("Please continue your visit at our <a href=\"");
            sb.append(getHomeRef());
            sb.append("\">home page</a>.\n");
        }

        sb.append("</p>\n");
        sb.append("</body>\n");
        sb.append("</html>\n");

        return new StringRepresentation(sb.toString(), MediaType.TEXT_HTML);
    }

    /**
     * Returns the home URI to propose in case of error.
     * 
     * @return The home URI.
     */
    public Reference getHomeRef() {
        return homeRef;
    }

    /**
     * Returns a representation for the given status.<br>
     * In order to customize the default representation, this method can be
     * overridden.
     * 
     * @param status
     *            The status to represent.
     * @param request
     *            The request handled.
     * @param response
     *            The response updated.
     * @return The representation of the given status.
     */
    protected Representation getRepresentation(Status status, Request request,
            Response response) {
        Representation result = getStatusService().getRepresentation(status,
                request, response);

        if (result == null) {
            result = getDefaultRepresentation(status, request, response);
        }

        return result;
    }

    /**
     * Returns a status for a given exception or error. By default it returns an
     * {@link Status#SERVER_ERROR_INTERNAL} status including the related error
     * or exception and logs a severe message.<br>
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
    protected Status getStatus(Throwable throwable, Request request,
            Response response) {
        return getStatusService().getStatus(throwable, request, response);
    }

    /**
     * Returns the status information to display in the default representation.
     * By default it returns the status's name.
     * 
     * @param status
     *            The status.
     * @return The status information.
     * @see #getDefaultRepresentation(Status, Request, Response)
     */
    protected String getStatusInfo(Status status) {
        return (status.getName() != null) ? status.getName()
                : "No information available for this result status";
    }

    /**
     * Returns the helped status service.
     * 
     * @return The helped status service.
     */
    public StatusService getStatusService() {
        return statusService;
    }

    /**
     * Indicates if existing representations should be overwritten.
     * 
     * @return True if existing representations should be overwritten.
     * @deprecated Use {@link #isOverwriting()} instead.
     */
    @Deprecated
    public boolean isOverwrite() {
        return overwriting;
    }

    /**
     * Indicates if existing representations should be overwritten.
     * 
     * @return True if existing representations should be overwritten.
     */
    public boolean isOverwriting() {
        return isOverwrite();
    }

    /**
     * Sets the email address of the administrator to contact in case of error.
     * 
     * @param email
     *            The email address.
     */
    public void setContactEmail(String email) {
        this.contactEmail = email;
    }

    /**
     * Sets the home URI to propose in case of error.
     * 
     * @param homeRef
     *            The home URI.
     */
    public void setHomeRef(Reference homeRef) {
        this.homeRef = homeRef;
    }

    /**
     * Indicates if existing representations should be overwritten.
     * 
     * @param overwriting
     *            True if existing representations should be overwritten.
     * @deprecated Use {@link #setOverwriting(boolean)} instead.
     */
    @Deprecated
    public void setOverwrite(boolean overwriting) {
        this.overwriting = overwriting;
    }

    /**
     * Indicates if existing representations should be overwritten.
     * 
     * @param overwriting
     *            True if existing representations should be overwritten.
     */
    public void setOverwriting(boolean overwriting) {
        setOverwrite(overwriting);
    }

    /**
     * Sets the helped status service.
     * 
     * @param statusService
     *            The helped status service.
     */
    public void setStatusService(StatusService statusService) {
        this.statusService = statusService;
    }
}
