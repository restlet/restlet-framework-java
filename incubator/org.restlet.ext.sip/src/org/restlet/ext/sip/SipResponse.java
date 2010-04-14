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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Tag;

/**
 * SIP specific response.
 * 
 * @author Jerome Louvel
 */
public class SipResponse extends Response {

    /** Alternative ring tone for the UAC. */
    private volatile Address alertInfo;

    /** The list of supported event packages. */
    private volatile List<EventType> allowedEventTypes;

    /** The description of the current callee. */
    private volatile List<Address> calleeInfo;

    /**
     * Identifies a particular invitation or all registrations of a particular
     * client.
     */
    private volatile String callId;

    /** The identifier of the transaction. */
    private volatile String callSequence;

    /** The data about the contact. */
    private volatile List<ContactInfo> contact;

    /** Pointer to additional information about the error response. */
    private volatile Address errorInfo;

    /** The description of an event notification. */
    private volatile Event event;

    /** The description of the request's initiator. */
    private volatile Address from;

    /** The version of the MIME protocol used to construct the message. */
    private volatile String mimeVersion;

    /** The minimum refresh interval supported for soft-state elements. */
    private volatile String minExpires;

    /**
     * The name of the organization to which the SIP element issuing the message
     * belongs.
     */
    private volatile String organization;

    /** The intermediary recipients information. */
    private volatile List<SipRecipientInfo> recipientsInfo;

    /**
     * The list of routes completed by proxies to force future requests to go
     * through the proxy.
     */
    private volatile List<Address> recordedRoutes;

    /**
     * A logical return URI that may be different from the
     * {@link SipRequest#from} attribute.
     */
    private volatile Address replyTo;

    /**
     * Indicates how long the service is expected to be unavailable to the
     * requesting client.
     */
    private volatile Availability retryAfter;

    /** The tag of the returned representation. */
    private volatile Tag sipTag;

    /** The extensions supported by the UAS. */
    private volatile List<OptionTag> supported;

    /** The logical recipient of the request. */
    private volatile Address to;

    /** The extensions not supported by the UAS. */
    private volatile List<OptionTag> unsupported;

    /**
     * Constructor.
     * 
     * @param request
     *            The associated request.
     */
    public SipResponse(Request request) {
        super(request);
    }

    /**
     * Returns the alternative ring tone for the UAC.
     * 
     * @return The alternative ring tone for the UAC.
     */
    public Address getAlertInfo() {
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
        return aet;
    }

    /**
     * Returns the description of the current callee.
     * 
     * @return The description of the current callee.
     */
    public List<Address> getCalleeInfo() {
        // Lazy initialization with double-check.
        List<Address> ci = this.calleeInfo;
        if (ci == null) {
            synchronized (this) {
                ci = this.calleeInfo;
                if (ci == null) {
                    this.calleeInfo = ci = new CopyOnWriteArrayList<Address>();
                }
            }
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
        return c;
    }

    /**
     * Returns the pointer to additional information about the error response.
     * 
     * @return The pointer to additional information about the error response.
     */
    public Address getErrorInfo() {
        return errorInfo;
    }

    /**
     * Returns the description of an event notification.
     * 
     * @return The description of an event notification.
     */
    public Event getEvent() {
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
     * Returns the version of the MIME protocol used to construct the message.
     * 
     * @return The version of the MIME protocol used to construct the message.
     */
    public String getMimeVersion() {
        return mimeVersion;
    }

    /**
     * Returns the minimum refresh interval supported for soft-state elements.
     * 
     * @return The minimum refresh interval supported for soft-state elements.
     */
    public String getMinExpires() {
        return minExpires;
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
        return rr;
    }

    /**
     * Returns a logical return URI.
     * 
     * @return A logical return URI.
     */
    public Address getReplyTo() {
        return replyTo;
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

        return sri;
    }

    public Availability getSipRetryAfter() {
        return retryAfter;
    }

    /**
     * Returns the tag of the returned representation.
     * 
     * @return The tag of the returned representation.
     */
    public Tag getSipTag() {
        return sipTag;
    }

    /**
     * Returns the extensions supported by the UAS.
     * 
     * @return The extensions supported by the UAS.
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
     * Returns the extensions not supported by the UAS.
     * 
     * @return The extensions not supported by the UAS.
     */
    public List<OptionTag> getUnsupported() {
        // Lazy initialization with double-check.
        List<OptionTag> u = this.unsupported;
        if (u == null) {
            synchronized (this) {
                u = this.unsupported;
                if (u == null) {
                    this.unsupported = u = new CopyOnWriteArrayList<OptionTag>();
                }
            }
        }
        return u;
    }

    /**
     * Sets the alternative ring tone for the UAC.
     * 
     * @param alertInfo
     *            The alternative ring tone for the UAC.
     */
    public void setAlertInfo(Address alertInfo) {
        this.alertInfo = alertInfo;
    }

    /**
     * Sets the list of supported event packages.
     * 
     * @param allowedEventTypes
     *            The list of supported event packages.
     */
    public void setAllowedEventTypes(List<EventType> allowedEventTypes) {
        this.allowedEventTypes = allowedEventTypes;
    }

    /**
     * Sets the description of the current callee.
     * 
     * @param calleeInfo
     *            The description of the current callee.
     */
    public void setCalleeInfo(List<Address> calleeInfo) {
        this.calleeInfo = calleeInfo;
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
    }

    /**
     * Sets the pointer to additional information about the error response.
     * 
     * @param errorInfo
     *            The pointer to additional information about the error
     *            response.
     */
    public void setErrorInfo(Address errorInfo) {
        this.errorInfo = errorInfo;
    }

    /**
     * Sets the description of an event notification.
     * 
     * @param event
     *            The description of an event notification.
     */
    public void setEvent(Event event) {
        this.event = event;
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
     * Sets the minimum refresh interval supported for soft-state elements.
     * 
     * @param minExpires
     *            The minimum refresh interval supported for soft-state
     *            elements.
     */
    public void setMinExpires(String minExpires) {
        this.minExpires = minExpires;
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
     * Sets the list of routes completed by proxies to force future requests to
     * go through the proxy.
     * 
     * @param recordedRoutes
     *            The list of routes completed by proxies to force future
     *            requests to go through the proxy.
     */
    public void setRecordedRoutes(List<Address> recordedRoutes) {
        this.recordedRoutes = recordedRoutes;
    }

    /**
     * Sets a logical return URI.
     * 
     * @param replyTo
     *            A logical return URI.
     */
    public void setReplyTo(Address replyTo) {
        this.replyTo = replyTo;
    }

    /**
     * Sets the intermediary recipients information.
     * 
     * @param recipientsInfo
     *            The intermediary recipients information.
     */
    public void setSipRecipientsInfo(List<SipRecipientInfo> recipientsInfo) {
        this.recipientsInfo = recipientsInfo;
    }

    public void setSipRetryAfter(Availability retryAfter) {
        this.retryAfter = retryAfter;
    }

    /**
     * Sets the tag of the returned representation.
     * 
     * @param sipTag
     *            The tag of the returned representation.
     */
    public void setSipTag(Tag sipTag) {
        this.sipTag = sipTag;
    }

    /**
     * Sets the extensions supported by the UAS.
     * 
     * @param supported
     *            The extensions supported by the UAS.
     */
    public void setSupported(List<OptionTag> supported) {
        this.supported = supported;
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

    /**
     * Sets the extensions not supported by the UAS.
     * 
     * @param unsupported
     *            The extensions not supported by the UAS.
     */
    public void setUnsupported(List<OptionTag> unsupported) {
        this.unsupported = unsupported;
    }

}
