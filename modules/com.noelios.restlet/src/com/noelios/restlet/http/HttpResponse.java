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

package com.noelios.restlet.http;

import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.ServerInfo;
import org.restlet.data.Status;

import com.noelios.restlet.Engine;

/**
 * Response wrapper for server HTTP calls.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class HttpResponse extends Response {
    /** The low-level HTTP call. */
    private HttpServerCall httpCall;

    /** Indicates if the server data was parsed and added. */
    private boolean serverAdded;

    /**
     * Constructor.
     * 
     * @param httpCall
     *            The low-level HTTP server call.
     * @param request
     *            The associated high-level request.
     */
    public HttpResponse(HttpServerCall httpCall, Request request) {
        super(request);
        this.serverAdded = false;
        this.httpCall = httpCall;

        // Set the properties
        setStatus(Status.SUCCESS_OK);
    }

    /**
     * Returns the low-level HTTP call.
     * 
     * @return The low-level HTTP call.
     */
    public HttpServerCall getHttpCall() {
        return this.httpCall;
    }

    /**
     * Returns the server-specific information.
     * 
     * @return The server-specific information.
     */
	@Override
    public ServerInfo getServerInfo() {
        ServerInfo result = super.getServerInfo();

        if (!this.serverAdded) {
            result.setAddress(httpCall.getServerAddress());
            result.setAgent(Engine.VERSION_HEADER);
            result.setPort(httpCall.getServerPort());
            this.serverAdded = true;
        }

        return result;
    }

}
