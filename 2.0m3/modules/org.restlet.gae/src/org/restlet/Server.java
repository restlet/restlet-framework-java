/**
 * Copyright 2005-2009 Noelios Technologies.
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
import org.restlet.data.Request;
import org.restlet.data.Response;
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

    /** The target Restlet. */
    private volatile Restlet target;

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param protocols
     *            The connector protocols.
     * @param port
     *            The listening port.
     * @param target
     *            The target Restlet.
     */
    public Server(Context context, List<Protocol> protocols, int port,
            Restlet target) {
        this(context, protocols, null, port, target);
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
     * @param target
     *            The target Restlet.
     */
    public Server(Context context, List<Protocol> protocols, String address,
            int port, Restlet target) {
        this(context, protocols, address, port, target, null);
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
     * @param target
     *            The target Restlet.
     * @param helperClass
     *            Optional helper class name.
     */
    public Server(Context context, List<Protocol> protocols, String address,
            int port, Restlet target, String helperClass) {
        super(context, protocols);
        this.address = address;
        this.port = port;
        this.target = target;

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
     * @param port
     *            The listening port.
     * @param target
     *            The target Restlet.
     */
    public Server(Context context, Protocol protocol, int port, Restlet target) {
        this(context, protocol, null, port, target);
    }

    /**
     * Constructor using the protocol's default port.
     * 
     * @param context
     *            The context.
     * @param protocol
     *            The connector protocol.
     * @param target
     *            The target Restlet.
     */
    public Server(Context context, Protocol protocol, Restlet target) {
        this(context, protocol, null, (protocol == null) ? -1 : protocol
                .getDefaultPort(), target);
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
     * @param target
     *            The target Restlet.
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
     *            The connector protocols.
     * @param port
     *            The listening port.
     * @param target
     *            The target Restlet.
     */
    public Server(List<Protocol> protocols, int port, Restlet target) {
        this(null, protocols, port, target);
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
     * @param target
     *            The target Restlet.
     */
    public Server(List<Protocol> protocols, String address, int port,
            Restlet target) {
        this(null, protocols, address, port, target);
    }

    /**
     * Constructor.
     * 
     * @param protocol
     *            The connector protocol.
     * @param port
     *            The listening port.
     * @param targetClass
     *            The target server resource.
     */
    public Server(Protocol protocol, int port, Class<?> targetClass) {
        this(null, protocol, port,
                new Finder(Context.getCurrent(), targetClass));
    }

    /**
     * Constructor.
     * 
     * @param protocol
     *            The connector protocol.
     * @param port
     *            The listening port.
     * @param target
     *            The target Restlet.
     */
    public Server(Protocol protocol, int port, Restlet target) {
        this(null, protocol, port, target);
    }

    /**
     * Constructor using the protocol's default port.
     * 
     * @param protocol
     *            The connector protocol.
     * @param target
     *            The target Restlet.
     */
    public Server(Protocol protocol, Restlet target) {
        this(null, protocol, target);
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
     * @param target
     *            The target Restlet.
     */
    public Server(Protocol protocol, String address, int port, Restlet target) {
        this(null, protocol, address, port, target);
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
     * @param target
     *            The target Restlet.
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
        super.handle(request, response);

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
     * Sets the target Restlet as a Finder for a given resource class. When the
     * call is delegated to the Finder instance, a new instance of the resource
     * class will be created and will actually handle the request.
     * 
     * @param targetClass
     *            The target resource class to attach.
     */
    public void setNext(Class<?> targetClass) {
        setTarget(new Finder(getContext(), targetClass));
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
     * Sets the target Restlet.
     * 
     * @param target
     *            The target Restlet.
     */
    public void setTarget(Restlet target) {
        this.target = target;
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
