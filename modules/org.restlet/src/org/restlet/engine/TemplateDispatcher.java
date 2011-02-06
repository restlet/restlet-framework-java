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

package org.restlet.engine;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.routing.Template;

/**
 * Base call dispatcher capable of resolving target resource URI templates.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state as member variables.
 * 
 * @author Jerome Louvel
 */
public class TemplateDispatcher extends Client {

    /** The context. */
    private volatile Context context;

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     */
    public TemplateDispatcher(Context context) {
        super(null, (Protocol) null);
        this.context = context;
    }

    /**
     * Actually handles the call. Since this method only sets the request's
     * original reference ({@link Request#getOriginalRef()} with the the
     * targeted one, it must be overridden by subclasses.
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
     * Returns the context. Override default behavior from {@link Restlet}.
     * 
     * @return The context.
     */
    @Override
    public Context getContext() {
        return context;
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
            Template template = new Template(targetUri);

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
                && (response.getEntity().getLocationRef() == null)) {
            response.getEntity().setLocationRef(
                    request.getResourceRef().getTargetRef().toString());
        }
    }

    /**
     * Sets the context. Override default behavior from {@link Restlet}.
     * 
     * @param context
     *            The context to set.
     */
    @Override
    public void setContext(Context context) {
        this.context = context;
    }
}
