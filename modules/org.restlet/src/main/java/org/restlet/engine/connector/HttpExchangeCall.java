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

package org.restlet.engine.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.restlet.Server;
import org.restlet.data.Header;
import org.restlet.engine.adapter.ServerCall;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

/**
 * Call that is used by the Basic HTTP server.
 * 
 * @author Jerome Louvel
 */
public class HttpExchangeCall extends ServerCall {

    /** The wrapped HTTP exchange. */
    private final HttpExchange exchange;

    /** Indicates if the request headers were parsed and added. */
    private volatile boolean requestHeadersAdded;

    /**
     * Constructor.
     * 
     * @param server
     * @param exchange
     */
    public HttpExchangeCall(Server server, HttpExchange exchange) {
        this(server, exchange, false);
    }

    /**
     * Constructor.
     * 
     * @param server
     * @param exchange
     * @param confidential
     */
    public HttpExchangeCall(Server server, HttpExchange exchange,
            boolean confidential) {
        super(server);
        this.exchange = exchange;
        setConfidential(confidential);
    }

    @Override
    public boolean abort() {
        this.exchange.close();
        return true;
    }

    @Override
    public void flushBuffers() throws IOException {
        this.exchange.getResponseBody().flush();
    }

    @Override
    public String getClientAddress() {
        return this.exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    @Override
    public int getClientPort() {
        return this.exchange.getRemoteAddress().getPort();
    }

    @Override
    public String getMethod() {
        return this.exchange.getRequestMethod();
    }

    @Override
    public Series<Header> getRequestHeaders() {
        final Series<Header> result = super.getRequestHeaders();

        if (!this.requestHeadersAdded) {
            final Headers headers = this.exchange.getRequestHeaders();

            for (String name : headers.keySet()) {
                for (String value : (List<String>) headers.get(name)) {
                    result.add(name, value);
                }
            }
            this.requestHeadersAdded = true;
        }

        return result;
    }

    @Override
    public InputStream getRequestEntityStream(long size) {
        return this.exchange.getRequestBody();
    }

    @Override
    public InputStream getRequestHeadStream() {
        return null;
    }

    @Override
    public String getRequestUri() {
        return this.exchange.getRequestURI().toString();
    }

    @Override
    public OutputStream getResponseEntityStream() {
        return this.exchange.getResponseBody();
    }

    @Override
    public void writeResponseHead(org.restlet.Response restletResponse)
            throws IOException {
        final Headers headers = this.exchange.getResponseHeaders();

        for (Header header : getResponseHeaders()) {
            headers.add(header.getName(), header.getValue());
        }

        // Send the headers
        Representation entity = restletResponse.getEntity();
        long responseLength = 0;

        if (entity == null || !entity.isAvailable()) {
            responseLength = -1;
        } else if (entity.getAvailableSize() != Representation.UNKNOWN_SIZE) {
            responseLength = entity.getAvailableSize();
        }

        this.exchange.sendResponseHeaders(getStatusCode(), responseLength);
    }

}
