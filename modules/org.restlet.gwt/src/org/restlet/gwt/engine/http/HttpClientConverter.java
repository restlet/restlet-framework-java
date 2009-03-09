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

package org.restlet.gwt.engine.http;

import java.util.Set;

import org.restlet.gwt.Callback;
import org.restlet.gwt.Context;
import org.restlet.gwt.data.ChallengeRequest;
import org.restlet.gwt.data.ClientInfo;
import org.restlet.gwt.data.Conditions;
import org.restlet.gwt.data.Dimension;
import org.restlet.gwt.data.MediaType;
import org.restlet.gwt.data.Method;
import org.restlet.gwt.data.Parameter;
import org.restlet.gwt.data.Reference;
import org.restlet.gwt.data.Request;
import org.restlet.gwt.data.Response;
import org.restlet.gwt.data.Status;
import org.restlet.gwt.engine.Engine;
import org.restlet.gwt.engine.util.AuthenticationUtils;
import org.restlet.gwt.util.DateUtils;
import org.restlet.gwt.util.Series;

/**
 * Converter of high-level uniform calls into low-level HTTP client calls.
 * 
 * @author Jerome Louvel
 */
public class HttpClientConverter extends HttpConverter {
    /**
     * Copies headers into a response.
     * 
     * @param headers
     *            The headers to copy.
     * @param response
     *            The response to update.
     * @see Engine#copyResponseHeaders(Iterable, Response)
     * @see HttpClientCall#copyResponseEntityHeaders(Iterable,
     *      org.restlet.resource.Representation)
     */
    public static void copyResponseTransportHeaders(
            Iterable<Parameter> headers, Response response) {
        // Read info from headers
        for (final Parameter header : headers) {
            if (header.getName()
                    .equalsIgnoreCase(HttpConstants.HEADER_LOCATION)) {
                response.setLocationRef(header.getValue());
            } else if ((header.getName()
                    .equalsIgnoreCase(HttpConstants.HEADER_SET_COOKIE))
                    || (header.getName()
                            .equalsIgnoreCase(HttpConstants.HEADER_SET_COOKIE2))) {
                try {
                    final CookieReader cr = new CookieReader(header.getValue());
                    response.getCookieSettings().add(cr.readCookieSetting());
                } catch (Exception e) {
                    System.err
                            .println("Error during cookie setting parsing. Header: "
                                    + header.getValue());
                }
            } else if (header.getName().equalsIgnoreCase(
                    HttpConstants.HEADER_WWW_AUTHENTICATE)) {
                final ChallengeRequest request = AuthenticationUtils
                        .parseAuthenticateHeader(header.getValue());
                response.setChallengeRequest(request);
            } else if (header.getName().equalsIgnoreCase(
                    HttpConstants.HEADER_SERVER)) {
                response.getServerInfo().setAgent(header.getValue());
            } else if (header.getName().equalsIgnoreCase(
                    HttpConstants.HEADER_ALLOW)) {
                final HeaderReader hr = new HeaderReader(header.getValue());
                String value = hr.readValue();
                final Set<Method> allowedMethods = response.getAllowedMethods();
                while (value != null) {
                    allowedMethods.add(Method.valueOf(value));
                    value = hr.readValue();
                }
            } else if (header.getName().equalsIgnoreCase(
                    HttpConstants.HEADER_VARY)) {
                final HeaderReader hr = new HeaderReader(header.getValue());
                String value = hr.readValue();
                final Set<Dimension> dimensions = response.getDimensions();
                while (value != null) {
                    if (value.equalsIgnoreCase(HttpConstants.HEADER_ACCEPT)) {
                        dimensions.add(Dimension.MEDIA_TYPE);
                    } else if (value
                            .equalsIgnoreCase(HttpConstants.HEADER_ACCEPT_CHARSET)) {
                        dimensions.add(Dimension.CHARACTER_SET);
                    } else if (value
                            .equalsIgnoreCase(HttpConstants.HEADER_ACCEPT_ENCODING)) {
                        dimensions.add(Dimension.ENCODING);
                    } else if (value
                            .equalsIgnoreCase(HttpConstants.HEADER_ACCEPT_LANGUAGE)) {
                        dimensions.add(Dimension.LANGUAGE);
                    } else if (value
                            .equalsIgnoreCase(HttpConstants.HEADER_AUTHORIZATION)) {
                        dimensions.add(Dimension.AUTHORIZATION);
                    } else if (value
                            .equalsIgnoreCase(HttpConstants.HEADER_USER_AGENT)) {
                        dimensions.add(Dimension.CLIENT_AGENT);
                    } else if (value.equals("*")) {
                        dimensions.add(Dimension.UNSPECIFIED);
                    }

                    value = hr.readValue();
                }
            }
        }
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context to use.
     */
    public HttpClientConverter(Context context) {
        super(context);
    }

    /**
     * Adds the request headers of a uniform call to a HTTP client call.
     * 
     * @param httpCall
     *            The HTTP client call.
     * @param request
     *            The high-level request.
     */
    @SuppressWarnings("unchecked")
    protected void addRequestHeaders(HttpClientCall httpCall, Request request) {
        if (httpCall != null) {
            final Series<Parameter> requestHeaders = httpCall
                    .getRequestHeaders();

            // Manually add the host name and port when it is potentially
            // different from the one specified in the target resource
            // reference.
            final Reference hostRef = (request.getResourceRef().getBaseRef() != null) ? request
                    .getResourceRef().getBaseRef()
                    : request.getResourceRef();

            if (hostRef.getHostDomain() != null) {
                String host = hostRef.getHostDomain();
                final int hostRefPortValue = hostRef.getHostPort();

                if ((hostRefPortValue != -1)
                        && (hostRefPortValue != request.getProtocol()
                                .getDefaultPort())) {
                    host = host + ':' + hostRefPortValue;
                }

                requestHeaders.add(HttpConstants.HEADER_HOST, host);
            }

            // Add the user agent header
            if (request.getClientInfo().getAgent() != null) {
                requestHeaders.add(HttpConstants.HEADER_USER_AGENT, request
                        .getClientInfo().getAgent());
            } else {
                requestHeaders.add(HttpConstants.HEADER_USER_AGENT,
                        Engine.VERSION_HEADER);
            }

            // Add the conditions
            final Conditions condition = request.getConditions();
            if (!condition.getMatch().isEmpty()) {
                final StringBuilder value = new StringBuilder();

                for (int i = 0; i < condition.getMatch().size(); i++) {
                    if (i > 0) {
                        value.append(", ");
                    }
                    value.append(condition.getMatch().get(i).format());
                }

                httpCall.getRequestHeaders().add(HttpConstants.HEADER_IF_MATCH,
                        value.toString());
            }

            if (condition.getModifiedSince() != null) {
                final String imsDate = DateUtils.format(condition
                        .getModifiedSince(), DateUtils.FORMAT_RFC_1123.get(0));
                requestHeaders.add(HttpConstants.HEADER_IF_MODIFIED_SINCE,
                        imsDate);
            }

            if (!condition.getNoneMatch().isEmpty()) {
                final StringBuilder value = new StringBuilder();

                for (int i = 0; i < condition.getNoneMatch().size(); i++) {
                    if (i > 0) {
                        value.append(", ");
                    }
                    value.append(condition.getNoneMatch().get(i).format());
                }

                requestHeaders.add(HttpConstants.HEADER_IF_NONE_MATCH, value
                        .toString());
            }

            if (condition.getUnmodifiedSince() != null) {
                final String iusDate = DateUtils
                        .format(condition.getUnmodifiedSince(),
                                DateUtils.FORMAT_RFC_1123.get(0));
                requestHeaders.add(HttpConstants.HEADER_IF_UNMODIFIED_SINCE,
                        iusDate);
            }

            // Add the cookies
            if (request.getCookies().size() > 0) {
                final String cookies = CookieUtils.format(request.getCookies());
                requestHeaders.add(HttpConstants.HEADER_COOKIE, cookies);
            }

            // Add the referrer header
            if (request.getReferrerRef() != null) {
                requestHeaders.add(HttpConstants.HEADER_REFERRER, request
                        .getReferrerRef().toString());
            }

            // Add the preferences
            final ClientInfo client = request.getClientInfo();
            if (client.getAcceptedMediaTypes().size() > 0) {
                try {
                    requestHeaders.add(HttpConstants.HEADER_ACCEPT,
                            PreferenceUtils.format(client
                                    .getAcceptedMediaTypes()));
                } catch (Exception ioe) {
                    System.err
                            .println("Unable to format the HTTP Accept header");
                }
            } else {
                requestHeaders.add(HttpConstants.HEADER_ACCEPT, MediaType.ALL
                        .getName());
            }

            if (client.getAcceptedCharacterSets().size() > 0) {
                try {
                    requestHeaders.add(HttpConstants.HEADER_ACCEPT_CHARSET,
                            PreferenceUtils.format(client
                                    .getAcceptedCharacterSets()));
                } catch (Exception ioe) {
                    System.err
                            .println("Unable to format the HTTP Accept header");
                }
            }

            if (client.getAcceptedEncodings().size() > 0) {
                try {
                    requestHeaders.add(HttpConstants.HEADER_ACCEPT_ENCODING,
                            PreferenceUtils.format(client
                                    .getAcceptedEncodings()));
                } catch (Exception ioe) {
                    System.err
                            .println("Unable to format the HTTP Accept header");
                }
            }

            if (client.getAcceptedLanguages().size() > 0) {
                try {
                    requestHeaders.add(HttpConstants.HEADER_ACCEPT_LANGUAGE,
                            PreferenceUtils.format(client
                                    .getAcceptedLanguages()));
                } catch (Exception ioe) {
                    System.err
                            .println("Unable to format the HTTP Accept header");
                }
            }

            // Add entity headers
            if (request.getEntity() != null) {
                if (request.getEntity().getMediaType() != null) {
                    String contentType = request.getEntity().getMediaType()
                            .toString();

                    // Specify the character set parameter if required
                    if ((request.getEntity().getMediaType().getParameters()
                            .getFirstValue("charset") == null)
                            && (request.getEntity().getCharacterSet() != null)) {
                        contentType = contentType
                                + "; charset="
                                + request.getEntity().getCharacterSet()
                                        .getName();
                    }

                    requestHeaders.add(HttpConstants.HEADER_CONTENT_TYPE,
                            contentType);
                }

                if (!request.getEntity().getEncodings().isEmpty()) {
                    final StringBuilder value = new StringBuilder();
                    for (int i = 0; i < request.getEntity().getEncodings()
                            .size(); i++) {
                        if (i > 0) {
                            value.append(", ");
                        }
                        value.append(request.getEntity().getEncodings().get(i)
                                .getName());
                    }
                    requestHeaders.add(HttpConstants.HEADER_CONTENT_ENCODING,
                            value.toString());
                }

                if (!request.getEntity().getLanguages().isEmpty()) {
                    final StringBuilder value = new StringBuilder();
                    for (int i = 0; i < request.getEntity().getLanguages()
                            .size(); i++) {
                        if (i > 0) {
                            value.append(", ");
                        }
                        value.append(request.getEntity().getLanguages().get(i)
                                .getName());
                    }
                    requestHeaders.add(HttpConstants.HEADER_CONTENT_LANGUAGE,
                            value.toString());
                }

                if (request.getEntity().getSize() > 0) {
                    requestHeaders.add(HttpConstants.HEADER_CONTENT_LENGTH,
                            String.valueOf(request.getEntity().getSize()));
                }
            }

            // Add user-defined extension headers
            final Series<Parameter> additionalHeaders = (Series<Parameter>) request
                    .getAttributes().get(HttpConstants.ATTRIBUTE_HEADERS);
            addAdditionalHeaders(requestHeaders, additionalHeaders);
        }
    }

    /**
     * Commits the changes to a handled HTTP client call back into the original
     * uniform call. The default implementation first invokes the
     * "addResponseHeaders" then asks the "htppCall" to send the response back
     * to the client.
     * 
     * @param httpCall
     *            The original HTTP call.
     * @param request
     *            The high-level request.
     * @param response
     *            The high-level response.
     * @param callback
     *            The callback invoked upon request completion.
     * @throws Exception
     */
    public void commit(final HttpClientCall httpCall, Request request,
            Response response, final Callback callback) throws Exception {
        if (httpCall != null) {
            // Send the request to the client
            httpCall.sendRequest(request, response, new Callback() {

                public void onEvent(Request request, Response response) {
                    updateResponse(response, httpCall);
                    callback.onEvent(request, response);
                }

            });
        }
    }

    /**
     * Updates the response with information from the lower-level HTTP client
     * call.
     * 
     * @param response
     *            The response to update.
     * @param httpCall
     *            The source HTTP client call.
     */
    public void updateResponse(Response response, HttpClientCall httpCall) {
        try {
            // Now we can access the status code
            response.setStatus(new Status(httpCall.getStatusCode(), null,
                    httpCall.getReasonPhrase(), null));

            // Get the server address
            response.getServerInfo().setAddress(httpCall.getServerAddress());
            response.getServerInfo().setPort(httpCall.getServerPort());

            // Read the response headers
            readResponseHeaders(httpCall, response);

            // Set the entity
            response.setEntity(httpCall.getResponseEntity(response));

            // Release the representation's content for some obvious cases
            if (response.getEntity() != null) {
                if (response.getEntity().getSize() == 0) {
                    response.getEntity().release();
                } else if (response.getRequest().getMethod()
                        .equals(Method.HEAD)) {
                    response.getEntity().release();
                } else if (response.getStatus().equals(
                        Status.SUCCESS_NO_CONTENT)) {
                    response.getEntity().release();
                } else if (response.getStatus().equals(
                        Status.SUCCESS_RESET_CONTENT)) {
                    response.getEntity().release();
                    response.setEntity(null);
                } else if (response.getStatus().equals(
                        Status.SUCCESS_PARTIAL_CONTENT)) {
                    response.getEntity().release();
                    response.setEntity(null);
                } else if (response.getStatus().equals(
                        Status.REDIRECTION_NOT_MODIFIED)) {
                    response.getEntity().release();
                } else if (response.getStatus().isInformational()) {
                    response.getEntity().release();
                    response.setEntity(null);
                }
            }
        } catch (Exception e) {
            // Unexpected exception occurred
            if ((response.getStatus() == null)
                    || !response.getStatus().isError()) {
                response.setStatus(Status.CONNECTOR_ERROR_INTERNAL, e);
            }
        }
    }

    /**
     * Reads the response headers of a handled HTTP client call to update the
     * original uniform call.
     * 
     * @param httpCall
     *            The handled HTTP client call.
     * @param response
     *            The high-level response to update.
     */
    protected void readResponseHeaders(HttpClientCall httpCall,
            Response response) {
        try {
            final Series<Parameter> responseHeaders = httpCall
                    .getResponseHeaders();
            // Put the response headers in the call's attributes map
            response.getAttributes().put(HttpConstants.ATTRIBUTE_HEADERS,
                    responseHeaders);
            copyResponseTransportHeaders(responseHeaders, response);
        } catch (Exception e) {
            System.err
                    .println("An error occured during the processing of the HTTP response.");
            response.setStatus(Status.CONNECTOR_ERROR_INTERNAL, e);
        }
    }

    /**
     * Converts a low-level HTTP call into a high-level uniform call.
     * 
     * @param client
     *            The HTTP client that will handle the call.
     * @param request
     *            The high-level request.
     * @return A new high-level uniform call.
     */
    public HttpClientCall toSpecific(HttpClientHelper client, Request request) {
        // Create the low-level HTTP client call
        final HttpClientCall result = client.create(request);

        // Add the request headers
        addRequestHeaders(result, request);

        return result;
    }
}
