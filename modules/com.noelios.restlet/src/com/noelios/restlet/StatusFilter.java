/*
 * Copyright 2005-2007 Noelios Consulting.
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

package com.noelios.restlet;

import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;

/**
 * Filter associating a response entity based on the status. In order to
 * customize the default representation, just subclass this class and override
 * the "getRepresentation" method.<br/> If any exception occurs during the call
 * handling, a "server internal error" status is automatically associated to the
 * call. Of course, you can personalize the representation of this error. Also,
 * if no status is set (null), then the "success ok" status is assumed.<br/>
 * 
 * @see <a href="http://www.restlet.org/tutorial#part08">Tutorial: Displaying
 *      error pages</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class StatusFilter extends Filter {
    /** Indicates whether an existing representation should be overwritten. */
    private boolean overwrite;

    /** Email address of the administrator to contact in case of error. */
    private String email;

    /** The home URI to propose in case of error. */
    private String homeURI;

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
     * @param homeUri
     *            The home URI to propose in case of error.
     */
    public StatusFilter(Context context, boolean overwrite, String email,
            String homeUri) {
        super(context);
        this.overwrite = overwrite;
        this.email = email;
        this.homeURI = homeUri;
    }

    /**
     * Handles the call by distributing it to the next Restlet.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    public void doHandle(Request request, Response response) {
        // Normally handle the call
        try {
            super.doHandle(request, response);
        } catch (Throwable t) {
            getLogger().log(Level.SEVERE,
                    "Unhandled exception or error intercepted", t);
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
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
    public void afterHandle(Request request, Response response) {
        // If no status is set, then the "success ok" status is assumed.
        if (response.getStatus() == null) {
            response.setStatus(Status.SUCCESS_OK);
        }

        // Do we need to get a representation for the current status?
        if (response.getStatus().isError()
                && ((response.getEntity() == null) || overwrite)) {
            response.setEntity(getRepresentation(response.getStatus(), request,
                    response));
        }
    }

    /**
     * Returns a representation for the given status.<br/> In order to
     * customize the default representation, this method can be overriden.
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
        StringBuilder sb = new StringBuilder();
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
        sb.append("\">here</a>.<br/>\n");

        if (email != null) {
            sb
                    .append("For further assistance, you can contact the <a href=\"mailto:");
            sb.append(email);
            sb.append("\">administrator</a>.<br/>\n");
        }

        if (homeURI != null) {
            sb.append("Please continue your visit at our <a href=\"");
            sb.append(homeURI);
            sb.append("\">home page</a>.\n");
        }

        sb.append("</p>\n");
        sb.append("</body>\n");
        sb.append("</html>\n");

        return new StringRepresentation(sb.toString(), MediaType.TEXT_HTML);
    }

}
