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

package org.restlet.ext.ssl.internal;

import java.io.IOException;

import javax.net.ssl.SSLEngineResult.HandshakeStatus;

import org.restlet.Server;
import org.restlet.engine.connector.Connection;
import org.restlet.engine.connector.HttpServerOutboundWay;
import org.restlet.engine.connector.MessageState;
import org.restlet.engine.io.IoState;

/**
 * HTTPS server outbound way.
 * 
 * @author Jerome Louvel
 */
public class HttpsServerOutboundWay extends HttpServerOutboundWay {

    /**
     * Constructor.
     * 
     * @param connection
     *            The parent connection.
     * @param bufferSize
     *            The byte buffer size.
     */
    public HttpsServerOutboundWay(Connection<Server> connection, int bufferSize) {
        super(connection, bufferSize);
    }

    @Override
    public SslConnection<Server> getConnection() {
        return (SslConnection<Server>) super.getConnection();
    }

    @Override
    protected boolean hasIoInterest() {
        return super.hasIoInterest()
                && (!getConnection().isSslHandshaking() || (getConnection()
                        .getSslHandshakeStatus() != HandshakeStatus.NEED_UNWRAP));
    }

    @Override
    public int preProcess(int maxDrained, Object... args) throws IOException {
        int result = 0;

        if ((getIoState() == IoState.READY)
                && (getMessageState() == MessageState.IDLE)) {
            // SSL handshake underway
            getBuffer().beforeDrain();
            result = onDrain(getBuffer(), maxDrained, args);
        }

        return result;
    }

    @Override
    public void postProcess(int drained) throws IOException {
        getConnection().handleSslResult();
    }

}
