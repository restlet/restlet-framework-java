package org.restlet.test;

import junit.framework.TestCase;

import org.restlet.Server;
import org.restlet.data.Protocol;

/**
 * Test the ability of a connector to be restarted.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class RestartTestCase extends TestCase {

    public void testRestart() throws Exception {
        int waitTime = 100;

        Server connector = new Server(Protocol.HTTP, 8182, null);

        System.out.print("Starting connector... ");
        connector.start();
        System.out.println("done");
        Thread.sleep(waitTime);

        System.out.print("Stopping connector... ");
        connector.stop();
        System.out.println("done");
        Thread.sleep(waitTime);

        System.out.print("Restarting connector... ");
        connector.start();
        System.out.println("done");
        Thread.sleep(waitTime);
    }

}
