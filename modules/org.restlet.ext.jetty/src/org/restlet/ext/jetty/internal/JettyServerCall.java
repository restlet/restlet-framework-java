/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.jetty.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.jetty.io.EofException;
import org.eclipse.jetty.server.HttpChannel;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.Status;
import org.restlet.engine.adapter.ServerCall;
import org.restlet.engine.header.Header;
import org.restlet.util.Series;

/**
 * Call that is used by the Jetty HTTP server connectors.
 * 
 * @author Jerome Louvel
 * @author Tal Liron
 */
public class JettyServerCall extends ServerCall {

    /**
     * Constructor.
     * 
     * @param server
     *            The parent server.
     * @param channel
     *            The wrapped Jetty HTTP channel.
     */
    public JettyServerCall(Server server, HttpChannel<?> channel) {
        super(server);
        this.channel = channel;
        this.requestHeadersAdded = false;
    }

    /**
     * Closes the end point.
     */
    public boolean abort() {
        getChannel().getEndPoint().close();
        return true;
    }

    @Override
    public void complete() {
        // Flush the response
        try {
            getChannel().getResponse().flushBuffer();
        } catch (IOException ex) {
            getLogger().log(Level.FINE, "Unable to flush the response", ex);
        }

        // Fully complete the response
        try {
            getChannel().getResponse().closeOutput();
        } catch (IOException ex) {
            getLogger().log(Level.FINE, "Unable to complete the response", ex);
        }
    }

    @Override
    public void flushBuffers() throws IOException {
        getChannel().getResponse().flushBuffer();
    }

    @Override
    public List<Certificate> getCertificates() {
        final Object certificateArray = getChannel().getRequest().getAttribute(
                "javax.servlet.request.X509Certificate");
        if (certificateArray instanceof Certificate[])
            return Arrays.asList((Certificate[]) certificateArray);
        return null;
    }

    @Override
    public String getCipherSuite() {
        final Object cipherSuite = getChannel().getRequest().getAttribute(
                "javax.servlet.request.cipher_suite");
        if (cipherSuite instanceof String)
            return (String) cipherSuite;
        return null;
    }

    @Override
    public String getClientAddress() {
        return getChannel().getRequest().getRemoteAddr();
    }

    @Override
    public int getClientPort() {
        return getChannel().getRequest().getRemotePort();
    }

    /**
     * Returns the wrapped Jetty HTTP channel.
     * 
     * @return The wrapped Jetty HTTP channel.
     */
    public HttpChannel<?> getChannel() {
        return this.channel;
    }

    /**
     * Returns the request method.
     * 
     * @return The request method.
     */
    @Override
    public String getMethod() {
        return getChannel().getRequest().getMethod();
    }

    public InputStream getRequestEntityStream(long size) {
        try {
            return getChannel().getRequest().getInputStream();
        } catch (IOException e) {
            getLogger().log(Level.WARNING,
                    "Unable to get request entity stream", e);
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
            // Copy the headers from the request object
            for (Enumeration<String> names = getChannel().getRequest()
                    .getHeaderNames(); names.hasMoreElements();) {
                final String headerName = names.nextElement();
                for (Enumeration<String> values = getChannel().getRequest()
                        .getHeaders(headerName); values.hasMoreElements();) {
                    final String headerValue = values.nextElement();
                    result.add(headerName, headerValue);
                }
            }

            this.requestHeadersAdded = true;
        }

        return result;
    }

    public InputStream getRequestHeadStream() {
        // Not available
        return null;
    }

    /**
     * Returns the URI on the request line (most like a relative reference, but
     * not necessarily).
     * 
     * @return The URI on the request line.
     */
    @Override
    public String getRequestUri() {
        return getChannel().getRequest().getUri().toString();
    }

    /**
     * Returns the response stream if it exists.
     * 
     * @return The response stream if it exists.
     */
    public OutputStream getResponseEntityStream() {
        try {
            return getChannel().getResponse().getOutputStream();
        } catch (IOException e) {
            getLogger().log(Level.WARNING,
                    "Unable to get response entity stream", e);
            return null;
        }
    }

    /**
     * Returns the response address.<br>
     * Corresponds to the IP address of the responding server.
     * 
     * @return The response address.
     */
    @Override
    public String getServerAddress() {
        return getChannel().getRequest().getLocalAddr();
    }

    @Override
    public Integer getSslKeySize() {
        Integer keySize = (Integer) getChannel().getRequest().getAttribute(
                "javax.servlet.request.key_size");
        if (keySize == null)
            keySize = super.getSslKeySize();
        return keySize;
    }

    @Override
    public String getSslSessionId() {
        final Object sessionId = getChannel().getRequest().getAttribute(
                "javax.servlet.request.ssl_session_id");
        if (sessionId instanceof String)
            return (String) sessionId;
        return null;
    }

    /**
     * Indicates if the request was made using a confidential mean.<br>
     * 
     * @return True if the request was made using a confidential mean.<br>
     */
    @Override
    public boolean isConfidential() {
        return getChannel().getRequest().isSecure();
    }

    @Override
    public boolean isConnectionBroken(Throwable exception) {
        return (exception instanceof EofException)
                || super.isConnectionBroken(exception);
    }

    @Override
    public void sendResponse(Response response) throws IOException {
        // Add call headers
        for (Iterator<Header> iter = getResponseHeaders().iterator(); iter
                .hasNext();) {
            Header header = iter.next();
            getChannel().getResponse().addHeader(header.getName(),
                    header.getValue());
        }

        // Set the status code in the response. We do this after adding the
        // headers because when we have to rely on the 'sendError' method,
        // the Servlet containers are expected to commit their response.
        if (Status.isError(getStatusCode()) && (response.getEntity() == null)) {
            try {
                getChannel().getResponse().sendError(getStatusCode(),
                        getReasonPhrase());
            } catch (IOException ioe) {
                getLogger().log(Level.WARNING,
                        "Unable to set the response error status", ioe);
            }
        } else {
            // Send the response entity
            getChannel().getResponse().setStatus(getStatusCode());
            super.sendResponse(response);
        }
    }

    /** The wrapped Jetty HTTP channel. */
    private final HttpChannel<?> channel;

    /** Indicates if the request headers were parsed and added. */
    private volatile boolean requestHeadersAdded;
}
