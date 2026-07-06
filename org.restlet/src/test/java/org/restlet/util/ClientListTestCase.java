/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Protocol;

class ClientListTestCase {

    @Test
    void add_clientWithoutContext_assignsChildContext() {
        Context context = new Context();
        ClientList list = new ClientList(context);
        Client client = new Client(Protocol.HTTP);

        list.add(client);

        assertNotNull(client.getContext());
    }

    @Test
    void add_clientWithExistingContext_leavesItUnchanged() {
        Context context = new Context();
        ClientList list = new ClientList(context);
        Client client = new Client(Protocol.HTTP);
        Context existing = new Context();
        client.setContext(existing);

        list.add(client);

        assertEquals(existing, client.getContext());
    }

    @Test
    void addProtocol_createsAndRegistersClient() {
        Context context = new Context();
        ClientList list = new ClientList(context);

        Client client = list.add(Protocol.HTTP);

        assertEquals(1, list.size());
        assertNotNull(client.getContext());
        assertEquals(client, list.getFirst());
    }

    @Test
    void getContextAndSetContext_roundTrip() {
        Context context = new Context();
        ClientList list = new ClientList(context);
        assertEquals(context, list.getContext());

        Context other = new Context();
        list.setContext(other);
        assertEquals(other, list.getContext());
    }
}
