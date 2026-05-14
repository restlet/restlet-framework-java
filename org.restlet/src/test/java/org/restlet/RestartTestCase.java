/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.restlet.data.Protocol;

/**
 * Test the ability of a connector to be restarted.
 *
 * @author Jerome Louvel
 */
class RestartTestCase {

    @Test
    void testRestart() throws Exception {
        final Duration waitTime = Duration.ofMillis(10);

        final Server connector = new Server(Protocol.HTTP, 0, (Restlet) null);

        connector.start();
        assertTrue(connector.isStarted());
        Thread.sleep(waitTime.toMillis());

        connector.stop();
        assertFalse(connector.isStarted());
        Thread.sleep(waitTime.toMillis());

        connector.start();
        assertTrue(connector.isStarted());
        Thread.sleep(waitTime.toMillis());
        connector.stop();
        assertFalse(connector.isStarted());
    }
}
