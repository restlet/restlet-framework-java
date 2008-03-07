/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;

import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.util.Engine;
import org.restlet.util.Helper;

/**
 * Connector acting as a generic server. It internally uses one of the available
 * connectors registered with the current Restlet implementation.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Server extends Connector {
    /** The listening address if specified. */
    private volatile String address;

    /** The helper provided by the implementation. */
    private volatile Helper helper;

    /** The listening port if specified. */
    private volatile int port;

    /** The target Restlet. */
    private volatile Restlet target;

    /**
     * Constructor.
     * 
     * @param context
     *                The context.
     * @param protocols
     *                The connector protocols.
     * @param port
     *                The listening port.
     * @param target
     *                The target Restlet.
     */
    public Server(Context context, List<Protocol> protocols, int port,
            Restlet target) {
        this(context, protocols, null, port, target);
    }

    /**
     * Constructor.
     * 
     * @param context
     *                The context.
     * @param protocols
     *                The connector protocols.
     * @param address
     *                The optional listening IP address (useful if multiple IP
     *                addresses available). You can also use a domain name as an
     *                alias for the IP address to listen to.
     * @param port
     *                The listening port.
     * @param target
     *                The target Restlet.
     */
    public Server(Context context, List<Protocol> protocols, String address,
            int port, Restlet target) {
        super(context, protocols);
        this.address = address;
        this.port = port;
        this.target = target;

        if (Engine.getInstance() != null) {
            this.helper = Engine.getInstance().createHelper(this);
        }
    }

    /**
     * Constructor.
     * 
     * @param context
     *                The context.
     * @param protocol
     *                The connector protocol.
     * @param port
     *                The listening port.
     * @param target
     *                The target Restlet.
     */
    public Server(Context context, Protocol protocol, int port, Restlet target) {
        this(context, protocol, null, port, target);
    }

    /**
     * Constructor using the protocol's default port.
     * 
     * @param context
     *                The context.
     * @param protocol
     *                The connector protocol.
     * @param target
     *                The target Restlet.
     */
    public Server(Context context, Protocol protocol, Restlet target) {
        this(context, protocol, null, (protocol == null) ? -1 : protocol
                .getDefaultPort(), target);
    }

    /**
     * Constructor.
     * 
     * @param context
     *                The context.
     * @param protocol
     *                The connector protocol.
     * @param address
     *                The optional listening IP address (useful if multiple IP
     *                addresses available). You can also use a domain name as an
     *                alias for the IP address to listen to.
     * @param port
     *                The listening port.
     * @param target
     *                The target Restlet.
     */
    public Server(Context context, Protocol protocol, String address, int port,
            Restlet target) {
        this(context, (protocol == null) ? null : Arrays.asList(protocol),
                address, port, target);
    }

    /**
     * Constructor.
     * 
     * @param protocols
     *                The connector protocols.
     * @param port
     *                The listening port.
     * @param target
     *                The target Restlet.
     */
    public Server(List<Protocol> protocols, int port, Restlet target) {
        this(null, protocols, port, target);
    }

    /**
     * Constructor.
     * 
     * @param protocols
     *                The connector protocols.
     * @param address
     *                The optional listening IP address (useful if multiple IP
     *                addresses available). You can also use a domain name as an
     *                alias for the IP address to listen to.
     * @param port
     *                The listening port.
     * @param target
     *                The target Restlet.
     */
    public Server(List<Protocol> protocols, String address, int port,
            Restlet target) {
        this(null, protocols, address, port, target);
    }

    /**
     * Constructor.
     * 
     * @param protocol
     *                The connector protocol.
     * @param port
     *                The listening port.
     * @param target
     *                The target Restlet.
     */
    public Server(Protocol protocol, int port, Restlet target) {
        this(null, protocol, port, target);
    }

    /**
     * Constructor using the protocol's default port.
     * 
     * @param protocol
     *                The connector protocol.
     * @param target
     *                The target Restlet.
     */
    public Server(Protocol protocol, Restlet target) {
        this(null, protocol, target);
    }

    /**
     * Constructor.
     * 
     * @param protocol
     *                The connector protocol.
     * @param address
     *                The optional listening IP address (useful if multiple IP
     *                addresses available). You can also use a domain name as an
     *                alias for the IP address to listen to.
     * @param port
     *                The listening port.
     * @param target
     *                The target Restlet.
     */
    public Server(Protocol protocol, String address, int port, Restlet target) {
        this(null, protocol, address, port, target);
    }

    /**
     * Constructor using the protocol's default port.
     * 
     * @param protocol
     *                The connector protocol.
     * @param address
     *                The listening IP address (useful if multiple IP addresses
     *                available). You can also use a domain name as an alias for
     *                the IP address to listen to.
     * @param target
     *                The target Restlet.
     */
    public Server(Protocol protocol, String address, Restlet target) {
        this(null, protocol, address, protocol.getDefaultPort(), target);
    }

    /**
     * Returns the optional listening IP address (local host used if null).
     * 
     * @return The optional listening IP address (local host used if null).
     */
    public String getAddress() {
        return this.address;
    }

    /**
     * Returns the actual ephemeral port used when the listening port is set to
     * '0'. The default value is '-1' if no ephemeral port is known.
     * 
     * @return The actual ephemeral port used.
     * @see InetSocketAddress#InetSocketAddress(int)
     * @see ServerSocket#getLocalPort()
     */
    public int getEphemeralPort() {
        return (Integer) getHelper().getAttributes().get("ephemeralPort");
    }

    /**
     * Returns the internal server.
     * 
     * @return The internal server.
     */
    private Helper getHelper() {
        return this.helper;
    }

    /**
     * Returns the listening port if specified.
     * 
     * @return The listening port if specified.
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Returns the target Restlet.
     * 
     * @return The target Restlet.
     */
    public Restlet getTarget() {
        return this.target;
    }

    @Override
    public void handle(Request request, Response response) {
        init(request, response);

        if (getTarget() != null) {
            getTarget().handle(request, response);
        }
    }

    /**
     * Indicates if a target Restlet is set.
     * 
     * @return True if a target Restlet is set.
     */
    public boolean hasTarget() {
        return this.target != null;
    }

    /**
     * Sets the optional listening IP address (local host used if null).
     * 
     * @param address
     *                The optional listening IP address (local host used if
     *                null).
     */
    protected void setAddress(String address) {
        this.address = address;
    }

    /**
     * Sets the listening port if specified. Note that '0' means that the system
     * will pick up an ephemeral port at the binding time. This ephemeral can be
     * retrieved once the server is started using the
     * {@link #getEphemeralPort()} method.
     * 
     * @param port
     *                The listening port if specified.
     */
    protected void setPort(int port) {
        this.port = port;
    }

    /**
     * Sets the target Restlet.
     * 
     * @param target
     *                The target Restlet.
     */
    public void setTarget(Restlet target) {
        this.target = target;
    }

    @Override
    public synchronized void start() throws Exception {
        if (isStopped()) {
            super.start();

            if (getHelper() != null)
                getHelper().start();
        }
    }

    @Override
    public synchronized void stop() throws Exception {
        if (isStarted()) {
            if (getHelper() != null)
                getHelper().stop();

            super.stop();
        }
    }

}
