/**
 * Copyright 2005-2024 Qlik
 * <p>
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * <p>
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * <p>
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * <p>
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * https://restlet.talend.com/
 * <p>
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.restlet.data.MediaType.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Form;
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

    private final static String TEXT = "text";
    private final static Form FORM = new Form("name=value");

    static Stream<Method> methodsProvider() {
        return Stream.of(SI, SNI, USI, USNI);
    }

    @ParameterizedTest
    @MethodSource("methodsProvider")
    public void testCustomMethod(final Method method) throws IOException, ResourceException {
        final String methodName = method.getName().toLowerCase();

        Request request = createRequest(method);
        Response response = handle(request);
        assertTrue(response.getStatus().isSuccess());
        releaseResponse(response);

        // the annotated method of the ServerResource that generates HTML and does not require an entity is invoked
        request = createRequest(method);
        request.getClientInfo().getAcceptedMediaTypes().add(new Preference<>(TEXT_HTML));
        response = handle(request);
        assertEquals(TEXT_HTML, response.getEntity().getMediaType());
        assertEquals(methodName + "-:html", response.getEntity().getText());
        releaseResponse(response);

        // Invokes the annotated method of the ServerResource that
        //  - generates XML
        //  - does not require an entity
        //  - which annotation does not handle input media type (@SIMethod(":xml") is preferred over @SIMethod("xml"))
        request = createRequest(method);
        request.getClientInfo().getAcceptedMediaTypes().add(new Preference<>(APPLICATION_XML));
        response = handle(request);
        assertEquals(APPLICATION_XML, response.getEntity().getMediaType());
        assertEquals(methodName + "-:xml", response.getEntity().getText());
        releaseResponse(response);

        request = createRequest(method);
        request.setEntity(TEXT, TEXT_PLAIN);
        request.getClientInfo().getAcceptedMediaTypes().add(new Preference<>(TEXT_HTML));
        response = handle(request);
        assertEquals(TEXT_HTML, response.getEntity().getMediaType());
        assertEquals(methodName + "-txt:html", response.getEntity().getText());

        request = createRequest(method);
        request.setEntity(FORM.getWebRepresentation());
        request.getClientInfo().getAcceptedMediaTypes().add(new Preference<>(TEXT_PLAIN));
        response = handle(request);
        assertEquals(TEXT_PLAIN, response.getEntity().getMediaType());
        assertEquals(methodName + "-form:txt", response.getEntity().getText());

        request = createRequest(method);
        request.setEntity(TEXT, TEXT_PLAIN);
        request.getClientInfo().getAcceptedMediaTypes().add(new Preference<>(TEXT_PLAIN));
        response = handle(request);
        assertEquals(TEXT_PLAIN, response.getEntity().getMediaType());
        assertEquals(methodName + "-txt|text", response.getEntity().getText());

        // Without client's preference, one of the annotated method of the ServerResource that handles a Form is invoked
        // (declarations order in class cannot be guaranteed)
        request = createRequest(method);
        request.setEntity(FORM.getWebRepresentation());
        response = handle(request);

        assertTrue(Arrays.asList(TEXT_PLAIN, TEXT_HTML).contains(response.getEntity().getMediaType()));
        assertTrue(Arrays.asList(methodName + "-form:txt", methodName + "-form:html").contains(response.getEntity().getText()));
    }

}
