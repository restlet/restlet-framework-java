/*
 * Copyright 2005-2007 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

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
        connector.stop();
    }

}
