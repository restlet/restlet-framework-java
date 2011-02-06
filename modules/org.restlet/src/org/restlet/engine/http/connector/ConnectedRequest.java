/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.engine.http.connector;

import java.security.Principal;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CacheDirective;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ClientInfo;
import org.restlet.data.Conditions;
import org.restlet.data.Cookie;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Range;
import org.restlet.data.RecipientInfo;
import org.restlet.data.Reference;
import org.restlet.data.Tag;
import org.restlet.data.Warning;
import org.restlet.engine.http.header.CacheDirectiveReader;
import org.restlet.engine.http.header.CookieReader;
import org.restlet.engine.http.header.ExpectationReader;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.engine.http.header.HeaderReader;
import org.restlet.engine.http.header.PreferenceReader;
import org.restlet.engine.http.header.RangeReader;
import org.restlet.engine.http.header.RecipientInfoReader;
import org.restlet.engine.http.header.WarningReader;
import org.restlet.engine.security.AuthenticatorUtils;
import org.restlet.engine.util.DateUtils;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

/**
 * Request wrapper for server HTTP calls.
 * 
 * @author Jerome Louvel
 */
public class ConnectedRequest extends Request {
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
        if (request instanceof ConnectedRequest) {
            ((ConnectedRequest) request).getHeaders().add(headerName,
                    headerValue);
        }
    }

    /** Indicates if the cache control data was parsed and added. */
    private volatile boolean cacheDirectivesAdded;

    /** Indicates if the client data was parsed and added. */
    private volatile boolean clientAdded;

    /** Indicates if the conditions were parsed and added. */
    private volatile boolean conditionAdded;

    /** The parent network connection. */
    private final ServerConnection connection;

    /** The context of the parent connector. */
    private final Context context;

    /** Indicates if the cookies were parsed and added. */
    private volatile boolean cookiesAdded;

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

    /** The user principal. */
    private final Principal userPrincipal;

    /** Indicates if the warning data was parsed and added. */
    private volatile boolean warningsAdded;

    /**
     * Copy constructor.
     * 
     * @param request
     *            The request to copy.
     */
    public ConnectedRequest(ConnectedRequest request) {
        super(request);
        this.connection = request.getConnection();
        this.context = request.context;
        this.userPrincipal = request.getUserPrincipal();
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context of the parent connector.
     * @param connection
     *            The associated network connection.
     * @param method
     *            The protocol method.
     * @param resourceUri
     *            The target resource URI.
     * @param version
     *            The protocol version.
     * @param headers
     *            The request headers.
     * @param entity
     *            The request entity.
     * @param confidential
     *            True if received confidentially.
     * @param userPrincipal
     *            The user principal.
     */
    public ConnectedRequest(Context context, ServerConnection connection,
            Method method, String resourceUri, String version,
            Series<Parameter> headers, Representation entity,
            boolean confidential, Principal userPrincipal) {
        super();
        this.connection = connection;
        this.context = context;
        this.userPrincipal = userPrincipal;
        this.cacheDirectivesAdded = false;
        this.clientAdded = false;
        this.conditionAdded = false;
        this.cookiesAdded = false;
        this.proxySecurityAdded = false;
        this.rangesAdded = false;
        this.recipientsInfoAdded = false;
        this.referrerAdded = false;
        this.securityAdded = false;
        this.warningsAdded = false;

        // Set the properties
        setMethod(method);
        setEntity(entity);

        // Put the headers in the request's attributes map
        getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS, headers);

        if (version != null) {
            int slashIndex = version.indexOf('/');

            if (slashIndex != -1) {
                version = version.substring(slashIndex + 1);
            } else {
                version = null;
            }
        }

        if (confidential) {
            List<Certificate> clientCertificates = getConnection()
                    .getSslClientCertificates();
            if (clientCertificates != null) {
                getAttributes().put(
                        HeaderConstants.ATTRIBUTE_HTTPS_CLIENT_CERTIFICATES,
                        clientCertificates);
            }

            String cipherSuite = getConnection().getSslCipherSuite();
            if (cipherSuite != null) {
                getAttributes().put(
                        HeaderConstants.ATTRIBUTE_HTTPS_CIPHER_SUITE,
                        cipherSuite);
            }

            Integer keySize = getConnection().getSslKeySize();
            if (keySize != null) {
                getAttributes().put(HeaderConstants.ATTRIBUTE_HTTPS_KEY_SIZE,
                        keySize);
            }
        }

        // Set the protocol used for this request
        Protocol serverProtocol = getConnection().getHelper().getHelped()
                .getProtocols().get(0);
        setProtocol(new Protocol(serverProtocol.getSchemeName(),
                serverProtocol.getName(), serverProtocol.getDescription(),
                serverProtocol.getDefaultPort(),
                serverProtocol.isConfidential(), version));

        // Parse the host header
        String host = (getHeaders() == null) ? null : getHeaders()
                .getFirstValue(HeaderConstants.HEADER_HOST, true);
        String hostDomain = null;
        int hostPort = -1;

        if (host != null) {
            int colonIndex = host.indexOf(':');

            if (colonIndex != -1) {
                hostDomain = host.substring(0, colonIndex);
                hostPort = Integer.valueOf(host.substring(colonIndex + 1));
            } else {
                hostDomain = host;
                hostPort = getProtocol().getDefaultPort();
            }
        } else {
            if (!Protocol.SIP.getSchemeName().equals(
                    serverProtocol.getSchemeName())
                    && !Protocol.SIPS.getSchemeName().equals(
                            serverProtocol.getSchemeName())) {
                Context.getCurrentLogger().info(
                        "Couldn't find the mandatory \"Host\" HTTP header.");
            }
        }

        // Set the host reference
        Protocol protocol = getConnection().getHelper().getHelped()
                .getProtocols().get(0);
        StringBuilder sb = new StringBuilder();
        sb.append(protocol.getSchemeName()).append(":");
        if (hostDomain != null) {
            sb.append("//").append(hostDomain);
        }

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
                    setResourceRef(new Reference(getHostRef().toString() + "/"
                            + resourceUri));
                } else {
                    setResourceRef(new Reference(getHostRef().toString()
                            + resourceUri));
                }
            }

            setOriginalRef(getResourceRef().getTargetRef());
        }

        // Set the request date
        String dateHeader = (getHeaders() == null) ? null : getHeaders()
                .getFirstValue(HeaderConstants.HEADER_DATE);
        Date date = null;
        if (dateHeader != null) {
            date = DateUtils.parse(dateHeader);
        }

        if (date == null) {
            date = new Date();
        }

        setDate(date);

        // Set the max forwards
        String maxForwardsHeader = (getHeaders() == null) ? null : getHeaders()
                .getFirstValue(HeaderConstants.HEADER_MAX_FORWARDS);
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
    public boolean abort() {
        getConnection().close();
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
    public List<CacheDirective> getCacheDirectives() {
        List<CacheDirective> result = super.getCacheDirectives();

        if (!this.cacheDirectivesAdded) {
            if (getHeaders() != null) {
                for (Parameter header : getHeaders().subList(
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
                    ExpectationReader.addValues(expect, result);
                } catch (Exception e) {
                    this.context.getLogger().log(Level.INFO, e.getMessage());
                }

                // Set other properties
                result.setAgent(getHeaders().getValues(
                        HeaderConstants.HEADER_USER_AGENT));
                result.setFrom(getHeaders().getFirstValue(
                        HeaderConstants.HEADER_FROM));
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
                        HeaderConstants.HEADER_IF_RANGE);

                for (Parameter header : getHeaders()) {
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

    protected ServerConnection getConnection() {
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

    /**
     * Returns the HTTP headers.
     * 
     * @return The HTTP headers.
     */
    @SuppressWarnings("unchecked")
    public Series<Parameter> getHeaders() {
        return (Series<Parameter>) getAttributes().get(
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
        if (!recipientsInfoAdded) {
            if (getHeaders() != null) {
                for (String header : getHeaders().getValuesArray(
                        HeaderConstants.HEADER_VIA)) {
                    new RecipientInfoReader(header).addValues(result);
                }
            }
            this.recipientsInfoAdded = true;
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

    public Principal getUserPrincipal() {
        return userPrincipal;
    }

    @Override
    public List<Warning> getWarnings() {
        List<Warning> result = super.getWarnings();

        if (!this.warningsAdded) {
            if (getHeaders() != null) {
                for (String warning : getHeaders().getValuesArray(
                        HeaderConstants.HEADER_WARNING)) {
                    new WarningReader(warning).addValues(result);
                }
            }

            this.warningsAdded = true;
        }
        return result;
    }

    @Override
    public void setChallengeResponse(ChallengeResponse response) {
        super.setChallengeResponse(response);
        this.securityAdded = true;
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
