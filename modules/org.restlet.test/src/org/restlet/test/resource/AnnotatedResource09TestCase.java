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

package org.restlet.test.resource;

import java.io.IOException;

import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Router;

/**
 * Test annotated resources with extra annotations and methods.
 * 
 * @author Thierry Boileau
 */
public class AnnotatedResource09TestCase extends InternalConnectorTestCase {

    public static final Method SI = new Method("SI", "What a method!",
            "http://restlet.org", true, true);

    public static final Method SNI = new Method("SNI", "What a method!",
            "http://restlet.org", true, false);

    public static final Method USI = new Method("USI", "What a method!",
            "http://restlet.org", false, true);

    public static final Method USNI = new Method("USNI", "What a method!",
            "http://restlet.org", false, false);

    protected Application createApplication(final String path) {
        return new Application() {
            @Override
            public Restlet createInboundRoot() {
                Router router = new Router(getContext());
                router.attach(path, MyResource09.class);
                return router;
            }
        };
    }

    /**
     * Test safe and idempotent method.
     * 
     * @throws IOException
     * @throws ResourceException
     */
    public void testSI() throws IOException, ResourceException {
        Method method = AnnotatedResource09TestCase.SI;

        String text = "text";
        Form form = new Form();
        form.add("key", "value");

        Request request = createRequest(method);
        Response response = handle(request);
        assertTrue(response.getStatus().isSuccess());
        releaseResponse(response);

        request = createRequest(method);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_HTML));
        response = handle(request);
        assertEquals(MediaType.TEXT_HTML, response.getEntity().getMediaType());
        releaseResponse(response);

        request = createRequest(method);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_PLAIN));
        response = handle(request);
        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        releaseResponse(response);

        request = createRequest(method);
        request.setEntity(text, MediaType.TEXT_PLAIN);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_HTML));
        response = handle(request);
        assertEquals(MediaType.TEXT_HTML, response.getEntity().getMediaType());
        assertEquals("si-html+txt", response.getEntity().getText());

        request = createRequest(method);
        request.setEntity(form.getWebRepresentation());
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_PLAIN));
        response = handle(request);
        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        assertEquals("si-string+form", response.getEntity().getText());

        request = createRequest(method);
        request.setEntity(text, MediaType.TEXT_PLAIN);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_PLAIN));
        response = handle(request);
        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        assertEquals("si-string+text", response.getEntity().getText());

        request = createRequest(method);
        request.setEntity(form.getWebRepresentation());
        response = handle(request);
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
        Method method = AnnotatedResource09TestCase.SNI;

        String text = "text";
        Form form = new Form();
        form.add("key", "value");

        Request request = createRequest(method);
        Response response = handle(request);
        assertTrue(response.getStatus().isSuccess());
        releaseResponse(response);

        request = createRequest(method);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_HTML));
        response = handle(request);
        assertEquals(MediaType.TEXT_HTML, response.getEntity().getMediaType());
        releaseResponse(response);

        request = createRequest(method);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_PLAIN));
        response = handle(request);
        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        releaseResponse(response);

        request = createRequest(method);
        request.setEntity(text, MediaType.TEXT_PLAIN);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_HTML));
        response = handle(request);
        assertEquals(MediaType.TEXT_HTML, response.getEntity().getMediaType());
        assertEquals("sni-html+txt", response.getEntity().getText());

        request = createRequest(method);
        request.setEntity(form.getWebRepresentation());
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_PLAIN));
        response = handle(request);
        assertEquals(MediaType.TEXT_HTML, response.getEntity().getMediaType());
        assertEquals("sni-html+form", response.getEntity().getText());

        request = createRequest(method);
        request.setEntity(text, MediaType.TEXT_PLAIN);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_PLAIN));
        response = handle(request);
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
        Method method = AnnotatedResource09TestCase.USI;

        String text = "text";
        Form form = new Form();
        form.add("key", "value");

        Request request = createRequest(method);
        Response response = getClient().handle(request);
        assertTrue(response.getStatus().isSuccess());
        releaseResponse(response);

        request = createRequest(method);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_HTML));
        response = handle(request);
        assertEquals(MediaType.TEXT_HTML, response.getEntity().getMediaType());
        releaseResponse(response);

        request = createRequest(method);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_PLAIN));
        response = handle(request);
        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        releaseResponse(response);

        request = createRequest(method);
        request.setEntity(text, MediaType.TEXT_PLAIN);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_HTML));
        response = handle(request);
        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        assertEquals("usi-string+text", response.getEntity().getText());

        request = createRequest(method);
        request.setEntity(form.getWebRepresentation());
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_PLAIN));
        response = handle(request);
        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        assertEquals("usi-string+form", response.getEntity().getText());

        request = createRequest(method);
        request.setEntity(text, MediaType.TEXT_PLAIN);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_PLAIN));
        response = handle(request);
        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        assertEquals("usi-string+text", response.getEntity().getText());

        request = createRequest(method);
        request.setEntity(form.getWebRepresentation());
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_PLAIN));
        response = handle(request);
        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        assertEquals("usi-string+form", response.getEntity().getText());

        request = createRequest(method);
        request.setEntity(form.getWebRepresentation());
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_HTML));
        response = handle(request);
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
        Method method = AnnotatedResource09TestCase.USNI;

        String text = "text";
        Form form = new Form();
        form.add("key", "value");

        Request request = createRequest(method);
        Response response = getClient().handle(request);
        assertTrue(response.getStatus().isSuccess());
        releaseResponse(response);

        request = createRequest(method);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_HTML));
        response = handle(request);
        assertEquals(MediaType.TEXT_HTML, response.getEntity().getMediaType());
        releaseResponse(response);

        request = createRequest(method);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_PLAIN));
        response = handle(request);
        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        releaseResponse(response);

        request = createRequest(method);
        request.setEntity(text, MediaType.TEXT_PLAIN);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_HTML));
        response = handle(request);
        assertEquals(MediaType.TEXT_HTML, response.getEntity().getMediaType());
        assertEquals("usni-html+txt", response.getEntity().getText());

        request = createRequest(method);
        request.setEntity(form.getWebRepresentation());
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_HTML));
        response = handle(request);
        assertEquals(MediaType.TEXT_HTML, response.getEntity().getMediaType());
        assertEquals("usni-html+form", response.getEntity().getText());

        request = createRequest(method);
        request.setEntity(text, MediaType.TEXT_PLAIN);
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.TEXT_PLAIN));
        response = handle(request);
        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        assertEquals("usni-string+text", response.getEntity().getText());
    }

}
