/*
 * Copyright 2005-2008 Noelios Technologies.
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

package com.noelios.restlet.component;

import org.restlet.Application;
import org.restlet.data.LocalReference;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

import com.noelios.restlet.TemplateDispatcher;

/**
 * Client dispatcher for a component child.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state as member variables.
 * 
 * @author Jerome Louvel (contact@noelios.com)
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

    @Override
    public void doHandle(Request request, Response response) {
        super.doHandle(request, response);
        final Protocol protocol = request.getProtocol();

        if (protocol.equals(Protocol.RIAP)) {
            // Consider that the request is confidential
            request.setConfidential(true);

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
                    application.getRoot().handle(request, response);
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
