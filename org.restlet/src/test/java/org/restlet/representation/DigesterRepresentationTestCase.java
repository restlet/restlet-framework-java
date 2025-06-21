/**
 * Copyright 2005-2024 Qlik
 * <p>
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * <p>
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.representation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.*;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.routing.Router;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test {@link DigesterRepresentation}.
 *
 * @author Thierry Boileau
 */
public class DigesterRepresentationTestCase {

    /** Component used for the tests. */
    private Component component;
    private int serverPort;

    @BeforeEach
    protected void setUpEach() throws Exception {
        Engine.register();
        component = new Component();
        final Server server = component.getServers().add(Protocol.HTTP, 0);
        component.getDefaultHost().attach(new TestDigestApplication());
        component.start();
        serverPort = server.getActualPort();
    }

    @AfterEach
    protected void tearDownEach() throws Exception {
        component.stop();
        component = null;
    }

    @Test
    public void checkDigestSentByClient() {
        Client client = new Client(Protocol.HTTP);
        Request request = new Request(Method.PUT, "http://localhost:" + serverPort + "/checkRequestEntity");
        request.setEntity(getDigestedRepresentation("0123456789"));
        Response response = client.handle(request);

        assertEquals(Status.SUCCESS_OK, response.getStatus());
    }

    @Test
    public void checkDigestSentByServer() {
        Client client = new Client(Protocol.HTTP);
        Request request = new Request(Method.GET, "http://localhost:" + serverPort + "/checkResponseEntity");
        Response response = client.handle(request);

        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertTrue(checkRepresentation(response.getEntity()));
    }

    /**
     * Internal class used for test purpose.
     *
     */
    private class TestDigestApplication extends Application {

        @Override
        public Restlet createInboundRoot() {
            Router router = new Router(getContext());

            router.attach("/checkRequestEntity", new Restlet() {
                @Override
                public void handle(Request request, Response response) {
                    Status responseStatus = checkRepresentation(request.getEntity())
                            ? Status.SUCCESS_OK
                            : Status.CLIENT_ERROR_BAD_REQUEST;
                    response.setStatus(responseStatus);
                }
            });

            router.attach("/checkResponseEntity", new Restlet() {
                @Override
                public void handle(Request request, Response response) {
                    response.setEntity(getDigestedRepresentation("9876543210"));
                }
            });
            return router;
        }
    }

    private boolean checkRepresentation(final Representation representation) {
        try {
            DigesterRepresentation digester = new DigesterRepresentation(representation);
            digester.exhaust();

            return digester.checkDigest();
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private DigesterRepresentation getDigestedRepresentation(final String string) {
        try {
            DigesterRepresentation digester = new DigesterRepresentation(new StringRepresentation(string));
            // Consume first
            digester.exhaust();
            // Set the digest
            digester.setDigest(digester.computeDigest());

            return digester;
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
