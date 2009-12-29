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

package org.restlet.engine.http.adapter;

import java.io.IOException;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Uniform;
import org.restlet.data.AuthenticationInfo;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.Dimension;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.engine.Edition;
import org.restlet.engine.http.ClientCall;
import org.restlet.engine.http.HttpClientHelper;
import org.restlet.engine.http.header.CacheControlReader;
import org.restlet.engine.http.header.CookieReader;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.engine.http.header.HeaderReader;
import org.restlet.engine.http.header.HeaderUtils;
import org.restlet.engine.http.header.WarningReader;
import org.restlet.engine.util.DateUtils;
import org.restlet.util.Series;

/**
 * Converter of high-level uniform calls into low-level HTTP client calls.
 * 
 * @author Jerome Louvel
 */
public class ClientAdapter extends Adapter {
    /**
     * Copies headers into a response.
     * 
     * @param headers
     *            The headers to copy.
     * @param response
     *            The response to update.
     */
    public static void copyResponseTransportHeaders(Series<Parameter> headers,
            Response response) {
        // Read info from headers
        for (Parameter header : headers) {
            if (header.getName().equalsIgnoreCase(
                    HeaderConstants.HEADER_LOCATION)) {
                response.setLocationRef(header.getValue());
            } else if (header.getName().equalsIgnoreCase(
                    HeaderConstants.HEADER_AGE)) {
                try {
                    response.setAge(Integer.parseInt(header.getValue()));
                } catch (NumberFormatException nfe) {
                    Context.getCurrentLogger().log(
                            Level.WARNING,
                            "Error during Age header parsing. Header: "
                                    + header.getValue(), nfe);
                }
            } else if (header.getName().equalsIgnoreCase(
                    HeaderConstants.HEADER_DATE)) {
                Date date = DateUtils.parse(header.getValue());

                if (date == null) {
                    date = new Date();
                }

                response.setDate(date);
            } else if (header.getName().equalsIgnoreCase(
                    HeaderConstants.HEADER_RETRY_AFTER)) {
                // [ifndef gwt]
                Date retryAfter = DateUtils.parse(header.getValue());

                if (retryAfter == null) {
                    // The date might be expressed as a number of seconds
                    try {
                        int retryAfterSecs = Integer
                                .parseInt(header.getValue());
                        java.util.Calendar calendar = java.util.Calendar
                                .getInstance();
                        calendar.add(java.util.Calendar.SECOND, retryAfterSecs);
                        retryAfter = calendar.getTime();
                    } catch (NumberFormatException nfe) {
                        Context.getCurrentLogger().log(
                                Level.WARNING,
                                "Error during Retry-After header parsing. Header: "
                                        + header.getValue(), nfe);
                    }
                }

                response.setRetryAfter(retryAfter);
                // [enddef]
            } else if ((header.getName()
                    .equalsIgnoreCase(HeaderConstants.HEADER_SET_COOKIE))
                    || (header.getName()
                            .equalsIgnoreCase(HeaderConstants.HEADER_SET_COOKIE2))) {
                try {
                    CookieReader cr = new CookieReader(header.getValue());
                    response.getCookieSettings().add(cr.readCookieSetting());
                } catch (Exception e) {
                    Context.getCurrentLogger().log(
                            Level.WARNING,
                            "Error during cookie setting parsing. Header: "
                                    + header.getValue(), e);
                }
            } else if (header.getName().equalsIgnoreCase(
                    HeaderConstants.HEADER_WWW_AUTHENTICATE)) {
                // [ifndef gwt]
                ChallengeRequest request = org.restlet.engine.security.AuthenticatorUtils
                        .parseRequest(response, header.getValue(), headers);
                response.getChallengeRequests().add(request);
                // [enddef]
            } else if (header.getName().equalsIgnoreCase(
                    HeaderConstants.HEADER_PROXY_AUTHENTICATE)) {
                // [ifndef gwt]
                ChallengeRequest request = org.restlet.engine.security.AuthenticatorUtils
                        .parseRequest(response, header.getValue(), headers);
                response.getProxyChallengeRequests().add(request);
                // [enddef]
            } else if (header.getName().equalsIgnoreCase(
                    HeaderConstants.HEADER_AUTHENTICATION_INFO)) {
                // [ifndef gwt]
                AuthenticationInfo authenticationInfo = org.restlet.engine.security.AuthenticatorUtils
                        .parseAuthenticationInfo(header.getValue());
                response.setAuthenticationInfo(authenticationInfo);
                // [enddef]
            } else if (header.getName().equalsIgnoreCase(
                    HeaderConstants.HEADER_SERVER)) {
                response.getServerInfo().setAgent(header.getValue());
            } else if (header.getName().equalsIgnoreCase(
                    HeaderConstants.HEADER_ALLOW)) {
                HeaderReader hr = new HeaderReader(header.getValue());
                String value = hr.readValue();
                Set<Method> allowedMethods = response.getAllowedMethods();

                while (value != null) {
                    allowedMethods.add(Method.valueOf(value));
                    value = hr.readValue();
                }
            } else if (header.getName().equalsIgnoreCase(
                    HeaderConstants.HEADER_VARY)) {
                HeaderReader hr = new HeaderReader(header.getValue());
                String value = hr.readValue();
                Set<Dimension> dimensions = response.getDimensions();

                while (value != null) {
                    if (value.equalsIgnoreCase(HeaderConstants.HEADER_ACCEPT)) {
                        dimensions.add(Dimension.MEDIA_TYPE);
                    } else if (value
                            .equalsIgnoreCase(HeaderConstants.HEADER_ACCEPT_CHARSET)) {
                        dimensions.add(Dimension.CHARACTER_SET);
                    } else if (value
                            .equalsIgnoreCase(HeaderConstants.HEADER_ACCEPT_ENCODING)) {
                        dimensions.add(Dimension.ENCODING);
                    } else if (value
                            .equalsIgnoreCase(HeaderConstants.HEADER_ACCEPT_LANGUAGE)) {
                        dimensions.add(Dimension.LANGUAGE);
                    } else if (value
                            .equalsIgnoreCase(HeaderConstants.HEADER_AUTHORIZATION)) {
                        dimensions.add(Dimension.AUTHORIZATION);
                    } else if (value
                            .equalsIgnoreCase(HeaderConstants.HEADER_USER_AGENT)) {
                        dimensions.add(Dimension.CLIENT_AGENT);
                    } else if (value.equals("*")) {
                        dimensions.add(Dimension.UNSPECIFIED);
                    }

                    value = hr.readValue();
                }
            } else if (header.getName().equalsIgnoreCase(
                    HeaderConstants.HEADER_WARNING)) {
                WarningReader hr = new WarningReader(header.getValue());
                try {
                    response.getWarnings().add(hr.readWarning());
                } catch (Exception e) {
                    Context.getCurrentLogger().log(
                            Level.WARNING,
                            "Error during warning parsing. Header: "
                                    + header.getValue(), e);
                }
            } else if (header.getName().equalsIgnoreCase(
                    HeaderConstants.HEADER_CACHE_CONTROL)) {
                CacheControlReader ccr = new CacheControlReader(header
                        .getValue());
                try {
                    response.getCacheDirectives().addAll(ccr.readDirectives());
                } catch (Exception e) {
                    Context.getCurrentLogger().log(
                            Level.WARNING,
                            "Error during cache control parsing. Header: "
                                    + header.getValue(), e);
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
    public ClientAdapter(Context context) {
        super(context);
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
     * @throws Exception
     */
    public void commit(final ClientCall httpCall, Request request,
            Response response) throws Exception {
        if (httpCall != null) {
            // Check if the call is asynchronous
            if (response.getOnReceived() != null) {
                final Uniform userCallback = response.getOnReceived();

                // Send the request to the client
                httpCall.sendRequest(request, response, new Uniform() {
                    public void handle(Request request, Response response) {
                        try {
                            updateResponse(response, new Status(httpCall
                                    .getStatusCode(), null, httpCall
                                    .getReasonPhrase(), null), httpCall);
                            userCallback.handle(request, response);
                        } catch (Exception e) {
                            // Unexpected exception occurred
                            if ((response.getStatus() == null)
                                    || !response.getStatus().isError()) {
                                response.setStatus(
                                        Status.CONNECTOR_ERROR_INTERNAL, e);
                                userCallback.handle(request, response);
                            }
                        }
                    }
                });
            } else {
                if (Edition.CURRENT == Edition.GWT) {
                    System.err
                            .println("HTTP client calls must have a callback in the GWT edition");
                } else {
                    // [ifndef gwt]
                    updateResponse(response, httpCall.sendRequest(request),
                            httpCall);
                    // [enddef]
                }
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
    protected void readResponseHeaders(ClientCall httpCall, Response response) {
        try {
            Series<Parameter> responseHeaders = httpCall.getResponseHeaders();

            // Put the response headers in the call's attributes map
            response.getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS,
                    responseHeaders);

            copyResponseTransportHeaders(responseHeaders, response);
        } catch (Exception e) {
            getLogger()
                    .log(
                            Level.FINE,
                            "An error occured during the processing of the HTTP response.",
                            e);
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
    public ClientCall toSpecific(HttpClientHelper client, Request request) {
        // Create the low-level HTTP client call
        ClientCall result = client.create(request);

        // Add the headers
        if (result != null) {
            HeaderUtils.addRequestHeaders(request, result.getRequestHeaders());

            if (request.isEntityAvailable()) {
                HeaderUtils.addEntityHeaders(request.getEntity(), result
                        .getRequestHeaders());
            }
        }

        return result;
    }

    /**
     * Updates the response with information from the lower-level HTTP client
     * call.
     * 
     * @param response
     *            The response to update.
     * @param status
     *            The response status to apply.
     * @param httpCall
     *            The source HTTP client call.
     * @throws IOException
     */
    public void updateResponse(Response response, Status status,
            ClientCall httpCall) {
        // Send the request to the client
        response.setStatus(status);

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
            } else if (response.getRequest().getMethod().equals(Method.HEAD)) {
                response.getEntity().release();
            } else if (response.getStatus().equals(Status.SUCCESS_NO_CONTENT)) {
                response.getEntity().release();
            } else if (response.getStatus()
                    .equals(Status.SUCCESS_RESET_CONTENT)) {
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
    }
}
