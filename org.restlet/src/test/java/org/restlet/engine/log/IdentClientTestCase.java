/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.log;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link IdentClient}. */
class IdentClientTestCase {

    @Test
    void constructor_withNullClientAddress_doesNothing() {
        IdentClient client = new IdentClient(null, 1234, 80);
        assertNull(client.getHostType());
        assertNull(client.getUserIdentifier());
    }

    @Test
    void constructor_withInvalidPorts_doesNothing() {
        IdentClient client = new IdentClient("127.0.0.1", -1, -1);
        assertNull(client.getHostType());
        assertNull(client.getUserIdentifier());
    }

    @Test
    void constructor_withUnreachableIdentServer_swallowsExceptionAndLeavesFieldsNull() {
        IdentClient client = new IdentClient("127.0.0.1", 1234, 80);
        assertNull(client.getHostType());
        assertNull(client.getUserIdentifier());
    }
}
