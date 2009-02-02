/**
 * Copyright 2005-2009 Noelios Technologies.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.simple;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import org.restlet.Server;
import org.restlet.data.Parameter;
import org.restlet.engine.http.HttpServerCall;
import org.restlet.util.Series;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

/**
 * Call that is used by the Simple HTTP server.
 * 
 * @author Lars Heuer
 * @author Jerome Louvel
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
     * The version of the request;
     */
    private final String version;

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
        this.version = request.getMajor() + "." + request.getMinor();
        this.request = request;
        this.response = response;
        setConfidential(confidential);
        this.requestHeadersAdded = false;
    }

    @Override
    protected boolean isClientKeepAlive() {
        return request.isKeepAlive();
    }

    @Override
    protected long getContentLength() {
        return request.getContentLength();
    }

    @Override
    public void complete() {
        try {
            // Commit the response
            this.response.commit();
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Unable to commit the response", ex);
        }
    }

    @Override
    public String getHostDomain() {
        return super.getHostDomain(); // FIXME
    }

    @Override
    public String getClientAddress() {
        return this.request.getClientAddress().getAddress().getHostAddress();
    }

    @Override
    public int getClientPort() {
        final SocketChannel socket = getSocket();
        return (socket != null) ? socket.socket().getPort() : -1;
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
    	// No performance benefit getting request channel
        //try {
        //    return this.request.getByteChannel();
        //} catch (Exception ex) {
        //    return null;
        //}
    	return null;
    }

    @Override
    public InputStream getRequestEntityStream(long size) {
        try {
            return this.request.getInputStream();
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public ReadableByteChannel getRequestHeadChannel() {
    	// No performance benefit getting request channel
        //try {
        //    return this.request.getByteChannel();
        //} catch (Exception ex) {
        //    return null;
        //}
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
            final List<String> names = this.request.getNames();

            for (String name : names) {
                result.add(new Parameter(name, this.request.getValue(name)));
            }
            this.requestHeadersAdded = true;
        }

        return result;
    }

    @Override
    public InputStream getRequestHeadStream() {
        //try {
        //    return this.request.getInputStream();
        //} catch (Exception ex) {
        //    return null;
        //}
    	return null;
    }

    /**
     * Returns the full request URI.
     * 
     * @return The full request URI.
     */
    @Override
    public String getRequestUri() {
        return this.request.getTarget();
    }

    /**
     * Returns the response channel if it exists.
     * 
     * @return The response channel if it exists.
     */
    @Override
    public WritableByteChannel getResponseEntityChannel() {
        try {
            return this.response.getByteChannel();
        } catch (Exception ex) {
            return null;
        }
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
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Returns the request socket.
     * 
     * @return The request socket.
     */
    private SocketChannel getSocket() {
        return (SocketChannel) this.request
                .getAttribute(SimpleServer.PROPERTY_SOCKET);
    }

    /**
     * Returns the SSL engine.
     * 
     * @return the SSL engine
     */
    private SSLEngine getSslEngine() {
        return (SSLEngine) this.request
                .getAttribute(SimpleServer.PROPERTY_ENGINE);
    }

    @Override
    public String getSslCipherSuite() {
        final SSLEngine sslEngine = getSslEngine();
        if (sslEngine != null) {
            final SSLSession sslSession = sslEngine.getSession();
            if (sslSession != null) {
                return sslSession.getCipherSuite();
            }
        }
        return null;
    }

    @Override
    public List<Certificate> getSslClientCertificates() {
        final SSLEngine sslEngine = getSslEngine();
        if (sslEngine != null) {
            final SSLSession sslSession = sslEngine.getSession();
            if (sslSession != null) {
                try {
                    final List<Certificate> clientCertificates = Arrays
                            .asList(sslSession.getPeerCertificates());

                    return clientCertificates;
                } catch (SSLPeerUnverifiedException e) {
                    getLogger().log(Level.FINE,
                            "Can't get the client certificates.", e);
                }
            }
        }
        return null;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public void writeResponseHead(org.restlet.data.Response restletResponse)
            throws IOException {
        // this.response.clear();
        for (final Parameter header : getResponseHeaders()) {
            this.response.add(header.getName(), header.getValue());
        }

        // Set the status
        this.response.setCode(getStatusCode());
        this.response.setText(getReasonPhrase());

        // Is this really required
        if (restletResponse.getEntity() == null) {
            this.response.setContentLength(0);
        }
    }
}
