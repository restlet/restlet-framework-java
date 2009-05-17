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

package org.restlet.engine;

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
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state as member variables.
 * 
 * @author Jerome Louvel
 */
public class TemplateDispatcher extends Uniform {
    /** The context. */
    private volatile Context context;

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     */
    public TemplateDispatcher(Context context) {
        this.context = context;
    }

    /**
     * Actually handles the call. Since this method only sets the request's
     * original reference ({@link Request#getOriginalRef()} with the the
     * targetted one, it must be overriden by subclasses.
     * 
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    protected void doHandle(Request request, Response response) {
        request.setOriginalRef(request.getResourceRef().getTargetRef());
    }

    /**
     * Returns the context.
     * 
     * @return The context.
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

    /**
     * Handles the call after resolving any URI template on the request's target
     * resource reference.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    @Override
    public void handle(Request request, Response response) {
        // Associate the response to the current thread
        Response.setCurrent(response);

        final Protocol protocol = request.getProtocol();

        if (protocol == null) {
            throw new UnsupportedOperationException(
                    "Unable to determine the protocol to use for this call.");
        }
        final String targetUri = request.getResourceRef().toString(true, false);

        if (targetUri.contains("{")) {
            // Template URI detected, create the template
            final Template template = new Template(targetUri);

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
