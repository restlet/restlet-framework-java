/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.client.util;

import org.restlet.client.engine.util.emul.CopyOnWriteArrayList;

import org.restlet.client.Client;
import org.restlet.client.Context;
import org.restlet.client.data.Protocol;

/**
 * Modifiable list of client connectors.
 * 
 * @author Jerome Louvel
 */
public final class ClientList extends WrapperList<Client> {

    /** The context. */
    private volatile Context context;

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     */
    public ClientList(Context context) {
        super(new CopyOnWriteArrayList<Client>());
        this.context = context;
    }

    @Override
    public boolean add(Client client) {
        // Set the client's context, if the client does not have already one.
        if (client.getContext() == null) {
            client.setContext(getContext().createChildContext());
        }

        return super.add(client);
    }

    /**
     * Adds a new client connector in the map supporting the given protocol.
     * 
     * @param protocol
     *            The connector protocol.
     * @return The added client.
     */
    public Client add(Protocol protocol) {
        final Client result = new Client(protocol);
        result.setContext(getContext().createChildContext());
        add(result);
        return result;
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
     * Sets the context.
     * 
     * @param context
     *            The context.
     */
    public void setContext(Context context) {
        this.context = context;
    }
}
