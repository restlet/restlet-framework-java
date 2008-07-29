/*
 * Copyright 2005-2008 Noelios Consulting.
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

package com.noelios.restlet.application;

import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.data.Request;
import org.restlet.data.Response;

import com.noelios.restlet.ChainHelper;
import com.noelios.restlet.component.ComponentContext;

/**
 * Application implementation.
 * 
 * @author Jerome Louvel (contact@noelios.com)
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
        Context context = application.getContext();

        if (context != null) {
            fireContextChanged(context);
        }
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
        return new ApplicationStatusFilter(application);
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

    @Override
    public void fireContextChanged(Context context) {
        if (context instanceof ApplicationContext) {
            ((ApplicationContext) context).setApplication(getHelped());
            String loggerName = ApplicationContext.getLoggerName(getHelped());
            context.setLogger(Logger.getLogger(loggerName));
        } else if (context instanceof ComponentContext) {
            getLogger()
                    .severe(
                            "For security reasons, don't pass the component context to your application anymore. Use the Context#createChildContext() method instead.");
        }
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
