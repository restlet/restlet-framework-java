/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.adapter;

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
     * @param response The response to modify.
     * @param headerName The header name to add.
     * @param headerValue The header value to add.
     */
    public static void addHeader(Response response, String headerName, String headerValue) {
        if (response instanceof HttpResponse) {
            response.getHeaders().add(headerName, headerValue);
        }
    }

    /** The low-level HTTP call. */
    private volatile ServerCall httpCall;

    /** Indicates if the server data was parsed and added. */
    private volatile boolean serverAdded;

    /**
     * Constructor.
     *
     * @param httpCall The low-level HTTP server call.
     * @param request The associated high-level request.
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
