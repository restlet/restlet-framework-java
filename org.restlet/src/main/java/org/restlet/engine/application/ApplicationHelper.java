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

// [excludes gwt]
/**
 * Application implementation.
 * 
 * @author Jerome Louvel
 */
public class ApplicationHelper extends CompositeHelper<Application> {
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
        Application current = Application.getCurrent();
        // Save the current application
        Application.setCurrent(getHelped());

        // Actually handle call
        try {
            super.handle(request, response);
        } finally {
            // restaure the current application
            Application.setCurrent(current);
        }
    }

    /**
     * Sets the context.
     * 
     * @param context
     *            The context.
     */
    public void setContext(Context context) {
        if (context != null) {
            setOutboundNext(context.getClientDispatcher());
        }
    }

    /** Start hook. */
    @Override
    public synchronized void start() throws Exception {
        Filter filter = null;

        for (Service service : getHelped().getServices()) {
            if (service.isEnabled()) {
                // Attach the service inbound filters
                filter = service
                        .createInboundFilter((getContext() == null) ? null
                                : getContext().createChildContext());

                if (filter != null) {
                    addInboundFilter(filter);
                }

                // Attach the service outbound filters
                filter = service
                        .createOutboundFilter((getContext() == null) ? null
                                : getContext().createChildContext());

                if (filter != null) {
                    addOutboundFilter(filter);
                }
            }
        }

        // Attach the Application's server root Restlet
        setInboundNext(getHelped().getInboundRoot());

        if (getOutboundNext() == null) {
            // Warn about chaining problem
            getLogger()
                    .fine("By default, an application should be attached to a parent component in order to let application's outbound root handle calls properly.");
            setOutboundNext(new Restlet() {
                Map<Protocol, Client> clients = new ConcurrentHashMap<Protocol, Client>();

                @Override
                public void handle(Request request, Response response) {
                    Protocol rProtocol = request.getProtocol();
                    Reference rReference = request.getResourceRef();
                    Protocol protocol = (rProtocol != null) ? rProtocol
                            : (rReference != null) ? rReference
                                    .getSchemeProtocol() : null;

                    if (protocol != null) {
                        Client c = clients.get(protocol);

                        if (c == null) {
                            c = new Client(protocol);
                            clients.put(protocol, c);
                            getLogger().fine(
                                    "Added runtime client for protocol: "
                                            + protocol.getName());
                        }

                        c.handle(request, response);
                    } else {
                        response.setStatus(Status.SERVER_ERROR_INTERNAL,
                                "The server isn't properly configured to handle client calls.");
                        getLogger().warning(
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
            });
        }
    }

    @Override
    public synchronized void stop() throws Exception {
        clear();
    }

    @Override
    public void update() throws Exception {
    }

}
