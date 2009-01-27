/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
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

package org.restlet.ext.simple;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.restlet.Server;
import org.restlet.data.Protocol;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;


/**
 * Simple HTTPS server connector.
 * 
 * @author Lars Heuer
 * @author Jerome Louvel
 */
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
        final String addr = getHelped().getAddress();
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
        	if(port > 0) {
        		setAddress(new InetSocketAddress(getHelped().getPort()));
        	}
        }
        final Container container = new SimpleContainer(this);
        final ContainerServer server = new ContainerServer(container, getDefaultThreads());
        final SimpleServer filter = new SimpleServer(server);
        final Connection connection = new SocketConnection(filter);
        
        setConfidential(false);
        setContainer(server);
        setConnection(connection);
        
        InetSocketAddress address = (InetSocketAddress)getConnection().connect(getAddress());
        setEphemeralPort(address.getPort());
        super.start();
    }
}
