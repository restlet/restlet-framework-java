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

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.ext.nio.internal.connection.Connection;
import org.restlet.ext.nio.internal.way.ServerInboundWay;
import org.restlet.ext.sip.SipResponse;

/**
 * SIP server inbound way.
 * 
 * @author Jerome Louvel
 * @deprecated Will be removed to focus on Web APIs.
 */
@Deprecated
public class SipServerInboundWay extends ServerInboundWay {

    /**
     * Constructor.
     * 
     * @param connection
     *            The parent connection.
     * @param bufferSize
     *            The byte buffer size.
     */
    public SipServerInboundWay(Connection<Server> connection, int bufferSize) {
        super(connection, bufferSize);
    }

    @Override
    protected Response createResponse(Request request) {
        return new SipResponse(request);
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && (getMessage() == null);
    }

}
