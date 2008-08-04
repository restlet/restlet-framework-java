/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
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
 * @author Jerome Louvel (contact@noelios.com)
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
