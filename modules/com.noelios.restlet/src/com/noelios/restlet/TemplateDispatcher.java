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

package com.noelios.restlet;

import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Uniform;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.util.Template;

/**
 * Base call dispatcher capable of resolving target resource URI templates.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class TemplateDispatcher extends Uniform {
    /** The parent context. */
    private volatile Context context;

    /**
     * Constructor.
     * 
     * @param context
     *                The parent context.
     */
    public TemplateDispatcher(Context context) {
        this.context = context;
    }

    /**
     * Actually handles the call after resolving any URI template on the
     * request's target resource reference.
     * 
     * @param request
     *                The request to handle.
     * @param response
     *                The response to update.
     */
    protected void doHandle(Request request, Response response) {
        request.setOriginalRef(request.getResourceRef().getTargetRef());
    }

    /**
     * Returns the parent context.
     * 
     * @return The parent context.
     */
    public Context getContext() {
        return this.context;
    }

    /**
     * Returns the context's logger.
     * 
     * @return The context's logger.
     */
    public Logger getLogger() {
        return getContext().getLogger();
    }

    @Override
    public void handle(Request request, Response response) {
        // Associate the response to the current thread
        Response.setCurrent(response);

        Protocol protocol = request.getProtocol();

        if (protocol == null) {
            throw new UnsupportedOperationException(
                    "Unable to determine the protocol to use for this call.");
        }
        String targetUri = request.getResourceRef().toString(true, false);

        if (targetUri.contains("{")) {
            // Template URI detected, create the template
            Template template = new Template(getContext().getLogger(),
                    targetUri);

            // Set the formatted target URI
            request.setResourceRef(template.format(request, response));
        }

        // Actually handle the formatted URI
        doHandle(request, response);

        // If the response entity comes back with no identifier,
        // automatically set the request's resource reference's identifier.
        // This is very useful to resolve relative references in XSLT for
        // example.
        if ((response.getEntity() != null)
                && (response.getEntity().getIdentifier() == null)) {
            response.getEntity().setIdentifier(
                    request.getResourceRef().toString());
        }
    }
}
