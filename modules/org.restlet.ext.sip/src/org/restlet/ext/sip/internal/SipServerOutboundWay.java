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

import org.restlet.Response;
import org.restlet.Server;
import org.restlet.engine.connector.Connection;
import org.restlet.engine.connector.ServerOutboundWay;
import org.restlet.engine.header.DateWriter;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.io.IoState;
import org.restlet.ext.sip.SipRecipientInfo;
import org.restlet.ext.sip.SipRequest;
import org.restlet.ext.sip.SipResponse;
import org.restlet.util.Series;

/**
 * SIP server outbound way.
 * 
 * @author Jerome Louvel
 */
public class SipServerOutboundWay extends ServerOutboundWay {

    /**
     * Constructor.
     * 
     * @param connection
     *            The parent connection.
     * @param bufferSize
     *            The byte buffer size.
     */
    public SipServerOutboundWay(Connection<Server> connection, int bufferSize) {
        super(connection, bufferSize);
    }

    @Override
    protected void addResponseHeaders(Series<Header> headers) {
        SipRequest sipRequest = (SipRequest) getMessage().getRequest();
        SipResponse sipResponse = (SipResponse) getMessage();

        if (!sipResponse.getSipRecipientsInfo().isEmpty()) {
            for (SipRecipientInfo recipient : sipResponse
                    .getSipRecipientsInfo()) {
                // Generate one Via header per recipient
                headers.add(HeaderConstants.HEADER_VIA,
                        SipRecipientInfoWriter.write(recipient));
            }
        }

        if (sipRequest.getCallId() != null) {
            headers.add(SipConstants.HEADER_CALL_ID, sipRequest.getCallId());
        }

        if (sipRequest.getCommandSequence() != null) {
            headers.add(SipConstants.HEADER_CALL_SEQ,
                    sipRequest.getCommandSequence());
        }

        if (sipRequest.getFrom() != null) {
            headers.add(HeaderConstants.HEADER_FROM,
                    AddressWriter.write(sipRequest.getFrom()));
        }

        if (sipRequest.getTo() != null) {
            headers.add(SipConstants.HEADER_TO,
                    AddressWriter.write(sipRequest.getTo()));
        }

        if (sipResponse.getAlertInfo() != null) {
            headers.add(SipConstants.HEADER_ALERT_INFO,
                    AddressWriter.write(sipResponse.getAlertInfo()));
        }

        if (!sipResponse.getAllowedEventTypes().isEmpty()) {
            headers.add(SipConstants.HEADER_ALLOW_EVENTS,
                    EventTypeWriter.write(sipResponse.getAllowedEventTypes()));
        }

        if (!sipResponse.getCalleeInfo().isEmpty()) {
            headers.add(SipConstants.HEADER_CALL_INFO,
                    AddressWriter.write(sipResponse.getCalleeInfo()));
        }

        if (!sipResponse.getContacts().isEmpty()) {
            headers.add(SipConstants.HEADER_CONTACT,
                    ContactInfoWriter.write(sipResponse.getContacts()));
        }

        if (sipResponse.getErrorInfo() != null) {
            headers.add(SipConstants.HEADER_ERROR_INFO,
                    AddressWriter.write(sipResponse.getErrorInfo()));
        }

        if (sipResponse.getEvent() != null) {
            headers.add(SipConstants.HEADER_EVENT,
                    EventWriter.write(sipResponse.getEvent()));
        }

        if (sipResponse.getMimeVersion() != null) {
            headers.add(SipConstants.HEADER_MIME_VERSION,
                    sipResponse.getMimeVersion());
        }

        if (sipResponse.getMinExpires() != null) {
            headers.add(SipConstants.HEADER_MIN_EXPIRES,
                    sipResponse.getMinExpires());
        }

        if (sipResponse.getOrganization() != null) {
            headers.add(SipConstants.HEADER_ORGANIZATION,
                    sipResponse.getOrganization());
        }

        if (!sipResponse.getRecordedRoutes().isEmpty()) {
            headers.add(SipConstants.HEADER_RECORD_ROUTE,
                    AddressWriter.write(sipResponse.getRecordedRoutes()));
        }

        if (sipResponse.getReplyTo() != null) {
            headers.add(SipConstants.HEADER_REPLY_TO,
                    AddressWriter.write(sipResponse.getReplyTo()));
        }

        if (sipResponse.getRetryAfter() != null) {
            headers.add(SipConstants.HEADER_RETRY_AFTER,
                    DateWriter.write(sipResponse.getRetryAfter()));
        }

        if (sipResponse.getSipTag() != null) {
            headers.add(SipConstants.HEADER_SIP_ETAG, sipResponse.getSipTag()
                    .format());
        }

        if (!sipResponse.getSupported().isEmpty()) {
            headers.add(SipConstants.HEADER_SUPPORTED,
                    OptionTagWriter.write(sipResponse.getSupported()));
        }

        if (!sipResponse.getUnsupported().isEmpty()) {
            headers.add(SipConstants.HEADER_UNSUPPORTED,
                    OptionTagWriter.write(sipResponse.getUnsupported()));
        }

        super.addResponseHeaders(headers);
    }

    @Override
    protected void handle(Response response) {
        setMessage(response);
        setIoState(IoState.INTEREST);
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && (getMessage() == null);
    }

    @Override
    public void updateState() {
        // Update the IO state if necessary
        if ((getIoState() == IoState.IDLE) && !isEmpty()) {
            setIoState(IoState.INTEREST);
        }

        super.updateState();
    }

}
