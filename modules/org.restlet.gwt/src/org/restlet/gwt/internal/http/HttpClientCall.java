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

package org.restlet.gwt.internal.http;

import java.util.logging.Logger;

import org.restlet.gwt.Callback;
import org.restlet.gwt.data.CharacterSet;
import org.restlet.gwt.data.Encoding;
import org.restlet.gwt.data.Language;
import org.restlet.gwt.data.Method;
import org.restlet.gwt.data.Parameter;
import org.restlet.gwt.data.Request;
import org.restlet.gwt.data.Response;
import org.restlet.gwt.data.Status;
import org.restlet.gwt.data.Tag;
import org.restlet.gwt.resource.Representation;
import org.restlet.gwt.resource.StringRepresentation;
import org.restlet.gwt.util.Series;

/**
 * Low-level HTTP client call.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class HttpClientCall extends HttpCall {
    /**
     * Copies entity headers into a response and ensures that a non null
     * representation is returned when at least one entity header is present.
     * 
     * @param responseHeaders
     *            The headers to copy.
     * @param representation
     *            The Representation to update.
     * @return a representation with the entity headers of the response or null
     *         if no representation has been provided and the response has not
     *         sent any entity header.
     * @throws NumberFormatException
     * @see org.restlet.util.Engine#copyResponseHeaders(Iterable, Response,
     *      Logger)
     * @see HttpClientConverter#copyResponseTransportHeaders(Iterable, Response,
     *      Logger)
     */
    public static Representation copyResponseEntityHeaders(
            Iterable<Parameter> responseHeaders, Representation representation)
            throws NumberFormatException {
        final Representation result = (representation == null) ? Representation
                .createEmpty() : representation;
        boolean entityHeaderFound = false;

        for (final Parameter header : responseHeaders) {
            if (header.getName().equalsIgnoreCase(
                    HttpConstants.HEADER_CONTENT_TYPE)) {
                final ContentType contentType = new ContentType(header
                        .getValue());
                result.setMediaType(contentType.getMediaType());
                final CharacterSet characterSet = contentType.getCharacterSet();
                if (characterSet != null) {
                    result.setCharacterSet(characterSet);
                }
                entityHeaderFound = true;
            } else if (header.getName().equalsIgnoreCase(
                    HttpConstants.HEADER_CONTENT_LENGTH)) {
                entityHeaderFound = true;
            } else if (header.getName().equalsIgnoreCase(
                    HttpConstants.HEADER_EXPIRES)) {
                result.setExpirationDate(parseDate(header.getValue(), false));
                entityHeaderFound = true;
            } else if (header.getName().equalsIgnoreCase(
                    HttpConstants.HEADER_CONTENT_ENCODING)) {
                final HeaderReader hr = new HeaderReader(header.getValue());
                String value = hr.readValue();
                while (value != null) {
                    final Encoding encoding = new Encoding(value);
                    if (!encoding.equals(Encoding.IDENTITY)) {
                        result.getEncodings().add(encoding);
                    }
                    value = hr.readValue();
                }
                entityHeaderFound = true;
            } else if (header.getName().equalsIgnoreCase(
                    HttpConstants.HEADER_CONTENT_LANGUAGE)) {
                final HeaderReader hr = new HeaderReader(header.getValue());
                String value = hr.readValue();
                while (value != null) {
                    result.getLanguages().add(new Language(value));
                    value = hr.readValue();
                }
                entityHeaderFound = true;
            } else if (header.getName().equalsIgnoreCase(
                    HttpConstants.HEADER_LAST_MODIFIED)) {
                result.setModificationDate(parseDate(header.getValue(), false));
                entityHeaderFound = true;
            } else if (header.getName().equalsIgnoreCase(
                    HttpConstants.HEADER_ETAG)) {
                result.setTag(Tag.parse(header.getValue()));
                entityHeaderFound = true;
            } else if (header.getName().equalsIgnoreCase(
                    HttpConstants.HEADER_CONTENT_LOCATION)) {
                result.setIdentifier(header.getValue());
                entityHeaderFound = true;
            } else if (header.getName().equalsIgnoreCase(
                    HttpConstants.HEADER_CONTENT_DISPOSITION)) {
                result.setDownloadName(parseContentDisposition(header
                        .getValue()));
                entityHeaderFound = true;
            }
        }

        if (!entityHeaderFound) {
            return null;
        }

        return result;
    }

    /**
     * Parse the Content-Disposition header value
     * 
     * @param value
     *            Content-disposition header
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

    /** The parent HTTP client helper. */
    private volatile HttpClientHelper helper;

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
        // TODO: fix me?
        // setClientAddress(getLocalAddress());
    }

    /**
     * Returns the content length of the request entity if know,
     * {@link Representation#UNKNOWN_SIZE} otherwise.
     * 
     * @return The request content length.
     */
    protected long getContentLength() {
        return getContentLength(getResponseHeaders());
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
     * Returns the representation wrapping the given stream.
     * 
     * @param entity
     *            The response entity.
     * @return The wrapping representation.
     */
    protected Representation getRepresentation(String entity) {
        return new StringRepresentation(entity);
    }

    /**
     * Returns the request entity string if it exists.
     * 
     * @return The request entity string if it exists.
     */
    public abstract String getRequestEntityString();

    /**
     * Returns the response entity if available. Note that no metadata is
     * associated by default, you have to manually set them from your headers.
     * 
     * @param response
     *            the Response to get the entity from
     * @return The response entity if available.
     */
    public Representation getResponseEntity(Response response) {
        Representation result = null;
        // boolean available = false;
        long size = Representation.UNKNOWN_SIZE;

        // Compute the content length
        final Series<Parameter> responseHeaders = getResponseHeaders();
        final String transferEncoding = responseHeaders.getFirstValue(
                HttpConstants.HEADER_TRANSFER_ENCODING, true);
        if ((transferEncoding != null)
                && !"identity".equalsIgnoreCase(transferEncoding)) {
            size = Representation.UNKNOWN_SIZE;
        } else {
            size = getContentLength();
        }

        if (!getMethod().equals(Method.HEAD.getName())
                && !response.getStatus().isInformational()
                && !response.getStatus()
                        .equals(Status.REDIRECTION_NOT_MODIFIED)
                && !response.getStatus().equals(Status.SUCCESS_NO_CONTENT)
                && !response.getStatus().equals(Status.SUCCESS_RESET_CONTENT)
                && !response.getStatus().equals(Status.SUCCESS_PARTIAL_CONTENT)) {
            // Make sure that an InputRepresentation will not be instantiated
            // while the stream is closed.
            final String text = getResponseEntityString(size);
            result = getRepresentation(text);
        }

        result = copyResponseEntityHeaders(responseHeaders, result);
        if (result != null) {
            result.setSize(size);
            // Informs that the size has not been specified in the header.
            if (size == Representation.UNKNOWN_SIZE) {
                System.err
                        .println("The length of the message body is unknown. The entity must be handled carefully and consumed entirely in order to surely release the connection.");
            }
        }
        // }

        return result;
    }

    /**
     * Returns the response entity string if it exists.
     * 
     * @param size
     *            The expected entity size or -1 if unknown.
     * @return The response entity stream if it exists.
     */
    public abstract String getResponseEntityString(long size);

    @Override
    protected boolean isClientKeepAlive() {
        return true;
    }

    @Override
    protected boolean isServerKeepAlive() {
        final String header = getResponseHeaders().getFirstValue(
                HttpConstants.HEADER_CONNECTION, true);
        return (header == null) || !header.equalsIgnoreCase("close");
    }

    /**
     * Sends the request to the client. Commits the request line, headers and
     * optional entity and send them over the network.
     * 
     * @param request
     *            The high-level request.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public abstract void sendRequest(Request request, Callback callback)
            throws Exception;

    /**
     * Indicates if the request entity should be chunked.
     * 
     * @return True if the request should be chunked
     */
    protected boolean shouldRequestBeChunked(Request request) {
        return request.isEntityAvailable()
                && (request.getEntity() != null)
                && (request.getEntity().getSize() == Representation.UNKNOWN_SIZE);
    }
}
