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

package org.restlet.test.resource;

import java.io.IOException;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Router;
import org.restlet.test.RestletTestCase;

/**
 * Test annotated resources with extra annotations and methods.
 * 
 * @author Thierry Boileau
 */
public class AnnotatedResource9TestCase extends RestletTestCase {

    private static class TestApplication extends Application {

        @Override
        public Restlet createInboundRoot() {
            Router router = new Router(getContext());
            router.attach("/test", MyResource9.class);
            return router;
        }

    }

    public static final Method SI = new Method("SI", "What a method!",
            "http://www.restlet.org", true, true);

    public static final Method SNI = new Method("SNI", "What a method!",
            "http://www.restlet.org", true, false);

    public static final Method USI = new Method("USI", "What a method!",
            "http://www.restlet.org", false, true);

    public static final Method USNI = new Method("USNI", "What a method!",
            "http://www.restlet.org", false, false);

    private Component c;

    private Client client;

    private String uri;

    protected void setUp() throws Exception {
        super.setUp();
        Engine.getInstance().getRegisteredConverters().clear();
        Engine.getInstance().registerDefaultConverters();
        c = new Component();
        final Server server = c.getServers().add(Protocol.HTTP, 0);
        c.getDefaultHost().attach(new TestApplication());
        c.start();

        client = new Client(Protocol.HTTP);

        uri = "http://localhost:" + server.getEphemeralPort() + "/test";
    }

    @Override
    protected void tearDown() throws Exception {
        c.stop();
        c = null;
        client.stop();
        client = null;
        super.tearDown();
    }

    /**
     * Test safe and idempotent method.
     * 
     * @throws IOException
     * @throws ResourceException
     */
    public void testSI() throws IOException, ResourceException {
        Method method = AnnotatedResource9TestCase.SI;

        String text = "text";
        Form form = new Form();
        form.add("key", "value");

        client = new Client(Protocol.HTTP);
        Request request = new Request(method, uri);
        Response response = client.handle(request);
        assertTrue(response.getStatus().isSuccess());
        response.getEntity().release();

        request = new Request(method, uri);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_HTML));
        response = client.handle(request);
        assertEquals(MediaType.TEXT_HTML, response.getEntity().getMediaType());
        response.getEntity().release();

        request = new Request(method, uri);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_PLAIN));
        response = client.handle(request);
        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        response.getEntity().release();

        request = new Request(method, uri);
        request.setEntity(text, MediaType.TEXT_PLAIN);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_HTML));
        response = client.handle(request);
        assertEquals(MediaType.TEXT_HTML, response.getEntity().getMediaType());
        assertEquals("si-html+txt", response.getEntity().getText());

        request = new Request(method, uri);
        request.setEntity(form.getWebRepresentation());
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_PLAIN));
        response = client.handle(request);
        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        assertEquals("si-string+form", response.getEntity().getText());

        request = new Request(method, uri);
        request.setEntity(text, MediaType.TEXT_PLAIN);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_PLAIN));
        response = client.handle(request);
        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        assertEquals("si-string+text", response.getEntity().getText());

        request = new Request(method, uri);
        request.setEntity(form.getWebRepresentation());
        response = client.handle(request);
        assertEquals(MediaType.TEXT_HTML, response.getEntity().getMediaType());
        assertEquals("si-html+form", response.getEntity().getText());
    }

    /**
     * Test safe and non-idempotent method.
     * 
     * @throws IOException
     * @throws ResourceException
     */
    public void testSNI() throws IOException, ResourceException {
        Method method = AnnotatedResource9TestCase.SNI;

        String text = "text";
        Form form = new Form();
        form.add("key", "value");

        client = new Client(Protocol.HTTP);
        Request request = new Request(method, uri);
        Response response = client.handle(request);
        assertTrue(response.getStatus().isSuccess());
        response.getEntity().release();

        request = new Request(method, uri);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_HTML));
        response = client.handle(request);
        assertEquals(MediaType.TEXT_HTML, response.getEntity().getMediaType());
        response.getEntity().release();

        request = new Request(method, uri);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_PLAIN));
        response = client.handle(request);
        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        response.getEntity().release();

        request = new Request(method, uri);
        request.setEntity(text, MediaType.TEXT_PLAIN);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_HTML));
        response = client.handle(request);
        assertEquals(MediaType.TEXT_HTML, response.getEntity().getMediaType());
        assertEquals("sni-html+txt", response.getEntity().getText());

        request = new Request(method, uri);
        request.setEntity(form.getWebRepresentation());
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_PLAIN));
        response = client.handle(request);
        assertEquals(MediaType.TEXT_HTML, response.getEntity().getMediaType());
        assertEquals("sni-html+form", response.getEntity().getText());

        request = new Request(method, uri);
        request.setEntity(text, MediaType.TEXT_PLAIN);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_PLAIN));
        response = client.handle(request);
        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        assertEquals("sni-string+text", response.getEntity().getText());
    }

    /**
     * Test unsafe and idempotent method.
     * 
     * @throws IOException
     * @throws ResourceException
     */
    public void testUSI() throws IOException, ResourceException {
        Method method = AnnotatedResource9TestCase.USI;

        String text = "text";
        Form form = new Form();
        form.add("key", "value");

        client = new Client(Protocol.HTTP);
        Request request = new Request(method, uri);
        Response response = client.handle(request);
        assertTrue(response.getStatus().isSuccess());
        response.getEntity().release();

        request = new Request(method, uri);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_HTML));
        response = client.handle(request);
        assertEquals(MediaType.TEXT_HTML, response.getEntity().getMediaType());
        response.getEntity().release();

        request = new Request(method, uri);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_PLAIN));
        response = client.handle(request);
        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        response.getEntity().release();

        request = new Request(method, uri);
        request.setEntity(text, MediaType.TEXT_PLAIN);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_HTML));
        response = client.handle(request);
        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        assertEquals("usi-string+text", response.getEntity().getText());

        request = new Request(method, uri);
        request.setEntity(form.getWebRepresentation());
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_PLAIN));
        response = client.handle(request);
        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        assertEquals("usi-string+form", response.getEntity().getText());

        request = new Request(method, uri);
        request.setEntity(text, MediaType.TEXT_PLAIN);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_PLAIN));
        response = client.handle(request);
        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        assertEquals("usi-string+text", response.getEntity().getText());

        request = new Request(AnnotatedResource9TestCase.USI, uri);
        request.setEntity(form.getWebRepresentation());
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_PLAIN));
        response = client.handle(request);
        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        assertEquals("usi-string+form", response.getEntity().getText());

        request = new Request(AnnotatedResource9TestCase.USI, uri);
        request.setEntity(form.getWebRepresentation());
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_HTML));
        response = client.handle(request);
        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        assertEquals("usi-string+form", response.getEntity().getText());

    }

    /**
     * Test unsafe and non-idempotent method.
     * 
     * @throws IOException
     * @throws ResourceException
     */
    public void testUSNI() throws IOException, ResourceException {
        Method method = AnnotatedResource9TestCase.USNI;

        String text = "text";
        Form form = new Form();
        form.add("key", "value");

        client = new Client(Protocol.HTTP);
        Request request = new Request(method, uri);
        Response response = client.handle(request);
        assertTrue(response.getStatus().isSuccess());
        response.getEntity().release();

        request = new Request(method, uri);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_HTML));
        response = client.handle(request);
        assertEquals(MediaType.TEXT_HTML, response.getEntity().getMediaType());
        response.getEntity().release();

        request = new Request(method, uri);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_PLAIN));
        response = client.handle(request);
        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        response.getEntity().release();

        request = new Request(method, uri);
        request.setEntity(text, MediaType.TEXT_PLAIN);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_HTML));
        response = client.handle(request);
        assertEquals(MediaType.TEXT_HTML, response.getEntity().getMediaType());
        assertEquals("usni-html+txt", response.getEntity().getText());

        request = new Request(method, uri);
        request.setEntity(form.getWebRepresentation());
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_HTML));
        response = client.handle(request);
        assertEquals(MediaType.TEXT_HTML, response.getEntity().getMediaType());
        assertEquals("usni-html+form", response.getEntity().getText());

        request = new Request(method, uri);
        request.setEntity(text, MediaType.TEXT_PLAIN);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_PLAIN));
        response = client.handle(request);
        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        assertEquals("usni-string+text", response.getEntity().getText());
    }

}
