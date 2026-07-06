/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.representation.StringRepresentation;

class Bug1145TestCase {
    public static class Bug1145TestCaseRestlet extends Restlet {
        @Override
        public void handle(Request request, Response response) {
            try {
                response.setAccessControlExposeHeaders(new HashSet<>(List.of("Modified")));
                response.setEntity(new StringRepresentation("NO-NPE", MediaType.TEXT_PLAIN));
            } catch (NullPointerException e) {
                response.setEntity(new StringRepresentation("NPE", MediaType.TEXT_PLAIN));
            }
        }
    }

    private int testPort;

    private Client client;
    private Component component;

    @BeforeEach
    void setUpEach() throws Exception {
        Engine.register(true);
        this.client = new Client(Protocol.HTTP);

        this.component = new Component();
        Server server = this.component.getServers().add(Protocol.HTTP, 0);
        this.component.getDefaultHost().attachDefault(new Bug1145TestCaseRestlet());

        this.component.start();
        testPort = server.getActualPort();
    }

    @AfterEach
    void tearDownEach() throws Exception {
        this.client.stop();
        this.component.stop();
        this.component = null;
    }

    @Test
    void test0() throws Exception {
        Request request = new Request(Method.GET, "http://localhost:" + testPort);
        Response result = client.handle(request);
        assertEquals(Status.SUCCESS_OK, result.getStatus());
        assertEquals("NO-NPE", result.getEntity().getText());
    }
}
