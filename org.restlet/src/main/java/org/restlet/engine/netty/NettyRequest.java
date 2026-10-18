/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.netty;

import static org.restlet.engine.header.HeaderConstants.HEADER_IF_MATCH;
import static org.restlet.engine.header.HeaderConstants.HEADER_IF_MODIFIED_SINCE;
import static org.restlet.engine.header.HeaderConstants.HEADER_IF_NONE_MATCH;
import static org.restlet.engine.header.HeaderConstants.HEADER_IF_RANGE;
import static org.restlet.engine.header.HeaderConstants.HEADER_IF_UNMODIFIED_SINCE;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.Context;
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
import org.restlet.engine.header.TagReader;
import org.restlet.engine.header.WarningReader;
import org.restlet.engine.security.AuthenticatorUtils;
import org.restlet.engine.util.DateUtils;
import org.restlet.engine.util.ReferenceUtils;
import org.restlet.engine.util.StringUtils;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

/**
 * Restlet request of a {@link NettyServerCall}. The request headers are parsed lazily, when the
 * corresponding property is first read.
 *
 * @author Jerome Louvel
 */
class NettyRequest extends Request {

    /** The exchange. */
    private final NettyServerCall call;

    /** The request headers. */
    private final Series<Header> headers;

    /** The context of the server connector. */
    private final Context context;

    private volatile boolean accessControlRequestHeadersAdded;

    private volatile boolean accessControlRequestMethodAdded;

    private volatile boolean cacheDirectivesAdded;

    private volatile boolean clientAdded;

    private volatile boolean conditionAdded;

    private volatile boolean cookiesAdded;

    private volatile boolean entityAdded;

    private volatile boolean proxySecurityAdded;

    private volatile boolean rangesAdded;

    private volatile boolean recipientsInfoAdded;

    private volatile boolean referrerAdded;

    private volatile boolean securityAdded;

    private volatile boolean warningsAdded;

    /**
     * Constructor.
     *
     * @param context The context of the server connector.
     * @param call The exchange.
     */
    NettyRequest(Context context, NettyServerCall call) {
        this.context = context;
        this.call = call;
        this.headers = call.getRequestHeaders();

        setMethod(Method.valueOf(call.getMethod()));
        setHostRef(hostRef(call.getProtocol(), headers));

        String uri = call.getRequestUri();
        Reference resourceRef = new Reference(getHostRef(), uri);
        if (resourceRef.isRelative()) {
            // Take care of the "/" between the host part and the segments.
            resourceRef =
                    new Reference(getHostRef().toString() + (uri.startsWith("/") ? "" : "/") + uri);
        }
        setResourceRef(resourceRef);
        setOriginalRef(ReferenceUtils.getOriginalRef(getResourceRef(), headers));

        String dateHeader = headers.getFirstValue(HeaderConstants.HEADER_DATE, true);
        Date date = (dateHeader != null) ? DateUtils.parse(dateHeader) : null;
        setDate((date != null) ? date : new Date());
    }

    /** Returns the host reference built from the "Host" header. */
    private static String hostRef(Protocol protocol, Series<Header> headers) {
        StringBuilder sb = new StringBuilder();
        sb.append(protocol.getSchemeName()).append("://");
        String host = headers.getFirstValue(HeaderConstants.HEADER_HOST, true);

        if (host == null) {
            Context.getCurrentLogger().info("Couldn't find the mandatory \"Host\" HTTP header.");
            sb.append("null");
            return sb.toString();
        }

        // Take care of IPv6 addresses
        int colonIndex = host.indexOf(':', host.indexOf(']'));
        if (colonIndex == -1) {
            sb.append(host);
        } else {
            sb.append(host, 0, colonIndex);
            String port = host.substring(colonIndex + 1);
            if (!port.equals(Integer.toString(protocol.getDefaultPort()))) {
                sb.append(':').append(port);
            }
        }
        return sb.toString();
    }

    private Logger getLogger() {
        return (context != null) ? context.getLogger() : Context.getCurrentLogger();
    }

    @Override
    public boolean abort() {
        return call.abort();
    }

    @Override
    public void flushBuffers() throws IOException {
        call.flushBuffers();
    }

    @Override
    public Set<String> getAccessControlRequestHeaders() {
        Set<String> result = super.getAccessControlRequestHeaders();
        if (!accessControlRequestHeadersAdded) {
            for (String header :
                    headers.getValuesArray(
                            HeaderConstants.HEADER_ACCESS_CONTROL_REQUEST_HEADERS, true)) {
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
            String header =
                    headers.getFirstValue(
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
            for (Header header : headers.subList(HeaderConstants.HEADER_CACHE_CONTROL)) {
                CacheDirectiveReader.addValues(header, result);
            }
            cacheDirectivesAdded = true;
        }
        return result;
    }

    @Override
    public ChallengeResponse getChallengeResponse() {
        ChallengeResponse result = super.getChallengeResponse();
        if (!securityAdded) {
            String authorization = headers.getValues(HeaderConstants.HEADER_AUTHORIZATION);
            result = AuthenticatorUtils.parseResponse(this, authorization, headers);
            setChallengeResponse(result);
        }
        return result;
    }

    @Override
    public ClientInfo getClientInfo() {
        ClientInfo result = super.getClientInfo();
        if (clientAdded) {
            return result;
        }
        clientAdded = true;

        addPreferences(result);
        result.setAgent(headers.getValues(HeaderConstants.HEADER_USER_AGENT));
        result.setFrom(headers.getFirstValue(HeaderConstants.HEADER_FROM, true));

        InetSocketAddress remote = call.getRemoteAddress();
        if (remote != null) {
            result.setAddress(remote.getAddress().getHostAddress());
            result.setPort(remote.getPort());
        }

        // Special handling for the non-standard but common "X-Forwarded-For" header, supported by
        // popular proxies and caches.
        if (context != null
                && Boolean.parseBoolean(
                        context.getParameters().getFirstValue("useForwardedForHeader", false))) {
            String header = headers.getValues(HeaderConstants.HEADER_X_FORWARDED_FOR);
            if (header != null) {
                for (String address : header.split(",")) {
                    result.getForwardedAddresses().add(address.trim());
                }
            }
        }

        return result;
    }

    /**
     * Parses the Accept* and Expect headers. A parsing error is traced, and the other headers are
     * still parsed.
     */
    private void addPreferences(ClientInfo result) {
        addPreference(
                HeaderConstants.HEADER_ACCEPT_CHARSET,
                v -> PreferenceReader.addCharacterSets(v, result));
        addPreference(
                HeaderConstants.HEADER_ACCEPT_ENCODING,
                v -> PreferenceReader.addEncodings(v, result));
        addPreference(
                HeaderConstants.HEADER_ACCEPT_LANGUAGE,
                v -> PreferenceReader.addLanguages(v, result));
        addPreference(
                HeaderConstants.HEADER_ACCEPT, v -> PreferenceReader.addMediaTypes(v, result));
        addPreference(
                HeaderConstants.HEADER_ACCEPT_PATCH, v -> PreferenceReader.addPatches(v, result));
        addPreference(HeaderConstants.HEADER_EXPECT, v -> ExpectationReader.addValues(v, result));
    }

    private void addPreference(String name, java.util.function.Consumer<String> parser) {
        try {
            parser.accept(headers.getValues(name));
        } catch (RuntimeException e) {
            getLogger().log(Level.INFO, e.getMessage());
        }
    }

    @Override
    public Conditions getConditions() {
        Conditions result = super.getConditions();
        if (conditionAdded) {
            return result;
        }
        conditionAdded = true;

        for (Header header : headers) {
            if (header.getName().equalsIgnoreCase(HEADER_IF_MODIFIED_SINCE)) {
                Date date = HeaderReader.readDate(header.getValue(), false);
                if (date != null && date.getTime() != -1) {
                    result.setModifiedSince(date);
                }
            } else if (header.getName().equalsIgnoreCase(HEADER_IF_UNMODIFIED_SINCE)) {
                Date date = HeaderReader.readDate(header.getValue(), false);
                if (date != null && date.getTime() != -1) {
                    result.setUnmodifiedSince(date);
                }
            }
        }

        addTags(HEADER_IF_MATCH, result.getMatch());
        addTags(HEADER_IF_NONE_MATCH, result.getNoneMatch());

        String ifRange = headers.getFirstValue(HEADER_IF_RANGE, true);
        if (!StringUtils.isNullOrEmpty(ifRange)) {
            Tag tag = Tag.parse(ifRange);
            if (tag != null) {
                result.setRangeTag(tag);
            } else {
                result.setRangeDate(HeaderReader.readDate(ifRange, false));
            }
        }

        return result;
    }

    private void addTags(String headerName, List<Tag> tags) {
        String value = headers.getValues(headerName);
        if (value != null) {
            try {
                new TagReader(value).addValues(tags);
            } catch (RuntimeException _) {
                getLogger()
                        .log(
                                Level.INFO,
                                "Unable to process the {0} header: {1}",
                                new Object[] {headerName, value});
            }
        }
    }

    @Override
    public Series<Cookie> getCookies() {
        Series<Cookie> result = super.getCookies();
        if (!cookiesAdded) {
            String cookies = headers.getValues(HeaderConstants.HEADER_COOKIE);
            if (cookies != null) {
                new CookieReader(cookies).addValues(result);
            }
            cookiesAdded = true;
        }
        return result;
    }

    @Override
    public Representation getEntity() {
        if (!entityAdded) {
            setEntity(call.getRequestEntity());
        }
        return super.getEntity();
    }

    @Override
    public ChallengeResponse getProxyChallengeResponse() {
        ChallengeResponse result = super.getProxyChallengeResponse();
        if (!proxySecurityAdded) {
            String authorization = headers.getValues(HeaderConstants.HEADER_PROXY_AUTHORIZATION);
            result = AuthenticatorUtils.parseResponse(this, authorization, headers);
            setProxyChallengeResponse(result);
        }
        return result;
    }

    @Override
    public List<Range> getRanges() {
        List<Range> result = super.getRanges();
        if (!rangesAdded) {
            result.addAll(RangeReader.read(headers.getValues(HeaderConstants.HEADER_RANGE)));
            rangesAdded = true;
        }
        return result;
    }

    @Override
    public List<RecipientInfo> getRecipientsInfo() {
        List<RecipientInfo> result = super.getRecipientsInfo();
        if (!recipientsInfoAdded) {
            for (String header : headers.getValuesArray(HeaderConstants.HEADER_VIA, true)) {
                new RecipientInfoReader(header).addValues(result);
            }
            recipientsInfoAdded = true;
        }
        return result;
    }

    @Override
    public Reference getReferrerRef() {
        if (!referrerAdded) {
            String referrer = headers.getValues(HeaderConstants.HEADER_REFERRER);
            if (referrer != null) {
                setReferrerRef(new Reference(referrer));
            }
            referrerAdded = true;
        }
        return super.getReferrerRef();
    }

    @Override
    public List<Warning> getWarnings() {
        List<Warning> result = super.getWarnings();
        if (!warningsAdded) {
            for (String header : headers.getValuesArray(HeaderConstants.HEADER_WARNING, true)) {
                new WarningReader(header).addValues(result);
            }
            warningsAdded = true;
        }
        return result;
    }

    @Override
    public void setAccessControlRequestHeaders(Set<String> accessControlRequestHeaders) {
        super.setAccessControlRequestHeaders(accessControlRequestHeaders);
        accessControlRequestHeadersAdded = true;
    }

    @Override
    public void setAccessControlRequestMethod(Method accessControlRequestMethod) {
        super.setAccessControlRequestMethod(accessControlRequestMethod);
        accessControlRequestMethodAdded = true;
    }

    @Override
    public void setChallengeResponse(ChallengeResponse response) {
        super.setChallengeResponse(response);
        securityAdded = true;
    }

    @Override
    public void setEntity(Representation entity) {
        super.setEntity(entity);
        entityAdded = true;
    }

    @Override
    public void setProxyChallengeResponse(ChallengeResponse response) {
        super.setProxyChallengeResponse(response);
        proxySecurityAdded = true;
    }

    @Override
    public void setRecipientsInfo(List<RecipientInfo> recipientsInfo) {
        super.setRecipientsInfo(recipientsInfo);
        recipientsInfoAdded = true;
    }

    @Override
    public void setWarnings(List<Warning> warnings) {
        super.setWarnings(warnings);
        warningsAdded = true;
    }
}
