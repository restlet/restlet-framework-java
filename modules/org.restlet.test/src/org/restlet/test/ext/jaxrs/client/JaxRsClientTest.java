/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.ext.jaxrs.client;

import java.awt.Point;
import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.restlet.ext.jaxrs.JaxRsClientResource;
import org.restlet.test.ext.jaxrs.services.point.EchoResource;
import org.restlet.test.ext.jaxrs.services.point.EchoResourceImpl;
import org.restlet.test.ext.jaxrs.services.tests.JaxRsTestCase;

/**
 * Test the client-side support of JAX-RS extension.
 * 
 * 
 * @see <a
 *      href="https://github.com/restlet/restlet-framework-java/issues/441">Issue
 *      #441</a>
 * @author Shaun Elliot
 */
public class JaxRsClientTest extends JaxRsTestCase {

    @Override
    protected Application getApplication() {
        return new Application() {
            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Set<Class<?>> getClasses() {
                return (Set) Collections.singleton(EchoResourceImpl.class);
            }
        };
    }

    public void testEchoString() throws Exception {
        final JaxRsClientTest clientTest = startSocketServerDaemon();

        EchoResource echoResource = JaxRsClientResource.createJaxRsClient(
                "http://localhost:" + clientTest.getServerPort(),
                EchoResource.class);

        assertEquals("this is a test", echoResource.echo("this is a test"));

        clientTest.stopServer();
    }

    /*
     * Shows the problem addressed in:
     * https://github.com/restlet/restlet-framework-java/issues/441
     */
    public void testEchoPoint() throws Exception {
        final JaxRsClientTest clientTest = startSocketServerDaemon();

        EchoResource echoResource = JaxRsClientResource.createJaxRsClient(
                "http://localhost:" + clientTest.getServerPort(),
                EchoResource.class);

        assertEquals(1, echoResource.echoPoint(new Point(1, 2)).x);

        clientTest.stopServer();
    }

    private JaxRsClientTest startSocketServerDaemon()
            throws InterruptedException {
        final JaxRsClientTest clientTest = new JaxRsClientTest();
        setUseTcp(true);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    clientTest.startServer(clientTest.createApplication());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        t.setDaemon(true);
        t.start();

        // give the server a chance to come up before using it
        Thread.sleep(500);
        return clientTest;
    }

}
