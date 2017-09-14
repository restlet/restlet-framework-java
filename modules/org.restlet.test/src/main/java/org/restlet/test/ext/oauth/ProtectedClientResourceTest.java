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

package org.restlet.test.ext.oauth;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.restlet.ext.oauth.OAuthResourceDefs.ACCESS_TOKEN;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.ext.oauth.ProtectedClientResource;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

/**
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class ProtectedClientResourceTest extends OAuthTestBase {

    public static class StubApplication extends Application {

        @Override
        public synchronized Restlet createInboundRoot() {
            Router router = new Router(getContext());
            router.attach("/resource1", StubServerResource1.class);
            router.attach("/resource2", StubServerResource2.class);
            router.attach("/resource3", StubServerResource3.class);
            return router;
        }
    }

    @BeforeClass
    public static void setupStub() throws Exception {
        // Setup Restlet
        component = new Component();
        component.getClients().add(Protocol.HTTP);
        component.getServers().add(Protocol.HTTP, 8080);
        component.getDefaultHost().attach("/app", new StubApplication());
        component.start();
    }

    @AfterClass
    public static void destroyStub() throws Exception {
        component.stop();
    }

    /**
     * Test case 1: Bearer Token (Authorization Request Header Field)
     */
    public static class StubServerResource1 extends ServerResource {

        @Get
        public Representation represent() {
            ChallengeResponse cr = getChallengeResponse();
            assertThat(cr.getScheme().getTechnicalName(),
                    is(ChallengeScheme.HTTP_OAUTH_BEARER.getTechnicalName()));
            assertThat(cr.getRawValue(), is(STUB_ACCESS_TOKEN));
            return new EmptyRepresentation();
        }
    }

    @Test
    public void testCase1() {
        ProtectedClientResource resource = new ProtectedClientResource(
                new Reference(baseURI, "/app/resource1"));
        resource.setToken(STUB_TOKEN);
        resource.setUseBodyMethod(false);
        resource.get();
    }

    /**
     * Test case 2: Bearer Token (Form-Encoded Body Parameter)
     */
    public static class StubServerResource2 extends ServerResource {

        @Post
        public Representation represent(Representation representation) {
            Form form = new Form(representation);
            assertThat(form.getFirstValue(ACCESS_TOKEN), is(STUB_ACCESS_TOKEN));
            assertThat(form.getFirstValue("foo"), is("bar"));
            return new EmptyRepresentation();
        }
    }

    @Test
    public void testCase2() {
        ProtectedClientResource resource = new ProtectedClientResource(
                new Reference(baseURI, "/app/resource2"));
        resource.setToken(STUB_TOKEN);
        resource.setUseBodyMethod(true);
        Form form = new Form();
        form.add("foo", "bar");
        resource.post(form.getWebRepresentation());
    }

    /**
     * Test case 3: Bearer Token (URI Query Parameter)
     */
    public static class StubServerResource3 extends ServerResource {

        @Get
        public Representation represent() {
            Form form = getQuery();
            assertThat(form.getFirstValue(ACCESS_TOKEN), is(STUB_ACCESS_TOKEN));
            assertThat(form.getFirstValue("foo"), is("bar"));
            return new EmptyRepresentation();
        }
    }

    @Test
    public void testCase3() {
        ProtectedClientResource resource = new ProtectedClientResource(
                new Reference(baseURI, "/app/resource3"));
        resource.setToken(STUB_TOKEN);
        resource.setUseBodyMethod(true);
        resource.addQueryParameter("foo", "bar");
        resource.get();
    }
    
    //Test compatibility with modules that don't match token type case
    @Test
    public void testCase4() {
        ProtectedClientResource resource = new ProtectedClientResource(
                new Reference(baseURI, "/app/resource1"));
        resource.setToken(SPRING_STUB_TOKEN);
        resource.setUseBodyMethod(false);
        resource.get();
    }
}
