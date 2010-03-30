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

import org.restlet.Context;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.Parameter;
import org.restlet.engine.http.connector.BaseHelper;
import org.restlet.engine.http.connector.ConnectedRequest;
import org.restlet.engine.http.connector.ServerConnection;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.ext.sip.SipConstants;
import org.restlet.ext.sip.SipRequest;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

/**
 * SIP server connector for the SIP connector.
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
        // SipResponse sipResponse = (SipResponse) response;

        if (sipRequest.getCallId() != null) {
            headers.add(SipConstants.HEADER_CALL_ID, sipRequest.getCallId());
        }

        if (sipRequest.getCallSeq() != null) {
            headers.add(SipConstants.HEADER_CALL_SEQ, sipRequest.getCallSeq());
        }

        if (sipRequest.getFrom() != null) {
            headers.add(HeaderConstants.HEADER_FROM, AddressWriter
                    .write(sipRequest.getFrom()));
        }

        if (sipRequest.getTo() != null) {
            headers.add(SipConstants.HEADER_TO, AddressWriter.write(sipRequest
                    .getTo()));
        }

        if (sipRequest.getVia() != null) {
            headers.add(HeaderConstants.HEADER_VIA, sipRequest.getVia());
        }
    }

    @Override
    protected ConnectedRequest createRequest(Context context,
            ServerConnection connection, String methodName, String resourceUri,
            String version, Series<Parameter> headers, Representation entity,
            boolean confidential, Principal userPrincipal) {
        return new SipRequest(getHelper().getContext(), this, methodName,
                resourceUri, version, headers, createInboundEntity(headers),
                false, null);
    }
}
