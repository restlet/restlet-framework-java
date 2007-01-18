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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ClientInfo;
import org.restlet.data.Conditions;
import org.restlet.data.Cookie;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Tag;
import org.restlet.resource.Representation;
import org.restlet.util.Series;

import com.noelios.restlet.util.CookieReader;
import com.noelios.restlet.util.HeaderReader;
import com.noelios.restlet.util.PreferenceUtils;
import com.noelios.restlet.util.SecurityUtils;

/**
 * Request wrapper for server HTTP calls.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class HttpRequest extends Request {
    /** The context of the HTTP server connector that issued the call. */
    private Context context;

    /** The low-level HTTP call. */
    private HttpCall httpCall;

    /** Indicates if the client data was parsed and added. */
    private boolean clientAdded;

    /** Indicates if the conditions were parsed and added. */
    private boolean conditionAdded;

    /** Indicates if the cookies were parsed and added. */
    private boolean cookiesAdded;

    /** Indicates if the request entity was added. */
    private boolean entityAdded;

    /** Indicates if the referrer was parsed and added. */
    private boolean referrerAdded;

    /** Indicates if the security data was parsed and added. */
    private boolean securityAdded;

    /**
     * Constructor.
     * 
     * @param context
     *            The context of the HTTP server connector that issued the call.
     * @param httpCall
     *            The low-level HTTP server call.
     */
    public HttpRequest(Context context, HttpServerCall httpCall) {
        this.context = context;
        this.clientAdded = false;
        this.conditionAdded = false;
        this.cookiesAdded = false;
        this.entityAdded = false;
        this.referrerAdded = false;
        this.securityAdded = false;
        this.httpCall = httpCall;

        // Set the properties
        setMethod(Method.valueOf(httpCall.getMethod()));

        if (getHttpCall().isConfidential()) {
            setConfidential(true);
        } else {
            // We don't want to autocreate the security data just for this
            // information
            // Because that will by the default value of this property if read
            // by someone.
        }

        // Set the host reference
        StringBuilder sb = new StringBuilder();
        sb.append(httpCall.getProtocol().getSchemeName()).append("://");
        sb.append(httpCall.getHostDomain());
        if ((httpCall.getHostPort() != -1)
                && (httpCall.getHostPort() != httpCall.getProtocol()
                        .getDefaultPort())) {
            sb.append(':').append(httpCall.getServerPort());
        }
        setHostRef(new Reference(sb.toString()));

        // Set the resource reference
        setResourceRef(new Reference(getHostRef(), httpCall.getRequestUri()));
        if (getResourceRef().isRelative()) {
            setResourceRef(getResourceRef().getTargetRef());
            getResourceRef().setBaseRef(getHostRef());
        }
    }

    /**
     * Returns the client-specific information.
     * 
     * @return The client-specific information.
     */
    public ClientInfo getClientInfo() {
        ClientInfo result = super.getClientInfo();

        if (!this.clientAdded) {
            // Extract the header values
            String acceptCharset = getHttpCall().getRequestHeaders().getValues(
                    HttpConstants.HEADER_ACCEPT_CHARSET);
            String acceptEncoding = getHttpCall().getRequestHeaders()
                    .getValues(HttpConstants.HEADER_ACCEPT_ENCODING);
            String acceptLanguage = getHttpCall().getRequestHeaders()
                    .getValues(HttpConstants.HEADER_ACCEPT_LANGUAGE);
            String acceptMediaType = getHttpCall().getRequestHeaders()
                    .getValues(HttpConstants.HEADER_ACCEPT);

            // Parse the headers and update the call preferences
            try {
                PreferenceUtils.parseCharacterSets(acceptCharset, result);
            } catch (Exception e) {
                this.context.getLogger().log(Level.INFO, e.getMessage());
            }
            try {
                PreferenceUtils.parseEncodings(acceptEncoding, result);
            } catch (Exception e) {
                this.context.getLogger().log(Level.INFO, e.getMessage());
            }
            try {
                PreferenceUtils.parseLanguages(acceptLanguage, result);
            } catch (Exception e) {
                this.context.getLogger().log(Level.INFO, e.getMessage());
            }
            try {
                PreferenceUtils.parseMediaTypes(acceptMediaType, result);
            } catch (Exception e) {
                this.context.getLogger().log(Level.INFO, e.getMessage());
            }

            // Set other properties
            result.setAgent(getHttpCall().getRequestHeaders().getValues(
                    HttpConstants.HEADER_USER_AGENT));
            result.setAddress(getHttpCall().getClientAddress());
            result.setPort(getHttpCall().getClientPort());

            // Special handling for the non standard but common
            // "X-Forwarded-For" header.
            boolean useForwardedForHeader = Boolean.parseBoolean(this.context
                    .getParameters().getFirstValue("useForwardedForHeader",
                            false));
            if (useForwardedForHeader) {
                // Lookup the "X-Forwarded-For" header supported by popular
                // proxies and caches.
                // This information is only safe for intermediary components
                // within your local network.
                // Other addresses could easily be changed by setting a fake
                // header and should not
                // be trusted for serious security checks.
                String header = getHttpCall().getRequestHeaders().getValues(
                        HttpConstants.HEADER_X_FORWARDED_FOR);
                if (header != null) {
                    String[] addresses = header.split(",");
                    for (int i = addresses.length - 1; i >= 0; i--) {
                        result.getAddresses().add(addresses[i].trim());
                    }
                }
            }

            this.clientAdded = true;
        }

        return result;
    }

    /**
     * Returns the condition data applying to this call.
     * 
     * @return The condition data applying to this call.
     */
    public Conditions getConditions() {
        Conditions result = super.getConditions();

        if (!this.conditionAdded) {
            // Extract the header values
            String ifMatchHeader = getHttpCall().getRequestHeaders().getValues(
                    HttpConstants.HEADER_IF_MATCH);
            String ifNoneMatchHeader = getHttpCall().getRequestHeaders()
                    .getValues(HttpConstants.HEADER_IF_NONE_MATCH);
            Date ifModifiedSince = null;
            Date ifUnmodifiedSince = null;

            for (Parameter header : getHttpCall().getRequestHeaders()) {
                if (header.getName().equalsIgnoreCase(
                        HttpConstants.HEADER_IF_MODIFIED_SINCE)) {
                    ifModifiedSince = getHttpCall().parseDate(
                            header.getValue(), false);
                } else if (header.getName().equalsIgnoreCase(
                        HttpConstants.HEADER_IF_UNMODIFIED_SINCE)) {
                    ifUnmodifiedSince = getHttpCall().parseDate(
                            header.getValue(), false);
                }
            }

            // Set the If-Modified-Since date
            if ((ifModifiedSince != null) && (ifModifiedSince.getTime() != -1)) {
                result.setModifiedSince(ifModifiedSince);
            }

            // Set the If-Unmodified-Since date
            if ((ifUnmodifiedSince != null)
                    && (ifUnmodifiedSince.getTime() != -1)) {
                result.setUnmodifiedSince(ifUnmodifiedSince);
            }

            // Set the If-Match tags
            List<Tag> match = null;
            Tag current = null;
            if (ifMatchHeader != null) {
                try {
                    HeaderReader hr = new HeaderReader(ifMatchHeader);
                    String value = hr.readValue();
                    while (value != null) {
                        current = Tag.parse(value);

                        // Is it the first tag?
                        if (match == null) {
                            match = new ArrayList<Tag>();
                            result.setMatch(match);
                        }

                        // Add the new tag
                        match.add(current);

                        // Read the next token
                        value = hr.readValue();
                    }
                } catch (Exception e) {
                    this.context.getLogger().log(
                            Level.WARNING,
                            "Unable to process the if-match header: "
                                    + ifMatchHeader);
                }
            }

            // Set the If-None-Match tags
            List<Tag> noneMatch = null;
            if (ifNoneMatchHeader != null) {
                try {
                    HeaderReader hr = new HeaderReader(ifNoneMatchHeader);
                    String value = hr.readValue();
                    while (value != null) {
                        current = Tag.parse(value);

                        // Is it the first tag?
                        if (noneMatch == null) {
                            noneMatch = new ArrayList<Tag>();
                            result.setNoneMatch(noneMatch);
                        }

                        noneMatch.add(current);

                        // Read the next token
                        value = hr.readValue();
                    }
                } catch (Exception e) {
                    this.context.getLogger().log(
                            Level.WARNING,
                            "Unable to process the if-none-match header: "
                                    + ifNoneMatchHeader);
                }
            }

            this.conditionAdded = true;
        }

        return result;
    }

    /**
     * Returns the low-level HTTP call.
     * 
     * @return The low-level HTTP call.
     */
    public HttpCall getHttpCall() {
        return this.httpCall;
    }

    /**
     * Returns the cookies provided by the client.
     * 
     * @return The cookies provided by the client.
     */
    public Series<Cookie> getCookies() {
        Series<Cookie> result = super.getCookies();

        if (!cookiesAdded) {
            String cookiesValue = getHttpCall().getRequestHeaders().getValues(
                    HttpConstants.HEADER_COOKIE);

            if (cookiesValue != null) {
                try {
                    CookieReader cr = new CookieReader(
                            this.context.getLogger(), cookiesValue);
                    Cookie current = cr.readCookie();
                    while (current != null) {
                        result.add(current);
                        current = cr.readCookie();
                    }
                } catch (Exception e) {
                    this.context.getLogger().log(
                            Level.WARNING,
                            "An exception occured during cookies parsing. Headers value: "
                                    + cookiesValue, e);
                }
            }

            this.cookiesAdded = true;
        }

        return result;
    }

    /**
     * Returns the representation provided by the client.
     * 
     * @return The representation provided by the client.
     */
    public Representation getEntity() {
        if (!this.entityAdded) {
            setEntity(((HttpServerCall) getHttpCall()).getRequestEntity());
            this.entityAdded = true;
        }

        return super.getEntity();
    }

    /**
     * Returns the referrer reference if available.
     * 
     * @return The referrer reference.
     */
    public Reference getReferrerRef() {
        if (!this.referrerAdded) {
            String referrerValue = getHttpCall().getRequestHeaders().getValues(
                    HttpConstants.HEADER_REFERRER);
            if (referrerValue != null) {
                setReferrerRef(new Reference(referrerValue));
            }

            this.referrerAdded = true;
        }

        return super.getReferrerRef();
    }

    /**
     * Returns the authentication response sent by a client to an origin server.
     * 
     * @return The authentication response sent by a client to an origin server.
     */
    public ChallengeResponse getChallengeResponse() {
        ChallengeResponse result = super.getChallengeResponse();

        if (!this.securityAdded) {
            // Extract the header value
            String authorization = getHttpCall().getRequestHeaders().getValues(
                    HttpConstants.HEADER_AUTHORIZATION);

            // Set the challenge response
            result = SecurityUtils.parseResponse(this,
                    this.context.getLogger(), authorization);
            setChallengeResponse(result);
            this.securityAdded = true;
        }

        return result;
    }
}
