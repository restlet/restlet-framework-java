/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;

class StdioHelperSelectionTestCase {

    @BeforeEach
    void setUp() {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
    }

    @AfterEach
    void tearDown() {
        Engine.clearThreadLocalVariables();
    }

    @Test
    void rejectsMissingAndAmbiguousHelpersRatherThanSelectingAProtocol() {
        assertThrows(IllegalStateException.class, () -> new Client(Protocol.STDIO));
        assertThrows(IllegalStateException.class, () -> new Server(Protocol.STDIO));
        Engine engine = Engine.getInstance();
        engine.getRegisteredClients().add(new BytesClient(null));
        engine.getRegisteredClients().add(new OtherClient(null));
        engine.getRegisteredServers().add(new BytesServer(null));
        engine.getRegisteredServers().add(new OtherServer(null));
        assertThrows(IllegalStateException.class, () -> new Client(Protocol.STDIO));
        assertThrows(IllegalStateException.class, () -> new Server(Protocol.STDIO));
        Context missing = new Context();
        List<Protocol> protocols = List.of(Protocol.STDIO);
        assertThrows(
                IllegalStateException.class,
                () -> new Client(missing, protocols, "missing.Helper"));
    }

    @Test
    void selectsExplicitHelperOrTheUniqueRegisteredProtocol() {
        Engine engine = Engine.getInstance();
        engine.getRegisteredClients().add(new BytesClient(null));
        engine.getRegisteredServers().add(new BytesServer(null));
        Context uniqueClient = new Context();
        new Client(uniqueClient, Protocol.STDIO);
        assertInstanceOf(
                BytesClient.class, uniqueClient.getAttributes().get("org.restlet.engine.helper"));
        Context uniqueServer = new Context();
        new Server(uniqueServer, Protocol.STDIO);
        assertInstanceOf(
                BytesServer.class, uniqueServer.getAttributes().get("org.restlet.engine.helper"));
        engine.getRegisteredClients().add(new OtherClient(null));
        engine.getRegisteredServers().add(new OtherServer(null));
        Context explicitClient = new Context();
        new Client(explicitClient, List.of(Protocol.STDIO), OtherClient.class.getCanonicalName());
        assertInstanceOf(
                OtherClient.class, explicitClient.getAttributes().get("org.restlet.engine.helper"));
        Context explicitServer = new Context();
        new Server(
                explicitServer,
                List.of(Protocol.STDIO),
                null,
                -1,
                null,
                OtherServer.class.getCanonicalName());
        assertInstanceOf(
                OtherServer.class, explicitServer.getAttributes().get("org.restlet.engine.helper"));
    }

    public static class BytesClient extends ClientHelper {
        public BytesClient(Client client) {
            super(client);
            getProtocols().add(Protocol.STDIO);
        }
    }

    public static class OtherClient extends BytesClient {
        public OtherClient(Client client) {
            super(client);
        }
    }

    public static class BytesServer extends ServerHelper {
        public BytesServer(Server server) {
            super(server);
            getProtocols().add(Protocol.STDIO);
        }
    }

    public static class OtherServer extends BytesServer {
        public OtherServer(Server server) {
            super(server);
        }
    }
}
