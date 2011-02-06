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

package org.restlet.util;

import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Protocol;

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
