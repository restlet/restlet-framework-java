/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet;

import java.util.Arrays;
import java.util.List;

import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.RestletHelper;
import org.restlet.resource.Finder;

/**
 * Connector acting as a generic server. It internally uses one of the available
 * connector helpers registered with the Restlet engine.<br>
 * <br>
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Jerome Louvel
 */
public class Server extends Connector {
    /** The listening address if specified. */
    private volatile String address;

    /** The helper provided by the implementation. */
    private final RestletHelper<Server> helper;

    /** The listening port if specified. */
    private volatile int port;

    /** The next Restlet. */
    private volatile Restlet next;

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param protocols
     *            The connector protocols.
     * @param port
     *            The listening port.
     * @param next
     *            The next Restlet.
     */
    public Server(Context context, List<Protocol> protocols, int port,
            Restlet next) {
        this(context, protocols, null, port, next);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param protocols
     *            The connector protocols.
     * @param address
     *            The optional listening IP address (useful if multiple IP
     *            addresses available). You can also use a domain name as an
     *            alias for the IP address to listen to.
     * @param port
     *            The listening port.
     * @param next
     *            The next Restlet.
     */
    public Server(Context context, List<Protocol> protocols, String address,
            int port, Restlet next) {
        this(context, protocols, address, port, next, null);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param protocols
     *            The connector protocols.
     * @param address
     *            The optional listening IP address (useful if multiple IP
     *            addresses available). You can also use a domain name as an
     *            alias for the IP address to listen to.
     * @param port
     *            The listening port.
     * @param next
     *            The next Restlet.
     * @param helperClass
     *            Optional helper class name.
     */
    public Server(Context context, List<Protocol> protocols, String address,
            int port, Restlet next, String helperClass) {
        super(context, protocols);
        this.address = address;
        this.port = port;
        this.next = next;

        if (Engine.getInstance() != null) {
            this.helper = Engine.getInstance().createHelper(this, helperClass);
        } else {
            this.helper = null;
        }
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param protocol
     *            The connector protocol.
     * @param nextClass
     *            The next server resource.
     */
    public Server(Context context, Protocol protocol, Class<?> nextClass) {
        this(context, protocol, null, (protocol == null) ? -1 : protocol
                .getDefaultPort(), new Finder(Context.getCurrent(), nextClass));
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The parent context.
     * @param protocol
     *            The connector protocol.
     * @param port
     *            The listening port.
     */
    public Server(Context context, Protocol protocol, int port) {
        this(context, protocol, port, (Restlet) null);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param protocol
     *            The connector protocol.
     * @param port
     *            The listening port.
     * @param nextClass
     *            The next server resource.
     */
    public Server(Context context, Protocol protocol, int port,
            Class<?> nextClass) {
        this(context, protocol, null, port, new Finder(Context.getCurrent(),
                nextClass));
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param protocol
     *            The connector protocol.
     * @param port
     *            The listening port.
     * @param next
     *            The next Restlet.
     */
    public Server(Context context, Protocol protocol, int port, Restlet next) {
        this(context, protocol, null, port, next);
    }

    /**
     * Constructor using the protocol's default port.
     * 
     * @param context
     *            The context.
     * @param protocol
     *            The connector protocol.
     * @param next
     *            The next Restlet.
     */
    public Server(Context context, Protocol protocol, Restlet next) {
        this(context, protocol, null, (protocol == null) ? -1 : protocol
                .getDefaultPort(), next);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param protocol
     *            The connector protocol.
     * @param address
     *            The optional listening IP address (useful if multiple IP
     *            addresses available). You can also use a domain name as an
     *            alias for the IP address to listen to.
     * @param port
     *            The listening port.
     * @param next
     *            The next Restlet.
     */
    public Server(Context context, Protocol protocol, String address, int port,
            Restlet next) {
        this(context, (protocol == null) ? null : Arrays.asList(protocol),
                address, port, next);
    }

    /**
     * Constructor.
     * 
     * @param protocols
     *            The connector protocols.
     * @param port
     *            The listening port.
     * @param next
     *            The next Restlet.
     */
    public Server(List<Protocol> protocols, int port, Restlet next) {
        this((Context) null, protocols, port, next);
    }

    /**
     * Constructor.
     * 
     * @param protocols
     *            The connector protocols.
     * @param address
     *            The optional listening IP address (useful if multiple IP
     *            addresses available). You can also use a domain name as an
     *            alias for the IP address to listen to.
     * @param port
     *            The listening port.
     * @param next
     *            The next Restlet.
     */
    public Server(List<Protocol> protocols, String address, int port,
            Restlet next) {
        this((Context) null, protocols, address, port, next);
    }

    /**
     * Constructor.
     * 
     * @param protocol
     *            The connector protocol.
     */
    public Server(Protocol protocol) {
        this((Context) null, protocol, (Restlet) null);
    }

    /**
     * Constructor using the protocol's default port.
     * 
     * @param protocol
     *            The connector protocol.
     * @param nextClass
     *            The next server resource.
     */
    public Server(Protocol protocol, Class<?> nextClass) {
        this((Context) null, protocol, new Finder(Context.getCurrent(),
                nextClass));
    }

    /**
     * Constructor.
     * 
     * @param protocol
     *            The connector protocol.
     * @param port
     *            The listening port.
     */
    public Server(Protocol protocol, int port) {
        this((Context) null, protocol, port, (Restlet) null);
    }

    /**
     * Constructor.
     * 
     * @param protocol
     *            The connector protocol.
     * @param port
     *            The listening port.
     * @param nextClass
     *            The next server resource.
     */
    public Server(Protocol protocol, int port, Class<?> nextClass) {
        this((Context) null, protocol, port, new Finder(Context.getCurrent(),
                nextClass));
    }

    /**
     * Constructor.
     * 
     * @param protocol
     *            The connector protocol.
     * @param port
     *            The listening port.
     * @param next
     *            The next Restlet.
     */
    public Server(Protocol protocol, int port, Restlet next) {
        this((Context) null, protocol, port, next);
    }

    /**
     * Constructor using the protocol's default port.
     * 
     * @param protocol
     *            The connector protocol.
     * @param next
     *            The next Restlet.
     */
    public Server(Protocol protocol, Restlet next) {
        this((Context) null, protocol, next);
    }

    /**
     * Constructor using the protocol's default port.
     * 
     * @param protocol
     *            The connector protocol.
     * @param address
     *            The listening IP address (useful if multiple IP addresses
     *            available). You can also use a domain name as an alias for the
     *            IP address to listen to.
     */
    public Server(Protocol protocol, String address) {
        this((Context) null, protocol, address, protocol.getDefaultPort(), null);
    }

    /**
     * Constructor using the protocol's default port.
     * 
     * @param protocol
     *            The connector protocol.
     * @param address
     *            The listening IP address (useful if multiple IP addresses
     *            available). You can also use a domain name as an alias for the
     *            IP address to listen to.
     * @param nextClass
     *            The next server resource.
     */
    public Server(Protocol protocol, String address, Class<?> nextClass) {
        this((Context) null, protocol, address, protocol.getDefaultPort(),
                new Finder(Context.getCurrent(), nextClass));
    }

    /**
     * Constructor.
     * 
     * @param protocol
     *            The connector protocol.
     * @param address
     *            The optional listening IP address (useful if multiple IP
     *            addresses available). You can also use a domain name as an
     *            alias for the IP address to listen to.
     * @param port
     *            The listening port.
     */
    public Server(Protocol protocol, String address, int port) {
        this((Context) null, protocol, address, port, null);
    }

    /**
     * Constructor.
     * 
     * @param protocol
     *            The connector protocol.
     * @param address
     *            The optional listening IP address (useful if multiple IP
     *            addresses available). You can also use a domain name as an
     *            alias for the IP address to listen to.
     * @param port
     *            The listening port.
     * @param next
     *            The next Restlet.
     */
    public Server(Protocol protocol, String address, int port, Restlet next) {
        this((Context) null, protocol, address, port, next);
    }

    /**
     * Constructor using the protocol's default port.
     * 
     * @param protocol
     *            The connector protocol.
     * @param address
     *            The listening IP address (useful if multiple IP addresses
     *            available). You can also use a domain name as an alias for the
     *            IP address to listen to.
     * @param next
     *            The next Restlet.
     */
    public Server(Protocol protocol, String address, Restlet next) {
        this((Context) null, protocol, address, protocol.getDefaultPort(), next);
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
     * '0'. The default value is '-1' if no ephemeral port is known. See
     * InetSocketAddress#InetSocketAddress(int) and ServerSocket#getLocalPort()
     * methods for details.
     * 
     * @return The actual ephemeral port used.
     */
    public int getEphemeralPort() {
        return (Integer) getHelper().getAttributes().get("ephemeralPort");
    }

    /**
     * Returns the internal server.
     * 
     * @return The internal server.
     */
    private RestletHelper<Server> getHelper() {
        return this.helper;
    }

    /**
     * Returns the next Restlet.
     * 
     * @return The next Restlet.
     */
    public Restlet getNext() {
        return getTarget();
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
     * Returns the next Restlet.
     * 
     * @return The next Restlet.
     * @deprecated Use the {@link #getNext()} method instead.
     */
    @Deprecated
    public Restlet getTarget() {
        return this.next;
    }

    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);

        if (getTarget() != null) {
            getTarget().handle(request, response);
        }
    }

    /**
     * Indicates if a next Restlet is set.
     * 
     * @return True if a next Restlet is set.
     */
    public boolean hasNext() {
        return this.next != null;
    }

    /**
     * Indicates if a next Restlet is set.
     * 
     * @return True if a next Restlet is set.
     * @deprecated Use the {@link #hasNext()} method instead.
     */
    @Deprecated
    public boolean hasTarget() {
        return hasNext();
    }

    /**
     * Indicates the underlying connector helper is available.
     * 
     * @return True if the underlying connector helper is available.
     */
    @Override
    public boolean isAvailable() {
        return getHelper() != null;
    }

    /**
     * Sets the optional listening IP address (local host used if null).
     * 
     * @param address
     *            The optional listening IP address (local host used if null).
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Sets the next Restlet as a Finder for a given resource class. When the
     * call is delegated to the Finder instance, a new instance of the resource
     * class will be created and will actually handle the request.
     * 
     * @param nextClass
     *            The next resource class to attach.
     */
    public void setNext(Class<?> nextClass) {
        setTarget(new Finder(getContext(), nextClass));
    }

    /**
     * Sets the next Restlet.
     * 
     * @param next
     *            The next Restlet.
     */
    public void setNext(Restlet next) {
        setTarget(next);
    }

    /**
     * Sets the listening port if specified. Note that '0' means that the system
     * will pick up an ephemeral port at the binding time. This ephemeral can be
     * retrieved once the server is started using the
     * {@link #getEphemeralPort()} method.
     * 
     * @param port
     *            The listening port if specified.
     */
    protected void setPort(int port) {
        this.port = port;
    }

    /**
     * Sets the next Restlet.
     * 
     * @param next
     *            The next Restlet.
     * @deprecated Use the {@link #setNext(Restlet)} method instead.
     */
    @Deprecated
    public void setTarget(Restlet next) {
        this.next = next;
    }

    @Override
    public synchronized void start() throws Exception {
        if (isStopped()) {
            super.start();

            if (getHelper() != null) {
                getHelper().start();
            }
        }
    }

    @Override
    public synchronized void stop() throws Exception {
        if (isStarted()) {
            if (getHelper() != null) {
                getHelper().stop();
            }

            super.stop();
        }
    }

}
