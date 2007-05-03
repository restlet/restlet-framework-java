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

package com.noelios.restlet.application;

import org.restlet.Application;
import org.restlet.Uniform;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Application dispatcher.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ApplicationDispatcher extends Uniform {
    /** The parent context. */
    private ApplicationContext applicationContext;

    /**
     * Constructor.
     * 
     * @param applicationContext
     *            The parent application context.
     */
    public ApplicationDispatcher(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Handles a call.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    public void handle(Request request, Response response) {
        Protocol protocol = request.getProtocol();

        if (protocol == null) {
            throw new UnsupportedOperationException(
                    "Unable to determine the protocol to use for this call.");
        } else {
            // Add the application in request and response attributes
            request.getAttributes().put(Application.KEY,
                    this.applicationContext.getApplication());
            response.getAttributes().put(Application.KEY,
                    this.applicationContext.getApplication());
            
            if (protocol.equals(Protocol.WAR)) {
                this.applicationContext.getWarClient()
                        .handle(request, response);
            } else {
                if (!this.applicationContext.getApplication()
                        .getConnectorService().getClientProtocols().contains(
                                protocol)) {
                    this.applicationContext
                            .getLogger()
                            .fine(
                                    "The protocol used by this request is not declared in the application's connector service. "
                                            + "Please update the list of client connectors used by your application and restart it.");
                }

                this.applicationContext.getParentContext().getDispatcher()
                        .handle(request, response);
            }
        }
    }

}
