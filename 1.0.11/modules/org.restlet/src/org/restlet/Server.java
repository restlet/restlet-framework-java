/**
 * Copyright 2005-2008 Noelios Technologies.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet;

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
    private String address;

    /** The listening port if specified. */
    private int port;

    /** The target Restlet. */
    private Restlet target;

    /** The helper provided by the implementation. */
    private Helper helper;

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
     *                addresses available).
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
        this(context, protocol, null, protocol.getDefaultPort(), target);
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
     *                addresses available).
     * @param port
     *                The listening port.
     * @param target
     *                The target Restlet.
     */
    public Server(Context context, Protocol protocol, String address, int port,
            Restlet target) {
        this(context, Arrays.asList(protocol), address, port, target);
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
     *                addresses available).
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
     * Constructor using the protocol's default port.
     * 
     * @param protocol
     *                The connector protocol.
     * @param address
     *                The listening IP address (useful if multiple IP addresses
     *                available).
     * @param target
     *                The target Restlet.
     */
    public Server(Protocol protocol, String address, Restlet target) {
        this(null, protocol, address, protocol.getDefaultPort(), target);
    }

    /**
     * Constructor.
     * 
     * @param protocol
     *                The connector protocol.
     * @param address
     *                The optional listening IP address (useful if multiple IP
     *                addresses available).
     * @param port
     *                The listening port.
     * @param target
     *                The target Restlet.
     */
    public Server(Protocol protocol, String address, int port, Restlet target) {
        this(null, protocol, address, port, target);
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

    /**
     * Handles a call.
     * 
     * @param request
     *                The request to handle.
     * @param response
     *                The response to update.
     */
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
     * Sets the listening port if specified.
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
    public void start() throws Exception {
        if (isStopped()) {
            super.start();

            if (getHelper() != null)
                getHelper().start();
        }
    }

    @Override
    public void stop() throws Exception {
        if (isStarted()) {
            if (getHelper() != null)
                getHelper().stop();

            super.stop();
        }
    }

}
