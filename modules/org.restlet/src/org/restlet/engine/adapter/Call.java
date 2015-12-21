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

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.data.Header;
import org.restlet.data.Protocol;
import org.restlet.engine.header.HeaderUtils;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

/**
 * Low-level call for the HTTP connectors.
 * 
 * @author Jerome Louvel
 */
public abstract class Call {
    /**
     * Returns true if the given exception is caused by a broken connection.
     * 
     * @param exception
     *            The exception to inspect.
     * @return True if the given exception is caused by a broken connection.
     */
    public static boolean isBroken(Throwable exception) {
        boolean result = false;

        // detect Tomcat and Jetty exceptions
        if (exception instanceof IOException) {
            String exceptionName = exception.getClass().getName();
            result = (exceptionName.endsWith("ClientAbortException") ||
                exceptionName.endsWith("jetty.io.EofException"));
        }

        // check for known exception messages
        if (!result) {
            String exceptionMessage = exception.getMessage();
            if (exceptionMessage != null) {
                result = (exceptionMessage.indexOf("Broken pipe") != -1) ||
                    (exceptionMessage.equals("An existing connection must have been closed by the remote party.") ||
                        (exceptionMessage.equals("An open connection has been abandonned by your network stack.")));
            }
        }

        if (!result && exception.getCause() != null) {
            result = isBroken(exception.getCause());
        }

        return result;
    }

    /** The client IP address. */
    private volatile String clientAddress;

    /** The client port. */
    private volatile int clientPort;

    /** Indicates if the call is confidential. */
    private volatile boolean confidential;

    /** The hostRef domain. */
    private volatile String hostDomain;

    /** The hostRef port. */
    private volatile int hostPort;

    /** The method. */
    private volatile String method;

    /** The exact protocol. */
    private volatile Protocol protocol;

    /** The reason phrase. */
    private volatile String reasonPhrase;

    /** The request headers. */
    private final Series<Header> requestHeaders;

    /** The request URI. */
    private volatile String requestUri;

    /** The response headers. */
    private final Series<Header> responseHeaders;

    /** The server IP address. */
    private volatile String serverAddress;

    /** The server port. */
    private volatile int serverPort;

    /** The status code. */
    private volatile int statusCode;

    // [ifndef gwt] member
    /** The user principal. */
    private volatile java.security.Principal userPrincipal;

    /** The protocol version. */
    private volatile String version;

    /**
     * Constructor.
     */
    public Call() {
        this.hostDomain = null;
        this.hostPort = -1;
        this.clientAddress = null;
        this.clientPort = -1;
        this.confidential = false;
        this.method = null;
        this.protocol = null;
        this.reasonPhrase = "";
        // [ifndef gwt] instruction
        this.requestHeaders = new Series<Header>(Header.class);
        // [ifdef gwt] instruction uncomment
        // this.requestHeaders = new org.restlet.engine.util.HeaderSeries();
        this.requestUri = null;
        // [ifndef gwt] instruction
        this.responseHeaders = new Series<Header>(Header.class);
        // [ifdef gwt] instruction uncomment
        // this.responseHeaders = new org.restlet.engine.util.HeaderSeries();
        this.serverAddress = null;
        this.serverPort = -1;
        this.statusCode = 200;
        // [ifndef gwt] line
        this.userPrincipal = null;
        this.version = null;
    }

    /**
     * Returns the client address.<br>
     * Corresponds to the IP address of the requesting client.
     * 
     * @return The client address.
     */
    public String getClientAddress() {
        return this.clientAddress;
    }

    /**
     * Returns the client port.<br>
     * Corresponds to the TCP/IP port of the requesting client.
     * 
     * @return The client port.
     */
    public int getClientPort() {
        return this.clientPort;
    }

    /**
     * Returns the host domain.
     * 
     * @return The host domain.
     */
    public String getHostDomain() {
        return this.hostDomain;
    }

    /**
     * Returns the host port.
     * 
     * @return The host port.
     */
    public int getHostPort() {
        return this.hostPort;
    }

    /**
     * Returns the logger.
     * 
     * @return The logger.
     */
    public Logger getLogger() {
        return Context.getCurrentLogger();
    }

    /**
     * Returns the request method.
     * 
     * @return The request method.
     */
    public String getMethod() {
        return this.method;
    }

    /**
     * Returns the exact protocol (HTTP or HTTPS).
     * 
     * @return The exact protocol (HTTP or HTTPS).
     */
    public Protocol getProtocol() {
        if (this.protocol == null) {
            this.protocol = isConfidential() ? Protocol.HTTPS : Protocol.HTTP;
        }
        return this.protocol;
    }

    /**
     * Returns the reason phrase.
     * 
     * @return The reason phrase.
     */
    public String getReasonPhrase() {
        return this.reasonPhrase;
    }

    /**
     * Returns the representation wrapping the given stream.
     * 
     * @param stream
     *            The response input stream.
     * @return The wrapping representation.
     */
    protected Representation getRepresentation(InputStream stream) {
        return new InputRepresentation(stream, null);
    }

    // [ifndef gwt] method
    /**
     * Returns the representation wrapping the given channel.
     * 
     * @param channel
     *            The response channel.
     * @return The wrapping representation.
     */
    protected Representation getRepresentation(
            java.nio.channels.ReadableByteChannel channel) {
        return new org.restlet.representation.ReadableRepresentation(channel,
                null);
    }

    /**
     * Returns the modifiable list of request headers.
     * 
     * @return The modifiable list of request headers.
     */
    public Series<Header> getRequestHeaders() {
        return this.requestHeaders;
    }

    /**
     * Returns the URI on the request line (most like a relative reference, but
     * not necessarily).
     * 
     * @return The URI on the request line.
     */
    public String getRequestUri() {
        return this.requestUri;
    }

    /**
     * Returns the modifiable list of server headers.
     * 
     * @return The modifiable list of server headers.
     */
    public Series<Header> getResponseHeaders() {
        return this.responseHeaders;
    }

    /**
     * Returns the response address.<br>
     * Corresponds to the IP address of the responding server.
     * 
     * @return The response address.
     */
    public String getServerAddress() {
        return this.serverAddress;
    }

    /**
     * Returns the server port.
     * 
     * @return The server port.
     */
    public int getServerPort() {
        return this.serverPort;
    }

    /**
     * Returns the status code.
     * 
     * @return The status code.
     * @throws IOException
     */
    public int getStatusCode() throws IOException {
        return this.statusCode;
    }

    // [ifndef gwt] method
    /**
     * Returns the user principal.
     * 
     * @return The user principal.
     */
    public java.security.Principal getUserPrincipal() {
        return this.userPrincipal;
    }

    /**
     * Returns the protocol version used.
     * 
     * @return The protocol version used.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Indicates if the client wants a persistent connection.
     * 
     * @return True if the client wants a persistent connection.
     */
    protected abstract boolean isClientKeepAlive();

    /**
     * Indicates if the confidentiality of the call is ensured (ex: via SSL).
     * 
     * @return True if the confidentiality of the call is ensured (ex: via SSL).
     */
    public boolean isConfidential() {
        return this.confidential;
    }

    // [ifndef gae,gwt] method
    /**
     * Returns true if the given exception is caused by a broken connection.
     * 
     * @param exception
     *            The exception to inspect.
     * @return True if the given exception is caused by a broken connection.
     */
    public boolean isConnectionBroken(Throwable exception) {
        return isBroken(exception);
    }

    /**
     * Indicates if both the client and the server want a persistent connection.
     * 
     * @return True if the connection should be kept alive after the call
     *         processing.
     */
    protected boolean isKeepAlive() {
        return isClientKeepAlive() && isServerKeepAlive();
    }

    /**
     * Indicates if the request entity is chunked.
     * 
     * @return True if the request entity is chunked.
     */
    protected boolean isRequestChunked() {
        return HeaderUtils.isChunkedEncoding(getRequestHeaders());
    }

    /**
     * Indicates if the response entity is chunked.
     * 
     * @return True if the response entity is chunked.
     */
    protected boolean isResponseChunked() {
        return HeaderUtils.isChunkedEncoding(getResponseHeaders());
    }

    /**
     * Indicates if the server wants a persistent connection.
     * 
     * @return True if the server wants a persistent connection.
     */
    protected abstract boolean isServerKeepAlive();

    /**
     * Sets the client address.
     * 
     * @param clientAddress
     *            The client address.
     */
    protected void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    /**
     * Sets the client port.
     * 
     * @param clientPort
     *            The client port.
     */
    protected void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    /**
     * Indicates if the confidentiality of the call is ensured (ex: via SSL).
     * 
     * @param confidential
     *            True if the confidentiality of the call is ensured (ex: via
     *            SSL).
     */
    protected void setConfidential(boolean confidential) {
        this.confidential = confidential;
    }

    /**
     * Sets the host domain name.
     * 
     * @param hostDomain
     *            The baseRef domain name.
     */
    public void setHostDomain(String hostDomain) {
        this.hostDomain = hostDomain;
    }

    /**
     * Sets the host port.
     * 
     * @param hostPort
     *            The host port.
     */
    public void setHostPort(int hostPort) {
        this.hostPort = hostPort;
    }

    /**
     * Sets the request method.
     * 
     * @param method
     *            The request method.
     */
    protected void setMethod(String method) {
        this.method = method;
    }

    /**
     * Sets the exact protocol used (HTTP or HTTPS).
     * 
     * @param protocol
     *            The protocol.
     */
    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    /**
     * Sets the reason phrase.
     * 
     * @param reasonPhrase
     *            The reason phrase.
     */
    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    /**
     * Sets the full request URI.
     * 
     * @param requestUri
     *            The full request URI.
     */
    protected void setRequestUri(String requestUri) {
        if ((requestUri == null) || (requestUri.equals(""))) {
            requestUri = "/";
        }

        this.requestUri = requestUri;
    }

    /**
     * Sets the response address.<br>
     * Corresponds to the IP address of the responding server.
     * 
     * @param responseAddress
     *            The response address.
     */
    public void setServerAddress(String responseAddress) {
        this.serverAddress = responseAddress;
    }

    /**
     * Sets the server port.
     * 
     * @param serverPort
     *            The server port.
     */
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * Sets the status code.
     * 
     * @param code
     *            The status code.
     */
    public void setStatusCode(int code) {
        this.statusCode = code;
    }

    // [ifndef gwt] method
    /**
     * Sets the user principal.
     * 
     * @param principal
     *            The user principal.
     */
    public void setUserPrincipal(java.security.Principal principal) {
        this.userPrincipal = principal;
    }

    /**
     * Sets the protocol version used.
     * 
     * @param version
     *            The protocol version used.
     */
    public void setVersion(String version) {
        this.version = version;
    }

}
