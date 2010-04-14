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

package org.restlet.ext.sip.internal;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.security.Principal;
import java.util.ArrayList;

import org.restlet.Context;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.Parameter;
import org.restlet.data.RecipientInfo;
import org.restlet.engine.http.connector.BaseHelper;
import org.restlet.engine.http.connector.ConnectedRequest;
import org.restlet.engine.http.connector.ServerConnection;
import org.restlet.engine.http.header.DateWriter;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.ext.sip.SipConstants;
import org.restlet.ext.sip.SipMethod;
import org.restlet.ext.sip.SipRequest;
import org.restlet.ext.sip.SipResponse;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

/**
 * SIP server connection for the SIP connector.
 * 
 * @author Jerome Louvel
 */
public class SipServerConnection extends ServerConnection {

    /**
     * Constructor.
     * 
     * @param helper
     *            The parent helper.
     * @param socket
     *            The underlying BIO socket.
     * @param socketChannel
     *            The underlying NIO socket channel.
     * @throws IOException
     */
    public SipServerConnection(BaseHelper<Server> helper, Socket socket,
            SocketChannel socketChannel) throws IOException {
        super(helper, socket, socketChannel);
    }

    @Override
    protected void addResponseHeaders(Response response,
            Series<Parameter> headers) {
        super.addResponseHeaders(response, headers);

        SipRequest sipRequest = (SipRequest) response.getRequest();
        SipResponse sipResponse = (SipResponse) response;

        if (sipRequest.getCallId() != null) {
            headers.add(SipConstants.HEADER_CALL_ID, sipRequest.getCallId());
        }

        if (sipRequest.getCallSequence() != null) {
            headers.add(SipConstants.HEADER_CALL_SEQ, sipRequest
                    .getCallSequence());
        }

        if (sipRequest.getFrom() != null) {
            headers.add(HeaderConstants.HEADER_FROM, AddressWriter
                    .write(sipRequest.getFrom()));
        }

        if (sipRequest.getTo() != null) {
            headers.add(SipConstants.HEADER_TO, AddressWriter.write(sipRequest
                    .getTo()));
        }
        if (sipResponse.getAlertInfo() != null) {
            headers.add(SipConstants.HEADER_ALERT_INFO, AddressWriter
                    .write(sipResponse.getAlertInfo()));
        }
        if (!sipResponse.getAllowedEventTypes().isEmpty()) {
            headers.add(SipConstants.HEADER_ALLOW_EVENTS, EventTypeWriter
                    .write(sipResponse.getAllowedEventTypes()));
        }
        if (!sipResponse.getCalleeInfo().isEmpty()) {
            headers.add(SipConstants.HEADER_CALL_INFO, AddressWriter
                    .write(sipResponse.getCalleeInfo()));
        }
        if (!sipResponse.getContact().isEmpty()) {
            headers.add(SipConstants.HEADER_CONTACT, ContactInfoWriter
                    .write(sipResponse.getContact()));
        }
        if (sipResponse.getErrorInfo() != null) {
            headers.add(SipConstants.HEADER_ERROR_INFO, AddressWriter
                    .write(sipResponse.getErrorInfo()));
        }
        if (sipResponse.getEvent() != null) {
            headers.add(SipConstants.HEADER_EVENT, EventWriter
                    .write(sipResponse.getEvent()));
        }
        if (sipResponse.getMimeVersion() != null) {
            headers.add(SipConstants.HEADER_MIME_VERSION, sipResponse
                    .getMimeVersion());
        }
        if (sipResponse.getMinExpires() != null) {
            headers.add(SipConstants.HEADER_MIN_EXPIRES, sipResponse
                    .getMinExpires());
        }
        if (sipResponse.getOrganization() != null) {
            headers.add(SipConstants.HEADER_ORGANIZATION, sipResponse
                    .getOrganization());
        }
        if (!sipResponse.getRecordedRoutes().isEmpty()) {
            headers.add(SipConstants.HEADER_RECORD_ROUTE, AddressWriter
                    .write(sipResponse.getRecordedRoutes()));
        }
        if (sipResponse.getReplyTo() != null) {
            headers.add(SipConstants.HEADER_REPLY_TO, AddressWriter
                    .write(sipResponse.getReplyTo()));
        }
        if (sipResponse.getRetryAfter() != null) {
            headers.add(SipConstants.HEADER_RETRY_AFTER, DateWriter
                    .write(sipResponse.getRetryAfter()));
        }
        if (sipResponse.getSipTag() != null) {
            headers.add(SipConstants.HEADER_SIP_ETAG, sipResponse.getSipTag()
                    .format());
        }
        if (!sipResponse.getSupported().isEmpty()) {
            headers.add(SipConstants.HEADER_SUPPORTED, OptionTagWriter
                    .write(sipResponse.getSupported()));
        }
        if (!sipResponse.getUnsupported().isEmpty()) {
            headers.add(SipConstants.HEADER_UNSUPPORTED, OptionTagWriter
                    .write(sipResponse.getUnsupported()));
        }
        if (!sipResponse.getSipRecipientsInfo().isEmpty()) {
            headers.add(HeaderConstants.HEADER_VIA, SipRecipientInfoWriter
                    .write(sipResponse.getSipRecipientsInfo()));
        }
    }

    @Override
    protected ConnectedRequest createRequest(Context context,
            ServerConnection connection, String methodName, String resourceUri,
            String version, Series<Parameter> headers, Representation entity,
            boolean confidential, Principal userPrincipal) {
        SipRequest request = new SipRequest(getHelper().getContext(), this,
                SipMethod.valueOf(methodName), resourceUri, version, headers,
                createInboundEntity(headers), false, null);
        // The via header is linked with the sipRecipientsInfo attribute, due to
        // distinct formats.
        request.setRecipientsInfo(new ArrayList<RecipientInfo>());
        return request;
    }
}
