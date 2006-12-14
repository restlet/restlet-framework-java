/*
 * Copyright 2005-2006 Noelios Consulting.
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

package com.noelios.restlet.ext.simple;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.restlet.Server;
import org.restlet.data.Parameter;
import org.restlet.data.ParameterList;

import simple.http.Request;
import simple.http.Response;

import com.noelios.restlet.http.HttpServerCall;

/**
 * Call that is used by the Simple HTTP server.
 * 
 * @author Lars Heuer (heuer[at]semagia.com) <a
 *         href="http://semagia.com/">Semagia</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class SimpleCall extends HttpServerCall {
    /**
     * Simple Request.
     */
    private Request request;

    /**
     * Simple Response.
     */
    private Response response;

    /** Indicates if the request headers were parsed and added. */
    private boolean requestHeadersAdded;

    /**
     * Constructs this class with the specified {@link simple.http.Request} and
     * {@link simple.http.Response}.
     * 
     * @param server
     *            The parent server.
     * @param request
     *            Request to wrap.
     * @param response
     *            Response to wrap.
     * @param confidential
     *            Inidicates if this call is acting in HTTP or HTTPS mode.
     * @param hostPort
     *            The listening port used.
     */
    SimpleCall(Server server, Request request, Response response,
            boolean confidential, int hostPort) {
        super(server);
        this.request = request;
        this.response = response;
        setConfidential(confidential);
        this.setHostPort(hostPort);
        this.requestHeadersAdded = false;
    }

    /**
     * Returns the full request URI.
     * 
     * @return The full request URI.
     */
    public String getRequestUri() {
        return request.getURI();
    }

    /**
     * Returns the request method.
     * 
     * @return The request method.
     */
    public String getMethod() {
        return request.getMethod();
    }

    /**
     * Returns the request address.<br/> Corresponds to the IP address of the
     * requesting client.
     * 
     * @return The request address.
     */
    public String getClientAddress() {
        return request.getInetAddress().getHostAddress();
    }

    /**
     * Returns the response address.<br/> Corresponds to the IP address of the
     * responding server.
     * 
     * @return The response address.
     */
    public String getServerAddress() {
        return response.getInetAddress().getHostAddress();
    }

    /**
     * Returns the list of request headers.
     * 
     * @return The list of request headers.
     */
    public ParameterList getRequestHeaders() {
        ParameterList result = super.getRequestHeaders();

        if (!this.requestHeadersAdded) {
            int headerCount = request.headerCount();
            for (int i = 0; i < headerCount; i++) {
                result.add(new Parameter(request.getName(i), request
                        .getValue(i)));
            }

            this.requestHeadersAdded = true;
        }

        return result;
    }

    /**
     * Sends the response back to the client. Commits the status, headers and
     * optional entity and send them on the network.
     * 
     * @param restletResponse
     *            The high-level response.
     */
    public void sendResponse(org.restlet.data.Response restletResponse)
            throws IOException {
        // Set the response headers
        response.clear();
        for (Parameter header : getResponseHeaders()) {
            response.add(header.getName(), header.getValue());
        }

        // Set the status
        response.setCode(getStatusCode());
        response.setText(getReasonPhrase());

        // To ensure that Simple doesn't switch to chunked encoding
        if (restletResponse.getEntity() == null) {
            response.setContentLength(0);
        }

        // Send the response entity
        super.sendResponse(restletResponse);
    }

    /**
     * Returns the request entity channel if it exists.
     * 
     * @return The request entity channel if it exists.
     */
    public ReadableByteChannel getRequestChannel() {
        // Unsupported.
        return null;
    }

    /**
     * Returns the request entity stream if it exists.
     * 
     * @return The request entity stream if it exists.
     */
    public InputStream getRequestStream() {
        try {
            return request.getInputStream();
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * Returns the response channel if it exists.
     * 
     * @return The response channel if it exists.
     */
    public WritableByteChannel getResponseChannel() {
        // Unsupported.
        return null;
    }

    /**
     * Returns the response stream if it exists.
     * 
     * @return The response stream if it exists.
     */
    public OutputStream getResponseStream() {
        try {
            return response.getOutputStream();
        } catch (IOException ex) {
            return null;
        }
    }
}
