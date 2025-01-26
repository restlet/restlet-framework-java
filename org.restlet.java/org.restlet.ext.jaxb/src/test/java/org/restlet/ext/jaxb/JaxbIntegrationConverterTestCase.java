/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.jaxb;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.restlet.*;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.resource.*;
import org.restlet.routing.Router;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple Integration Tests that uses the JAXB Converter to perform POST, PUT
 * and GET operations.
 * 
 * Note: You must have registered the JaxbConverter in
 * META-INF/services/org.restlet.engine.converter.ConverterHelper
 * 
 * @author Sanjay Acharya
 */
public class JaxbIntegrationConverterTestCase {

    private static final String IN_STRING = "foo";

    private static final String HELLO_OUT_STRING = "Hello World " + IN_STRING;

    private Component component;

    private String uri;

    @BeforeEach
    public void setUpEach() throws Exception {
        // make sure the jaxb converter is registered
        Engine.getInstance().getRegisteredConverters().add(new JaxbConverter());
        this.component = new Component();
        final Server server = this.component.getServers().add(Protocol.HTTP, 0);
        final Application application = createApplication();
        this.component.getDefaultHost().attach(application);
        this.component.start();
        uri = "http://localhost:" + server.getEphemeralPort() + "/test";
    }

    @AfterEach
    public void tearDownEach() throws Exception {
        if (component != null) {
            component.stop();
        }

        this.component = null;
    }

    protected Application createApplication() {
        return new Application() {
            @Override
            public Restlet createInboundRoot() {
                final Router router = new Router(getContext());
                router.attach("/test", SampleResource.class);
                return router;
            }
        };
    }

    @ParameterizedTest
    @MethodSource("integrationTestCases")
    public void testIntegration(final Method method, final String input, final String expectedResponse) throws Exception {
        Client client = new Client(Protocol.HTTP);
        Request request = new Request(method, uri);
        request.setEntity(new JaxbRepresentation<>(new Sample(input)));

        Response response = client.handle(request);

        JaxbRepresentation<Sample> resultRepresentation = new JaxbRepresentation<>(response.getEntity(), Sample.class);
        Sample sample = resultRepresentation.getObject();
        assertEquals(expectedResponse, sample.getVal());
    }

    public static Stream<Arguments> integrationTestCases() {
        return Stream.of(
                Arguments.of(Method.POST, IN_STRING, HELLO_OUT_STRING),
                Arguments.of(Method.PUT, IN_STRING, HELLO_OUT_STRING),
                Arguments.of(Method.GET, null, IN_STRING)
        );
    }

    /**
     * Test POST, PUT and GET using the ClientResource class
     *
     */
    @Test
    public void testWithClientResource() {
        ClientResource sampleResource = new ClientResource(uri);
        List<Preference<MediaType>> m = List.of(new Preference<>(MediaType.APPLICATION_XML));
        sampleResource.getClientInfo().setAcceptedMediaTypes(m);

        Sample sample = new Sample(IN_STRING);
        sample = sampleResource.post(sample, Sample.class);
        assertEquals(HELLO_OUT_STRING, sample.getVal());

        sampleResource.put(sample);
        assertTrue(sampleResource.getStatus().isSuccess());

        sample = sampleResource.put(sample, Sample.class);
        assertEquals(HELLO_OUT_STRING, sample.getVal());

        sample = sampleResource.get(Sample.class);
        assertEquals(IN_STRING, sample.getVal());
    }

    public static class SampleResource extends ServerResource {
        @Post("xml")
        public Sample post(Sample sample) {
            assertNotNull(sample);
            return new Sample(HELLO_OUT_STRING);
        }

        @Get("xml")
        public Sample getSample() {
            return new Sample(IN_STRING);
        }

        @Put("xml:xml")
        public JaxbRepresentation<Sample> putSample(Sample sample) {
            assertNotNull(sample);
            return new JaxbRepresentation<>(new Sample(HELLO_OUT_STRING));
        }
    }
}
