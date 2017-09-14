package org.restlet.test.regression;

import java.util.Arrays;
import java.util.HashSet;

import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;
import org.restlet.test.RestletTestCase;

public class Bug1145TestCase extends RestletTestCase {
    public static class Bug1145TestCaseRestlet extends Restlet {
        @Override
        public void handle(Request request, Response response) {
            try {
                response.setAccessControlExposeHeaders(new HashSet<>(Arrays.asList("Modified")));
                response.setEntity(new StringRepresentation("NO-NPE", MediaType.TEXT_PLAIN));
            }
            catch(NullPointerException e) {
                response.setEntity(new StringRepresentation("NPE", MediaType.TEXT_PLAIN));
            }
        }
    }

    private Client client;

    private Component component;


    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.client = new Client(Protocol.HTTP);

        if (this.component == null) {
            this.component = new Component();
            this.component.getServers().add(Protocol.HTTP, TEST_PORT);
            this.component.getDefaultHost().attachDefault(new Bug1145TestCaseRestlet());
        }

        if (!this.component.isStarted()) {
            this.component.start();
        }
    }

    @Override
    public void tearDown() throws Exception {
        this.client.stop();
        this.component.stop();
        this.component = null;
        super.tearDown();
    }

    public void test0() throws Exception {
        Request request = new Request(Method.GET, "http://localhost:" + TEST_PORT);
        Response result = client.handle(request);
        assertEquals(Status.SUCCESS_OK, result.getStatus());
        assertEquals("NO-NPE", result.getEntity().getText());
    }
}
