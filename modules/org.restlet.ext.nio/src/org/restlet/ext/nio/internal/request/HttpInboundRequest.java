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

package org.restlet.ext.nio.internal.request;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Server;
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
import org.restlet.ext.nio.internal.connection.Connection;
import org.restlet.util.Series;

/**
 * Request wrapper for server HTTP calls.
 * 
 * @author Jerome Louvel
 */
public class HttpInboundRequest extends Request implements InboundRequest {
    /**
     * Adds a new header to the given request.
     * 
     * @param request
     *            The request to update.
     * @param headerName
     *            The header name to add.
     * @param headerValue
     *            The header value to add.
     */
    public static void addHeader(Request request, String headerName,
            String headerValue) {
        if (request instanceof HttpInboundRequest) {
            ((InboundRequest) request).getHeaders()
                    .add(headerName, headerValue);
        }
    }

    /**
     * Indicates if the access control data for request headers was parsed and
     * added.
     */
    private volatile boolean accessControlRequestHeadersAdded;

    /**
     * Indicates if the access control data for request method was parsed and
     * added
     */
    private volatile boolean accessControlRequestMethodAdded;

    /** Indicates if the cache control data was parsed and added. */
    private volatile boolean cacheDirectivesAdded;

    /** Indicates if the client data was parsed and added. */
    private volatile boolean clientAdded;

    /** Indicates if the conditions were parsed and added. */
    private volatile boolean conditionAdded;

    /** The parent network connection. */
    private final Connection<Server> connection;

    /** The context of the parent connector. */
    private final Context context;

    /** Indicates if the cookies were parsed and added. */
    private volatile boolean cookiesAdded;

    /** The protocol name and version. */
    private volatile String protocol;

    /** Indicates if the proxy security data was parsed and added. */
    private volatile boolean proxySecurityAdded;

    /** Indicates if the ranges data was parsed and added. */
    private volatile boolean rangesAdded;

    /** Indicates if the recipients info was parsed and added. */
    private volatile boolean recipientsInfoAdded;

    /** Indicates if the referrer was parsed and added. */
    private volatile boolean referrerAdded;

    /** The target resource URI. */
    private volatile String resourceUri;

    /** Indicates if the security data was parsed and added. */
    private volatile boolean securityAdded;

    /** The user principal. */
    private final Principal userPrincipal;

    /** Indicates if the warning data was parsed and added. */
    private volatile boolean warningsAdded;

    /**
     * Constructor.
     * 
     * @param context
     *            The context of the parent connector.
     * @param connection
     *            The associated network connection.
     * @param methodName
     *            The protocol method name.
     * @param resourceUri
     *            The target resource URI.
     * @param protocol
     *            The protocol name and version.
     */
    public HttpInboundRequest(Context context, Connection<Server> connection,
            String methodName, String resourceUri, String protocol) {
        super();
        this.connection = connection;
        this.context = context;
        this.userPrincipal = null;
        this.accessControlRequestHeadersAdded = false;
        this.accessControlRequestMethodAdded = false;
        this.cacheDirectivesAdded = false;
        this.clientAdded = false;
        this.conditionAdded = false;
        this.cookiesAdded = false;
        this.proxySecurityAdded = false;
        this.rangesAdded = false;
        this.recipientsInfoAdded = false;
        this.referrerAdded = false;
        this.resourceUri = resourceUri;
        this.securityAdded = false;
        this.warningsAdded = false;

        // Set the protocol
        int versionSeparator = protocol.indexOf('/');
        Protocol connectorProtocol = getConnection().getHelper().getHelped()
                .getProtocols().get(0);

        if (versionSeparator != -1) {
            String name = protocol.substring(0, versionSeparator);
            String version = protocol.substring(versionSeparator + 1);

            if (connectorProtocol.getTechnicalName().equals(name)
                    && connectorProtocol.getVersion().equals(version)) {
                setProtocol(connectorProtocol);
            } else {
                setProtocol(Protocol.valueOf(name, version));
            }
        }

        // Set the properties
        setMethod(Method.valueOf(methodName));
    }

    /**
     * Copy constructor.
     * 
     * @param request
     *            The request to copy.
     */
    public HttpInboundRequest(HttpInboundRequest request) {
        super(request);
        this.connection = request.getConnection();
        this.context = request.context;
        this.userPrincipal = request.getUserPrincipal();
    }

    @Override
    public boolean abort() {
        getConnection().close(false);
        return true;
    }

    @Override
    public synchronized void commit(Response response) {
        if ((response != null) && !response.isCommitted()) {
            getConnection().commit(response);
            response.setCommitted(true);
        }
    }

    @Override
    public void flushBuffers() {
        getConnection().getOutboundWay().flushBuffer();
    }

    @Override
    public Set<String> getAccessControlRequestHeaders() {
        Set<String> result = super.getAccessControlRequestHeaders();
        if (!accessControlRequestHeadersAdded) {
            for (String header : getHeaders()
                    .getValuesArray(
                            HeaderConstants.HEADER_ACCESS_CONTROL_REQUEST_HEADERS,
                            true)) {
                new StringReader(header).addValues(result);
            }
            this.accessControlRequestHeadersAdded = true;
        }
        return result;
    }

    @Override
    public Method getAccessControlRequestMethod() {
        Method result = super.getAccessControlRequestMethod();
        if (!accessControlRequestMethodAdded) {
            String header = getHeaders().getFirstValue(
                    HeaderConstants.HEADER_ACCESS_CONTROL_REQUEST_METHOD, true);
            if (header != null) {
                result = Method.valueOf(header);
                super.setAccessControlRequestMethod(result);
            }
            this.accessControlRequestMethodAdded = true;
        }
        return result;
    }

    @Override
    public List<CacheDirective> getCacheDirectives() {
        List<CacheDirective> result = super.getCacheDirectives();

        if (!this.cacheDirectivesAdded) {
            if (getHeaders() != null) {
                for (Header header : getHeaders().subList(
                        HeaderConstants.HEADER_CACHE_CONTROL)) {
                    CacheDirectiveReader.addValues(header, result);
                }
            }

            this.cacheDirectivesAdded = true;
        }
        return result;
    }

    @Override
    public ChallengeResponse getChallengeResponse() {
        ChallengeResponse result = super.getChallengeResponse();

        if (!this.securityAdded) {
            if (getHeaders() != null) {
                // Extract the header value
                String authorization = getHeaders().getValues(
                        HeaderConstants.HEADER_AUTHORIZATION);

                // Set the challenge response
                result = AuthenticatorUtils.parseResponse(this, authorization,
                        getHeaders());
                setChallengeResponse(result);
            }

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
        ClientInfo result = super.getClientInfo();

        if (!this.clientAdded) {
            if (getHeaders() != null) {
                // Extract the header values
                String acceptMediaType = getHeaders().getValues(
                        HeaderConstants.HEADER_ACCEPT);
                String acceptCharset = getHeaders().getValues(
                        HeaderConstants.HEADER_ACCEPT_CHARSET);
                String acceptEncoding = getHeaders().getValues(
                        HeaderConstants.HEADER_ACCEPT_ENCODING);
                String acceptLanguage = getHeaders().getValues(
                        HeaderConstants.HEADER_ACCEPT_LANGUAGE);
                String acceptPatch = getHeaders().getValues(
                        HeaderConstants.HEADER_ACCEPT_PATCH);
                String expect = getHeaders().getValues(
                        HeaderConstants.HEADER_EXPECT);

                // Parse the headers and update the call preferences

                // Parse the Accept* headers. If an error occurs during the
                // parsing of each header, the error is traced and we keep on
                // with the other headers.
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
                result.setAgent(getHeaders().getValues(
                        HeaderConstants.HEADER_USER_AGENT));
                result.setFrom(getHeaders().getFirstValue(
                        HeaderConstants.HEADER_FROM, true));
                result.setAddress(getConnection().getAddress());
                result.setPort(getConnection().getPort());

                if (userPrincipal != null) {
                    result.getPrincipals().add(userPrincipal);
                }

                if (this.context != null) {
                    // Special handling for the non standard but common
                    // "X-Forwarded-For" header.
                    final boolean useForwardedForHeader = Boolean
                            .parseBoolean(this.context.getParameters()
                                    .getFirstValue("useForwardedForHeader",
                                            false));
                    if (useForwardedForHeader) {
                        // Lookup the "X-Forwarded-For" header supported by
                        // popular
                        // proxies and caches.
                        final String header = getHeaders().getValues(
                                HeaderConstants.HEADER_X_FORWARDED_FOR);
                        if (header != null) {
                            final String[] addresses = header.split(",");
                            for (int i = 0; i < addresses.length; i++) {
                                String address = addresses[i].trim();
                                result.getForwardedAddresses().add(address);
                            }
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
        Conditions result = super.getConditions();

        if (!this.conditionAdded) {
            if (getHeaders() != null) {
                // Extract the header values
                String ifMatchHeader = getHeaders().getValues(
                        HeaderConstants.HEADER_IF_MATCH);
                String ifNoneMatchHeader = getHeaders().getValues(
                        HeaderConstants.HEADER_IF_NONE_MATCH);
                Date ifModifiedSince = null;
                Date ifUnmodifiedSince = null;
                String ifRangeHeader = getHeaders().getFirstValue(
                        HeaderConstants.HEADER_IF_RANGE, true);

                for (Header header : getHeaders()) {
                    if (header.getName().equalsIgnoreCase(
                            HeaderConstants.HEADER_IF_MODIFIED_SINCE)) {
                        ifModifiedSince = HeaderReader.readDate(
                                header.getValue(), false);
                    } else if (header.getName().equalsIgnoreCase(
                            HeaderConstants.HEADER_IF_UNMODIFIED_SINCE)) {
                        ifUnmodifiedSince = HeaderReader.readDate(
                                header.getValue(), false);
                    }
                }

                // Set the If-Modified-Since date
                if ((ifModifiedSince != null)
                        && (ifModifiedSince.getTime() != -1)) {
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
            }

            this.conditionAdded = true;
        }

        return result;
    }

    /**
     * Returns the related connection.
     * 
     * @return The related connection.
     */
    public Connection<Server> getConnection() {
        return connection;
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
            if (getHeaders() != null) {
                String cookieValues = getHeaders().getValues(
                        HeaderConstants.HEADER_COOKIE);

                if (cookieValues != null) {
                    new CookieReader(cookieValues).addValues(result);
                }
            }

            this.cookiesAdded = true;
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.restlet.engine.nio.CRequest#getHeaders()
     */
    @SuppressWarnings("unchecked")
    public Series<Header> getHeaders() {
        return (Series<Header>) getAttributes().get(
                HeaderConstants.ATTRIBUTE_HEADERS);
    }

    @Override
    public ChallengeResponse getProxyChallengeResponse() {
        ChallengeResponse result = super.getProxyChallengeResponse();

        if (!this.proxySecurityAdded) {
            if (getHeaders() != null) {
                // Extract the header value
                final String authorization = getHeaders().getValues(
                        HeaderConstants.HEADER_PROXY_AUTHORIZATION);

                // Set the challenge response
                result = AuthenticatorUtils.parseResponse(this, authorization,
                        getHeaders());
                setProxyChallengeResponse(result);
            }

            this.proxySecurityAdded = true;
        }

        return result;
    }

    @Override
    public List<Range> getRanges() {
        final List<Range> result = super.getRanges();

        if (!this.rangesAdded) {
            if (getHeaders() != null) {
                // Extract the header value
                String ranges = getHeaders().getValues(
                        HeaderConstants.HEADER_RANGE);
                result.addAll(RangeReader.read(ranges));
            }

            this.rangesAdded = true;
        }

        return result;
    }

    @Override
    public List<RecipientInfo> getRecipientsInfo() {
        List<RecipientInfo> result = super.getRecipientsInfo();

        if (!recipientsInfoAdded && (getHeaders() != null)) {
            for (String header : getHeaders().getValuesArray(
                    HeaderConstants.HEADER_VIA, true)) {
                new RecipientInfoReader(header).addValues(result);
            }

            setRecipientsInfo(result);
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
            if (getHeaders() != null) {
                final String referrerValue = getHeaders().getValues(
                        HeaderConstants.HEADER_REFERRER);

                if (referrerValue != null) {
                    setReferrerRef(new Reference(referrerValue));
                }
            }

            this.referrerAdded = true;
        }

        return super.getReferrerRef();
    }

    /**
     * Returns the associated user principal.
     * 
     * @return The associated user principal.
     */
    public Principal getUserPrincipal() {
        return userPrincipal;
    }

    @Override
    public List<Warning> getWarnings() {
        List<Warning> result = super.getWarnings();

        if (!this.warningsAdded) {
            if (getHeaders() != null) {
                for (String warning : getHeaders().getValuesArray(
                        HeaderConstants.HEADER_WARNING, true)) {
                    new WarningReader(warning).addValues(result);
                }
            }

            this.warningsAdded = true;
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
    public void setHeaders(Series<Header> headers) {
        getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS, headers);

        // Parse the version string
        if (protocol != null) {
            int slashIndex = protocol.indexOf('/');

            if (slashIndex != -1) {
                protocol = protocol.substring(slashIndex + 1);
            } else {
                protocol = null;
            }
        }

        // Parse the host header
        String host = (getHeaders() == null) ? null : getHeaders()
                .getFirstValue(HeaderConstants.HEADER_HOST, true);
        String hostDomain = null;
        int hostPort = -1;

        if (host != null) {
            // Decide whether the address is IPv4 or IPv6
            int rightSquareBracketIndex = host.indexOf(']');
            boolean ipv4 = (rightSquareBracketIndex == -1);

            if (ipv4) {
                // IPv4 address handling.
                int colonIndex = host.indexOf(':');

                if (colonIndex != -1) {
                    hostDomain = host.substring(0, colonIndex);
                    hostPort = Integer.valueOf(host.substring(colonIndex + 1));
                } else {
                    hostDomain = host;
                    hostPort = getProtocol().getDefaultPort();
                }

                Context.getCurrentLogger().fine(
                        "HttpInboundRequest::setHeaders, IPv4 hostDomain: "
                                + hostDomain + ", hostPort: " + hostPort);
            } else {
                // IPv6 address handling.
                //
                // Two possible cases:
                // - host == "[::1]:8182" --using the 8182 port.
                // - host == "[::1]" ------ using the default port 80.
                //
                // For IPv6 address, we use ']' to separate the domain and the
                // port, because it's unique.
                if (rightSquareBracketIndex + 1 < host.length()) {
                    // Using specified port
                    hostDomain = host.substring(0, rightSquareBracketIndex + 1);
                    hostPort = Integer.valueOf(host.substring(rightSquareBracketIndex + 2));
                } else if (rightSquareBracketIndex + 1 == host.length()) {
                    // Must be using default port 80,
                    hostDomain = host;
                    hostPort = getProtocol().getDefaultPort();
                }

                Context.getCurrentLogger().fine(
                        "HttpInboundRequest::setHeaders, IPv6 hostDomain: "
                                + hostDomain + ", hostPort: " + hostPort);
            }
        } else {
            Protocol serverProtocol = getConnection().getHelper().getHelped().getProtocols().get(0);

            if (!Protocol.SIP.getSchemeName().equals(serverProtocol.getSchemeName())
                    && !Protocol.SIPS.getSchemeName().equals(serverProtocol.getSchemeName())) {
                Context.getCurrentLogger().info("Couldn't find the mandatory \"Host\" HTTP header. Falling back to the IP address.");
                hostDomain = getConnection().getAddress();
                hostPort = getConnection().getPort();

                if (hostDomain == null) {
                    hostDomain = "localhost";
                }

                if (hostPort == -1) {
                    hostPort = getConnection().getHelper().getHelped().getActualPort();
                }

                if (hostPort == -1) {
                    getProtocol().getDefaultPort();
                }
            }
        }

        // Set the host reference
        Protocol protocol = getConnection().getHelper().getHelped().getProtocols().get(0);
        StringBuilder sb = new StringBuilder();
        sb.append(protocol.getSchemeName()).append("://");
        sb.append(hostDomain);

        if ((hostPort != -1) && (hostPort != protocol.getDefaultPort())) {
            sb.append(':').append(hostPort);
        }

        setHostRef(sb.toString());

        // Set the resource reference
        if (resourceUri != null) {
            setResourceRef(new Reference(getHostRef(), resourceUri));

            if (getResourceRef().isRelative()) {
                // Take care of the "/" between the host part and the segments.
                if (!resourceUri.startsWith("/")) {
                    setResourceRef(new Reference(getHostRef().toString() + "/" + resourceUri));
                } else {
                    setResourceRef(new Reference(getHostRef().toString() + resourceUri));
                }
            }

            setOriginalRef(ReferenceUtils.getOriginalRef(getResourceRef(), headers));
        }

        // Set the request date
        String dateHeader = (getHeaders() == null) ? null : 
            getHeaders().getFirstValue(HeaderConstants.HEADER_DATE, true);
        Date date = null;
        if (dateHeader != null) {
            date = DateUtils.parse(dateHeader);
        }

        if (date == null) {
            date = new Date();
        }

        setDate(date);

        // Set the max forwards
        String maxForwardsHeader = (getHeaders() == null) ? null : 
            getHeaders().getFirstValue(HeaderConstants.HEADER_MAX_FORWARDS, true);
        if (maxForwardsHeader != null) {
            try {
                setMaxForwards(Integer.parseInt(maxForwardsHeader));
            } catch (NumberFormatException nfe) {
                Context.getCurrentLogger().info(
                        "Unable to parse the Max-Forwards header: "
                                + maxForwardsHeader);
            }
        }
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
