/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.application;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.CompositeHelper;
import org.restlet.routing.Filter;
import org.restlet.service.Service;

/**
 * Application implementation.
 *
 * @author Jerome Louvel
 */
public class ApplicationHelper extends CompositeHelper<Application> {
    /**
     * Constructor.
     *
     * @param application The application to help.
     */
    public ApplicationHelper(Application application) {
        super(application);
    }

    /**
     * In addition to the default behavior, it saves the current application instance into the
     * current thread.
     *
     * @param request The request to handle.
     * @param response The response to modify.
     */
    @Override
    public void handle(Request request, Response response) {
        // Save the current application
        // Plan to move the current application as an attribute of the Response in the incoming 2.5
        // release
        final Application currentApplication =
                getHelped() != null ? getHelped() : Application.getCurrent();
        Application.setCurrent(currentApplication);

        // Actually handle call
        try {
            super.handle(request, response);
        } finally {
            // restore the current application
            Application.setCurrent(currentApplication);
        }
    }

    /**
     * Sets the context.
     *
     * @param context The context.
     */
    public void setContext(Context context) {
        if (context != null) {
            setOutboundNext(context.getClientDispatcher());
        }
    }

    /** Start hook. */
    @Override
    public synchronized void start() throws Exception {
        attachServicesFilters();

        // Attach the Application's server root Restlet
        setInboundNext(getHelped().getInboundRoot());

        if (getOutboundNext() == null) {
            // Warn about a chaining problem
            getLogger()
                    .fine(
                            "By default, an application should be attached to a parent component to let application's outbound root handle calls properly.");
            setOutboundNext(newOutboundNext());
        }
    }

    private void attachServicesFilters() {
        Filter filter;

        for (Service service : getHelped().getServices()) {
            if (service.isEnabled()) {
                // Attach the service inbound filters
                Context context = (getContext() == null) ? null : getContext().createChildContext();
                filter = service.createInboundFilter(context);

                if (filter != null) {
                    addInboundFilter(filter);
                }

                // Attach the service outbound filters
                context = (getContext() == null) ? null : getContext().createChildContext();
                filter = service.createOutboundFilter(context);

                if (filter != null) {
                    addOutboundFilter(filter);
                }
            }
        }
    }

    /** Creates a new outbound next Restlet. */
    private static Restlet newOutboundNext() {
        return new Restlet() {
            final Map<Protocol, Client> clients = new ConcurrentHashMap<>();

            @Override
            public void handle(Request request, Response response) {
                final Protocol rProtocol = request.getProtocol();
                final Reference rReference = request.getResourceRef();
                final Protocol protocol;

                if (rProtocol != null) {
                    protocol = rProtocol;
                } else {
                    protocol = (rReference != null) ? rReference.getSchemeProtocol() : null;
                }

                if (protocol != null) {
                    Client c =
                            clients.computeIfAbsent(
                                    protocol,
                                    p -> {
                                        getLogger()
                                                .fine(
                                                        "Added runtime client for protocol: "
                                                                + p.getName());
                                        return new Client(p);
                                    });
                    c.handle(request, response);
                } else {
                    response.setStatus(
                            Status.SERVER_ERROR_INTERNAL,
                            "The server isn't properly configured to handle client calls.");
                    getLogger()
                            .warning(
                                    "There is no protocol detected for this request: "
                                            + request.getResourceRef());
                }
            }

            @Override
            public synchronized void stop() throws Exception {
                super.stop();
                for (Client client : clients.values()) {
                    client.stop();
                }
            }
        };
    }

    @Override
    public synchronized void stop() throws Exception {
        clear();
    }

    @Override
    public void update() throws Exception {
        // Nothing to do here
    }
}
