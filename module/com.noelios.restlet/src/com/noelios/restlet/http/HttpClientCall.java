/*
 * Copyright 2005-2007 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Level;

import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Request;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.resource.InputRepresentation;
import org.restlet.resource.ReadableRepresentation;
import org.restlet.resource.Representation;
import org.restlet.service.ConnectorService;

import com.noelios.restlet.util.HeaderReader;

/**
 * Low-level HTTP client call.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class HttpClientCall extends HttpCall {
    /** The parent HTTP client helper. */
    private HttpClientHelper helper;

    /**
     * Constructor setting the request address to the local host.
     * 
     * @param helper
     *            The parent HTTP client helper.
     * @param method
     *            The method name.
     * @param requestUri
     *            The request URI.
     */
    public HttpClientCall(HttpClientHelper helper, String method,
            String requestUri) {
        this.helper = helper;
        setMethod(method);
        setRequestUri(requestUri);
        setClientAddress(getLocalAddress());
    }

    /**
     * Returns the HTTP client helper.
     * 
     * @return The HTTP client helper.
     */
    public HttpClientHelper getHelper() {
        return this.helper;
    }

    /**
     * Returns the local IP address or 127.0.0.1 if the resolution fails.
     * 
     * @return The local IP address or 127.0.0.1 if the resolution fails.
     */
    public static String getLocalAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }

    /**
     * Returns the request entity channel if it exists.
     * 
     * @return The request entity channel if it exists.
     */
    public WritableByteChannel getRequestChannel() {
        return null;
    }

    /**
     * Returns the request entity stream if it exists.
     * 
     * @return The request entity stream if it exists.
     */
    public OutputStream getRequestStream() {
        return null;
    }

    /**
     * Sends the request to the client. Commits the request line, headers and
     * optional entity and send them over the network.
     * 
     * @param request
     *            The high-level request.
     */
    public Status sendRequest(Request request) {
        Status result = null;

        try {
            Representation entity = request.isEntityAvailable() ? request
                    .getEntity() : null;

            if (entity != null) {
                // Get the connector service to callback
                ConnectorService connectorService = getConnectorService(request);
                if (connectorService != null)
                    connectorService.beforeSend(entity);

                // In order to workaround bug #6472250
                // (http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6472250),
                // it is very important to reuse that exact same "rs" reference
                // when manipulating the request stream, otherwise "infufficient
                // data sent" exceptions will occur in "fixedLengthMode"
                OutputStream rs = getRequestStream();
                WritableByteChannel wbc = getRequestChannel();
                if (wbc != null) {
                    if (entity != null) {
                        entity.write(wbc);
                    }
                } else if (rs != null) {
                    if (entity != null) {
                        entity.write(rs);
                    }

                    rs.flush();
                }

                // Call-back after writing
                if (connectorService != null)
                    connectorService.afterSend(entity);

                if (rs != null) {
                    rs.close();
                } else if (wbc != null) {
                    wbc.close();
                }
            }

            // Now we can access the status code, this MUST happen after closing
            // any open request stream.
            result = new Status(getStatusCode(), null, getReasonPhrase(), null);
        } catch (IOException ioe) {
            getHelper()
                    .getLogger()
                    .log(
                            Level.FINE,
                            "An error occured during the communication with the remote HTTP server.",
                            ioe);
            result = new Status(
                    Status.CONNECTOR_ERROR_COMMUNICATION,
                    "Unable to complete the HTTP call due to a communication error with the remote server. "
                            + ioe.getMessage());
        }

        return result;
    }

    /**
     * Returns the response channel if it exists.
     * 
     * @return The response channel if it exists.
     */
    public ReadableByteChannel getResponseChannel() {
        return null;
    }

    /**
     * Returns the response stream if it exists.
     * 
     * @return The response stream if it exists.
     */
    public InputStream getResponseStream() {
        return null;
    }

    /**
     * Returns the response entity if available. Note that no metadata is
     * associated by default, you have to manually set them from your headers.
     * 
     * @return The response entity if available.
     */
    public Representation getResponseEntity() {
        Representation result = null;

        if (getResponseStream() != null) {
            result = new InputRepresentation(getResponseStream(), null);
        } else if (getResponseChannel() != null) {
            result = new ReadableRepresentation(getResponseChannel(), null);
        } else if (getMethod().equals(Method.HEAD.getName())) {
            result = new Representation();
        }

        if (result != null) {
            for (Parameter header : getResponseHeaders()) {
                if (header.getName().equalsIgnoreCase(
                        HttpConstants.HEADER_CONTENT_TYPE)) {
                    ContentType contentType = new ContentType(header.getValue());
                    if (contentType != null) {
                        result.setMediaType(contentType.getMediaType());
                        result.setCharacterSet(contentType.getCharacterSet());
                    }
                } else if (header.getName().equalsIgnoreCase(
                        HttpConstants.HEADER_CONTENT_LENGTH)) {
                    result.setSize(Long.parseLong(header.getValue()));
                } else if (header.getName().equalsIgnoreCase(
                        HttpConstants.HEADER_EXPIRES)) {
                    result
                            .setExpirationDate(parseDate(header.getValue(),
                                    false));
                } else if (header.getName().equalsIgnoreCase(
                        HttpConstants.HEADER_CONTENT_ENCODING)) {
                    HeaderReader hr = new HeaderReader(header.getValue());
                    String value = hr.readValue();
                    while (value != null) {
                        Encoding encoding = new Encoding(value);
                        if (!encoding.equals(Encoding.IDENTITY)) {
                            result.getEncodings().add(encoding);
                        }
                        value = hr.readValue();
                    }
                } else if (header.getName().equalsIgnoreCase(
                        HttpConstants.HEADER_CONTENT_LANGUAGE)) {
                    HeaderReader hr = new HeaderReader(header.getValue());
                    String value = hr.readValue();
                    while (value != null) {
                        result.getLanguages().add(new Language(value));
                        value = hr.readValue();
                    }
                } else if (header.getName().equalsIgnoreCase(
                        HttpConstants.HEADER_LAST_MODIFIED)) {
                    result.setModificationDate(parseDate(header.getValue(),
                            false));
                } else if (header.getName().equalsIgnoreCase(
                        HttpConstants.HEADER_ETAG)) {
                    result.setTag(Tag.parse(header.getValue()));
                } else if (header.getName().equalsIgnoreCase(
                        HttpConstants.HEADER_CONTENT_LOCATION)) {
                    result.setIdentifier(header.getValue());
                }
            }
        }

        return result;
    }

}
