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

import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Reference;
import org.restlet.data.Status;
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
     */
    public StatusFilter(Context context, boolean overwriting, String email,
            Reference homeRef) {
        super(context);
        this.overwriting = overwriting;
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
     * Allows filtering after its handling by the target Restlet. If the status
     * is not set, set {@link org.restlet.data.Status#SUCCESS_OK} by default.
     * 
     * If this is an error status, try to get a representation of it with
     * {@link org.restlet.service.StatusService#toRepresentation(Status, Request, Response)}
     * .
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
        try {
            if (response.getStatus().isError()
                    && ((response.getEntity() == null) || isOverwriting())) {
                response.setEntity(getStatusService().toRepresentation(
                        response.getStatus(), request, response));
            }
        } catch (Exception e) {
            getLogger().log(Level.WARNING,
                    "Unable to get the custom status representation", e);
        }
    }

    /**
     * Handles the call by distributing it to the next Restlet. If a throwable
     * is caught, the
     * {@link org.restlet.service.StatusService#toStatus(Throwable, Request, Response)}
     * method is invoked.
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
        } catch (Throwable throwable) {
            Status status = getStatusService().toStatus(throwable, request,
                    response);

            Level level = Level.INFO;
            if (status.isServerError()) {
                level = Level.WARNING;
            } else if (status.isConnectorError()) {
                level = Level.INFO;
            } else if (status.isClientError()) {
                level = Level.FINE;
            }
            getLogger().log(level,
                    "Exception or error caught by status service", throwable);

            if (response != null) {
                response.setStatus(status);
            }
        }

        return CONTINUE;
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
    public boolean isOverwriting() {
        return overwriting;
    }

    /**
     * Indicates if existing representations should be overwritten.
     * 
     * @param overwriting
     *            True if existing representations should be overwritten.
     */
    public void setOverwriting(boolean overwriting) {
        this.overwriting = overwriting;
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
