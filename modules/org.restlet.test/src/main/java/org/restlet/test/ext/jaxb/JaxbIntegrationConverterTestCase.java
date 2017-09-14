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

package org.restlet.test.ext.jaxb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.restlet.test.RestletTestCase;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

/**
 * Simple Integration Tests that uses the JAXB Converter to perform POST, PUT
 * and GET operations.
 * 
 * Note: You must have registered the JaxbConverter in
 * META-INF/services/org.restlet.engine.converter.ConverterHelper
 * 
 * @author Sanjay Acharya
 */
public class JaxbIntegrationConverterTestCase extends RestletTestCase {
    private static final String IN_STRING = "foo";

    private static final String HELLO_OUT_STRING = "Hello World " + IN_STRING;

    private Component component;

    private String uri;

    public void setUp() throws Exception {
        super.setUp();
        this.component = new Component();
        final Server server = this.component.getServers().add(Protocol.HTTP, 0);
        final Application application = createApplication(this.component);
        this.component.getDefaultHost().attach(application);
        this.component.start();
        uri = "http://localhost:" + server.getEphemeralPort() + "/test";
    }

    public void tearDown() throws Exception {
        if (component != null) {
            component.stop();
        }

        this.component = null;
        super.tearDown();
    }

    protected Application createApplication(Component component) {
        final Application application = new Application() {
            @Override
            public Restlet createInboundRoot() {
                final Router router = new Router(getContext());
                router.attach("/test", SampleResource.class);
                return router;
            }
        };

        return application;
    }

    /**
     * Test POST, PUT and GET using the Client class
     * 
     * @throws Exception
     */
    public void testIntegration() throws Exception {
        Client client = new Client(new Context(), Arrays.asList(Protocol.HTTP));
        Request request = new Request(Method.POST, uri);
        request.setEntity(new JaxbRepresentation<Sample>(new Sample(IN_STRING)));

        Response response = client.handle(request);

        JaxbRepresentation<Sample> resultRepresentation = new JaxbRepresentation<Sample>(
                response.getEntity(), Sample.class);
        Sample sample = resultRepresentation.getObject();
        assertEquals(HELLO_OUT_STRING, sample.getVal());

        request = new Request(Method.PUT, uri);
        request.setEntity(new JaxbRepresentation<Sample>(new Sample(IN_STRING)));

        response = client.handle(request);
        resultRepresentation = new JaxbRepresentation<Sample>(
                response.getEntity(), Sample.class);
        sample = resultRepresentation.getObject();
        assertEquals(HELLO_OUT_STRING, sample.getVal());

        request = new Request(Method.GET, uri);
        response = client.handle(request);
        resultRepresentation = new JaxbRepresentation<Sample>(
                response.getEntity(), Sample.class);
        sample = resultRepresentation.getObject();
        assertEquals(IN_STRING, sample.getVal());

        client.stop();
    }

    /**
     * Test POST, PUT and GET using the ClientResource class
     * 
     * @throws Exception
     */
    public void testWithClientResource() throws Exception {
        ClientResource sampleResource = new ClientResource(uri);
        List<Preference<MediaType>> m = new ArrayList<Preference<MediaType>>();
        m.add(new Preference<MediaType>(MediaType.APPLICATION_XML));
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
            return new JaxbRepresentation<Sample>(new Sample(HELLO_OUT_STRING));
        }
    }
}
