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

package org.restlet.engine.component;

import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.LocalReference;
import org.restlet.data.Protocol;
import org.restlet.engine.TemplateDispatcher;

/**
 * Client dispatcher for a component child.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state as member variables.
 * 
 * @author Jerome Louvel
 */
public class ChildClientDispatcher extends TemplateDispatcher {
    /**
     * Constructor.
     * 
     * @param childContext
     *            The child context.
     */
    public ChildClientDispatcher(ChildContext childContext) {
        super(childContext);
    }

    /**
     * Transmits the call to the parent component except if the call is internal
     * as denoted by the {@link Protocol#RIAP} protocol and targets this child
     * application.
     * 
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    @Override
    public void doHandle(Request request, Response response) {
        super.doHandle(request, response);
        final Protocol protocol = request.getProtocol();

        if (protocol.equals(Protocol.RIAP)) {
            // Let's dispatch it
            final LocalReference cr = new LocalReference(request
                    .getResourceRef());

            if (cr.getRiapAuthorityType() == LocalReference.RIAP_APPLICATION) {
                if ((getChildContext() != null)
                        && (getChildContext().getChild() instanceof Application)) {
                    Application application = (Application) getChildContext()
                            .getChild();
                    request.getResourceRef().setBaseRef(
                            request.getResourceRef().getHostIdentifier());
                    application.getInboundRoot().handle(request, response);
                }
            } else if (cr.getRiapAuthorityType() == LocalReference.RIAP_COMPONENT) {
                parentHandle(request, response);
            } else if (cr.getRiapAuthorityType() == LocalReference.RIAP_HOST) {
                parentHandle(request, response);
            } else {
                getLogger()
                        .warning(
                                "Unknown RIAP authority. Only \"component\", \"host\" and \"application\" are supported.");
            }
        } else {
            if ((getChildContext() != null)
                    && (getChildContext().getChild() instanceof Application)) {
                Application application = (Application) getChildContext()
                        .getChild();

                if (!application.getConnectorService().getClientProtocols()
                        .contains(protocol)) {
                    getLogger()
                            .fine(
                                    "The protocol used by this request is not declared in the application's connector service. "
                                            + "Please update the list of client connectors used by your application and restart it.");
                }
            }

            parentHandle(request, response);
        }
    }

    /**
     * Returns the child context.
     * 
     * @return The child context.
     */
    private ChildContext getChildContext() {
        return (ChildContext) getContext();
    }

    /**
     * Asks to the parent component to handle the call.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    private void parentHandle(Request request, Response response) {
        if (getChildContext() != null) {
            if (getChildContext().getParentContext() != null) {
                if (getChildContext().getParentContext().getClientDispatcher() != null) {
                    getChildContext().getParentContext().getClientDispatcher()
                            .handle(request, response);
                } else {
                    getLogger()
                            .warning(
                                    "The parent context doesn't have a client dispatcher available. Unable to handle call.");
                }
            } else {
                getLogger()
                        .warning(
                                "Your Restlet doesn't have a parent context available.");
            }
        } else {
            getLogger().warning(
                    "Your Restlet doesn't have a context available.");
        }
    }

}
