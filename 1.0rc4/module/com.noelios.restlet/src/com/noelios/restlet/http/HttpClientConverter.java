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
import java.util.Set;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ClientInfo;
import org.restlet.data.Conditions;
import org.restlet.data.Dimension;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.util.DateUtils;
import org.restlet.util.Series;

import com.noelios.restlet.Engine;
import com.noelios.restlet.util.CookieReader;
import com.noelios.restlet.util.CookieUtils;
import com.noelios.restlet.util.HeaderReader;
import com.noelios.restlet.util.PreferenceUtils;
import com.noelios.restlet.util.SecurityUtils;

/**
 * Converter of high-level uniform calls into low-level HTTP client calls.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class HttpClientConverter extends HttpConverter {
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
        HttpClientCall result = client.create(request);

        // Add the request headers
        addRequestHeaders(result, request);

        return result;
    }

    /**
     * Commits the changes to a handled HTTP client call back into the original
     * uniform call. The default implementation first invokes the
     * "addResponseHeaders" the asks the "htppCall" to send the response back to
     * the client.
     * 
     * @param httpCall
     *            The original HTTP call.
     * @param request
     *            The high-level request.
     * @param response
     *            The high-level response.
     */
    public void commit(HttpClientCall httpCall, Request request,
            Response response) {
        if (httpCall != null) {
            // Send the request to the client
            response.setStatus(httpCall.sendRequest(request));

            // Get the server address
            response.getServerInfo().setAddress(httpCall.getServerAddress());
            response.getServerInfo().setPort(httpCall.getServerPort());

            // Read the response headers
            readResponseHeaders(httpCall, response);

            // Set the entity
            response.setEntity(httpCall.getResponseEntity());
        }
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
            Series<Parameter> requestHeaders = httpCall.getRequestHeaders();

            // Manually add the host name and port when it is potentially
            // different from the one specified in the target resource
            // reference.
            Reference hostRef = (request.getResourceRef().getBaseRef() != null) ? request
                    .getResourceRef().getBaseRef()
                    : request.getResourceRef();

            if (hostRef.getHostDomain() != null) {
                String host = hostRef.getHostDomain();
                int hostRefPortValue = hostRef.getHostPort();

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
            Conditions condition = request.getConditions();
            if (condition.getMatch() != null) {
                StringBuilder value = new StringBuilder();

                for (int i = 0; i < condition.getMatch().size(); i++) {
                    if (i > 0)
                        value.append(", ");
                    value.append(condition.getMatch().get(i).format());
                }

                httpCall.getRequestHeaders().add(HttpConstants.HEADER_IF_MATCH,
                        value.toString());
            }

            if (condition.getModifiedSince() != null) {
                String imsDate = DateUtils.format(condition.getModifiedSince(),
                        DateUtils.FORMAT_RFC_1123.get(0));
                requestHeaders.add(HttpConstants.HEADER_IF_MODIFIED_SINCE,
                        imsDate);
            }

            if (condition.getNoneMatch() != null) {
                StringBuilder value = new StringBuilder();

                for (int i = 0; i < condition.getNoneMatch().size(); i++) {
                    if (i > 0)
                        value.append(", ");
                    value.append(condition.getNoneMatch().get(i).format());
                }

                requestHeaders.add(HttpConstants.HEADER_IF_NONE_MATCH, value
                        .toString());
            }

            if (condition.getUnmodifiedSince() != null) {
                String iusDate = DateUtils
                        .format(condition.getUnmodifiedSince(),
                                DateUtils.FORMAT_RFC_1123.get(0));
                requestHeaders.add(HttpConstants.HEADER_IF_UNMODIFIED_SINCE,
                        iusDate);
            }

            // Add the cookies
            if (request.getCookies().size() > 0) {
                String cookies = CookieUtils.format(request.getCookies());
                requestHeaders.add(HttpConstants.HEADER_COOKIE, cookies);
            }

            // Add the referrer header
            if (request.getReferrerRef() != null) {
                requestHeaders.add(HttpConstants.HEADER_REFERRER, request
                        .getReferrerRef().toString());
            }

            // Add the preferences
            ClientInfo client = request.getClientInfo();
            if (client.getAcceptedMediaTypes().size() > 0) {
                try {
                    requestHeaders.add(HttpConstants.HEADER_ACCEPT,
                            PreferenceUtils.format(client
                                    .getAcceptedMediaTypes()));
                } catch (IOException ioe) {
                    getLogger().log(Level.WARNING,
                            "Unable to format the HTTP Accept header", ioe);
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
                } catch (IOException ioe) {
                    getLogger().log(Level.WARNING,
                            "Unable to format the HTTP Accept header", ioe);
                }
            }

            if (client.getAcceptedEncodings().size() > 0) {
                try {
                    requestHeaders.add(HttpConstants.HEADER_ACCEPT_ENCODING,
                            PreferenceUtils.format(client
                                    .getAcceptedEncodings()));
                } catch (IOException ioe) {
                    getLogger().log(Level.WARNING,
                            "Unable to format the HTTP Accept header", ioe);
                }
            }

            if (client.getAcceptedLanguages().size() > 0) {
                try {
                    requestHeaders.add(HttpConstants.HEADER_ACCEPT_LANGUAGE,
                            PreferenceUtils.format(client
                                    .getAcceptedLanguages()));
                } catch (IOException ioe) {
                    getLogger().log(Level.WARNING,
                            "Unable to format the HTTP Accept header", ioe);
                }
            }

            // Add entity headers
            if (request.getEntity() != null) {
                if (request.getEntity().getMediaType() != null) {
                    requestHeaders.add(HttpConstants.HEADER_CONTENT_TYPE,
                            request.getEntity().getMediaType().toString());
                }

                if (!request.getEntity().getEncodings().isEmpty()) {
                    StringBuilder value = new StringBuilder();
                    for (int i = 0; i < request.getEntity().getEncodings()
                            .size(); i++) {
                        if (i > 0)
                            value.append(", ");
                        value.append(request.getEntity().getEncodings().get(i)
                                .getName());
                    }
                    requestHeaders.add(HttpConstants.HEADER_CONTENT_ENCODING,
                            value.toString());
                }

                if (!request.getEntity().getLanguages().isEmpty()) {
                    StringBuilder value = new StringBuilder();
                    for (int i = 0; i < request.getEntity().getLanguages()
                            .size(); i++) {
                        if (i > 0)
                            value.append(", ");
                        value.append(request.getEntity().getLanguages().get(i)
                                .getName());
                    }
                    requestHeaders.add(HttpConstants.HEADER_CONTENT_LANGUAGE,
                            value.toString());
                }
            }

            // Add user-defined extension headers
            Series<Parameter> additionalHeaders = (Series<Parameter>) request
                    .getAttributes().get(HttpConstants.ATTRIBUTE_HEADERS);
            addAdditionalHeaders(requestHeaders, additionalHeaders);

            // Add the security headers. NOTE: This must stay at the end because
            // the AWS challenge scheme requires access to all HTTP headers
            ChallengeResponse challengeResponse = request
                    .getChallengeResponse();
            if (challengeResponse != null) {
                requestHeaders.add(HttpConstants.HEADER_AUTHORIZATION,
                        SecurityUtils.format(challengeResponse, request,
                                requestHeaders));
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
            // Put the response headers in the call's attributes map
            response.getAttributes().put(HttpConstants.ATTRIBUTE_HEADERS,
                    httpCall.getResponseHeaders());

            // Read info from headers
            for (Parameter header : httpCall.getResponseHeaders()) {
                if (header.getName().equalsIgnoreCase(
                        HttpConstants.HEADER_LOCATION)) {
                    response.setRedirectRef(header.getValue());
                } else if ((header.getName()
                        .equalsIgnoreCase(HttpConstants.HEADER_SET_COOKIE))
                        || (header.getName()
                                .equalsIgnoreCase(HttpConstants.HEADER_SET_COOKIE2))) {
                    try {
                        CookieReader cr = new CookieReader(getLogger(), header
                                .getValue());
                        response.getCookieSettings()
                                .add(cr.readCookieSetting());
                    } catch (Exception e) {
                        getLogger().log(
                                Level.WARNING,
                                "Error during cookie setting parsing. Header: "
                                        + header.getValue(), e);
                    }
                } else if (header.getName().equalsIgnoreCase(
                        HttpConstants.HEADER_WWW_AUTHENTICATE)) {
                    ChallengeRequest request = SecurityUtils
                            .parseRequest(header.getValue());
                    response.setChallengeRequest(request);
                } else if (header.getName().equalsIgnoreCase(
                        HttpConstants.HEADER_SERVER)) {
                    response.getServerInfo().setAgent(header.getValue());
                } else if (header.getName().equalsIgnoreCase(
                        HttpConstants.HEADER_ALLOW)) {
                    HeaderReader hr = new HeaderReader(header.getValue());
                    String value = hr.readValue();
                    Set<Method> allowedMethods = response.getAllowedMethods();
                    while (value != null) {
                        allowedMethods.add(Method.valueOf(value));
                        value = hr.readValue();
                    }
                } else if (header.getName().equalsIgnoreCase(
                        HttpConstants.HEADER_VARY)) {
                    HeaderReader hr = new HeaderReader(header.getValue());
                    String value = hr.readValue();
                    Set<Dimension> dimensions = response.getDimensions();
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
                                .equalsIgnoreCase(HttpConstants.HEADER_USER_AGENT)) {
                            dimensions.add(Dimension.CLIENT_AGENT);
                        } else if (value.equals("*")) {
                            dimensions.add(Dimension.UNSPECIFIED);
                        }

                        value = hr.readValue();
                    }
                }
            }
        } catch (Exception e) {
            getLogger()
                    .log(
                            Level.FINE,
                            "An error occured during the processing of the HTTP response.",
                            e);
            response.setStatus(new Status(Status.CONNECTOR_ERROR_INTERNAL,
                    "Unable to process the response. " + e.getMessage()));
        }
    }

}
