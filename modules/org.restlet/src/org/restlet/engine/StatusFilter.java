/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
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

package org.restlet.engine;

import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.service.StatusService;

/**
 * Filter associating a response entity based on the status. In order to
 * customize the default representation, just subclass this class and override
 * the "getRepresentation" method.<br>
 * If any exception occurs during the call handling, a "server internal error"
 * status is automatically associated to the call. Of course, you can
 * personalize the representation of this error. Also, if no status is set
 * (null), then the "success ok" status is assumed.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @see <a
 *      href="http://www.restlet.org/documentation/1.1/tutorial#part08">Tutorial
 *      : Displaying error pages</a>
 * @author Jerome Louvel
 */
public class StatusFilter extends Filter {
    /** The email address of the administrator to contact in case of error. */
    private volatile String contactEmail;

    /** The home URI to propose in case of error. */
    private volatile Reference homeRef;

    /** Indicates if existing representations should be overwritten. */
    private volatile boolean overwrite;

    /** The helped status service. */
    private volatile StatusService statusService;

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param overwrite
     *            Indicates whether an existing representation should be
     *            overwritten.
     * @param email
     *            Email address of the administrator to contact in case of
     *            error.
     * @param homeRef
     *            The home URI to propose in case of error.
     */
    public StatusFilter(Context context, boolean overwrite, String email,
            Reference homeRef) {
        super(context);
        this.overwrite = overwrite;
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
        this(context, statusService.isOverwrite(), statusService
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
                && ((response.getEntity() == null) || isOverwrite())) {
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
    public int doHandle(Request request, Response response) {
        // Normally handle the call
        try {
            super.doHandle(request, response);
        } catch (Throwable t) {
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
     * overriden.
     * 
     * @param status
     *            The status to represent.
     * @param request
     *            The request handled.
     * @param response
     *            The response updated.
     * @return The representation of the given status.
     */
    public Representation getDefaultRepresentation(Status status,
            Request request, Response response) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<html>\n");
        sb.append("<head>\n");
        sb.append("   <title>Status page</title>\n");
        sb.append("</head>\n");
        sb.append("<body>\n");

        sb.append("<h3>");
        if (status.getDescription() != null) {
            sb.append(status.getDescription());
        } else {
            sb.append("No description available for this result status");
        }
        sb.append("</h3>");
        sb.append("<p>You can get technical details <a href=\"");
        sb.append(status.getUri());
        sb.append("\">here</a>.<br>\n");

        if (getContactEmail() != null) {
            sb
                    .append("For further assistance, you can contact the <a href=\"mailto:");
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
     * overriden.
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
    public Status getStatus(Throwable throwable, Request request,
            Response response) {
        Status result = getStatusService().getStatus(throwable, request,
                response);
        if (result == null) {
            getLogger().log(Level.SEVERE,
                    "Unhandled exception or error intercepted", throwable);
            result = new Status(Status.SERVER_ERROR_INTERNAL, throwable);
        }
        return result;
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
     */
    public boolean isOverwrite() {
        return overwrite;
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
     * @param overwrite
     *            True if existing representations should be overwritten.
     */
    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
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
