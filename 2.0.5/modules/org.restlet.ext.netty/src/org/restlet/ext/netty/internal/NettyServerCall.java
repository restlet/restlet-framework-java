/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
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

package org.restlet.ext.netty.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.Parameter;
import org.restlet.engine.http.ServerCall;
import org.restlet.util.Series;

/**
 * Call that is used by the Netty HTTP server connectors.
 * 
 * @author Gabriel Ciuloaica (gciuloaica@gmail.com)
 * @author Jerome Louvel
 */
public class NettyServerCall extends ServerCall {

    /** The Netty HTTP request. */
    private final HttpRequest request;

    /** The Netty HTTP response. */
    private HttpResponse response;

    /** The content buffer. */
    private final ChannelBuffer contentBuffer;

    /** The Netty message event. */
    private final MessageEvent messageEvent;

    /** Indicates if HTTP request headers were added. */
    private volatile boolean requestHeadersAdded;

    /** The underlying SSL engine. */
    private final SSLEngine sslEngine;

    /** The remote client IP address. */
    private final InetSocketAddress remoteAddress;

    /** The Restlet response. */
    private Response restletResponse;

    /**
     * Constructor.
     * 
     * @param server
     *            The helped server.
     * @param messageEvent
     *            The message event received.
     * @param contentBuffer
     *            The content buffer.
     * @param request
     *            The Netty request.
     * @param clientAddress
     *            client information.
     * @param isConfidential
     *            Indicates if the call is confidential or not.
     * @param sslEngine
     *            The SSL engine.
     */
    public NettyServerCall(Server server, MessageEvent messageEvent,
            ChannelBuffer contentBuffer, HttpRequest request,
            InetSocketAddress clientAddress, boolean isConfidential,
            SSLEngine sslEngine) {
        super(server);
        setConfidential(isConfidential);
        this.contentBuffer = contentBuffer;
        this.messageEvent = messageEvent;
        this.request = request;
        this.sslEngine = sslEngine;
        this.remoteAddress = clientAddress;
    }

    /**
     * Closes the socket.
     */
    @Override
    public boolean abort() {
        messageEvent.getChannel().close();
        return true;
    }

    @Override
    public String getClientAddress() {
        return this.remoteAddress.getAddress().getHostAddress();
    }

    @Override
    public int getClientPort() {
        return this.remoteAddress.getPort();
    }

    @Override
    public String getMethod() {
        return request.getMethod().getName();
    }

    @Override
    public ReadableByteChannel getRequestEntityChannel(long size) {
        return null;
    }

    @Override
    public InputStream getRequestEntityStream(long size) {
        return new ChannelBufferInputStream(contentBuffer);
    }

    @Override
    public ReadableByteChannel getRequestHeadChannel() {
        return null;
    }

    @Override
    public Series<Parameter> getRequestHeaders() {
        final Series<Parameter> result = super.getRequestHeaders();

        if (!this.requestHeadersAdded) {
            final Set<String> names = this.request.getHeaderNames();

            for (String name : names) {
                result.add(new Parameter(name, this.request.getHeader(name)));
            }
            this.requestHeadersAdded = true;
        }

        return result;
    }

    @Override
    public InputStream getRequestHeadStream() {
        return null;
    }

    @Override
    public String getRequestUri() {
        return request.getUri();
    }

    /**
     * Get response.
     * 
     * @return the response
     */
    public HttpResponse getResponse() {
        return response;
    }

    @Override
    public WritableByteChannel getResponseEntityChannel() {
        return null;
    }

    @Override
    public OutputStream getResponseEntityStream() {
        return null;
    }

    /**
     * Returns the Restlet response.
     * 
     * @return The Restlet response.
     */
    public Response getRestletResponse() {
        return restletResponse;
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

    /**
     * Returns the SSL engine.
     * 
     * @return The SSL engine.
     */
    private SSLEngine getSslEngine() {
        return this.sslEngine;
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
        return request.getProtocolVersion().getText();
    }

    @Override
    protected boolean isClientKeepAlive() {
        return HttpHeaders.isKeepAlive(request);
    }

    @Override
    public void sendResponse(Response response) throws IOException {
        setRestletResponse(response);
    }

    /**
     * Sets the Restlet response.
     * 
     * @param restletResponse
     *            The Restlet response.
     */
    public void setRestletResponse(Response restletResponse) {
        this.restletResponse = restletResponse;
    }

    @Override
    public void writeResponseHead(Response restletResponse) throws IOException {
    }

}
