/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.jetty.internal;

import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.EofException;
import org.eclipse.jetty.io.ssl.SslConnection.SslEndPoint;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.restlet.Server;
import org.restlet.data.Header;
import org.restlet.engine.adapter.ServerCall;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.util.Series;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.List;

/**
 * Call that is used by the Jetty HTTP server connectors.
 * 
 * @author Jerome Louvel
 * @author Tal Liron
 */
public class JettyServerCall extends ServerCall {

    /** The wrapped Jetty HTTP request. */
    private final Request request;

    /** The wrapped Jetty HTTP response. */
    private final Response response;

    /** The wrapped Jetty HTTP callback. */
    private final Callback callback;

    /** Indicates if the request headers were parsed and added. */
    private volatile boolean requestHeadersAdded;

    /**
     * Constructor.
     * 
     * @param server  The parent server.
     * @param request The wrapped Jetty HTTP request.
     * @param response The wrapped Jetty HTTP response.
     * @param callback The wrapped Jetty HTTP callback.
     */
    public JettyServerCall(Server server, Request request, Response response,
            Callback callback) {
        super(server);
        this.request = request;
        this.response = response;
        this.callback = callback;
        this.requestHeadersAdded = false;
    }

    @Override
    public boolean abort() {
        getEndPoint().close();
        return true;
    }

    @Override
    public void complete() {
        getCallback().succeeded();
    }

    @Override
    public void flushBuffers() throws IOException {
        getEndPoint().flush();
    }

    /**
     * Returns the wrapped Jetty HTTP callback.
     * 
     * @return The wrapped Jetty HTTP callback.
     */
    public Callback getCallback() {
        return this.callback;
    }

    @Override
    public List<Certificate> getCertificates() {
        final List<Certificate> result;

        if (getEndPoint() instanceof SslEndPoint sslEndPoint) {
            if (sslEndPoint.getSslSessionData() != null
                    && sslEndPoint.getSslSessionData().peerCertificates() != null) {
                result = Arrays.asList(sslEndPoint.getSslSessionData().peerCertificates());
            } else {
                result = null;
            }
       } else {
            result = null;
        }

        return result;
    }

    /**
     * Returns the wrapped Jetty HTTP request.
     * 
     * @return The wrapped Jetty HTTP request.
     */
    public Request getRequest() {
        return this.request;
    }

    /**
     * Returns the wrapped Jetty HTTP response.
     * 
     * @return The wrapped Jetty HTTP response.
     */
    public Response getResponse() {
        return this.response;
    }

    @Override
    public String getCipherSuite() {
        if (getEndPoint() instanceof SslEndPoint sslEndPoint) {
            return sslEndPoint.getSslSessionData().cipherSuite();
        } else {
            return null;
        }
    }

    @Override
    public String getClientAddress() {
        return Request.getRemoteAddr(getRequest());
    }

    @Override
    public int getClientPort() {
        return Request.getRemotePort(getRequest());
    }

    /**
     * Returns the underlying Jetty's connection.
     * 
     * @return The underlying Jetty's connection.
     */
    protected Connection getConnection() {
        return getRequest().getConnectionMetaData().getConnection();
    }

    /**
     * Returns the underlying Jetty's endpoint.
     * 
     * @return The underlying Jetty's endpoint.
     */
    protected EndPoint getEndPoint() {
        return getConnection().getEndPoint();
    }

    @Override
    public String getMethod() {
        return getRequest().getMethod();
    }

    @Override
    public InputStream getRequestEntityStream(long size) {
        return Request.asInputStream(getRequest());
    }

    @Override
    public String getHostDomain() {
        return request.getHttpURI().getHost();
    }

    @Override
    public int getHostPort() {
        return request.getHttpURI().getPort();
    }

    @Override
    public Series<Header> getRequestHeaders() {
        final Series<Header> result = super.getRequestHeaders();

        if (!this.requestHeadersAdded) {
            // Copy the headers from the request object
            for (HttpField field : getRequest().getHeaders()) {
                result.add(field.getName(), field.getValue());
            }

            this.requestHeadersAdded = true;
        }

        return result;
    }

    public InputStream getRequestHeadStream() {
        // Not available
        return null;
    }

    @Override
    public String getRequestUri() {
        return getRequest().getHttpURI().asString();
    }

    @Override
    public OutputStream getResponseEntityStream() {
        return Response.asBufferedOutputStream(getRequest(), getResponse());
    }

    @Override
    public String getServerAddress() {
        return Request.getLocalAddr(getRequest());
    }

    @Override
    public Integer getSslKeySize() {
        if (getEndPoint() instanceof SslEndPoint sslEndPoint) {
            return sslEndPoint.getSslSessionData().keySize();
        } else {
            return null;
        }
    }

    @Override
    public String getSslSessionId() {
        if (getEndPoint() instanceof SslEndPoint sslEndPoint) {
            return sslEndPoint.getSslSessionData().sslSessionId();
        } else {
            return null;
        }
    }

    @Override
    public boolean isConfidential() {
        return getRequest().isSecure();
    }

    @Override
    public boolean isConnectionBroken(Throwable exception) {
        return (exception instanceof EofException)
                || super.isConnectionBroken(exception);
    }

    @Override
    public void sendResponse(org.restlet.Response response) throws IOException {
        // Add call headers
        for (Header header : getResponseHeaders()) {
            getResponse().getHeaders().add(header.getName(), header.getValue());
        }

        // First set the response status
        getResponse().setStatus(getStatusCode());

        // Write the response entity if it exists
        super.sendResponse(response);
    }
}
