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

package org.restlet.ext.sip.internal;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.restlet.Context;
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
import org.restlet.engine.header.WarningReader;
import org.restlet.engine.security.AuthenticatorUtils;
import org.restlet.engine.util.DateUtils;
import org.restlet.engine.util.ReferenceUtils;
import org.restlet.ext.nio.internal.connection.Connection;
import org.restlet.ext.nio.internal.request.InboundRequest;
import org.restlet.ext.sip.Address;
import org.restlet.ext.sip.ContactInfo;
import org.restlet.ext.sip.Event;
import org.restlet.ext.sip.EventType;
import org.restlet.ext.sip.OptionTag;
import org.restlet.ext.sip.Priority;
import org.restlet.ext.sip.SipRecipientInfo;
import org.restlet.ext.sip.SipRequest;
import org.restlet.ext.sip.Subscription;
import org.restlet.util.Series;

/**
 * Request part of a SIP transaction.
 * 
 * @author Jerome Louvel
 * @deprecated Will be removed to focus on Web APIs.
 */
@Deprecated
public class SipInboundRequest extends SipRequest implements InboundRequest {

    // HTTP specific members (copy from ConnectedRequest)

    /** Indicates if the alert info data was parsed and added. */
    private volatile boolean alertInfoAdded;

    /** Indicates if the allowed event types data was parsed and added. */
    private volatile boolean allowedEventTypesAdded;

    /** Indicates if the cache control data was parsed and added. */
    private volatile boolean cacheDirectivesAdded;

    /** Indicates if the caller data was parsed and added. */
    private volatile boolean callerInfoAdded;

    /** Indicates if the client data was parsed and added. */
    private volatile boolean clientAdded;

    /** Indicates if the conditions were parsed and added. */
    private volatile boolean conditionAdded;

    /** The parent network connection. */
    private final Connection<Server> connection;

    /** Indicates if the contact data was parsed and added. */
    private volatile boolean contactAdded;

    /** The context of the parent connector. */
    private final Context context;

    /** Indicates if the cookies were parsed and added. */
    private volatile boolean cookiesAdded;

    /** Indicates if the event data was parsed and added. */
    private volatile boolean eventAdded;

    /** Indicates if the "in reply to" data was parsed and added. */
    private volatile boolean inReplyToAdded;

    /** Indicates if the priority data was parsed and added. */
    private volatile boolean priorityAdded;

    /** The protocol name and version. */
    private volatile String protocol;

    /** Indicates if the proxy data was parsed and added. */
    private volatile boolean proxyRequiresAdded;

    // SIP specific members

    /** Indicates if the proxy security data was parsed and added. */
    private volatile boolean proxySecurityAdded;

    /** Indicates if the ranges data was parsed and added. */
    private volatile boolean rangesAdded;

    /** Indicates if the recipients info was parsed and added. */
    private volatile boolean recipientsInfoAdded;

    /** Indicates if the recorded routes data was parsed and added. */
    private volatile boolean recordedRoutesAdded;

    /** Indicates if the referrer was parsed and added. */
    private volatile boolean referrerAdded;

    /** Indicates if the refer-to data was parsed and added. */
    private volatile boolean referToAdded;

    /** Indicates if the reply-to data was parsed and added. */
    private volatile boolean replyToAdded;

    /** Indicates if the requires data was parsed and added. */
    private volatile boolean requiresAdded;

    /** The target resource URI. */
    private volatile String resourceUri;

    /** Indicates if the routes data was parsed and added. */
    private volatile boolean routesAdded;

    /** Indicates if the security data was parsed and added. */
    private volatile boolean securityAdded;

    /** Indicates if the if-match data was parsed and added. */
    private volatile boolean sipIfMatchAdded;

    /** Indicates if the SIP recipients data was parsed and added. */
    private volatile boolean sipRecipientsInfoAdded;

    /** Indicates if the subscription data was parsed and added. */
    private volatile boolean subscriptionAdded;

    /** Indicates if the supported data was parsed and added. */
    private volatile boolean supportedAdded;

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
    public SipInboundRequest(Context context, Connection<Server> connection,
            String methodName, String resourceUri, String protocol) {
        super();
        this.context = context;
        this.cacheDirectivesAdded = false;
        this.clientAdded = false;
        this.conditionAdded = false;
        this.connection = connection;
        this.cookiesAdded = false;
        this.referrerAdded = false;
        this.securityAdded = false;
        this.userPrincipal = null;
        this.proxySecurityAdded = false;
        this.recipientsInfoAdded = false;
        this.resourceUri = resourceUri;
        this.protocol = protocol;
        this.warningsAdded = false;

        // SIP specific initialization
        this.alertInfoAdded = false;
        this.allowedEventTypesAdded = false;
        this.callerInfoAdded = false;
        this.contactAdded = false;
        this.eventAdded = false;
        this.inReplyToAdded = false;
        this.priorityAdded = false;

        // Set the properties
        setMethod(Method.valueOf(methodName));
    }

    /**
     * Copy constructor.
     * 
     * @param request
     *            The request to copy.
     */
    public SipInboundRequest(SipInboundRequest request) {
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
    public Address getAlertInfo() {
        if (!alertInfoAdded) {
            String header = (getHeaders() == null) ? null : getHeaders()
                    .getValues(SipConstants.HEADER_ALERT_INFO);

            if (header != null) {
                try {
                    setAlertInfo(new AddressReader(header).readValue());
                } catch (Exception e) {
                    Context.getCurrentLogger().info(e.getMessage());
                }
            }

            alertInfoAdded = true;
        }

        return super.getAlertInfo();
    }

    @Override
    public List<EventType> getAllowedEventTypes() {
        List<EventType> aet = super.getAllowedEventTypes();

        if (!allowedEventTypesAdded) {
            String header = (getHeaders() == null) ? null : getHeaders()
                    .getValues(SipConstants.HEADER_ALLOW_EVENTS);

            if (header != null) {
                try {
                    aet.addAll(new EventTypeReader(header).readValues());
                    allowedEventTypesAdded = true;
                } catch (Exception e) {
                    Context.getCurrentLogger().info(e.getMessage());
                }
            }

            allowedEventTypesAdded = true;
        }

        return aet;
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
    public List<Address> getCallerInfo() {
        List<Address> ci = super.getCallerInfo();

        if (!callerInfoAdded) {
            String header = (getHeaders() == null) ? null : getHeaders()
                    .getValues(SipConstants.HEADER_CALL_INFO);

            if (header != null) {
                try {
                    ci.addAll(new AddressReader(header).readValues());
                } catch (Exception e) {
                    Context.getCurrentLogger().info(e.getMessage());
                }
            }

            callerInfoAdded = true;
        }

        return ci;
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
                // parsing
                // of each header, the error is traced and we keep on with the
                // other
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

    @Override
    public List<ContactInfo> getContacts() {
        List<ContactInfo> c = super.getContacts();

        if (!contactAdded) {
            String header = (getHeaders() == null) ? null : getHeaders()
                    .getValues(SipConstants.HEADER_CONTACT);

            if (header != null) {
                try {
                    c.addAll(new ContactInfoReader(header).readValues());
                } catch (Exception e) {
                    Context.getCurrentLogger().info(e.getMessage());
                }
            }

            contactAdded = true;
        }

        return c;
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

    @Override
    public Event getEvent() {
        if (!eventAdded) {
            String header = (getHeaders() == null) ? null : getHeaders()
                    .getValues(SipConstants.HEADER_EVENT);

            if (header != null) {
                try {
                    setEvent(new EventReader(header).readValue());
                } catch (Exception e) {
                    Context.getCurrentLogger().info(e.getMessage());
                }
            }

            eventAdded = true;
        }

        return super.getEvent();
    }

    @Override
    public List<String> getInReplyTo() {
        List<String> irt = super.getInReplyTo();

        if (!inReplyToAdded) {
            String header = (getHeaders() == null) ? null : getHeaders()
                    .getValues(SipConstants.HEADER_ALLOW_EVENTS);

            if (header != null) {
                try {
                    irt.addAll(new HeaderReader<String>(header).readValues());
                } catch (Exception e) {
                    Context.getCurrentLogger().info(e.getMessage());
                }
            }

            inReplyToAdded = true;
        }

        return irt;
    }

    @Override
    public Priority getPriority() {
        if (!priorityAdded) {
            String header = (getHeaders() == null) ? null : getHeaders()
                    .getValues(SipConstants.HEADER_PRIORITY);

            if (header != null) {
                try {
                    setPriority(Priority.valueOf(header));
                } catch (Exception e) {
                    Context.getCurrentLogger().info(e.getMessage());
                }
            }

            priorityAdded = true;
        }

        return super.getPriority();
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
    public List<OptionTag> getProxyRequires() {
        List<OptionTag> pr = super.getProxyRequires();

        if (!proxyRequiresAdded) {
            String header = (getHeaders() == null) ? null : getHeaders()
                    .getValues(SipConstants.HEADER_PROXY_REQUIRE);

            if (header != null) {
                try {
                    pr.addAll(new OptionTagReader(header).readValues());
                } catch (Exception e) {
                    Context.getCurrentLogger().info(e.getMessage());
                }
            }

            proxyRequiresAdded = true;
        }

        return pr;
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

    @Override
    public List<Address> getRecordedRoutes() {
        List<Address> rr = super.getRecordedRoutes();

        if (!recordedRoutesAdded) {
            String header = (getHeaders() == null) ? null : getHeaders()
                    .getValues(SipConstants.HEADER_RECORD_ROUTE);

            if (header != null) {
                try {
                    rr.addAll(new AddressReader(header).readValues());
                } catch (Exception e) {
                    Context.getCurrentLogger().info(e.getMessage());
                }
            }

            recordedRoutesAdded = true;
        }

        return rr;
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

    @Override
    public Address getReferTo() {
        if (!referToAdded) {
            String header = (getHeaders() == null) ? null : getHeaders()
                    .getValues(SipConstants.HEADER_REFER_TO);

            if (header != null) {
                try {
                    setReferTo(new AddressReader(header).readValue());
                } catch (Exception e) {
                    Context.getCurrentLogger().info(e.getMessage());
                }
            }

            referToAdded = true;
        }

        return super.getReferTo();
    }

    @Override
    public Address getReplyTo() {
        if (!replyToAdded) {
            String header = (getHeaders() == null) ? null : getHeaders()
                    .getValues(SipConstants.HEADER_REPLY_TO);

            if (header != null) {
                try {
                    setReplyTo(new AddressReader(header).readValue());
                } catch (Exception e) {
                    Context.getCurrentLogger().info(e.getMessage());
                }
            }

            replyToAdded = true;
        }

        return super.getReplyTo();
    }

    @Override
    public List<OptionTag> getRequires() {
        List<OptionTag> r = super.getRequires();

        if (!requiresAdded) {
            String header = (getHeaders() == null) ? null : getHeaders()
                    .getValues(SipConstants.HEADER_REQUIRE);

            if (header != null) {
                try {
                    r.addAll(new OptionTagReader(header).readValues());
                } catch (Exception e) {
                    Context.getCurrentLogger().info(e.getMessage());
                }
            }

            requiresAdded = true;
        }

        return r;
    }

    @Override
    public List<Address> getRoutes() {
        List<Address> r = super.getRoutes();

        if (!routesAdded) {
            String header = (getHeaders() == null) ? null : getHeaders()
                    .getValues(SipConstants.HEADER_ROUTE);

            if (header != null) {
                try {
                    r.addAll(new AddressReader(header).readValues());
                } catch (Exception e) {
                    Context.getCurrentLogger().info(e.getMessage());
                }
            }

            routesAdded = true;
        }

        return r;
    }

    @Override
    public Tag getSipIfMatch() {
        if (!sipIfMatchAdded) {
            String header = (getHeaders() == null) ? null : getHeaders()
                    .getValues(SipConstants.HEADER_SIP_IF_MATCH);

            if (header != null) {
                try {
                    setSipIfMatch(Tag.parse(header));
                } catch (Exception e) {
                    Context.getCurrentLogger().info(e.getMessage());
                }
            }

            sipIfMatchAdded = true;
        }

        return super.getSipIfMatch();
    }

    @Override
    public List<SipRecipientInfo> getSipRecipientsInfo() {
        List<SipRecipientInfo> sri = super.getSipRecipientsInfo();

        if (!sipRecipientsInfoAdded) {
            String header = (getHeaders() == null) ? null : getHeaders()
                    .getValues(HeaderConstants.HEADER_VIA);

            if (header != null) {
                try {
                    sri.addAll(new SipRecipientInfoReader(header).readValues());
                    sipRecipientsInfoAdded = true;
                } catch (Exception e) {
                    Context.getCurrentLogger().info(e.getMessage());
                }
            }

            sipRecipientsInfoAdded = true;
        }

        return sri;
    }

    @Override
    public Subscription getSubscriptionState() {
        if (!subscriptionAdded) {
            String header = (getHeaders() == null) ? null : getHeaders()
                    .getValues(SipConstants.HEADER_SUBSCRIPTION_STATE);

            if (header != null) {
                try {
                    setSubscriptionState(new SubscriptionReader(header)
                            .readValue());
                } catch (Exception e) {
                    Context.getCurrentLogger().info(e.getMessage());
                }
            }

            subscriptionAdded = true;
        }

        return super.getSubscriptionState();
    }

    @Override
    public List<OptionTag> getSupported() {
        List<OptionTag> s = super.getSupported();

        if (!supportedAdded) {
            String header = (getHeaders() == null) ? null : getHeaders()
                    .getValues(SipConstants.HEADER_SUPPORTED);

            if (header != null) {
                try {
                    s.addAll(new OptionTagReader(header).readValues());
                } catch (Exception e) {
                    Context.getCurrentLogger().info(e.getMessage());
                }
            }

            supportedAdded = true;
        }

        return s;
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
    public void setAlertInfo(Address alertInfo) {
        super.setAlertInfo(alertInfo);
        this.alertInfoAdded = true;
    }

    @Override
    public void setAllowedEventTypes(List<EventType> allowedEventTypes) {
        super.setAllowedEventTypes(allowedEventTypes);
        allowedEventTypesAdded = true;
    }

    @Override
    public void setCallerInfo(List<Address> callerInfo) {
        super.setCallerInfo(callerInfo);
        callerInfoAdded = true;
    }

    @Override
    public void setChallengeResponse(ChallengeResponse response) {
        super.setChallengeResponse(response);
        this.securityAdded = true;
    }

    @Override
    public void setContacts(List<ContactInfo> contact) {
        super.setContacts(contact);
        this.contactAdded = true;
    }

    @Override
    public void setEvent(Event event) {
        super.setEvent(event);
        eventAdded = true;
    }

    /**
     * Put the headers in the request's attributes map.
     * 
     * @param headers
     */
    public void setHeaders(Series<Header> headers) {
        getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS, headers);

        // Parse the protocol string
        if (protocol != null) {
            int slashIndex = protocol.indexOf('/');

            if (slashIndex != -1) {
                protocol = protocol.substring(slashIndex + 1);
            } else {
                protocol = null;
            }
        }

        // Set the protocol used for this request
        Protocol serverProtocol = getConnection().getHelper().getHelped()
                .getProtocols().get(0);
        setProtocol(new Protocol(serverProtocol.getSchemeName(),
                serverProtocol.getName(), serverProtocol.getDescription(),
                serverProtocol.getDefaultPort(),
                serverProtocol.isConfidential(), protocol));

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
                    setResourceRef(new Reference(getHostRef().toString() + "/"
                            + resourceUri));
                } else {
                    setResourceRef(new Reference(getHostRef().toString()
                            + resourceUri));
                }
            }

            setOriginalRef(ReferenceUtils.getOriginalRef(getResourceRef(), headers));
        }

        // Set the request date
        String dateHeader = (getHeaders() == null) ? null : getHeaders()
                .getFirstValue(HeaderConstants.HEADER_DATE, true);
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
                .getFirstValue(HeaderConstants.HEADER_MAX_FORWARDS, true);
        if (maxForwardsHeader != null) {
            try {
                setMaxForwards(Integer.parseInt(maxForwardsHeader));
            } catch (NumberFormatException nfe) {
                Context.getCurrentLogger().info(
                        "Unable to parse the Max-Forwards header: "
                                + maxForwardsHeader);
            }
        }

        // Set the "callId" property
        String callIdHeader = (getHeaders() == null) ? null : getHeaders()
                .getFirstValue(SipConstants.HEADER_CALL_ID, true);
        if (callIdHeader != null) {
            setCallId(callIdHeader);
        }

        // Set the "callSeq" property
        String callSeqHeader = (getHeaders() == null) ? null : getHeaders()
                .getFirstValue(SipConstants.HEADER_CALL_SEQ, true);
        if (callSeqHeader != null) {
            setCommandSequence(callSeqHeader);
        }

        // Set the "to" property
        String toHeader = (getHeaders() == null) ? null : getHeaders()
                .getFirstValue(SipConstants.HEADER_TO, true);
        if (toHeader != null) {
            try {
                setTo(new AddressReader(toHeader).readValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Set the "from" property
        String fromHeader = (getHeaders() == null) ? null : getHeaders()
                .getFirstValue(HeaderConstants.HEADER_FROM, true);
        if (fromHeader != null) {
            try {
                setFrom(new AddressReader(fromHeader).readValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Set the "mime-version" property
        String mimeVersionHeader = (getHeaders() == null) ? null : getHeaders()
                .getFirstValue(SipConstants.HEADER_MIME_VERSION, true);
        setMimeVersion(mimeVersionHeader);

        // Set the "mime-version" property
        String organizationHeader = (getHeaders() == null) ? null
                : getHeaders().getFirstValue(SipConstants.HEADER_ORGANIZATION,
                        true);
        setOrganization(organizationHeader);

        String subjectHeader = (getHeaders() == null) ? null : getHeaders()
                .getFirstValue(SipConstants.HEADER_SUBJECT, true);
        setSubject(subjectHeader);
    }

    @Override
    public void setInReplyTo(List<String> inReplyTo) {
        super.setInReplyTo(inReplyTo);
        this.inReplyToAdded = true;
    }

    @Override
    public void setPriority(Priority priority) {
        super.setPriority(priority);
        this.priorityAdded = true;
    }

    @Override
    public void setProxyChallengeResponse(ChallengeResponse response) {
        super.setProxyChallengeResponse(response);
        this.proxySecurityAdded = true;
    }

    @Override
    public void setProxyRequires(List<OptionTag> proxyRequires) {
        super.setProxyRequires(proxyRequires);
        this.proxyRequiresAdded = true;
    }

    @Override
    public void setRecipientsInfo(List<RecipientInfo> recipientsInfo) {
        super.setRecipientsInfo(recipientsInfo);
        this.recipientsInfoAdded = true;
    }

    @Override
    public void setRecordedRoutes(List<Address> recordedRoutes) {
        super.setRecordedRoutes(recordedRoutes);
        this.recordedRoutesAdded = true;
    }

    @Override
    public void setReferTo(Address referTo) {
        super.setReferTo(referTo);
        this.referToAdded = true;
    }

    @Override
    public void setReplyTo(Address replyTo) {
        super.setReplyTo(replyTo);
        this.replyToAdded = true;
    }

    @Override
    public void setRequires(List<OptionTag> requires) {
        super.setRequires(requires);
        this.requiresAdded = true;
    }

    @Override
    public void setRoutes(List<Address> routes) {
        super.setRoutes(routes);
        this.routesAdded = true;
    }

    @Override
    public void setSipIfMatch(Tag sipIfMatch) {
        super.setSipIfMatch(sipIfMatch);
        this.sipIfMatchAdded = true;
    }

    @Override
    public void setSipRecipientsInfo(List<SipRecipientInfo> recipientsInfo) {
        super.setSipRecipientsInfo(recipientsInfo);
        this.recipientsInfoAdded = true;
    }

    @Override
    public void setSubscriptionState(Subscription subscription) {
        super.setSubscriptionState(subscription);
        this.subscriptionAdded = true;
    }

    @Override
    public void setSupported(List<OptionTag> supported) {
        super.setSupported(supported);
        this.supportedAdded = true;
    }

    @Override
    public void setWarnings(List<Warning> warnings) {
        super.setWarnings(warnings);
        this.warningsAdded = true;
    }

}
