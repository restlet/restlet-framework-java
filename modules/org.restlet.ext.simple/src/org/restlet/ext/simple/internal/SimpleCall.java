/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.simple.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SocketChannel;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import org.restlet.Server;
import org.restlet.data.Method;
import org.restlet.engine.adapter.ServerCall;
import org.restlet.engine.header.Header;
import org.restlet.ext.ssl.internal.SslUtils;
import org.restlet.util.Series;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

/**
 * Call that is used by the Simple HTTP server.
 * 
 * @author Lars Heuer
 * @author Jerome Louvel
 */
public class SimpleCall extends ServerCall {

    /**
     * Simple Request.
     */
    private final Request request;

    /** Indicates if the request headers were parsed and added. */
    private volatile boolean requestHeadersAdded;

    /**
     * Simple Response.
     */
    private final Response response;

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

    /**
     * Closes the socket.
     */
    @Override
    public boolean abort() {
        try {
            getSocket().close();
        } catch (IOException e) {
        }

        return true;
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
    public List<Certificate> getCertificates() {
        SSLEngine sslEngine = getSslEngine();

        if (sslEngine != null) {
             SSLSession sslSession = sslEngine.getSession();

            if (sslSession != null) {
                try {
                    return Arrays.asList(sslSession.getPeerCertificates());
                } catch (SSLPeerUnverifiedException e) {
                    getLogger().log(Level.FINE,
                            "Can't get the client certificates.", e);
                }
            }
        }
        return null;
    }

    @Override
    public String getCipherSuite() {
        SSLEngine sslEngine = getSslEngine();

        if (sslEngine != null) {
            SSLSession sslSession = sslEngine.getSession();

            if (sslSession != null) {
                return sslSession.getCipherSuite();
            }
        }
        return null;
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

    @Override
    protected long getContentLength() {
        return request.getContentLength();
    }

    @Override
    public String getHostDomain() {
        return super.getHostDomain(); // FIXME
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
    public InputStream getRequestEntityStream(long size) {
        try {
            return this.request.getInputStream();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Returns the list of request headers.
     * 
     * @return The list of request headers.
     */
    @Override
    public Series<Header> getRequestHeaders() {
        final Series<Header> result = super.getRequestHeaders();

        if (!this.requestHeadersAdded) {
            final List<String> names = this.request.getNames();

            for (String name : names) {
                for (String value : this.request.getValues(name)) {
                    result.add(name, value);
                }
            }
            this.requestHeadersAdded = true;
        }

        return result;
    }

    @Override
    public InputStream getRequestHeadStream() {
        // try {
        // return this.request.getInputStream();
        // } catch (Exception ex) {
        // return null;
        // }
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
    public Integer getSslKeySize() {
        Integer keySize = null;
        String sslCipherSuite = getCipherSuite();

        if (sslCipherSuite != null) {
            keySize = SslUtils.extractKeySize(sslCipherSuite);
        }

        return keySize;
    }

    @Override
    protected byte[] getSslSessionIdBytes() {
        final SSLEngine sslEngine = getSslEngine();

        if (sslEngine != null) {
            final SSLSession sslSession = sslEngine.getSession();

            if (sslSession != null) {
                return sslSession.getId();
            }
        }

        return null;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    protected boolean isClientKeepAlive() {
        return request.isKeepAlive();
    }

    @Override
    public void writeResponseHead(org.restlet.Response restletResponse)
            throws IOException {
        // this.response.clear();
        for (Header header : getResponseHeaders()) {
            this.response.add(header.getName(), header.getValue());
        }

        // Set the status
        this.response.setCode(getStatusCode());
        this.response.setText(getReasonPhrase());

        // Ensure the HEAD response sends back the right Content-length header.
        if (!Method.HEAD.equals(restletResponse.getRequest().getMethod())
                && restletResponse.getEntity() == null) {
            this.response.setContentLength(0);
        }
    }
}
