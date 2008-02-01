/*
 * Copyright 2005-2008 Noelios Consulting.
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
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.resource.InputRepresentation;
import org.restlet.resource.ReadableRepresentation;
import org.restlet.resource.Representation;
import org.restlet.service.ConnectorService;
import org.restlet.util.Series;

import com.noelios.restlet.util.HeaderReader;

/**
 * Low-level HTTP client call.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class HttpClientCall extends HttpCall {
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

    /** The parent HTTP client helper. */
    private HttpClientHelper helper;

    /**
     * Constructor setting the request address to the local host.
     * 
     * @param helper
     *                The parent HTTP client helper.
     * @param method
     *                The method name.
     * @param requestUri
     *                The request URI.
     */
    public HttpClientCall(HttpClientHelper helper, String method,
            String requestUri) {
        setLogger((helper == null) ? null : helper.getLogger());
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
     * Returns the request entity channel if it exists.
     * 
     * @param size
     *                The total size that will be read from the channel.
     * 
     * @return The request entity channel if it exists.
     */
    public abstract WritableByteChannel getRequestEntityChannel();

    /**
     * Returns the request entity stream if it exists.
     * 
     * @param size
     *                The total size that will be read from the stream.
     * 
     * @return The request entity stream if it exists.
     */
    public abstract OutputStream getRequestEntityStream();

    /**
     * Returns the request head stream if it exists.
     * 
     * @return The request head stream if it exists.
     */
    public abstract OutputStream getRequestHeadStream();

    /**
     * Returns the response entity if available. Note that no metadata is
     * associated by default, you have to manually set them from your headers.
     * 
     * @return The response entity if available.
     */
    public Representation getResponseEntity(Response response) {
        Representation result = null;

        long contentLength = Representation.UNKNOWN_SIZE;

        // Extract the content length header
        Series<Parameter> responseHeaders = getResponseHeaders();
        for (Parameter header : responseHeaders) {
            if (header.getName().equalsIgnoreCase(
                    HttpConstants.HEADER_CONTENT_LENGTH)) {
                try {
                    contentLength = Long.parseLong(header.getValue());
                } catch (NumberFormatException e) {
                    contentLength = Representation.UNKNOWN_SIZE;
                }
            }
        }

        InputStream stream = getResponseEntityStream(contentLength);
        ReadableByteChannel channel = getResponseEntityChannel(contentLength);

        if (stream != null) {
            result = new InputRepresentation(stream, null);
        } else if (channel != null) {
            result = new ReadableRepresentation(channel, null);
        } else if (getMethod().equals(Method.HEAD.getName())) {
            result = new Representation() {
                @Override
                public ReadableByteChannel getChannel() throws IOException {
                    return null;
                }

                @Override
                public Reader getReader() throws IOException {
                    return null;
                }

                @Override
                public InputStream getStream() throws IOException {
                    return null;
                }

                @Override
                public void write(OutputStream outputStream) throws IOException {
                    // Do nothing
                }

                @Override
                public void write(WritableByteChannel writableChannel)
                        throws IOException {
                    // Do nothing
                }

                @Override
                public void write(Writer writer) throws IOException {
                    // Do nothing
                }
            };
        }

        if (result != null)
            copyResponseEntityHeaders(responseHeaders, result);
        return result;
    }

    /**
     * Copies headers into a response.
     * 
     * @param responseHeaders
     *                The headers to copy.
     * @param result
     *                The Representation to update.
     * @throws NumberFormatException
     * @see org.restlet.util.Engine#copyResponseHeaders(Iterable, Response,
     *      Logger)
     * @see HttpClientConverter#copyResponseTransportHeaders(Iterable, Response,
     *      Logger)
     */
    public static void copyResponseEntityHeaders(
            Iterable<Parameter> responseHeaders, Representation result)
            throws NumberFormatException {
        for (Parameter header : responseHeaders) {
            if (header.getName().equalsIgnoreCase(
                    HttpConstants.HEADER_CONTENT_TYPE)) {
                ContentType contentType = new ContentType(header.getValue());
                result.setMediaType(contentType.getMediaType());
                result.setCharacterSet(contentType.getCharacterSet());
            } else if (header.getName().equalsIgnoreCase(
                    HttpConstants.HEADER_CONTENT_LENGTH)) {
                result.setSize(Long.parseLong(header.getValue()));
            } else if (header.getName().equalsIgnoreCase(
                    HttpConstants.HEADER_EXPIRES)) {
                result.setExpirationDate(parseDate(header.getValue(), false));
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
                result.setModificationDate(parseDate(header.getValue(), false));
            } else if (header.getName().equalsIgnoreCase(
                    HttpConstants.HEADER_ETAG)) {
                result.setTag(Tag.parse(header.getValue()));
            } else if (header.getName().equalsIgnoreCase(
                    HttpConstants.HEADER_CONTENT_LOCATION)) {
                result.setIdentifier(header.getValue());
            } else if (header.getName().equalsIgnoreCase(
                    HttpConstants.HEADER_CONTENT_DISPOSITION)) {
                result.setDownloadName(parseContentDisposition(header
                        .getValue()));
            }
        }
    }

    /**
     * Returns the response channel if it exists.
     * 
     * @param size
     *                The expected entity size or -1 if unknown.
     * @return The response channel if it exists.
     */
    public abstract ReadableByteChannel getResponseEntityChannel(long size);

    /**
     * Returns the response entity stream if it exists.
     * 
     * @param size
     *                The expected entity size or -1 if unknown.
     * @return The response entity stream if it exists.
     */
    public abstract InputStream getResponseEntityStream(long size);

    /**
     * Parse the Content-Disposition header value
     * 
     * @param value
     *                Content-disposition header
     * @return Filename
     */
    public static String parseContentDisposition(String value) {
        if (value != null) {
            String key = "FILENAME=\"";
            int index = value.toUpperCase().indexOf(key);
            if (index > 0) {
                return value
                        .substring(index + key.length(), value.length() - 1);
            } else {
                key = "FILENAME=";
                index = value.toUpperCase().indexOf(key);
                if (index > 0) {
                    return value
                            .substring(index + key.length(), value.length());
                }
            }
        }
        return null;
    }

    /**
     * Sends the request to the client. Commits the request line, headers and
     * optional entity and send them over the network.
     * 
     * @param request
     *                The high-level request.
     */
    public Status sendRequest(Request request) {
        Status result = null;
        Representation entity = request.isEntityAvailable() ? request
                .getEntity() : null;
        try {
            if (entity != null) {
                // Get the connector service to callback
                ConnectorService connectorService = getConnectorService(request);
                if (connectorService != null)
                    connectorService.beforeSend(entity);

                // In order to workaround bug #6472250
                // (http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6472250),
                // it is very important to reuse that exact same "rs" reference
                // when manipulating the request stream, otherwise "insufficient
                // data sent" exceptions will occur in "fixedLengthMode"
                OutputStream rs = getRequestEntityStream();
                WritableByteChannel wbc = getRequestEntityChannel();

                if (wbc != null) {
                    entity.write(wbc);
                } else if (rs != null) {
                    entity.write(rs);
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
                            Level.WARNING,
                            "An error occured during the communication with the remote HTTP server.",
                            ioe);
            result = new Status(
                    Status.CONNECTOR_ERROR_COMMUNICATION,
                    "Unable to complete the HTTP call due to a communication error with the remote server. "
                            + ioe.getMessage());
        } finally {
            if (entity != null) {
                entity.release();
            }
        }

        return result;
    }

    /**
     * Indicates if the request entity should be chunked.
     * 
     * @return True if the request should be chunked
     */
    protected boolean shouldRequestBeChunked(Request request) {
        return request.isEntityAvailable() && request.getEntity() != null
                && request.getEntity().getSize() == Representation.UNKNOWN_SIZE;
    }

    /**
     * Empty representation with no content.
     */
    private class EmptyRepresentation extends Representation {
        @Override
        public ReadableByteChannel getChannel() throws IOException {
            return null;
        }

        @Override
        public Reader getReader() throws IOException {
            return null;
        }

        @Override
        public InputStream getStream() throws IOException {
            return null;
        }

        @Override
        public void write(OutputStream outputStream) throws IOException {
            // Do nothing
        }

        @Override
        public void write(WritableByteChannel writableChannel)
                throws IOException {
            // Do nothing
        }

        @Override
        public void write(Writer writer) throws IOException {
            // Do nothing
        }
    }

}
