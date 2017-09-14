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

package org.restlet.util;

import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Protocol;

/**
 * Modifiable list of server connectors.
 * 
 * @author Jerome Louvel
 */
public final class ServerList extends WrapperList<Server> {

    /** The context. */
    private volatile Context context;

    /** The next Restlet of added servers. */
    private volatile Restlet next;

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param next
     *            The next Restlet of added servers.
     */
    public ServerList(Context context, Restlet next) {
        super(new CopyOnWriteArrayList<Server>());
        this.context = context;
        this.next = next;
    }

    /**
     * Adds a new server connector in the map supporting the given protocol.
     * 
     * @param protocol
     *            The connector protocol.
     * @return The added server.
     */
    public Server add(Protocol protocol) {
        Server result = new Server(protocol, null, protocol.getDefaultPort(),
                getNext());
        add(result);
        return result;
    }

    /**
     * Adds a new server connector in the map supporting the given protocol on
     * the specified port.
     * 
     * @param protocol
     *            The connector protocol.
     * @param port
     *            The listening port.
     * @return The added server.
     */
    public Server add(Protocol protocol, int port) {
        Server result = new Server(protocol, null, port, getNext());
        add(result);
        return result;
    }

    /**
     * Adds a new server connector in the map supporting the given protocol on
     * the specified IP address and port.
     * 
     * @param protocol
     *            The connector protocol.
     * @param address
     *            The optional listening IP address (useful if multiple IP
     *            addresses available).
     * @param port
     *            The listening port.
     * @return The added server.
     */
    public Server add(Protocol protocol, String address, int port) {
        Server result = new Server(protocol, address, port, getNext());
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
        // Set the server's context, if the server does not have already one.
        if (server.getContext() == null) {
            server.setContext(getContext().createChildContext());
        }

        server.setNext(getNext());
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
     * Returns the next Restlet.
     * 
     * @return The next Restlet.
     */
    public Restlet getNext() {
        return this.next;
    }

    /**
     * Sets the context.
     * 
     * @param context
     *            The context.
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Sets the next Restlet.
     * 
     * @param next
     *            The next Restlet.
     */
    public void setNext(Restlet next) {
        this.next = next;
    }

}
