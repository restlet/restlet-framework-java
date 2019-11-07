/**
 * Copyright 2005-2019 Talend
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
 * Restlet is a registered trademark of Talend S.A.
 */

package org.restlet.ext.simple;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.ext.simple.internal.SimpleContainer;
import org.restlet.ext.simple.internal.SimpleServer;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

/**
 * Simple HTTPS server connector.
 * 
 * @author Lars Heuer
 * @author Jerome Louvel
 * @deprecated Will be removed to favor lower-level network extensions allowing
 *             more control at the Restlet API level.
 */
@Deprecated
public class HttpServerHelper extends SimpleServerHelper {
    /**
     * Constructor.
     * 
     * @param server
     *            The server to help.
     */
    public HttpServerHelper(Server server) {
        super(server);
        getProtocols().add(Protocol.HTTP);
    }

    /** Starts the Restlet. */
    @Override
    public void start() throws Exception {
        String addr = getHelped().getAddress();

        if (addr != null) {
            // This call may throw UnknownHostException and otherwise always
            // returns an instance of INetAddress.
            // Note: textual representation of inet addresses are supported
            final InetAddress iaddr = InetAddress.getByName(addr);

            // Note: the backlog of 50 is the default
            setAddress(new InetSocketAddress(iaddr, getHelped().getPort()));
        } else {
            int port = getHelped().getPort();

            // Use ephemeral port
            if (port > 0) {
                setAddress(new InetSocketAddress(getHelped().getPort()));
            }
        }

        Container container = new SimpleContainer(this);
        ContainerServer server = new ContainerServer(container,
                getDefaultThreads());
        SimpleServer restletServer = new SimpleServer(server);
        Connection connection = new SocketConnection(restletServer);

        setConfidential(false);
        setContainerServer(server);
        setConnection(connection);

        InetSocketAddress address = (InetSocketAddress) getConnection()
                .connect(getAddress());
        setEphemeralPort(address.getPort());
        super.start();
    }
}
