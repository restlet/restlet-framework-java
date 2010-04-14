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
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Protocol;
import org.restlet.engine.http.connector.BaseClientHelper;
import org.restlet.engine.http.connector.BaseHelper;
import org.restlet.engine.http.connector.Connection;
import org.restlet.ext.sip.internal.SipClientConnection;

/**
 * SIP client helper based on NIO blocking sockets.
 * 
 * @author Jerome Louvel
 */
public class SipClientHelper extends BaseClientHelper {

    /**
     * Constructor.
     * 
     * @param client
     *            The client to help.
     */
    public SipClientHelper(Client client) {
        super(client);
        getProtocols().add(Protocol.SIP);
        getProtocols().add(Protocol.SIPS);
    }

    @Override
    protected Connection<Client> createConnection(BaseHelper<Client> helper,
            Socket socket, SocketChannel socketChannel) throws IOException {
        return new SipClientConnection(helper, socket, socketChannel);
    }

    @Override
    public Socket createSocket(boolean secure, String hostDomain, int hostPort)
            throws UnknownHostException, IOException {
        // TODO: parameterize the SIP proxy !
        String domain = getHelpedParameters().getFirstValue("hostDomain",
                "localhost");
        String port = getHelpedParameters().getFirstValue("hostPort", "5060");
        return super.createSocket(secure, domain, Integer.parseInt(port));
    }

    @Override
    protected Response createResponse(Request request) {
        return new SipResponse(request);
    }

    @Override
    public synchronized void start() throws Exception {
        getLogger().info("Starting the SIP client");
        super.start();
    }

    @Override
    public synchronized void stop() throws Exception {
        getLogger().info("Stopping the SIP client");
        super.stop();
    }
}
