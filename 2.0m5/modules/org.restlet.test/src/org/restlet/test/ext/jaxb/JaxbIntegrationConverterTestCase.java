/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test.ext.jaxb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
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
public class JaxbIntegrationConverterTestCase extends TestCase {
    private static final String SERVER_URI = "http://localhost:9091";

    private Component component;

    private static final String IN_STRING = "foo";

    private static final String HELLO_OUT_STRING = "Hello World " + IN_STRING;

    @Override
    public void setUp() throws Exception {
        component = new Component();
        component.getServers().add(Protocol.HTTP, 9091);
        component.getDefaultHost().attach(new SampleApplication());
        component.start();
    }

    @Override
    public void tearDown() throws Exception {
        if (component != null) {
            component.stop();
        }
    }

    /**
     * Test POST, PUT and GET using the Client class
     * 
     * @throws Exception
     */
    public void testIntegration() throws Exception {
        Client client = new Client(new Context(), Arrays.asList(Protocol.HTTP));
        Request request = new Request(Method.POST, SERVER_URI);
        request
                .setEntity(new JaxbRepresentation<Sample>(new Sample(IN_STRING)));

        Response response = client.handle(request);

        JaxbRepresentation<Sample> resultRepresentation = new JaxbRepresentation<Sample>(
                response.getEntity(), Sample.class);
        Sample sample = resultRepresentation.getObject();
        assertEquals(HELLO_OUT_STRING, sample.getVal());

        request = new Request(Method.PUT, SERVER_URI);
        request
                .setEntity(new JaxbRepresentation<Sample>(new Sample(IN_STRING)));

        response = client.handle(request);
        resultRepresentation = new JaxbRepresentation<Sample>(response
                .getEntity(), Sample.class);
        sample = resultRepresentation.getObject();
        assertEquals(HELLO_OUT_STRING, sample.getVal());

        request = new Request(Method.GET, SERVER_URI);
        response = client.handle(request);
        resultRepresentation = new JaxbRepresentation<Sample>(response
                .getEntity(), Sample.class);
        sample = resultRepresentation.getObject();
        assertEquals(IN_STRING, sample.getVal());
    }

    /**
     * Test POST, PUT and GET using the ClientResource class
     * 
     * @throws Exception
     */
    public void testWithClientResource() throws Exception {
        ClientResource sampleResource = new ClientResource(SERVER_URI);
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

    private static class SampleApplication extends Application {
        public SampleApplication() {
            super();
        }

        @Override
        public Restlet createRoot() {
            Router root = new Router(getContext());
            root.attachDefault(SampleResource.class);
            return root;
        }
    }

    public static class SampleResource extends ServerResource {
        @Post
        public Sample post(Sample sample) {
            assertNotNull(sample);
            return new Sample(HELLO_OUT_STRING);
        }

        @Get
        public Sample getSample() {
            return new Sample(IN_STRING);
        }

        @Put
        public Sample putSample(Sample sample) {
            assertNotNull(sample);
            return new Sample(HELLO_OUT_STRING);
        }
    }
}
