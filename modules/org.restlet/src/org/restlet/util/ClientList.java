/*
 * Copyright 2005-2007 Noelios Technologies.
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

package org.restlet.util;

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
    private Context context;

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     */
    public ClientList(Context context) {
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
        Client result = new Client(getContext(), protocol);
        add(result);
        return result;
    }

    /**
     * Returns the context.
     * 
     * @return The context.
     */
    private Context getContext() {
        return this.context;
    }
}
