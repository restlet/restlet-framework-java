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
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Protocol;

class ServerListTestCase {

    @Test
    void add_protocolOnly_createsServerWithDefaultPort() {
        Context context = new Context();
        Restlet next = new Restlet() {};
        ServerList list = new ServerList(context, next);

        Server server = list.add(Protocol.HTTP);

        assertEquals(Protocol.HTTP.getDefaultPort(), server.getPort());
        assertEquals(next, server.getNext());
        assertEquals(1, list.size());
    }

    @Test
    void add_protocolAndPort_usesGivenPort() {
        Context context = new Context();
        ServerList list = new ServerList(context, new Restlet() {});

        Server server = list.add(Protocol.HTTP, 8080);

        assertEquals(8080, server.getPort());
    }

    @Test
    void add_protocolAddressAndPort_usesGivenAddress() {
        Context context = new Context();
        ServerList list = new ServerList(context, new Restlet() {});

        Server server = list.add(Protocol.HTTP, "127.0.0.1", 8080);

        assertEquals("127.0.0.1", server.getAddress());
        assertEquals(8080, server.getPort());
    }

    @Test
    void add_serverWithoutContext_assignsChildContext() {
        Context context = new Context();
        ServerList list = new ServerList(context, new Restlet() {});
        Server server = new Server(Protocol.HTTP, null, 8080, null);

        list.add(server);

        assertNotNull(server.getContext());
    }

    @Test
    void add_serverWithExistingContext_leavesItUnchanged() {
        Context context = new Context();
        ServerList list = new ServerList(context, new Restlet() {});
        Server server = new Server(Protocol.HTTP, null, 8080, null);
        Context existing = new Context();
        server.setContext(existing);

        list.add(server);

        assertEquals(existing, server.getContext());
    }

    @Test
    void add_server_setsNextRestlet() {
        Context context = new Context();
        Restlet next = new Restlet() {};
        ServerList list = new ServerList(context, next);
        Server server = new Server(Protocol.HTTP, null, 8080, null);

        list.add(server);

        assertEquals(next, server.getNext());
    }

    @Test
    void getContextAndSetContext_roundTrip() {
        Context context = new Context();
        ServerList list = new ServerList(context, new Restlet() {});
        assertEquals(context, list.getContext());

        Context other = new Context();
        list.setContext(other);
        assertEquals(other, list.getContext());
    }

    @Test
    void getNextAndSetNext_roundTrip() {
        Context context = new Context();
        Restlet next = new Restlet() {};
        ServerList list = new ServerList(context, next);
        assertEquals(next, list.getNext());

        Restlet other = new Restlet() {};
        list.setNext(other);
        assertEquals(other, list.getNext());
    }
}
