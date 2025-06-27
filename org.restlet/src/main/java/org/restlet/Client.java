/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet;

import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.RestletHelper;

import java.util.Arrays;
import java.util.List;

/**
 * Connector acting as a generic client. It internally uses one of the available
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
public class Client extends Connector {

    /** The helper provided by the implementation. */
    private final RestletHelper<Client> helper;

    /**
     * Constructor.
     * 
     * @param context   The context.
     * @param protocols The connector protocols.
     */
    public Client(Context context, List<Protocol> protocols) {
        this(context, protocols, null);
    }

    /**
     * Constructor.
     * 
     * @param context     The context.
     * @param protocols   The connector protocols.
     * @param helperClass Optional helper class name.
     */
    public Client(Context context, List<Protocol> protocols,
            String helperClass) {
        super(context, protocols);

        if ((protocols != null) && !protocols.isEmpty()) {
            if (Engine.getInstance() != null) {
                this.helper = Engine.getInstance().createHelper(this,
                        helperClass);
            } else {
                this.helper = null;
            }
        } else {
            this.helper = null;
        }

        if (context != null && this.helper != null) {
            context.getAttributes().put("org.restlet.engine.helper",
                    this.helper);
        }
    }

    /**
     * Constructor.
     * 
     * @param context   The context.
     * @param protocols The connector protocols.
     */
    public Client(Context context, Protocol... protocols) {
        this(context, (protocols == null) ? null : Arrays.asList(protocols),
                null);
    }

    /**
     * Constructor.
     * 
     * @param protocols The connector protocols.
     */
    public Client(List<Protocol> protocols) {
        this(null, protocols, null);
    }

    /**
     * Constructor.
     * 
     * @param protocols The connector protocols.
     */
    public Client(Protocol... protocols) {
        this(null, protocols);
    }

    /**
     * Constructor.
     * 
     * @param protocolName The connector protocol.
     */
    public Client(String protocolName) {
        this(Protocol.valueOf(protocolName));
    }

    /**
     * Returns the helper provided by the implementation.
     * 
     * @return The helper provided by the implementation.
     */
    private RestletHelper<Client> getHelper() {
        return this.helper;
    }

    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);

        if (getHelper() != null) {
            getHelper().handle(request, response);
        } else {
            String sb = "No available client connector supports the required protocol: " +
                    "'" + request.getProtocol().getName() + "'." +
                    " Please add the JAR of a matching connector to your classpath.";
            response.setStatus(Status.CONNECTOR_ERROR_INTERNAL, sb);
        }
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
