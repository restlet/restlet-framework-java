/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.application;

import java.util.logging.Logger;

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
     *                The parent application context.
     */
    public ApplicationDispatcher(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Handles a call.
     * 
     * @param request
     *                The request to handle.
     * @param response
     *                The response to update.
     */
	@Override
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

                if (this.applicationContext != null) {
                    if (this.applicationContext.getParentContext() != null) {
                        this.applicationContext.getParentContext()
                                .getDispatcher().handle(request, response);
                    } else {
                        Logger
                                .getLogger(
                                        ApplicationDispatcher.class
                                                .getCanonicalName())
                                .warning(
                                        "Your Application doesn't have a parent context available. Ensure that the parent Component has a context set.");
                    }
                } else {
                    Logger
                            .getLogger(
                                    ApplicationDispatcher.class
                                            .getCanonicalName())
                            .warning(
                                    "Your Application doesn't have a context set. Ensure that you pass the parent Component's context to your Application constructor.");
                }
            }
        }
    }

}
