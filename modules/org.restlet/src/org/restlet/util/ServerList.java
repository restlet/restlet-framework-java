/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.util;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Protocol;

/**
 * Modifiable list of server connectors.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public final class ServerList extends WrapperList<Server> {
    /** The context. */
    private Context context;

    /** The target Restlet of added servers. */
    private Restlet target;

    /**
     * Constructor.
     * 
     * @param context
     *                The context.
     * @param target
     *                The target Restlet of added servers.
     */
    public ServerList(Context context, Restlet target) {
        this.context = context;
        this.target = target;
    }

    /**
     * Adds a new server connector in the map supporting the given protocol.
     * 
     * @param protocol
     *                The connector protocol.
     * @return The added server.
     */
    public Server add(Protocol protocol) {
        Server result = new Server(getContext(), protocol, null, protocol
                .getDefaultPort(), getTarget());
        add(result);
        return result;
    }

    /**
     * Adds a new server connector in the map supporting the given protocol on
     * the specified port.
     * 
     * @param protocol
     *                The connector protocol.
     * @param port
     *                The listening port.
     * @return The added server.
     */
    public Server add(Protocol protocol, int port) {
        Server result = new Server(getContext(), protocol, null, port,
                getTarget());
        add(result);
        return result;
    }

    /**
     * Adds a new server connector in the map supporting the given protocol on
     * the specified IP address and port.
     * 
     * @param protocol
     *                The connector protocol.
     * @param address
     *                The optional listening IP address (useful if multiple IP
     *                addresses available).
     * @param port
     *                The listening port.
     * @return The added server.
     */
    public Server add(Protocol protocol, String address, int port) {
        Server result = new Server(getContext(), protocol, address, port,
                getTarget());
        add(result);
        return result;
    }

    /**
     * Adds a server at the end of the list.
     * 
     * @return True (as per the general contract of the Collection.add method).
     */
	@Override
    public boolean add(Server server) {
        server.setTarget(getTarget());
        return super.add(server);
    }

    /**
     * Returns the context.
     * 
     * @return The context.
     */
    public Context getContext() {
        return this.context;
    }

    /**
     * Returns the target Restlet.
     * 
     * @return The target Restlet.
     */
    public Restlet getTarget() {
        return this.target;
    }

}
