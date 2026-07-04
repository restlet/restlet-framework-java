/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.adapter;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.restlet.Server;
import org.restlet.data.Protocol;

/** Unit tests for {@link JettyHandler}. */
class JettyHandlerTestCase {

    @Test
    void constructor_defaultsToHttp() {
        Server server = new Server(Protocol.HTTP, 0);
        JettyHandler handler = new JettyHandler(server);
        assertNotNull(handler);
    }

    @Test
    void constructor_secureFlagTrue_usesHttpsHelper() {
        Server server = new Server(Protocol.HTTPS, 0);
        JettyHandler handler = new JettyHandler(server, true);
        assertNotNull(handler);
    }

    @Test
    void constructor_secureFlagFalse_usesHttpHelper() {
        Server server = new Server(Protocol.HTTP, 0);
        JettyHandler handler = new JettyHandler(server, false);
        assertNotNull(handler);
    }
}
