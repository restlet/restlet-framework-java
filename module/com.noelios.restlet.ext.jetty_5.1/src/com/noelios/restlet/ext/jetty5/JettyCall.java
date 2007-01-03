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

package com.noelios.restlet.ext.jetty5;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Enumeration;
import java.util.Iterator;

import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.restlet.Server;
import org.restlet.data.Parameter;
import org.restlet.data.Response;
import org.restlet.util.Series;

import com.noelios.restlet.http.HttpServerCall;

/**
 * Call that is used by the Jetty HTTP server connector.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class JettyCall extends HttpServerCall {
    /** The wrapped Jetty HTTP request. */
    private HttpRequest request;

    /** The wrapped Jetty HTTP response. */
    private HttpResponse response;

    /** Indicates if the request headers were parsed and added. */
    private boolean requestHeadersAdded;

    /**
     * Constructor.
     * 
     * @param server
     *            The parent server connector.
     * @param request
     *            The Jetty HTTP request.
     * @param response
     *            The Jetty HTTP response.
     */
    public JettyCall(Server server, HttpRequest request, HttpResponse response) {
        super(server);
        this.request = request;
        this.response = response;
        this.requestHeadersAdded = false;
    }

    @Override
    public String getClientAddress() {
        return getRequest().getRemoteAddr();
    }

    @Override
    public Integer getClientPort() {
        return getRequest().getHttpConnection().getRemotePort();
    }

    /**
     * Returns the request method.
     * 
     * @return The request method.
     */
    public String getMethod() {
        return getRequest().getMethod();
    }

    /**
     * Returns the HTTP Jetty request.
     * 
     * @return The HTTP Jetty request.
     */
    public HttpRequest getRequest() {
        return this.request;
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
     * Returns the list of request headers.
     * 
     * @return The list of request headers.
     */
    public Series<Parameter> getRequestHeaders() {
        Series<Parameter> result = super.getRequestHeaders();

        if (!requestHeadersAdded) {
            // Copy the headers from the request object
            String headerName;
            String headerValue;
            for (Enumeration names = getRequest().getFieldNames(); names
                    .hasMoreElements();) {
                headerName = (String) names.nextElement();
                for (Enumeration values = getRequest().getFieldValues(
                        headerName); values.hasMoreElements();) {
                    headerValue = (String) values.nextElement();
                    result.add(new Parameter(headerName, headerValue));
                }
            }

            requestHeadersAdded = true;
        }

        return result;
    }

    /**
     * Returns the request entity stream if it exists.
     * 
     * @return The request entity stream if it exists.
     */
    public InputStream getRequestStream() {
        return getRequest().getInputStream();
    }

    /**
     * Returns the URI on the request line (most like a relative reference, but
     * not necessarily).
     * 
     * @return The URI on the request line.
     */
    public String getRequestUri() {
        return getRequest().getURI().toString();
    }

    /**
     * Returns the HTTP Jetty response.
     * 
     * @return The HTTP Jetty response.
     */
    public HttpResponse getResponse() {
        return this.response;
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
        return getResponse().getOutputStream();
    }

    /**
     * Returns the response address.<br/> Corresponds to the IP address of the
     * responding server.
     * 
     * @return The response address.
     */
    public String getServerAddress() {
        return getRequest().getHttpConnection().getServerAddr();
    }

    @Override
    public String getVersion() {
        String result = null;
        int index = getRequest().getVersion().indexOf('/');

        if (index != -1) {
            result = getRequest().getVersion().substring(index + 1);
        }

        return result;
    }

    /**
     * Indicates if the request was made using a confidential mean.<br/>
     * 
     * @return True if the request was made using a confidential mean.<br/>
     */
    public boolean isConfidential() {
        return getRequest().isConfidential();
    }

    /**
     * Sends the response back to the client. Commits the status, headers and
     * optional entity and send them on the network.
     * 
     * @param response
     *            The high-level response.
     */
    public void sendResponse(Response response) throws IOException {
        // Set the response status
        getResponse().setStatus(getStatusCode(), getReasonPhrase());

        // Remove existings headers if any
        for (Enumeration fields = getResponse().getFieldNames(); fields
                .hasMoreElements();) {
            getResponse().removeField((String) fields.nextElement());
        }

        // Add response headers
        Parameter header;
        for (Iterator<Parameter> iter = getResponseHeaders().iterator(); iter
                .hasNext();) {
            header = iter.next();
            getResponse().addField(header.getName(), header.getValue());
        }

        // Send the response entity
        super.sendResponse(response);
    }

}
