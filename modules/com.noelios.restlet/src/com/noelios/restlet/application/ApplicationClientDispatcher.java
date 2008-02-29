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
import org.restlet.data.LocalReference;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

import com.noelios.restlet.TemplateDispatcher;

/**
 * Application client dispatcher.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ApplicationClientDispatcher extends TemplateDispatcher {
    /**
     * Constructor.
     * 
     * @param applicationContext
     *                The application context.
     */
    public ApplicationClientDispatcher(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    /**
     * Returns the application context.
     * 
     * @return The application context.
     */
    private ApplicationContext getApplicationContext() {
        return (ApplicationContext) getContext();
    }

    @Override
    public void doHandle(Request request, Response response) {
        // Add the application in request and response attributes
        request.getAttributes().put(Application.KEY,
                getApplicationContext().getApplication());
        response.getAttributes().put(Application.KEY,
                getApplicationContext().getApplication());

        Protocol protocol = request.getProtocol();

        if (protocol.equals(Protocol.WAR)) {
            getApplicationContext().getWarClient().handle(request, response);
        } else if (protocol.equals(Protocol.RIAP)) {
            // Consider that the request is confidential
            request.setConfidential(true);

            // Let's dispatch it
            LocalReference cr = new LocalReference(request.getResourceRef());

            if (cr.getRiapAuthorityType() == LocalReference.RIAP_APPLICATION) {
                request.getResourceRef().setBaseRef(
                        request.getResourceRef().getHostIdentifier());

                if (getApplicationContext() != null) {
                    getApplicationContext().getApplication().getRoot().handle(
                            request, response);
                }
            } else if (cr.getRiapAuthorityType() == LocalReference.RIAP_COMPONENT) {
                parentHandle(request, response);
            } else {
                Logger
                        .getLogger(
                                ApplicationClientDispatcher.class
                                        .getCanonicalName())
                        .warning(
                                "Unknown RIAP authority. Only \"component\" and \"application\" are supported.");
            }
        } else {
            if (!getApplicationContext().getApplication().getConnectorService()
                    .getClientProtocols().contains(protocol)) {
                getApplicationContext()
                        .getLogger()
                        .fine(
                                "The protocol used by this request is not declared in the application's connector service. "
                                        + "Please update the list of client connectors used by your application and restart it.");
            }

            parentHandle(request, response);
        }
    }

    /**
     * Asks to the parent component to handle the call.
     * 
     * @param request
     *                The request to handle.
     * @param response
     *                The response to update.
     */
    private void parentHandle(Request request, Response response) {
        if (getApplicationContext() != null) {
            if (getApplicationContext().getParentContext() != null) {
                getApplicationContext().getParentContext()
                        .getClientDispatcher().handle(request, response);
            } else {
                Logger
                        .getLogger(
                                ApplicationClientDispatcher.class
                                        .getCanonicalName())
                        .warning(
                                "Your Application doesn't have a parent context available. Ensure that your parent Component has a context available.");
            }
        } else {
            Logger
                    .getLogger(
                            ApplicationClientDispatcher.class
                                    .getCanonicalName())
                    .warning(
                            "Your Application doesn't have a context set. Ensure that you pass the parent Component's context to your Application constructor.");
        }
    }

}
