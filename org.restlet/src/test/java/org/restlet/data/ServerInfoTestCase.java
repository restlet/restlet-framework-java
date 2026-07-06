/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Test {@link org.restlet.data.ServerInfo}. */
class ServerInfoTestCase {

    @Test
    void constructor_setsDefaultValues() {
        ServerInfo info = new ServerInfo();
        assertNull(info.getAddress());
        assertNull(info.getAgent());
        assertEquals(-1, info.getPort());
        assertFalse(info.isAcceptingRanges());
    }

    @Test
    void settersUpdateState() {
        ServerInfo info = new ServerInfo();
        info.setAddress("127.0.0.1");
        info.setAgent("Restlet-Framework/2.5");
        info.setPort(8080);
        info.setAcceptingRanges(true);

        assertEquals("127.0.0.1", info.getAddress());
        assertEquals("Restlet-Framework/2.5", info.getAgent());
        assertEquals(8080, info.getPort());
        assertTrue(info.isAcceptingRanges());
    }
}
