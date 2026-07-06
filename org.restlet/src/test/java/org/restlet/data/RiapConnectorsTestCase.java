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

import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.engine.Engine;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.routing.Router;

/** Unit test case for the RIAP Internal routing protocol. */
class RiapConnectorsTestCase {

    /** Test the RIAP client and server connectors. */
    @ParameterizedTest
    @ValueSource(
            strings = {
                "riap://component/app/test",
                "riap://component/app/redirectToInternalResource"
            })
    void testRiapConnectors(final String url) throws IOException {
        ClientResource res = new ClientResource(url);
        Representation rep = res.get();

        assertEquals("hello, world", rep.getText());
    }

    private static Component component;

    @BeforeAll
    static void setUp() throws Exception {
        Engine.clearThreadLocalVariables();
        Engine.register();

        component = new Component();
        component.getServers().add(Protocol.RIAP);
        component.getClients().add(Protocol.RIAP);

        Application app =
                new Application() {
                    @Override
                    public Restlet createInboundRoot() {
                        Router router = new Router(getContext());
                        router.attach(
                                "/test",
                                new Restlet(getContext()) {

                                    @Override
                                    public void handle(Request request, Response response) {
                                        response.setEntity("hello, world", MediaType.TEXT_PLAIN);
                                    }
                                });
                        router.attach(
                                "/redirectToInternalResource",
                                new Restlet(getContext()) {
                                    @Override
                                    public void handle(Request request, Response response) {
                                        ClientResource resource =
                                                new ClientResource("riap://component/app/test");
                                        response.setEntity(resource.get());
                                    }
                                });
                        return router;
                    }
                };

        // Attach the private application
        component.getInternalRouter().attach("/app", app);

        component.start();
    }

    @AfterAll
    static void tearDown() throws Exception {
        Engine.clearThreadLocalVariables();
        component.stop();
        component = null;
    }
}
