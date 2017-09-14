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

package org.restlet.ext.xdb.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.Header;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.adapter.ServerCall;
import org.restlet.util.Series;

/**
 * Call that is used by the XDB Servlet HTTP connector. This is a downgrade
 * version to Servlet 2.2 of ServletCall class.
 * 
 * @see org.restlet.ext.servlet.internal.ServletCall
 * @author Marcelo F. Ochoa (mochoa@ieee.org)
 * @deprecated Not actively developed anymore.
 */
@Deprecated
public class XdbServletCall extends ServerCall {
    /** The HTTP Servlet request to wrap. */
    private volatile HttpServletRequest request;

    /** The request entity stream */
    private volatile InputStream requestEntityStream;

    /** The request headers. */
    private volatile Series<Header> requestHeaders;

    /** The HTTP Servlet response to wrap. */
    private volatile HttpServletResponse response;

    /** The response entity output stream. */
    private volatile OutputStream responseEntityStream;

    /**
     * Constructor.
     * 
     * @param server
     *            The parent server.
     * @param request
     *            The HTTP Servlet request to wrap.
     * @param response
     *            The HTTP Servlet response to wrap.
     */
    public XdbServletCall(Server server, HttpServletRequest request,
            HttpServletResponse response) {
        super(server);
        this.request = request;
        this.response = response;
    }

    /**
     * Constructor.
     * 
     * @param serverAddress
     *            The server IP address.
     * @param serverPort
     *            The server port.
     * @param request
     *            The Servlet request.
     * @param response
     *            The Servlet response.
     */
    public XdbServletCall(String serverAddress, int serverPort,
            HttpServletRequest request, HttpServletResponse response) {
        super(serverAddress, serverPort);
        this.request = request;
        this.response = response;
    }

    /**
     * Not supported. Always returns false.
     */
    @Override
    public boolean abort() {
        return false;
    }

    @Override
    public List<Certificate> getCertificates() {
        Certificate[] certificateArray = (Certificate[]) getRequest()
                .getAttribute("javax.servlet.request.X509Certificate");

        if (certificateArray != null) {
            return Arrays.asList(certificateArray);
        }

        return Arrays.asList(new Certificate[0]);
    }

    @Override
    public String getCipherSuite() {
        return (String) getRequest().getAttribute(
                "javax.servlet.request.cipher_suite");
    }

    @Override
    public String getMethod() {
        return getRequest().getMethod();
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.valueOf(getRequest().getScheme());
    }

    /**
     * Returns the HTTP Servlet request.
     * 
     * @return The HTTP Servlet request.
     */
    public HttpServletRequest getRequest() {
        return this.request;
    }

    @Override
    public InputStream getRequestEntityStream(long size) {
        if (this.requestEntityStream == null) {
            try {
                if (isRequestChunked()) {
                    this.requestEntityStream = new ChunkedInputStream(null,
                            getRequest().getInputStream());
                } else {
                    this.requestEntityStream = new SizedInputStream(null,
                            getRequest().getInputStream(), size);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this.requestEntityStream;
    }

    @Override
    public Series<Header> getRequestHeaders() {
        if (this.requestHeaders == null) {
            this.requestHeaders = new Series<Header>(Header.class);

            // Copy the headers from the request object
            String headerName;
            String headerValue;

            for (Enumeration<String> names = getRequest().getHeaderNames(); names
                    .hasMoreElements();) {
                headerName = names.nextElement();

                for (Enumeration<String> values = getRequest().getHeaders(
                        headerName); values.hasMoreElements();) {
                    headerValue = values.nextElement();
                    this.requestHeaders.add(headerName, headerValue);
                }
            }
        }

        return this.requestHeaders;
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
        final String queryString = getRequest().getQueryString();

        if ((queryString == null) || (queryString.equals(""))) {
            return getRequest().getRequestURI();
        }

        return getRequest().getRequestURI() + '?' + queryString;
    }

    /**
     * Returns the HTTP Servlet response.
     * 
     * @return The HTTP Servlet response.
     */
    public HttpServletResponse getResponse() {
        return this.response;
    }

    @Override
    public OutputStream getResponseEntityStream() {
        if (this.responseEntityStream == null) {
            try {
                if (isResponseChunked()) {
                    this.responseEntityStream = new ChunkedOutputStream(
                            getResponse().getOutputStream());
                } else {
                    // this.responseEntityStream = new KeepAliveOutputStream(
                    this.responseEntityStream = getResponse().getOutputStream();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this.responseEntityStream;
    }

    @Override
    public Integer getSslKeySize() {
        Integer keySize = (Integer) getRequest().getAttribute(
                "javax.servlet.request.key_size");

        if (keySize == null) {
            keySize = super.getSslKeySize();
        }

        return keySize;
    }

    @Override
    public String getSslSessionId() {
        Object sessionId = getRequest().getAttribute(
                "javax.servlet.request.ssl_session_id");

        if ((sessionId != null) && (sessionId instanceof String)) {
            return (String) sessionId;
        }

        /*
         * The following is for the non-standard, pre-Servlet 3 spec used by
         * Tomcat/Coyote.
         */
        sessionId = getRequest().getAttribute(
                "javax.servlet.request.ssl_session");

        if (sessionId instanceof String) {
            return (String) sessionId;
        }

        return null;
    }

    @Override
    public String getVersion() {
        String result = null;
        final int index = getRequest().getProtocol().indexOf('/');

        if (index != -1) {
            result = getRequest().getProtocol().substring(index + 1);
        }

        return result;
    }

    @Override
    public boolean isConfidential() {
        return getRequest().isSecure();
    }

    /**
     * Sends the response back to the client. Commits the status, headers and
     * optional entity and send them on the network.
     * 
     * @param response
     *            The high-level response.
     * @throws IOException
     */
    @Override
    public void sendResponse(Response response) throws IOException {
        // Add the response headers
        Header header;

        for (Iterator<Header> iter = getResponseHeaders().iterator(); iter
                .hasNext();) {
            header = iter.next();
            getResponse().addHeader(header.getName(), header.getValue());
        }

        // Set the status code in the response. We do this after adding the
        // headers because when we have to rely on the 'sendError' method,
        // the Servlet containers are expected to commit their response.
        if (Status.isError(getStatusCode()) && (response == null)) {
            try {
                getResponse().sendError(getStatusCode(), getReasonPhrase());
            } catch (IOException ioe) {
                getLogger().log(Level.WARNING,
                        "Unable to set the response error status", ioe);
            }
        } else {
            // Send the response entity
            getResponse().setStatus(getStatusCode());
            super.sendResponse(response);
        }
    }

}
