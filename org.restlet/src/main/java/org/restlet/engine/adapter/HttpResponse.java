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

package org.restlet.engine.adapter;

import org.restlet.Message;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ServerInfo;
import org.restlet.data.Status;
import org.restlet.engine.Engine;

/**
 * Response wrapper for server HTTP calls.
 * 
 * @author Jerome Louvel
 */
public class HttpResponse extends Response {
    /**
     * Adds a new header to the given request.
     * 
     * @param response
     *            The response to update.
     * @param headerName
     *            The header name to add.
     * @param headerValue
     *            The header value to add.
     */
    public static void addHeader(Response response, String headerName,
            String headerValue) {
        if (response instanceof HttpResponse) {
            ((Message) response).getHeaders().add(headerName, headerValue);
        }
    }

    /** The low-level HTTP call. */
    private volatile ServerCall httpCall;

    /** Indicates if the server data was parsed and added. */
    private volatile boolean serverAdded;

    /**
     * Constructor.
     * 
     * @param httpCall
     *            The low-level HTTP server call.
     * @param request
     *            The associated high-level request.
     */
    public HttpResponse(ServerCall httpCall, Request request) {
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
    public ServerCall getHttpCall() {
        return this.httpCall;
    }

    /**
     * Returns the server-specific information.
     * 
     * @return The server-specific information.
     */
    @Override
    public ServerInfo getServerInfo() {
        final ServerInfo result = super.getServerInfo();

        if (!this.serverAdded) {
            result.setAddress(this.httpCall.getServerAddress());
            result.setAgent(Engine.VERSION_HEADER);
            result.setPort(this.httpCall.getServerPort());
            this.serverAdded = true;
        }

        return result;
    }

}
