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

package org.restlet.engine.adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Message;
import org.restlet.Request;
import org.restlet.data.CacheDirective;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ClientInfo;
import org.restlet.data.Conditions;
import org.restlet.data.Cookie;
import org.restlet.data.Header;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Range;
import org.restlet.data.RecipientInfo;
import org.restlet.data.Reference;
import org.restlet.data.Tag;
import org.restlet.data.Warning;
import org.restlet.engine.header.CacheDirectiveReader;
import org.restlet.engine.header.CookieReader;
import org.restlet.engine.header.ExpectationReader;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.header.HeaderReader;
import org.restlet.engine.header.PreferenceReader;
import org.restlet.engine.header.RangeReader;
import org.restlet.engine.header.RecipientInfoReader;
import org.restlet.engine.header.StringReader;
import org.restlet.engine.header.WarningReader;
import org.restlet.engine.security.AuthenticatorUtils;
import org.restlet.engine.util.DateUtils;
import org.restlet.engine.util.ReferenceUtils;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

/**
 * Request wrapper for server HTTP calls.
 * 
 * @author Jerome Louvel
 */
public class HttpRequest extends Request {
    /**
     * Adds a new header to the given request.
     * 
     * @param request
     *            The request to update.
     * @param headerName
     *            The header name to add.
     * @param headerValue
     *            The header value to add.
     * @deprecated Use {@link Message#getHeaders()} directly instead.
     */
    @Deprecated
    public static void addHeader(Request request, String headerName,
            String headerValue) {
        request.getHeaders().add(new Header(headerName, headerValue));
    }

    /**
     * Indicates if the access control data for request headers was parsed and
     * added
     */
    private volatile boolean accessControlRequestHeadersAdded;

    /**
     * Indicates if the access control data for request methods was parsed and
     * added
     */
    private volatile boolean accessControlRequestMethodAdded;

    /** Indicates if the cache control data was parsed and added. */
    private volatile boolean cacheDirectivesAdded;

    /** Indicates if the client data was parsed and added. */
    private volatile boolean clientAdded;

    /** Indicates if the conditions were parsed and added. */
    private volatile boolean conditionAdded;

    /** The context of the HTTP server connector that issued the call. */
    private volatile Context context;

    /** Indicates if the cookies were parsed and added. */
    private volatile boolean cookiesAdded;

    /** Indicates if the request entity was added. */
    private volatile boolean entityAdded;

    /** The low-level HTTP call. */
    private volatile ServerCall httpCall;

    /** Indicates if the proxy security data was parsed and added. */
    private volatile boolean proxySecurityAdded;

    /** Indicates if the ranges data was parsed and added. */
    private volatile boolean rangesAdded;

    /** Indicates if the recipients info was parsed and added. */
    private volatile boolean recipientsInfoAdded;

    /** Indicates if the referrer was parsed and added. */
    private volatile boolean referrerAdded;

    /** Indicates if the security data was parsed and added. */
    private volatile boolean securityAdded;

    /** Indicates if the warning data was parsed and added. */
    private volatile boolean warningsAdded;

    /**
     * Constructor.
     * 
     * @param context
     *            The context of the HTTP server connector that issued the call.
     * @param httpCall
     *            The low-level HTTP server call.
     */
    public HttpRequest(Context context, ServerCall httpCall) {
        this.context = context;
        this.clientAdded = false;
        this.conditionAdded = false;
        this.cookiesAdded = false;
        this.entityAdded = false;
        this.referrerAdded = false;
        this.securityAdded = false;
        this.proxySecurityAdded = false;
        this.recipientsInfoAdded = false;
        this.warningsAdded = false;
        this.httpCall = httpCall;

        // Set the properties
        setMethod(Method.valueOf(httpCall.getMethod()));

        // Set the host reference
        StringBuilder sb = new StringBuilder();
        sb.append(httpCall.getProtocol().getSchemeName()).append("://");
        sb.append(httpCall.getHostDomain());
        if ((httpCall.getHostPort() != -1)
                && (httpCall.getHostPort() != httpCall.getProtocol().getDefaultPort())) {
            sb.append(':').append(httpCall.getHostPort());
        }
        setHostRef(sb.toString());

        // Set the resource reference
        if (httpCall.getRequestUri() != null) {
            setResourceRef(new Reference(getHostRef(), httpCall.getRequestUri()));

            if (getResourceRef().isRelative()) {
                // Take care of the "/" between the host part and the segments.
                if (!httpCall.getRequestUri().startsWith("/")) {
                    setResourceRef(new Reference(getHostRef().toString() + "/"
                            + httpCall.getRequestUri()));
                } else {
                    setResourceRef(new Reference(getHostRef().toString()
                            + httpCall.getRequestUri()));
                }
            }

            setOriginalRef(ReferenceUtils.getOriginalRef(getResourceRef(), httpCall.getRequestHeaders()));
        }

        // Set the request date
        String dateHeader = httpCall.getRequestHeaders().getFirstValue(HeaderConstants.HEADER_DATE, true);
        Date date = null;
        if (dateHeader != null) {
            date = DateUtils.parse(dateHeader);
        }

        if (date == null) {
            date = new Date();
        }

        setDate(date);
    }

    @Override
    public boolean abort() {
        return getHttpCall().abort();
    }

    @Override
    public void flushBuffers() throws IOException {
        getHttpCall().flushBuffers();
    }

    @Override
    public Set<String> getAccessControlRequestHeaders() {
        Set<String> result = super.getAccessControlRequestHeaders();
        if (!accessControlRequestHeadersAdded) {
            for (String header : getHttpCall()
                    .getRequestHeaders()
                    .getValuesArray(
                            HeaderConstants.HEADER_ACCESS_CONTROL_REQUEST_HEADERS,
                            true)) {
                new StringReader(header).addValues(result);
            }
            accessControlRequestHeadersAdded = true;
        }
        return result;
    }

    @Override
    public Method getAccessControlRequestMethod() {
        Method result = super.getAccessControlRequestMethod();
        if (!accessControlRequestMethodAdded) {
            String header = getHttpCall().getRequestHeaders().getFirstValue(
                    HeaderConstants.HEADER_ACCESS_CONTROL_REQUEST_METHOD, true);
            if (header != null) {
                result = Method.valueOf(header);
                super.setAccessControlRequestMethod(result);
            }
            accessControlRequestMethodAdded = true;
        }
        return result;
    }

    @Override
    public List<CacheDirective> getCacheDirectives() {
        List<CacheDirective> result = super.getCacheDirectives();

        if (!cacheDirectivesAdded) {
            for (Header header : getHttpCall().getRequestHeaders().subList(
                    HeaderConstants.HEADER_CACHE_CONTROL)) {
                CacheDirectiveReader.addValues(header, result);
            }

            cacheDirectivesAdded = true;
        }

        return result;
    }

    @Override
    public ChallengeResponse getChallengeResponse() {
        ChallengeResponse result = super.getChallengeResponse();

        if (!this.securityAdded) {
            // Extract the header value
            String authorization = getHttpCall().getRequestHeaders().getValues(
                    HeaderConstants.HEADER_AUTHORIZATION);

            // Set the challenge response
            result = AuthenticatorUtils.parseResponse(this, authorization,
                    getHttpCall().getRequestHeaders());
            setChallengeResponse(result);
            this.securityAdded = true;
        }

        return result;
    }

    /**
     * Returns the client-specific information.
     * 
     * @return The client-specific information.
     */
    @Override
    public ClientInfo getClientInfo() {
        final ClientInfo result = super.getClientInfo();

        if (!this.clientAdded) {
            // Extract the header values
            String acceptMediaType = getHttpCall().getRequestHeaders()
                    .getValues(HeaderConstants.HEADER_ACCEPT);
            String acceptCharset = getHttpCall().getRequestHeaders().getValues(
                    HeaderConstants.HEADER_ACCEPT_CHARSET);
            String acceptEncoding = getHttpCall().getRequestHeaders()
                    .getValues(HeaderConstants.HEADER_ACCEPT_ENCODING);
            String acceptLanguage = getHttpCall().getRequestHeaders()
                    .getValues(HeaderConstants.HEADER_ACCEPT_LANGUAGE);
            String acceptPatch = getHttpCall().getRequestHeaders().getValues(
                    HeaderConstants.HEADER_ACCEPT_PATCH);
            String expect = getHttpCall().getRequestHeaders().getValues(
                    HeaderConstants.HEADER_EXPECT);

            // Parse the headers and update the call preferences

            // Parse the Accept* headers. If an error occurs during the parsing
            // of each header, the error is traced and we keep on with the other
            // headers.
            try {
                PreferenceReader.addCharacterSets(acceptCharset, result);
            } catch (Exception e) {
                this.context.getLogger().log(Level.INFO, e.getMessage());
            }

            try {
                PreferenceReader.addEncodings(acceptEncoding, result);
            } catch (Exception e) {
                this.context.getLogger().log(Level.INFO, e.getMessage());
            }

            try {
                PreferenceReader.addLanguages(acceptLanguage, result);
            } catch (Exception e) {
                this.context.getLogger().log(Level.INFO, e.getMessage());
            }

            try {
                PreferenceReader.addMediaTypes(acceptMediaType, result);
            } catch (Exception e) {
                this.context.getLogger().log(Level.INFO, e.getMessage());
            }

            try {
                PreferenceReader.addPatches(acceptPatch, result);
            } catch (Exception e) {
                this.context.getLogger().log(Level.INFO, e.getMessage());
            }

            try {
                ExpectationReader.addValues(expect, result);
            } catch (Exception e) {
                this.context.getLogger().log(Level.INFO, e.getMessage());
            }

            // Set other properties
            result.setAgent(getHttpCall().getRequestHeaders().getValues(
                    HeaderConstants.HEADER_USER_AGENT));
            result.setFrom(getHttpCall().getRequestHeaders().getFirstValue(
                    HeaderConstants.HEADER_FROM, true));
            result.setAddress(getHttpCall().getClientAddress());
            result.setPort(getHttpCall().getClientPort());

            if (getHttpCall().getUserPrincipal() != null) {
                result.getPrincipals().add(getHttpCall().getUserPrincipal());
            }

            if (this.context != null) {
                // Special handling for the non standard but common
                // "X-Forwarded-For" header.
                final boolean useForwardedForHeader = Boolean
                        .parseBoolean(this.context.getParameters()
                                .getFirstValue("useForwardedForHeader", false));
                if (useForwardedForHeader) {
                    // Lookup the "X-Forwarded-For" header supported by popular
                    // proxies and caches.
                    final String header = getHttpCall().getRequestHeaders()
                            .getValues(HeaderConstants.HEADER_X_FORWARDED_FOR);
                    if (header != null) {
                        final String[] addresses = header.split(",");
                        for (int i = 0; i < addresses.length; i++) {
                            String address = addresses[i].trim();
                            result.getForwardedAddresses().add(address);
                        }
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
    @Override
    public Conditions getConditions() {
        final Conditions result = super.getConditions();

        if (!this.conditionAdded) {
            // Extract the header values
            String ifMatchHeader = getHttpCall().getRequestHeaders().getValues(
                    HeaderConstants.HEADER_IF_MATCH);
            String ifNoneMatchHeader = getHttpCall().getRequestHeaders()
                    .getValues(HeaderConstants.HEADER_IF_NONE_MATCH);
            Date ifModifiedSince = null;
            Date ifUnmodifiedSince = null;
            String ifRangeHeader = getHttpCall().getRequestHeaders()
                    .getFirstValue(HeaderConstants.HEADER_IF_RANGE, true);

            for (Header header : getHttpCall().getRequestHeaders()) {
                if (header.getName().equalsIgnoreCase(
                        HeaderConstants.HEADER_IF_MODIFIED_SINCE)) {
                    ifModifiedSince = HeaderReader.readDate(header.getValue(),
                            false);
                } else if (header.getName().equalsIgnoreCase(
                        HeaderConstants.HEADER_IF_UNMODIFIED_SINCE)) {
                    ifUnmodifiedSince = HeaderReader.readDate(
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
                    HeaderReader<Object> hr = new HeaderReader<Object>(
                            ifMatchHeader);
                    String value = hr.readRawValue();

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
                        value = hr.readRawValue();
                    }
                } catch (Exception e) {
                    this.context.getLogger().log(
                            Level.INFO,
                            "Unable to process the if-match header: "
                                    + ifMatchHeader);
                }
            }

            // Set the If-None-Match tags
            List<Tag> noneMatch = null;
            if (ifNoneMatchHeader != null) {
                try {
                    HeaderReader<Object> hr = new HeaderReader<Object>(
                            ifNoneMatchHeader);
                    String value = hr.readRawValue();

                    while (value != null) {
                        current = Tag.parse(value);

                        // Is it the first tag?
                        if (noneMatch == null) {
                            noneMatch = new ArrayList<Tag>();
                            result.setNoneMatch(noneMatch);
                        }

                        noneMatch.add(current);

                        // Read the next token
                        value = hr.readRawValue();
                    }
                } catch (Exception e) {
                    this.context.getLogger().log(
                            Level.INFO,
                            "Unable to process the if-none-match header: "
                                    + ifNoneMatchHeader);
                }
            }

            if (ifRangeHeader != null && ifRangeHeader.length() > 0) {
                Tag tag = Tag.parse(ifRangeHeader);

                if (tag != null) {
                    result.setRangeTag(tag);
                } else {
                    Date date = HeaderReader.readDate(ifRangeHeader, false);
                    result.setRangeDate(date);
                }
            }

            this.conditionAdded = true;
        }

        return result;
    }

    /**
     * Returns the cookies provided by the client.
     * 
     * @return The cookies provided by the client.
     */
    @Override
    public Series<Cookie> getCookies() {
        Series<Cookie> result = super.getCookies();

        if (!this.cookiesAdded) {
            String cookieValues = getHttpCall().getRequestHeaders().getValues(
                    HeaderConstants.HEADER_COOKIE);

            if (cookieValues != null) {
                new CookieReader(cookieValues).addValues(result);
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
    @Override
    public Representation getEntity() {
        if (!this.entityAdded) {
            setEntity(((ServerCall) getHttpCall()).getRequestEntity());
            this.entityAdded = true;
        }

        return super.getEntity();
    }

    /**
     * Returns the low-level HTTP call.
     * 
     * @return The low-level HTTP call.
     */
    public ServerCall getHttpCall() {
        return this.httpCall;
    }

    @Override
    public ChallengeResponse getProxyChallengeResponse() {
        ChallengeResponse result = super.getProxyChallengeResponse();

        if (!this.proxySecurityAdded) {
            // Extract the header value
            final String authorization = getHttpCall().getRequestHeaders()
                    .getValues(HeaderConstants.HEADER_PROXY_AUTHORIZATION);

            // Set the challenge response
            result = AuthenticatorUtils.parseResponse(this, authorization,
                    getHttpCall().getRequestHeaders());
            setProxyChallengeResponse(result);
            this.proxySecurityAdded = true;
        }

        return result;
    }

    @Override
    public List<Range> getRanges() {
        final List<Range> result = super.getRanges();

        if (!this.rangesAdded) {
            // Extract the header value
            final String ranges = getHttpCall().getRequestHeaders().getValues(
                    HeaderConstants.HEADER_RANGE);
            result.addAll(RangeReader.read(ranges));

            this.rangesAdded = true;
        }

        return result;
    }

    @Override
    public List<RecipientInfo> getRecipientsInfo() {
        List<RecipientInfo> result = super.getRecipientsInfo();
        if (!recipientsInfoAdded) {
            for (String header : getHttpCall().getRequestHeaders()
                    .getValuesArray(HeaderConstants.HEADER_VIA, true)) {
                new RecipientInfoReader(header).addValues(result);
            }
            recipientsInfoAdded = true;
        }
        return result;
    }

    /**
     * Returns the referrer reference if available.
     * 
     * @return The referrer reference.
     */
    @Override
    public Reference getReferrerRef() {
        if (!this.referrerAdded) {
            final String referrerValue = getHttpCall().getRequestHeaders()
                    .getValues(HeaderConstants.HEADER_REFERRER);
            if (referrerValue != null) {
                setReferrerRef(new Reference(referrerValue));
            }

            this.referrerAdded = true;
        }

        return super.getReferrerRef();
    }

    @Override
    public List<Warning> getWarnings() {
        List<Warning> result = super.getWarnings();
        if (!warningsAdded) {
            for (String header : getHttpCall().getRequestHeaders()
                    .getValuesArray(HeaderConstants.HEADER_WARNING, true)) {
                new WarningReader(header).addValues(result);
            }
            warningsAdded = true;
        }
        return result;
    }

    @Override
    public void setAccessControlRequestHeaders(
            Set<String> accessControlRequestHeaders) {
        super.setAccessControlRequestHeaders(accessControlRequestHeaders);
        this.accessControlRequestHeadersAdded = true;
    }

    @Override
    public void setAccessControlRequestMethod(Method accessControlRequestMethod) {
        super.setAccessControlRequestMethod(accessControlRequestMethod);
        this.accessControlRequestMethodAdded = true;
    }

    @Override
    public void setChallengeResponse(ChallengeResponse response) {
        super.setChallengeResponse(response);
        this.securityAdded = true;
    }

    @Override
    public void setEntity(Representation entity) {
        super.setEntity(entity);
        this.entityAdded = true;
    }

    @Override
    public void setProxyChallengeResponse(ChallengeResponse response) {
        super.setProxyChallengeResponse(response);
        this.proxySecurityAdded = true;
    }

    @Override
    public void setRecipientsInfo(List<RecipientInfo> recipientsInfo) {
        super.setRecipientsInfo(recipientsInfo);
        this.recipientsInfoAdded = true;
    }

    @Override
    public void setWarnings(List<Warning> warnings) {
        super.setWarnings(warnings);
        this.warningsAdded = true;
    }
}
