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

package org.restlet.test.ext.jaxrs.client;

import java.awt.Point;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ws.rs.core.Application;

import org.restlet.engine.Engine;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.ext.jaxrs.JaxRsClientResource;
import org.restlet.test.ext.jaxrs.services.echo.EchoResource;
import org.restlet.test.ext.jaxrs.services.echo.EchoResourceImpl;
import org.restlet.test.ext.jaxrs.services.tests.JaxRsTestCase;

/**
 * Test the client-side support of JAX-RS extension.
 * 
 * 
 * @see <a
 *      href="https://github.com/restlet/restlet-framework-java/issues/441">Issue
 *      #441</a>
 * @author Shaun Elliott
 */
public class JaxRsClientTest extends JaxRsTestCase {

    // TODO - add tests for remaining param type: MatrixParam,

    private AtomicBoolean _serverStarted = new AtomicBoolean(false);

    private Object lock = new Object();

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
        performEchoTest(new EchoTest() {
            @Override
            public void performTest(EchoResource echoResource) {
                assertEquals("this is a test",
                        echoResource.echo("this is a test"));
            }
        });
    }

    /*
     * Shows the problem addressed in:
     * https://github.com/restlet/restlet-framework-java/issues/441
     */
    public void testEchoPointHeaderParam() throws Exception {
        performEchoTest(new EchoTest() {
            @Override
            public void performTest(EchoResource echoResource) {
                assertEquals(1,
                        echoResource.echoPointHeaderParam(new Point(1, 2)).x);
            }
        });
    }

    public void testEchoPointQueryParam() throws Exception {
        performEchoTest(new EchoTest() {
            @Override
            public void performTest(EchoResource echoResource) {
                assertEquals(3,
                        echoResource.echoPointQueryParam(new Point(3, 4)).x);
            }
        });
    }

    public void testEchoPointPathParam() throws Exception {
        performEchoTest(new EchoTest() {
            @Override
            public void performTest(EchoResource echoResource) {
                assertEquals(5,
                        echoResource.echoPointPathParam(new Point(5, 6)).x);
            }
        });
    }

    public void testEchoPointCookieParam() throws Exception {
        performEchoTest(new EchoTest() {
            @Override
            public void performTest(EchoResource echoResource) {
                assertEquals(7,
                        echoResource.echoPointCookieParam(new Point(7, 8)).x);
            }
        });
    }

    public void testEchoStringFormParam() throws Exception {
        performEchoTest(new EchoTest() {
            @Override
            public void performTest(EchoResource echoResource) {
                assertEquals("formparam",
                        echoResource.echoStringFormParam("formparam"));
            }
        });
    }

    // TODO - regex path params are not quite ready
    // public void testRegexPathParam() throws Exception {
    // performEchoTest(new EchoTest() {
    // @Override
    // public void performTest(EchoResource echoResource) {
    // assertEquals("this_Is_A_Test123",
    // echoResource.echoStringRegexPathParam("this_Is_A_Test123"));
    // }
    // });
    // }

    @SuppressWarnings("deprecation")
    private JaxRsClientTest startSocketServerDaemon()
            throws InterruptedException {

        // there are a bunch of converters registered in the unit test project,
        // we only want xstream
        List<ConverterHelper> registeredConverters = Engine.getInstance()
                .getRegisteredConverters();
        for (int i = registeredConverters.size() - 1; i >= 0; i--) {
            ConverterHelper converterHelper = registeredConverters.get(i);
            if (!(converterHelper instanceof org.restlet.ext.xstream.XstreamConverter)) {
                registeredConverters.remove(i);
            }
        }

        final JaxRsClientTest clientTest = new JaxRsClientTest();
        setUseTcp(true);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    clientTest.startServer(clientTest.createApplication());
                    _serverStarted.set(true);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        t.setDaemon(true);
        t.start();

        return clientTest;
    }

    private void performEchoTest(EchoTest echoTest) throws Exception {
        final JaxRsClientTest clientTest = startSocketServerDaemon();

        // give the server a chance to come up before using it
        while (!_serverStarted.get()) {
            Thread.sleep(100);
            System.out.println("waiting for the server to start...");
        }

        EchoResource echoResource = JaxRsClientResource.createJaxRsClient(
                "http://localhost:" + clientTest.getServerPort(),
                EchoResource.class);

        echoTest.performTest(echoResource);

        synchronized (lock) {
            clientTest.stopServer();
            _serverStarted.set(false);
        }
    }

    private static interface EchoTest {
        void performTest(EchoResource echoResource);
    }

}
