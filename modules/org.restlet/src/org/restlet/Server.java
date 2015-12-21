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

package org.restlet;

import java.util.Arrays;
import java.util.List;

import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.RestletHelper;
import org.restlet.resource.ServerResource;

/**
 * Connector acting as a generic server. It internally uses one of the available
 * connector helpers registered with the Restlet engine.<br>
 * <br>
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.<br>
 * <br>
 * For advanced cases, it is possible to obtained the wrapped
 * {@link RestletHelper} instance that is used by this client to handle the
 * calls via the "org.restlet.engine.helper" attribute stored in the
 * {@link Context} object.
 * 
 * @author Jerome Louvel
 */
public class Server extends Connector {
    /** The listening address if specified. */
    private volatile String address;

    /** The helper provided by the implementation. */
    private final RestletHelper<Server> helper;

    /** The next Restlet. */
    private volatile Restlet next;

    /** The listening port if specified. */
    private volatile int port;

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

        if (context != null && this.helper != null) {
            context.getAttributes().put("org.restlet.engine.helper",
                    this.helper);
        }
    }

    /**
     * Constructor. Note that it uses the protocol's default port.
     * 
     * @param context
     *            The parent context.
     * @param protocol
     *            The connector protocol.
     */
    public Server(Context context, Protocol protocol) {
        this(context, protocol, (protocol == null) ? -1 : protocol
                .getDefaultPort());
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
    public Server(Context context, Protocol protocol,
            Class<? extends ServerResource> nextClass) {
        this(context, protocol);
        setNext(createFinder(nextClass));
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
            Class<? extends ServerResource> nextClass) {
        this(context, protocol, port);
        setNext(createFinder(nextClass));
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
    public Server(Protocol protocol, Class<? extends ServerResource> nextClass) {
        this((Context) null, protocol);
        setNext(createFinder(nextClass));
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
    public Server(Protocol protocol, int port,
            Class<? extends ServerResource> nextClass) {
        this(protocol, port);
        setNext(createFinder(nextClass));
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
    public Server(Protocol protocol, String address,
            Class<? extends ServerResource> nextClass) {
        this(protocol, address);
        setNext(createFinder(nextClass));
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
     * Returns the actual server port after it has started. If an ephemeral port
     * is used it will be returned, otherwise the fixed port will be provided.
     * 
     * @return The actual server port.
     */
    public int getActualPort() {
        return (getPort() == 0) ? getEphemeralPort() : getPort();
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
        return this.next;
    }

    /**
     * Returns the listening port if specified.
     * 
     * @return The listening port if specified.
     */
    public int getPort() {
        return this.port;
    }

    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);

        if (getNext() != null) {
            getNext().handle(request, response);
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
    public void setNext(Class<? extends ServerResource> nextClass) {
        setNext(createFinder(nextClass));
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

    @Override
    public synchronized void start() throws Exception {
        if (isStopped()) {
            if (getHelper() != null) {
                getHelper().start();
            }

            // Must be invoked as a last step
            super.start();
        }
    }

    @Override
    public synchronized void stop() throws Exception {
        if (isStarted()) {
            // Must be invoked as a first step
            super.stop();

            if (getHelper() != null) {
                getHelper().stop();
            }
        }
    }

}
