/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.connector;

import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.test.RestletTestCase;

/**
 * Test the ability of a connector to be restarted.
 * 
 * @author Jerome Louvel
 */
public class RestartTestCase extends RestletTestCase {

    public void testRestart() throws Exception {
        final int waitTime = 100;

        final Server connector = new Server(Protocol.HTTP, TEST_PORT,
                (Restlet) null);

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
        connector.stop();
    }

}
