/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.sip.internal;

import java.io.IOException;

import org.restlet.Client;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.engine.connector.ClientInboundWay;
import org.restlet.engine.connector.Connection;
import org.restlet.engine.connector.MessageState;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.io.IoState;
import org.restlet.ext.sip.SipRequest;
import org.restlet.ext.sip.SipResponse;
import org.restlet.ext.sip.SipStatus;
import org.restlet.util.Series;

/**
 * SIP client inbound way.
 * 
 * @author Thierry Boileau
 */
public class SipClientInboundWay extends ClientInboundWay {
    /**
     * Constructor.
     * 
     * @param connection
     *            The parent connection.
     * @param bufferSize
     *            The byte buffer size.
     * @throws IOException
     */
    public SipClientInboundWay(Connection<Client> connection, int bufferSize) {
        super(connection, bufferSize);
    }

    @Override
    protected void copyResponseTransportHeaders(Series<Header> headers,
            Response response) {
        SipResponse sr = (SipResponse) response;

        // Set the "alertInfo" header
        String header = (getHeaders() == null) ? null : getHeaders().getValues(
                SipConstants.HEADER_ALERT_INFO);

        if (header != null) {
            try {
                sr.setAlertInfo(new AddressReader(header).readValue());
            } catch (Exception e) {
                getLogger().info(e.getMessage());
            }
        }

        // Set "allowedEventTypes" property
        header = (getHeaders() == null) ? null : getHeaders().getValues(
                SipConstants.HEADER_ALLOW_EVENTS);

        if (header != null) {
            try {
                sr.getAllowedEventTypes().addAll(
                        new EventTypeReader(header).readValues());
            } catch (Exception e) {
                getLogger().info(e.getMessage());
            }
        }

        // Set the "callerInfo" property
        header = (getHeaders() == null) ? null : getHeaders().getValues(
                SipConstants.HEADER_CALL_INFO);

        if (header != null) {
            try {
                sr.getCalleeInfo().addAll(
                        new AddressReader(header).readValues());
            } catch (Exception e) {
                getLogger().info(e.getMessage());
            }
        }

        // Set the "callId" property
        String callIdHeader = (getHeaders() == null) ? null : getHeaders()
                .getFirstValue(SipConstants.HEADER_CALL_ID);
        if (callIdHeader != null) {
            sr.setCallId(callIdHeader);
        }

        // Set the "commandSeq" property
        String commandSeqHeader = (getHeaders() == null) ? null : getHeaders()
                .getFirstValue(SipConstants.HEADER_CALL_SEQ);
        if (commandSeqHeader != null) {
            sr.setCommandSequence(commandSeqHeader);
        }

        // Set the "contacts" property
        header = (getHeaders() == null) ? null : getHeaders().getValues(
                SipConstants.HEADER_CONTACT);

        if (header != null) {
            try {
                sr.getContacts().addAll(
                        new ContactInfoReader(header).readValues());
            } catch (Exception e) {
                getLogger().info(e.getMessage());
            }
        }

        // Set the "errorInfo" header
        header = (getHeaders() == null) ? null : getHeaders().getValues(
                SipConstants.HEADER_ERROR_INFO);

        if (header != null) {
            try {
                sr.setErrorInfo(new AddressReader(header).readValue());
            } catch (Exception e) {
                getLogger().info(e.getMessage());
            }
        }

        // Set the "event" property
        header = (getHeaders() == null) ? null : getHeaders().getValues(
                SipConstants.HEADER_EVENT);

        if (header != null) {
            try {
                sr.setEvent(new EventReader(header).readValue());
            } catch (Exception e) {
                getLogger().info(e.getMessage());
            }
        }

        // Set the "from" property
        String fromHeader = (getHeaders() == null) ? null : getHeaders()
                .getFirstValue(HeaderConstants.HEADER_FROM);
        if (fromHeader != null) {
            try {
                sr.setFrom(new AddressReader(fromHeader).readValue());
            } catch (IOException e) {
                getLogger().info(e.getMessage());
            }
        }

        // Set the "minExpires" property
        header = (getHeaders() == null) ? null : getHeaders().getFirstValue(
                SipConstants.HEADER_MIN_EXPIRES);
        sr.setMinExpires(header);

        // Set the "mime-version" property
        header = (getHeaders() == null) ? null : getHeaders().getFirstValue(
                SipConstants.HEADER_MIME_VERSION);
        sr.setMimeVersion(header);

        // Set the "mime-version" property
        header = (getHeaders() == null) ? null : getHeaders().getFirstValue(
                SipConstants.HEADER_MIME_VERSION);
        sr.setMimeVersion(header);

        // Set the "organization" property
        header = (getHeaders() == null) ? null : getHeaders().getFirstValue(
                SipConstants.HEADER_MIME_VERSION);
        sr.setOrganization(header);

        // Set the "recordedRoute" property
        header = (getHeaders() == null) ? null : getHeaders().getValues(
                SipConstants.HEADER_RECORD_ROUTE);

        if (header != null) {
            try {
                sr.getRecordedRoutes().addAll(
                        new AddressReader(header).readValues());
            } catch (Exception e) {
                getLogger().info(e.getMessage());
            }
        }

        // Set the "replyTo" property
        header = (getHeaders() == null) ? null : getHeaders().getValues(
                SipConstants.HEADER_REPLY_TO);

        if (header != null) {
            try {
                sr.setReplyTo(new AddressReader(header).readValue());
            } catch (Exception e) {
                getLogger().info(e.getMessage());
            }
        }

        // Set the "sipRecipientsInfo" property
        header = (getHeaders() == null) ? null : getHeaders().getValues(
                HeaderConstants.HEADER_VIA);

        if (header != null) {
            try {
                sr.getSipRecipientsInfo().addAll(
                        new SipRecipientInfoReader(header).readValues());
            } catch (Exception e) {
                getLogger().info(e.getMessage());
            }
        }

        // Set the "sipRetryAfter" property
        header = (getHeaders() == null) ? null : getHeaders().getFirstValue(
                SipConstants.HEADER_RETRY_AFTER);
        if (header != null) {
            try {
                sr.setSipRetryAfter(new AvailabilityReader(header).readValue());
            } catch (IOException e) {
                getLogger().info(e.getMessage());
            }
        }

        // Set the "sipTag" property
        header = (getHeaders() == null) ? null : getHeaders().getFirstValue(
                SipConstants.HEADER_SIP_ETAG);
        if (header != null) {
            sr.setSipTag(Tag.parse(header));
        }

        // Set the "supported" property
        header = (getHeaders() == null) ? null : getHeaders().getValues(
                SipConstants.HEADER_SUPPORTED);

        if (header != null) {
            try {
                sr.getSupported().addAll(
                        new OptionTagReader(header).readValues());
            } catch (Exception e) {
                getLogger().info(e.getMessage());
            }
        }

        // Set the "to" property
        header = (getHeaders() == null) ? null : getHeaders().getFirstValue(
                SipConstants.HEADER_TO);
        if (header != null) {
            try {
                sr.setTo(new AddressReader(header).readValue());
            } catch (IOException e) {
                getLogger().info(e.getMessage());
            }
        }

        // Set the "unsupported" property
        header = (getHeaders() == null) ? null : getHeaders().getValues(
                SipConstants.HEADER_UNSUPPORTED);

        if (header != null) {
            try {
                sr.getUnsupported().addAll(
                        new OptionTagReader(header).readValues());
            } catch (Exception e) {
                getLogger().info(e.getMessage());
            }
        }

        for (Header headerParam : headers) {
            if (headerParam.getName().equalsIgnoreCase(
                    HeaderConstants.HEADER_VIA)) {
                SipRecipientInfoReader.addValues(headerParam,
                        sr.getSipRecipientsInfo());
            }
        }

        // Don't let the parent code handle the VIA header
        // according to HTTP syntax.
        headers.removeAll(HeaderConstants.HEADER_VIA, true);
        super.copyResponseTransportHeaders(headers, response);
    }

    @Override
    protected Response createResponse(Status status) {
        return new SipResponse(null);
    }

    @Override
    protected Status createStatus(int code) {
        return SipStatus.valueOf(code);
    }

    @Override
    public SipClientHelper getHelper() {
        return (SipClientHelper) super.getHelper();
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && (getMessage() == null);
    }

    @Override
    protected void onReceived(Response message) {
        if (message != null) {
            SipRequest request = (SipRequest) getHelper().getRequest(message);

            if (request != null) {
                request.updateLastActivity();
                message.setRequest(request);
            } else {
                getLogger()
                        .fine("Unable to find the transaction associated to a given response");
            }
        }

        super.onReceived(message);
    }

    @Override
    public void updateState() {
        if (getMessageState() == MessageState.IDLE) {
            setMessageState(MessageState.START);
        }

        if (getIoState() == IoState.IDLE) {
            // Read the next response
            setIoState(IoState.INTEREST);
        }

        // Update the registration
        super.updateState();
    }
}
