/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.client.engine.connector;

import java.util.List;
import org.restlet.client.engine.util.emul.CopyOnWriteArrayList;

import org.restlet.client.Connector;
import org.restlet.client.Context;
import org.restlet.client.data.Protocol;
import org.restlet.client.engine.RestletHelper;

/**
 * Base connector helper.
 * 
 * @author Jerome Louvel
 */
public abstract class ConnectorHelper<T extends Connector> extends
        RestletHelper<T> {


    /** The protocols simultaneously supported. */
    private final List<Protocol> protocols;

    /**
     * Constructor.
     */
    public ConnectorHelper(T connector) {
        super(connector);
        this.protocols = new CopyOnWriteArrayList<Protocol>();
    }

    /**
     * Returns the helped Restlet context.
     * 
     * @return The helped Restlet context.
     */
    @Override
    public Context getContext() {
        return null;
    }

    /**
     * Returns the protocols simultaneously supported.
     * 
     * @return The protocols simultaneously supported.
     */
    public List<Protocol> getProtocols() {
        return this.protocols;
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
    }

    @Override
    public void update() throws Exception {
    }

}
