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

import java.util.Arrays;
import java.util.List;

import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.util.Engine;
import org.restlet.util.Helper;

/**
 * Connector acting as a generic client. It internally uses one of the available
 * connectors registered with the current Restlet implementation.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Client extends Connector {
    /**
     * The number of milliseconds the client should wait for a response before
     * aborting the request and setting its status to an error status.
     */
    private int connectTimeout = 0;

    /** The helper provided by the implementation. */
    private volatile Helper<Client> helper;

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param protocols
     *            The connector protocols.
     */
    public Client(Context context, List<Protocol> protocols) {
        this(context, protocols, null);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param protocols
     *            The connector protocols.
     * @param helperClass
     *            Optional helper class name.
     */
    public Client(Context context, List<Protocol> protocols, String helperClass) {
        super(context, protocols);

        if ((protocols != null) && (protocols.size() > 0)) {
            if (Engine.getInstance() != null) {
                this.helper = Engine.getInstance().createHelper(this,
                        helperClass);

                // Create the client context
                setContext(this.helper.createContext(getClass()
                        .getCanonicalName()));
            }
        }
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param protocol
     *            The connector protocol.
     */
    public Client(Context context, Protocol protocol) {
        this(context, (protocol == null) ? null : Arrays.asList(protocol), null);
    }

    /**
     * Constructor.
     * 
     * @param protocols
     *            The connector protocols.
     */
    public Client(List<Protocol> protocols) {
        this(null, protocols, null);
    }

    /**
     * Constructor.
     * 
     * @param protocol
     *            The connector protocol.
     */
    public Client(Protocol protocol) {
        this(null, protocol);
    }

    /**
     * Constructor.
     * 
     * @param protocolName
     *            The connector protocol.
     */
    public Client(String protocolName) {
        this(Protocol.valueOf(protocolName));
    }

    /**
     * Returns the connection timeout.
     * 
     * @return The connection timeout.
     */
    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    /**
     * Returns the helper provided by the implementation.
     * 
     * @return The helper provided by the implementation.
     */
    private Helper<Client> getHelper() {
        return this.helper;
    }

    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);

        if (getHelper() != null) {
            getHelper().handle(request, response);
        }
    }

    /**
     * Sets the connection timeout.
     * 
     * @param connectTimeout
     *            The connection timeout.
     */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
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
