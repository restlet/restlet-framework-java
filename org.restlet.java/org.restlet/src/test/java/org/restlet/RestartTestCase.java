/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet;

import org.junit.jupiter.api.Test;
import org.restlet.data.Protocol;

import java.time.Duration;

/**
 * Test the ability of a connector to be restarted.
 * 
 * @author Jerome Louvel
 */
public class RestartTestCase {

	@Test
    public void testRestart() throws Exception {
        final Duration waitTime = Duration.ofMillis(10);

        final Server connector = new Server(Protocol.HTTP, 0, (Restlet) null);

        System.out.print("Starting connector... ");
        connector.start();
        System.out.println("done");
        Thread.sleep(waitTime.toMillis());

        System.out.print("Stopping connector... ");
        connector.stop();
        System.out.println("done");
        Thread.sleep(waitTime.toMillis());

        System.out.print("Restarting connector... ");
        connector.start();
        System.out.println("done");
        Thread.sleep(waitTime.toMillis());
        connector.stop();
    }

}
