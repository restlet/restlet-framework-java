/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.ext.jetty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.mortbay.jetty.EofException;
import org.mortbay.jetty.HttpConnection;
import org.restlet.Server;
import org.restlet.data.Parameter;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.engine.http.HttpServerCall;
import org.restlet.util.Series;

/**
 * Call that is used by the Jetty 6 HTTP server connector.
 * 
 * @author Jerome Louvel
 */
public class JettyCall extends HttpServerCall {
    /** The wrapped Jetty HTTP connection. */
    private final HttpConnection connection;

    /** Indicates if the request headers were parsed and added. */
    private volatile boolean requestHeadersAdded;

    /**
     * Constructor.
     * 
     * @param server
     *            The parent server.
     * @param connection
     *            The wrapped Jetty HTTP connection.
     */
    public JettyCall(Server server, HttpConnection connection) {
        super(server);
        this.connection = connection;
        this.requestHeadersAdded = false;
    }

    @Override
    public void complete() {
        // Flush the response
        try {
            this.connection.flushResponse();
        } catch (IOException ex) {
            getLogger().log(Level.FINE,
                    "Unable to flush the response", ex);
        }

        // Fully complete the response
        try {
            this.connection.completeResponse();
        } catch (IOException ex) {
            getLogger().log(Level.FINE,
                    "Unable to complete the response", ex);
        }
    }

    @Override
    public String getClientAddress() {
        return getConnection().getRequest().getRemoteAddr();
    }

    @Override
    public int getClientPort() {
        return getConnection().getRequest().getRemotePort();
    }

    /**
     * Returns the wrapped Jetty HTTP connection.
     * 
     * @return The wrapped Jetty HTTP connection.
     */
    public HttpConnection getConnection() {
        return this.connection;
    }

    /**
     * Returns the request method.
     * 
     * @return The request method.
     */
    @Override
    public String getMethod() {
        return getConnection().getRequest().getMethod();
    }

    @Override
    public ReadableByteChannel getRequestEntityChannel(long size) {
        return null;
    }

    @Override
    public InputStream getRequestEntityStream(long size) {
        try {
            return getConnection().getRequest().getInputStream();
        } catch (IOException e) {
            getLogger().log(Level.WARNING,
                    "Unable to get request entity stream", e);
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
    @SuppressWarnings("unchecked")
    public Series<Parameter> getRequestHeaders() {
        final Series<Parameter> result = super.getRequestHeaders();

        if (!this.requestHeadersAdded) {
            // Copy the headers from the request object
            String headerName;
            String headerValue;
            for (final Enumeration<String> names = getConnection()
                    .getRequestFields().getFieldNames(); names
                    .hasMoreElements();) {
                headerName = names.nextElement();
                for (final Enumeration<String> values = getConnection()
                        .getRequestFields().getValues(headerName); values
                        .hasMoreElements();) {
                    headerValue = values.nextElement();
                    result.add(new Parameter(headerName, headerValue));
                }
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
     * Returns the URI on the request line (most like a relative reference, but
     * not necessarily).
     * 
     * @return The URI on the request line.
     */
    @Override
    public String getRequestUri() {
        return getConnection().getRequest().getUri().toString();
    }

    /**
     * Returns the response channel if it exists.
     * 
     * @return The response channel if it exists.
     */
    @Override
    public WritableByteChannel getResponseEntityChannel() {
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
            return getConnection().getResponse().getOutputStream();
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
        return getConnection().getRequest().getLocalAddr();
    }

    @Override
    public String getSslCipherSuite() {
        return (String) getConnection().getRequest().getAttribute(
                "javax.servlet.request.cipher_suite");
    }

    @Override
    public List<Certificate> getSslClientCertificates() {
        final Certificate[] certificateArray = (Certificate[]) getConnection()
                .getRequest().getAttribute(
                        "javax.servlet.request.X509Certificate");
        if (certificateArray != null) {
            return Arrays.asList(certificateArray);
        } else {
            return null;
        }
    }

    @Override
    public Integer getSslKeySize() {
        Integer keySize = (Integer) getConnection().getRequest().getAttribute(
                "javax.servlet.request.key_size");
        if (keySize == null) {
            keySize = super.getSslKeySize();
        }
        return keySize;
    }

    /**
     * Indicates if the request was made using a confidential mean.<br>
     * 
     * @return True if the request was made using a confidential mean.<br>
     */
    @Override
    public boolean isConfidential() {
        return getConnection().getRequest().isSecure();
    }

    @Override
    public boolean isConnectionBroken(Exception exception) {
        return (exception instanceof EofException)
                || super.isConnectionBroken(exception);
    }

    @Override
    public void sendResponse(Response response) throws IOException {
        // Add call headers
        Parameter header;
        for (final Iterator<Parameter> iter = getResponseHeaders().iterator(); iter
                .hasNext();) {
            header = iter.next();
            getConnection().getResponse().addHeader(header.getName(),
                    header.getValue());
        }

        // Set the status code in the response. We do this after adding the
        // headers because when we have to rely on the 'sendError' method,
        // the Servlet containers are expected to commit their response.
        if (Status.isError(getStatusCode()) && (response.getEntity() == null)) {
            try {
                getConnection().getResponse().sendError(getStatusCode(),
                        getReasonPhrase());
            } catch (IOException ioe) {
                getLogger().log(Level.WARNING,
                        "Unable to set the response error status", ioe);
            }
        } else {
            // Send the response entity
            getConnection().getResponse().setStatus(getStatusCode());
            super.sendResponse(response);
        }

    }
}
