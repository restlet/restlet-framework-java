/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.gae.engine.application;

import org.restlet.gae.Application;
import org.restlet.gae.Context;
import org.restlet.gae.data.Request;
import org.restlet.gae.data.Response;
import org.restlet.gae.engine.ChainHelper;
import org.restlet.gae.engine.StatusFilter;
import org.restlet.gae.routing.Filter;

/**
 * Application implementation.
 * 
 * @author Jerome Louvel
 */
public class ApplicationHelper extends ChainHelper<Application> {
    /**
     * Constructor.
     * 
     * @param application
     *            The application to help.
     */
    public ApplicationHelper(Application application) {
        super(application);
    }

    /**
     * Creates a new decoder filter. Allows overriding.
     * 
     * @param application
     *            The parent application.
     * @return The new decoder filter.
     */
    protected Filter createDecoderFilter(Application application) {
        return new Decoder(application.getContext(), true, false);
    }

    /**
     * Creates a new status filter. Allows overriding.
     * 
     * @param application
     *            The parent application.
     * @return The new status filter.
     */
    protected Filter createStatusFilter(Application application) {
        return new StatusFilter(getContext(), application.getStatusService());
    }

    /**
     * Creates a new tunnel filter. Allows overriding.
     * 
     * @param context
     *            The parent context.
     * @return The new tunnel filter.
     */
    protected Filter createTunnelFilter(Context context) {
        return new TunnelFilter(context);
    }

    /**
     * Creates a new Range filter. Allows overriding.
     * 
     * @param context
     *            The parent context.
     * @return The new range filter.
     */
    protected Filter createRangeFilter(Context context) {
        return new RangeFilter(context);
    }

    /**
     * In addition to the default behavior, it saves the current application
     * instance into the current thread.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    @Override
    public void handle(Request request, Response response) {
        // Save the current application
        Application.setCurrent(getHelped());

        // Actually handle call
        super.handle(request, response);
    }

    /** Start hook. */
    @Override
    public synchronized void start() throws Exception {
        // Addition of tunnel filter
        if (getHelped().getTunnelService().isEnabled()) {
            addFilter(createTunnelFilter(getContext()));
        }

        // Addition of status pages
        if (getHelped().getStatusService().isEnabled()) {
            addFilter(createStatusFilter(getHelped()));
        }

        // Addition of decoder filter
        if (getHelped().getDecoderService().isEnabled()) {
            addFilter(createDecoderFilter(getHelped()));
        }

        // Addition of range filter
        if (getHelped().getRangeService().isEnabled()) {
            addFilter(createRangeFilter(getContext()));
        }

        // Attach the Application's root Restlet
        setNext(getHelped().getRoot());
    }

    @Override
    public synchronized void stop() throws Exception {
        clear();
    }

    @Override
    public void update() throws Exception {
    }

}
