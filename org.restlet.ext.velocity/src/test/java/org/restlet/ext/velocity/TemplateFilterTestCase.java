/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.velocity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Encoding;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Directory;
import org.restlet.util.Resolver;

/**
 * Test case for template filters.
 *
 * @author Thierry Boileau
 */
class TemplateFilterTestCase {

    @Test
    void representationShouldBeUsedAsTemplate() throws Exception {
        Request request = new Request(Method.GET, "/template.txt.vm");
        Response response = testApplication.handle(request);
        assertEquals("Method=GET/Path=/template.txt.vm", response.getEntity().getText());
    }

    @Test
    void representationShouldNotBeUsedAsTemplate() throws Exception {
        Request request = new Request(Method.GET, "/notATemplate.txt");
        Response response = testApplication.handle(request);

        assertEquals("Method=${m}/Path=${rp}", response.getEntity().getText());
    }

    /** A Restlet that always answers with a fixed Velocity-encoded entity. */
    private static class VelocityEntityRestlet extends Restlet {
        private final String content;

        VelocityEntityRestlet(String content) {
            this.content = content;
        }

        @Override
        public void handle(Request request, Response response) {
            StringRepresentation entity = new StringRepresentation(content, MediaType.TEXT_PLAIN);
            entity.getEncodings().add(Encoding.VELOCITY);
            response.setEntity(entity);
        }
    }

    /** A representation whose reader always fails, used to trigger a ResourceNotFoundException. */
    private static class BrokenVelocityRepresentation extends StringRepresentation {
        BrokenVelocityRepresentation(String text, MediaType mediaType) {
            super(text, mediaType);
            getEncodings().add(Encoding.VELOCITY);
        }

        @Override
        public Reader getReader() throws IOException {
            throw new IOException("Simulated I/O error");
        }
    }

    @Test
    void defaultConstructor_createsUsableFilter() {
        TemplateFilter filter = new TemplateFilter();
        assertNull(filter.getContext());
    }

    @Test
    void constructorWithContextOnly_setsContext() {
        Context context = new Context();
        TemplateFilter filter = new TemplateFilter(context);
        assertEquals(context, filter.getContext());
    }

    @Test
    void constructorWithMapDataModel_usesMapDataModelWhenHandling() {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("value", "fromMap");

        TemplateFilter filter =
                new TemplateFilter(
                        new Context(), new VelocityEntityRestlet("Value=$value"), dataModel);

        Response response = filter.handle(new Request(Method.GET, "/"));

        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        assertEquals(true, response.getEntity() instanceof TemplateRepresentation);
    }

    @Test
    void constructorWithResolverDataModel_usesResolverDataModelWhenHandling() {
        Resolver<Object> resolver =
                new Resolver<Object>() {
                    @Override
                    public Object resolve(String name) {
                        return "value".equals(name) ? "fromResolver" : null;
                    }
                };

        TemplateFilter filter =
                new TemplateFilter(
                        new Context(), new VelocityEntityRestlet("Value=$value"), resolver);

        Response response = filter.handle(new Request(Method.GET, "/"));

        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        assertEquals(true, response.getEntity() instanceof TemplateRepresentation);
    }

    @Test
    void afterHandle_whenTemplateResourceCannotBeRead_setsStatusNotFound() {
        TemplateFilter filter =
                new TemplateFilter(
                        new Context(),
                        new Restlet() {
                            @Override
                            public void handle(Request request, Response response) {
                                response.setEntity(
                                        new BrokenVelocityRepresentation(
                                                "content", MediaType.TEXT_PLAIN));
                            }
                        });

        Response response = filter.handle(new Request(Method.GET, "/"));

        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    @Test
    void afterHandle_whenTemplateHasParseError_setsStatusInternalServerError() {
        TemplateFilter filter =
                new TemplateFilter(
                        new Context(), new VelocityEntityRestlet("#if($unclosedDirective)"));

        Response response = filter.handle(new Request(Method.GET, "/"));

        assertEquals(Status.SERVER_ERROR_INTERNAL, response.getStatus());
    }

    /**
     * Internal class used for test purpose
     *
     * @author Thierry Boileau
     */
    private static class MyVelocityApplication extends Application {

        MyVelocityApplication() {
            setContext(new Context());
            getContext().setClientDispatcher(new Client(Protocol.CLAP));
        }

        @Override
        public Restlet createInboundRoot() {
            final Directory directory =
                    new Directory(
                            getContext(),
                            LocalReference.createClapReference(
                                    TemplateFilterTestCase.class.getPackage()));

            // Create a Directory that manages a local directory
            return new TemplateFilter(getContext(), directory);
        }
    }

    @BeforeAll
    static void setUpTestCases() {
        Engine.clearThreadLocalVariables();

        // Create an application filtered with Velocity
        testApplication = new MyVelocityApplication();
    }

    private static Application testApplication;
}
