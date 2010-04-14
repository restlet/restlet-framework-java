/**
 * Copyright 2005-2010 Noelios Technologies.
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

package org.restlet.ext.sip;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Tag;
import org.restlet.engine.http.connector.ConnectedRequest;
import org.restlet.engine.http.connector.ServerConnection;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.engine.http.header.HeaderReader;
import org.restlet.ext.sip.internal.AddressReader;
import org.restlet.ext.sip.internal.ContactInfoReader;
import org.restlet.ext.sip.internal.EventTypeReader;
import org.restlet.ext.sip.internal.OptionTagReader;
import org.restlet.ext.sip.internal.SipRecipientInfoReader;
import org.restlet.ext.sip.internal.SubscriptionStateReader;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

/**
 * SIP specific request.
 * 
 * @author Jerome Louvel
 */
public class SipRequest extends ConnectedRequest {

    /** Alternative ring tone for the UAS. */
    private volatile Address alertInfo;

    private volatile boolean alertInfoAdded;

    /** The list of supported event packages. */
    private volatile List<EventType> allowedEventTypes;

    private volatile boolean allowedEventTypesAdded;

    /** The description of the current caller. */
    private volatile List<Address> callerInfo;

    private volatile boolean callerInfoAdded;

    /**
     * Identifies a particular invitation or all registrations of a particular
     * client.
     */
    private volatile String callId;

    /** The identifier of the transaction. */
    private volatile String callSequence;

    /** The data about the contact. */
    private volatile List<ContactInfo> contact;

    private volatile boolean contactAdded;

    /** The description of an event notification. */
    private volatile Event event;

    private volatile boolean eventAdded;

    /** The description of the request's initiator. */
    private volatile Address from;

    /** The list of references to call-ids. */
    private volatile List<String> inReplyTo;

    private volatile boolean inReplyToAdded;

    /** The version of the MIME protocol used to construct the message. */
    private volatile String mimeVersion;

    /**
     * The name of the organization to which the SIP element issuing the message
     * belongs.
     */
    private volatile String organization;

    /** The urgency of the request as perceived by the client. */
    private volatile Priority priority;

    private volatile boolean priorityAdded;

    /** The proxy-sensitive features that the proxy must support. */
    private volatile List<OptionTag> proxyRequires;

    private volatile boolean proxyRequiresAdded;

    /** The intermediary recipients information. */
    private volatile List<SipRecipientInfo> recipientsInfo;

    private volatile boolean recipientsInfoAdded;

    /**
     * The list of routes completed by proxies to force future requests to go
     * through the proxy.
     */
    private volatile List<Address> recordedRoutes;

    private volatile boolean recordedRoutesAdded;

    /**
     * The reference that the recipient of a {@link SipMethod#REFER} method
     * should contact.
     */
    private volatile Address referTo;

    private volatile boolean referToAdded;

    /**
     * A logical return URI that may be different from the
     * {@link SipRequest#from} attribute.
     */
    private volatile Address replyTo;

    private volatile boolean replyToAdded;

    /** The sensitive features that the server must support. */
    private volatile List<OptionTag> requires;

    private volatile boolean requiresAdded;

    /** The set of proxies used to force routing for a request. */
    private volatile List<Address> routes;

    private volatile boolean routesAdded;

    /** Identifies the specific event state that the request is refreshing. */
    private volatile Tag sipIfMatch;

    private volatile boolean sipIfMatchAdded;

    /** The subject of the call. */
    private volatile String subject;

    /** The state of the subscription. */
    private volatile SubscriptionState subscriptionState;

    private volatile boolean subscriptionStateAdded;

    /** The extensions supported by the UAC. */
    private volatile List<OptionTag> supported;

    private volatile boolean supportedAdded;

    /** The logical recipient of the request. */
    private volatile Address to;

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
    public SipRequest(Context context, ServerConnection connection,
            Method method, String resourceUri, String version,
            Series<Parameter> headers, Representation entity,
            boolean confidential, Principal userPrincipal) {
        super(context, connection, method, resourceUri, version, headers,
                entity, confidential, userPrincipal);

        // Set the "callId" property
        String callIdHeader = (getHeaders() == null) ? null : getHeaders()
                .getFirstValue(SipConstants.HEADER_CALL_ID);
        if (callIdHeader != null) {
            setCallId(callIdHeader);
        }

        // Set the "callSeq" property
        String callSeqHeader = (getHeaders() == null) ? null : getHeaders()
                .getFirstValue(SipConstants.HEADER_CALL_SEQ);
        if (callSeqHeader != null) {
            setCallSequence(callSeqHeader);
        }

        // Set the "to" property
        String toHeader = (getHeaders() == null) ? null : getHeaders()
                .getFirstValue(SipConstants.HEADER_TO);
        if (toHeader != null) {
            try {
                setTo(new AddressReader(toHeader).readValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Set the "from" property
        String fromHeader = (getHeaders() == null) ? null : getHeaders()
                .getFirstValue(HeaderConstants.HEADER_FROM);
        if (fromHeader != null) {
            try {
                setFrom(new AddressReader(fromHeader).readValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Set the "mime-version" property
        String mimeVersionHeader = (getHeaders() == null) ? null : getHeaders()
                .getFirstValue(SipConstants.HEADER_MIME_VERSION);
        setMimeVersion(mimeVersionHeader);

        // Set the "mime-version" property
        String organizationHeader = (getHeaders() == null) ? null
                : getHeaders().getFirstValue(SipConstants.HEADER_ORGANIZATION);
        setOrganization(organizationHeader);

        String subjectHeader = (getHeaders() == null) ? null : getHeaders()
                .getFirstValue(SipConstants.HEADER_SUBJECT);
        setSubject(subjectHeader);
    }

    /**
     * Copy constructor.
     * 
     * @param request
     *            The request to copy.
     */
    public SipRequest(SipRequest request) {
        super(request);
        this.alertInfo = request.getAlertInfo();
        this.allowedEventTypes = request.getAllowedEventTypes();
        this.callerInfo = request.getCallerInfo();
        this.callId = request.getCallId();
        this.callSequence = request.getCallSequence();
        this.contact = request.getContact();
        this.event = request.getEvent();
        this.from = request.getFrom();
        this.inReplyTo = request.getInReplyTo();
        this.mimeVersion = request.getMimeVersion();
        this.organization = request.getOrganization();
        this.priority = request.getPriority();
        this.proxyRequires = request.getProxyRequires();
        this.recipientsInfo = request.getSipRecipientsInfo();
        this.recordedRoutes = request.getRecordedRoutes();
        this.referTo = request.getReferTo();
        this.replyTo = request.getReplyTo();
        this.requires = request.getRequires();
        this.routes = request.getRoutes();
        this.sipIfMatch = request.getSipIfMatch();
        this.subject = request.getSubject();
        this.subscriptionState = request.getSubscriptionState();
        this.supported = request.getSupported();
        this.to = request.getTo();
    }

    /**
     * Returns the alternative ring tone for the UAS.
     * 
     * @return The alternative ring tone for the UAS.
     */
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

        return alertInfo;
    }

    /**
     * Returns the list of supported event packages.
     * 
     * @return The list of supported event packages.
     */
    public List<EventType> getAllowedEventTypes() {
        // Lazy initialization with double-check.
        List<EventType> aet = this.allowedEventTypes;
        if (aet == null) {
            synchronized (this) {
                aet = this.allowedEventTypes;
                if (aet == null) {
                    this.allowedEventTypes = aet = new CopyOnWriteArrayList<EventType>();
                }
            }
        }
        if (!allowedEventTypesAdded) {
            String header = (getHeaders() == null) ? null : getHeaders()
                    .getValues(SipConstants.HEADER_ALLOW_EVENTS);
            if (header != null) {
                try {
                    this.allowedEventTypes.addAll(new EventTypeReader(header)
                            .readValues());
                    allowedEventTypesAdded = true;
                } catch (Exception e) {
                    Context.getCurrentLogger().info(e.getMessage());
                }
            }
            allowedEventTypesAdded = true;
        }

        return aet;
    }

    /**
     * Returns the description of the current caller.
     * 
     * @return The description of the current caller.
     */
    public List<Address> getCallerInfo() {
        // Lazy initialization with double-check.
        List<Address> ci = this.callerInfo;
        if (ci == null) {
            synchronized (this) {
                ci = this.callerInfo;
                if (ci == null) {
                    this.callerInfo = ci = new CopyOnWriteArrayList<Address>();
                }
            }
        }
        if (!callerInfoAdded) {
            String header = (getHeaders() == null) ? null : getHeaders()
                    .getValues(SipConstants.HEADER_CALL_INFO);
            if (header != null) {
                try {
                    this.callerInfo.addAll(new AddressReader(header)
                            .readValues());
                } catch (Exception e) {
                    Context.getCurrentLogger().info(e.getMessage());
                }
            }
            callerInfoAdded = true;
        }

        return ci;
    }

    /**
     * Returns the identifier of the call.
     * 
     * @return The identifier of the call.
     */
    public String getCallId() {
        return callId;
    }

    /**
     * Returns the identifier of a transaction.
     * 
     * @return The identifier of the transaction.
     */
    public String getCallSequence() {
        return callSequence;
    }

    /**
     * Returns the data about the contact.
     * 
     * @return The data about the contact.
     */
    public List<ContactInfo> getContact() {
        // Lazy initialization with double-check.
        List<ContactInfo> c = this.contact;
        if (c == null) {
            synchronized (this) {
                c = this.contact;
                if (c == null) {
                    this.contact = c = new CopyOnWriteArrayList<ContactInfo>();
                }
            }
        }
        if (!contactAdded) {
            String header = (getHeaders() == null) ? null : getHeaders()
                    .getValues(SipConstants.HEADER_CONTACT);
            if (header != null) {
                try {
                    this.contact.addAll(new ContactInfoReader(header)
                            .readValues());
                } catch (Exception e) {
                    Context.getCurrentLogger().info(e.getMessage());
                }
            }
            contactAdded = true;
        }

        return c;
    }

    /**
     * Returns the description of an event notification.
     * 
     * @return The description of an event notification.
     */
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

        return event;
    }

    /**
     * Returns the description of the request's initiator.
     * 
     * @return The description of the request's initiator.
     */
    public Address getFrom() {
        return from;
    }

    /**
     * Returns The list of references to call-ids.
     * 
     * @return The list of references to call-ids.
     */
    public List<String> getInReplyTo() {
        // Lazy initialization with double-check.
        List<String> irt = this.inReplyTo;
        if (irt == null) {
            synchronized (this) {
                irt = this.inReplyTo;
                if (irt == null) {
                    this.inReplyTo = irt = new CopyOnWriteArrayList<String>();
                }
            }
        }

        if (!inReplyToAdded) {
            String header = (getHeaders() == null) ? null : getHeaders()
                    .getValues(SipConstants.HEADER_ALLOW_EVENTS);
            if (header != null) {
                try {
                    this.inReplyTo.addAll(new HeaderReader<String>(header)
                            .readValues());
                } catch (Exception e) {
                    Context.getCurrentLogger().info(e.getMessage());
                }
            }
            inReplyToAdded = true;
        }

        return irt;
    }

    /**
     * Returns the version of the MIME protocol used to construct the message.
     * 
     * @return The version of the MIME protocol used to construct the message.
     */
    public String getMimeVersion() {
        return mimeVersion;
    }

    /**
     * Returns the name of the organization to which the SIP element issuing the
     * message belongs.
     * 
     * @return The name of the organization to which the SIP element issuing the
     *         message belongs.
     */
    public String getOrganization() {
        return organization;
    }

    /**
     * Returns the urgency of the request as perceived by the client.
     * 
     * @return The urgency of the request as perceived by the client.
     */
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

        return priority;
    }

    /**
     * Returns the proxy-sensitive features that the proxy must support.
     * 
     * @return The proxy-sensitive features that the proxy must support.
     */
    public List<OptionTag> getProxyRequires() {
        // Lazy initialization with double-check.
        List<OptionTag> pr = this.proxyRequires;
        if (pr == null) {
            synchronized (this) {
                pr = this.proxyRequires;
                if (pr == null) {
                    this.proxyRequires = pr = new CopyOnWriteArrayList<OptionTag>();
                }
            }
        }
        if (!proxyRequiresAdded) {
            String header = (getHeaders() == null) ? null : getHeaders()
                    .getValues(SipConstants.HEADER_PROXY_REQUIRE);
            if (header != null) {
                try {
                    this.proxyRequires.addAll(new OptionTagReader(header)
                            .readValues());
                } catch (Exception e) {
                    Context.getCurrentLogger().info(e.getMessage());
                }
            }
            proxyRequiresAdded = true;
        }

        return pr;
    }

    /**
     * Returns the list of routes completed by proxies to force future requests
     * to go through the proxy.
     * 
     * @return The list of routes completed by proxies to force future requests
     *         to go through the proxy.
     */
    public List<Address> getRecordedRoutes() {
        // Lazy initialization with double-check.
        List<Address> rr = this.recordedRoutes;
        if (rr == null) {
            synchronized (this) {
                rr = this.recordedRoutes;
                if (rr == null) {
                    this.recordedRoutes = rr = new CopyOnWriteArrayList<Address>();
                }
            }
        }
        if (!recordedRoutesAdded) {
            String header = (getHeaders() == null) ? null : getHeaders()
                    .getValues(SipConstants.HEADER_RECORD_ROUTE);
            if (header != null) {
                try {
                    this.recordedRoutes.addAll(new AddressReader(header)
                            .readValues());
                } catch (Exception e) {
                    Context.getCurrentLogger().info(e.getMessage());
                }
            }
            recordedRoutesAdded = true;
        }
        return rr;
    }

    /**
     * Returns the reference that the recipient of a {@link SipMethod#REFER}
     * method should contact.
     * 
     * @return The reference that the recipient of a {@link SipMethod#REFER}
     *         method should contact.
     */
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

        return referTo;
    }

    /**
     * Returns a logical return URI.
     * 
     * @return A logical return URI.
     */
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

        return replyTo;
    }

    /**
     * Returns the sensitive features that the server must support.
     * 
     * @return The sensitive features that the server must support.
     */
    public List<OptionTag> getRequires() {
        // Lazy initialization with double-check.
        List<OptionTag> r = this.requires;
        if (r == null) {
            synchronized (this) {
                r = this.requires;
                if (r == null) {
                    this.requires = r = new CopyOnWriteArrayList<OptionTag>();
                }
            }
        }
        if (!requiresAdded) {
            String header = (getHeaders() == null) ? null : getHeaders()
                    .getValues(SipConstants.HEADER_REQUIRE);
            if (header != null) {
                try {
                    this.requires.addAll(new OptionTagReader(header)
                            .readValues());
                } catch (Exception e) {
                    Context.getCurrentLogger().info(e.getMessage());
                }
            }
            requiresAdded = true;
        }

        return r;
    }

    /**
     * Returns the set of proxies used to force routing for a request.
     * 
     * @return The set of proxies used to force routing for a request.
     */
    public List<Address> getRoutes() {
        // Lazy initialization with double-check.
        List<Address> r = this.routes;
        if (r == null) {
            synchronized (this) {
                r = this.routes;
                if (r == null) {
                    this.routes = r = new CopyOnWriteArrayList<Address>();
                }
            }
        }
        if (!routesAdded) {
            String header = (getHeaders() == null) ? null : getHeaders()
                    .getValues(SipConstants.HEADER_ROUTE);
            if (header != null) {
                try {
                    this.routes.addAll(new AddressReader(header).readValues());
                } catch (Exception e) {
                    Context.getCurrentLogger().info(e.getMessage());
                }
            }
            routesAdded = true;
        }

        return r;
    }

    /**
     * Returns the identifier of the specific event state that the request is
     * refreshing.
     * 
     * @return The identifier of the specific event state that the request is
     *         refreshing.
     */
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

        return sipIfMatch;
    }

    /**
     * Returns the intermediary recipients information.
     * 
     * @return The intermediary recipients information.
     */
    public List<SipRecipientInfo> getSipRecipientsInfo() {
        // Lazy initialization with double-check.
        List<SipRecipientInfo> sri = this.recipientsInfo;
        if (sri == null) {
            synchronized (this) {
                sri = this.recipientsInfo;
                if (sri == null) {
                    this.recipientsInfo = sri = new CopyOnWriteArrayList<SipRecipientInfo>();
                }
            }
        }
        if (!recipientsInfoAdded) {
            String header = (getHeaders() == null) ? null : getHeaders()
                    .getValues(HeaderConstants.HEADER_VIA);
            if (header != null) {
                try {
                    this.recipientsInfo.addAll(new SipRecipientInfoReader(
                            header).readValues());
                    recipientsInfoAdded = true;
                } catch (Exception e) {
                    Context.getCurrentLogger().info(e.getMessage());
                }
            }
            recipientsInfoAdded = true;
        }

        return sri;
    }

    /**
     * Returns the subject of the call.
     * 
     * @return The subject of the call.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Returns the state of the subscription.
     * 
     * @return The state of the subscription.
     */
    public SubscriptionState getSubscriptionState() {
        if (!subscriptionStateAdded) {
            String header = (getHeaders() == null) ? null : getHeaders()
                    .getValues(SipConstants.HEADER_SUBSCRIPTION_STATE);
            if (header != null) {
                try {
                    setSubscriptionState(new SubscriptionStateReader(header)
                            .readValue());
                } catch (Exception e) {
                    Context.getCurrentLogger().info(e.getMessage());
                }
            }
            subscriptionStateAdded = true;
        }

        return subscriptionState;
    }

    /**
     * Returns the extensions supported by the UAC.
     * 
     * @return The extensions supported by the UAC.
     */
    public List<OptionTag> getSupported() {
        // Lazy initialization with double-check.
        List<OptionTag> s = this.supported;
        if (s == null) {
            synchronized (this) {
                s = this.supported;
                if (s == null) {
                    this.supported = s = new CopyOnWriteArrayList<OptionTag>();
                }
            }
        }
        if (!supportedAdded) {
            String header = (getHeaders() == null) ? null : getHeaders()
                    .getValues(SipConstants.HEADER_SUPPORTED);
            if (header != null) {
                try {
                    this.supported.addAll(new OptionTagReader(header)
                            .readValues());
                } catch (Exception e) {
                    Context.getCurrentLogger().info(e.getMessage());
                }
            }
            supportedAdded = true;
        }

        return s;
    }

    /**
     * Returns the logical recipient of the request.
     * 
     * @return The logical recipient of the request.
     */
    public Address getTo() {
        return to;
    }

    /**
     * Sets the alternative ring tone for the UAS.
     * 
     * @param alertInfo
     *            The alternative ring tone for the UAS.
     */
    public void setAlertInfo(Address alertInfo) {
        this.alertInfo = alertInfo;
        this.alertInfoAdded = true;
    }

    /**
     * Sets the list of supported event packages.
     * 
     * @param allowedEventTypes
     *            The list of supported event packages.
     */
    public void setAllowedEventTypes(List<EventType> allowedEventTypes) {
        this.allowedEventTypes = allowedEventTypes;
        allowedEventTypesAdded = true;
    }

    /**
     * Sets the description of the current caller.
     * 
     * @param callerInfo
     *            The description of the current caller.
     */
    public void setCallerInfo(List<Address> callerInfo) {
        this.callerInfo = callerInfo;
        callerInfoAdded = true;
    }

    /**
     * Sets the identifier of the call.
     * 
     * @param callId
     *            The identifier of the call.
     */
    public void setCallId(String callId) {
        this.callId = callId;
    }

    /**
     * Sets the identifier of the transaction.
     * 
     * @param callSequence
     *            The identifier of the transaction.
     */
    public void setCallSequence(String callSequence) {
        this.callSequence = callSequence;
    }

    /**
     * Sets the data about the contact.
     * 
     * @param contact
     *            The data about the contact.
     */
    public void setContact(List<ContactInfo> contact) {
        this.contact = contact;
        this.contactAdded = true;
    }

    /**
     * Sets the description of an event notification.
     * 
     * @param event
     *            The description of an event notification.
     */
    public void setEvent(Event event) {
        this.event = event;
        eventAdded = true;
    }

    /**
     * Sets the description of the request's initiator.
     * 
     * @param from
     *            The description of the request's initiator.
     */
    public void setFrom(Address from) {
        this.from = from;
    }

    public void setInReplyTo(List<String> inReplyTo) {
        this.inReplyTo = inReplyTo;
        this.inReplyToAdded = true;
    }

    /**
     * Sets the version of the MIME protocol used to construct the message.
     * 
     * @param mimeVersion
     *            The version of the MIME protocol used to construct the
     *            message.
     */
    public void setMimeVersion(String mimeVersion) {
        this.mimeVersion = mimeVersion;
    }

    /**
     * Sets the name of the organization to which the SIP element issuing the
     * message belongs.
     * 
     * @param organization
     *            The name of the organization to which the SIP element issuing
     *            the message belongs.
     */
    public void setOrganization(String organization) {
        this.organization = organization;
    }

    /**
     * Sets the urgency of the request as perceived by the client.
     * 
     * @param priority
     *            The urgency of the request as perceived by the client.
     */
    public void setPriority(Priority priority) {
        this.priority = priority;
        this.priorityAdded = true;
    }

    /**
     * Sets the proxy-sensitive features that the proxy must support.
     * 
     * @param proxyRequires
     *            The proxy-sensitive features that the proxy must support.
     */
    public void setProxyRequires(List<OptionTag> proxyRequires) {
        this.proxyRequires = proxyRequires;
        this.proxyRequiresAdded = true;
    }

    /**
     * Sets the list of routes completed by proxies to force future requests to
     * go through the proxy.
     * 
     * @param recordedRoutes
     *            The list of routes completed by proxies to force future
     *            requests to go through the proxy.
     */
    public void setRecordedRoutes(List<Address> recordedRoutes) {
        this.recordedRoutes = recordedRoutes;
        this.recordedRoutesAdded = true;
    }

    /**
     * Sets the reference that the recipient of a {@link SipMethod#REFER} method
     * should contact.
     * 
     * @param referTo
     *            The reference that the recipient of a {@link SipMethod#REFER}
     *            method should contact.
     */
    public void setReferTo(Address referTo) {
        this.referTo = referTo;
        this.referToAdded = true;
    }

    /**
     * Sets a logical return URI.
     * 
     * @param replyTo
     *            A logical return URI.
     */
    public void setReplyTo(Address replyTo) {
        this.replyTo = replyTo;
        this.replyToAdded = true;
    }

    /**
     * Sets the sensitive features that the server must support.
     * 
     * @param requires
     *            The sensitive features that the server must support.
     */
    public void setRequires(List<OptionTag> requires) {
        this.requires = requires;
        this.requiresAdded = true;
    }

    /**
     * Sets the set of proxies used to force routing for a request.
     * 
     * @param routes
     *            The set of proxies used to force routing for a request.
     */
    public void setRoutes(List<Address> routes) {
        this.routes = routes;
        this.routesAdded = true;
    }

    /**
     * Sets the identifier of the specific event state that the request is
     * refreshing.
     * 
     * @param sipIfMatch
     *            The identifier of the specific event state that the request is
     *            refreshing.
     */
    public void setSipIfMatch(Tag sipIfMatch) {
        this.sipIfMatch = sipIfMatch;
        this.sipIfMatchAdded = true;
    }

    /**
     * Sets the intermediary recipients information.
     * 
     * @param recipientsInfo
     *            The intermediary recipients information.
     */
    public void setSipRecipientsInfo(List<SipRecipientInfo> recipientsInfo) {
        this.recipientsInfo = recipientsInfo;
        this.recipientsInfoAdded = true;
    }

    /**
     * Sets the subject of the call.
     * 
     * @param subject
     *            the subject of the call.
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Sets the state of the subscription.
     * 
     * @param subscriptionState
     *            The state of the subscription.
     */
    public void setSubscriptionState(SubscriptionState subscriptionState) {
        this.subscriptionState = subscriptionState;
        this.subscriptionStateAdded = true;
    }

    /**
     * Sets the extensions supported by the UAC.
     * 
     * @param supported
     *            The extensions supported by the UAC.
     */
    public void setSupported(List<OptionTag> supported) {
        this.supported = supported;
        this.supportedAdded = true;
    }

    /**
     * Sets the logical recipient of the request.
     * 
     * @param to
     *            The logical recipient of the request.
     */
    public void setTo(Address to) {
        this.to = to;
    }

}
