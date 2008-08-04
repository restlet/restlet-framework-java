/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.ext.simple;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.restlet.Server;
import org.restlet.data.Parameter;
import org.restlet.util.Series;

import simple.http.Request;
import simple.http.Response;

import com.noelios.restlet.http.HttpServerCall;
import com.noelios.restlet.util.KeepAliveInputStream;

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
    private final Request request;

    /**
     * Simple Response.
     */
    private final Response response;

    /** Indicates if the request headers were parsed and added. */
    private volatile boolean requestHeadersAdded;

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
     *            Indicates if this call is acting in HTTP or HTTPS mode.
     */
    SimpleCall(Server server, Request request, Response response,
            boolean confidential) {
        super(server);
        this.request = request;
        this.response = response;
        setConfidential(confidential);
        this.requestHeadersAdded = false;
    }

    @Override
    public void complete() {
        try {
            // Commit the response
            this.response.commit();
        } catch (final IOException ex) {
            getLogger().log(Level.WARNING, "Unable to commit the response", ex);
        }
    }

    @Override
    public String getClientAddress() {
        return this.request.getInetAddress().getHostAddress();
    }

    @Override
    public int getClientPort() {
        final Socket socket = getSocket();
        return (socket != null) ? socket.getPort() : -1;
    }

    /**
     * Returns the request method.
     * 
     * @return The request method.
     */
    @Override
    public String getMethod() {
        return this.request.getMethod();
    }

    @Override
    public ReadableByteChannel getRequestEntityChannel(long size) {
        // Unsupported.
        return null;
    }

    @Override
    public InputStream getRequestEntityStream(long size) {
        try {
            return new KeepAliveInputStream(this.request.getInputStream());
        } catch (final IOException ex) {
            return null;
        }
    }

    @Override
    public ReadableByteChannel getRequestHeadChannel() {
        // Not available
        return null;
    }

    /**
     * Returns the list of request headers.
     * 
     * @return The list of request headers.
     */
    @Override
    public Series<Parameter> getRequestHeaders() {
        final Series<Parameter> result = super.getRequestHeaders();

        if (!this.requestHeadersAdded) {
            final int headerCount = this.request.headerCount();
            for (int i = 0; i < headerCount; i++) {
                result.add(new Parameter(this.request.getName(i), this.request
                        .getValue(i)));
            }

            this.requestHeadersAdded = true;
        }

        return result;
    }

    @Override
    public InputStream getRequestHeadStream() {
        // Not available
        return null;
    }

    /**
     * Returns the full request URI.
     * 
     * @return The full request URI.
     */
    @Override
    public String getRequestUri() {
        return this.request.getURI();
    }

    /**
     * Returns the response channel if it exists.
     * 
     * @return The response channel if it exists.
     */
    @Override
    public WritableByteChannel getResponseEntityChannel() {
        // Unsupported.
        return null;
    }

    /**
     * Returns the response stream if it exists.
     * 
     * @return The response stream if it exists.
     */
    @Override
    public OutputStream getResponseEntityStream() {
        try {
            return this.response.getOutputStream();
        } catch (final IOException ex) {
            return null;
        }
    }

    /**
     * Returns the request socket.
     * 
     * @return The request socket.
     */
    private Socket getSocket() {
        return (Socket) this.request
                .getAttribute(SimplePipelineFactory.PROPERTY_SOCKET);
    }

    @Override
    public String getSslCipherSuite() {
        final Socket socket = getSocket();
        if (socket instanceof SSLSocket) {
            final SSLSocket sslSocket = (SSLSocket) socket;
            final SSLSession sslSession = sslSocket.getSession();
            if (sslSession != null) {
                return sslSession.getCipherSuite();
            }
        }
        return null;
    }

    @Override
    public List<Certificate> getSslClientCertificates() {
        final Socket socket = getSocket();
        if (socket instanceof SSLSocket) {
            final SSLSocket sslSocket = (SSLSocket) socket;
            final SSLSession sslSession = sslSocket.getSession();
            if (sslSession != null) {
                try {
                    final List<Certificate> clientCertificates = Arrays
                            .asList(sslSession.getPeerCertificates());

                    return clientCertificates;
                } catch (final SSLPeerUnverifiedException e) {
                    getLogger().log(Level.FINE,
                            "Can't get the client certificates.", e);
                }
            }
        }
        return null;
    }

    @Override
    public String getVersion() {
        return this.request.getMajor() + "." + this.request.getMinor();
    }

    @Override
    public void writeResponseHead(org.restlet.data.Response restletResponse)
            throws IOException {
        this.response.clear();
        for (final Parameter header : getResponseHeaders()) {
            this.response.add(header.getName(), header.getValue());
        }

        // Set the status
        this.response.setCode(getStatusCode());
        this.response.setText(getReasonPhrase());

        // To ensure that Simple doesn't switch to chunked encoding
        if (restletResponse.getEntity() == null) {
            this.response.setContentLength(0);
        }
    }
}
