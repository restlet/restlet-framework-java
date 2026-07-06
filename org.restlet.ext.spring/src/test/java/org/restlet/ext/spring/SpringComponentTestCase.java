/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Protocol;

/** Unit tests for {@link SpringComponent}. */
class SpringComponentTestCase {

    private static class TestRestlet extends Restlet {}

    private SpringComponent component;

    @BeforeEach
    void setUpEach() {
        this.component = new SpringComponent();
    }

    @AfterEach
    void tearDownEach() {
        this.component = null;
    }

    @Test
    void testSetClientAddsClientInstance() {
        Client client = new Client(Protocol.FILE);
        this.component.setClient(client);
        assertEquals(1, this.component.getClients().size(), "Wrong number of clients");
        assertTrue(this.component.getClients().contains(client), "Client instance not added");
    }

    @Test
    void testSetClientAddsProtocolInstance() {
        this.component.setClient(Protocol.HTTP);
        assertEquals(1, this.component.getClients().size(), "Wrong number of clients");
        assertEquals(
                Protocol.HTTP, this.component.getClients().getFirst().getProtocols().getFirst());
    }

    @Test
    void testSetClientAddsProtocolNamedByString() {
        this.component.setClient("FILE");
        assertEquals(1, this.component.getClients().size(), "Wrong number of clients");
        assertEquals(
                Protocol.FILE, this.component.getClients().getFirst().getProtocols().getFirst());
    }

    @Test
    void testSetClientsListIgnoresNullAndUnknownEntries() {
        List<Object> clients = Arrays.asList("FILE", null, 42);
        this.component.setClientsList(clients);
        assertEquals(1, this.component.getClients().size(), "Only the valid entry should be added");
    }

    @Test
    void testSetClientsListWithMultipleEntries() {
        Client client = new Client(Protocol.FILE);
        List<Object> clients = Arrays.asList("HTTP", Protocol.HTTPS, client);
        this.component.setClientsList(clients);
        assertEquals(3, this.component.getClients().size(), "Wrong number of clients");
    }

    @Test
    void testSetDefaultTargetAttachesToDefaultHost() {
        TestRestlet target = new TestRestlet();
        this.component.setDefaultTarget(target);
        assertEquals(
                target,
                this.component.getDefaultHost().getRoutes().getFirst().getNext(),
                "Default target not attached to the default host");
    }

    @Test
    void testSetServerAddsServerInstance() {
        Server server = new Server(Protocol.HTTP, 0);
        this.component.setServer(server);
        assertEquals(1, this.component.getServers().size(), "Wrong number of servers");
        assertTrue(this.component.getServers().contains(server), "Server instance not added");
    }

    @Test
    void testSetServerAddsProtocolNamedByString() {
        this.component.setServer("HTTP");
        assertEquals(1, this.component.getServers().size(), "Wrong number of servers");
        assertEquals(
                Protocol.HTTP, this.component.getServers().getFirst().getProtocols().getFirst());
    }

    @Test
    void testSetServersListIgnoresNullAndUnknownEntries() {
        List<Object> servers = Arrays.asList("HTTP", null, 3.14);
        this.component.setServersList(servers);
        assertEquals(1, this.component.getServers().size(), "Only the valid entry should be added");
    }

    @Test
    void testSetServersListWithEmptyList() {
        this.component.setServersList(Collections.emptyList());
        assertEquals(0, this.component.getServers().size(), "No servers should be added");
    }
}
