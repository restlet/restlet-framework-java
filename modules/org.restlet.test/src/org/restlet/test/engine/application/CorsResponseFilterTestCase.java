package org.restlet.test.engine.application;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.engine.application.CorsFilter;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.ServerResource;
import org.restlet.test.RestletTestCase;

import java.util.Collection;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

/**
 * @author Manuel Boillod
 */
public class CorsResponseFilterTestCase extends RestletTestCase {

    private CorsFilter corsFilter;

    public static class DummyServerResource extends ServerResource {
        @Options
        public void doOption(){}
        @Get
        public void doGet(){}
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        corsFilter = new CorsFilter();
        corsFilter.setNext(DummyServerResource.class);
    }

    // INVALID CORS REQUESTS

    public void testGet_withoutOrigin() {
        Request request = new Request();
        request.setMethod(Method.GET);
        Response response = corsFilter.handle(request);
        assertNoCorsHeaders(response);
    }

    public void testOption_withoutOrigin() {
        Request request = new Request();
        request.setMethod(Method.OPTIONS);
        Response response = corsFilter.handle(request);
        assertNoCorsHeaders(response);
    }

    public void testOption_withoutRequestMethod() {
        Request request = new Request();
        request.setMethod(Method.OPTIONS);
        request.getHeaders().set("Origin", "localhost");
        Response response = corsFilter.handle(request);
        assertNoCorsHeaders(response);
    }

    // VALID CORS REQUESTS

    public void testGet() {
        Request request = new Request();
        request.setMethod(Method.GET);
        request.getHeaders().set("Origin", "localhost");
        Response response = corsFilter.handle(request);
        assertEquals("*", response.getAccessControlAllowOrigin());
        assertNull(response.getAccessControlAllowCredentials());
        assertIsEmpty(response.getAccessControlAllowHeaders());
        assertIsEmpty(response.getAccessControlAllowMethods());
        assertIsEmpty(response.getAccessControlExposeHeaders());
    }

    public void testGet_withAuthenticationAllowed() {
        corsFilter.setAllowedCredentials(true);

        Request request = new Request();
        request.setMethod(Method.GET);
        request.getHeaders().set("Origin", "localhost");
        Response response = corsFilter.handle(request);
        assertEquals("localhost", response.getAccessControlAllowOrigin());
        assertEquals(Boolean.TRUE, response.getAccessControlAllowCredentials());
        assertIsEmpty(response.getAccessControlAllowHeaders());
        assertIsEmpty(response.getAccessControlAllowMethods());
        assertIsEmpty(response.getAccessControlExposeHeaders());
    }

    public void testOption_requestGet() {
        Request request = new Request();
        request.setMethod(Method.OPTIONS);
        request.getHeaders().set("Origin", "localhost");
        request.setAccessControlRequestMethod(Method.GET);
        Response response = corsFilter.handle(request);
        assertEquals("*", response.getAccessControlAllowOrigin());
        assertNull(response.getAccessControlAllowCredentials());
        assertIsEmpty(response.getAccessControlAllowHeaders());
        assertThat(response.getAccessControlAllowMethods(), contains(Method.GET, Method.OPTIONS));
        assertIsEmpty(response.getAccessControlExposeHeaders());
    }

    public void testOption_requestGet_skippingResource() {
        corsFilter.setSkippingResourceForCorsOptions(true);

        Request request = new Request();
        request.setMethod(Method.OPTIONS);
        request.getHeaders().set("Origin", "localhost");
        request.setAccessControlRequestMethod(Method.GET);
        Response response = corsFilter.handle(request);
        assertEquals("*", response.getAccessControlAllowOrigin());
        assertNull(response.getAccessControlAllowCredentials());
        assertIsEmpty(response.getAccessControlAllowHeaders());
        assertThat(response.getAccessControlAllowMethods(), containsInAnyOrder(Method.GET, Method.POST, Method.PUT, Method.DELETE));
        assertIsEmpty(response.getAccessControlExposeHeaders());
    }

    public void testOption_requestPost_skippingResource() {
        corsFilter.setSkippingResourceForCorsOptions(true);

        Request request = new Request();
        request.setMethod(Method.OPTIONS);
        request.getHeaders().set("Origin", "localhost");
        request.setAccessControlRequestMethod(Method.POST);
        Response response = corsFilter.handle(request);
        assertEquals("*", response.getAccessControlAllowOrigin());
        assertNull(response.getAccessControlAllowCredentials());
        assertIsEmpty(response.getAccessControlAllowHeaders());
        assertThat(response.getAccessControlAllowMethods(), containsInAnyOrder(Method.GET, Method.POST, Method.PUT, Method.DELETE));
        assertIsEmpty(response.getAccessControlExposeHeaders());
    }

    public void testOption_requestGet_withAuthenticationAllowed() {
        corsFilter.setAllowedCredentials(true);

        Request request = new Request();
        request.setMethod(Method.OPTIONS);
        request.getHeaders().set("Origin", "localhost");
        request.setAccessControlRequestMethod(Method.GET);
        Response response = corsFilter.handle(request);
        assertEquals("localhost", response.getAccessControlAllowOrigin());
        assertEquals(Boolean.TRUE, response.getAccessControlAllowCredentials());
        assertIsEmpty(response.getAccessControlAllowHeaders());
        assertThat(response.getAccessControlAllowMethods(), contains(Method.GET, Method.OPTIONS));
        assertIsEmpty(response.getAccessControlExposeHeaders());
    }

    private void assertIsEmpty(Collection<?> collection) {
        assertNotNull(collection);
        assertTrue(collection.isEmpty());
    }

    public void assertNoCorsHeaders(Response response) {
        assertNull(response.getAccessControlAllowOrigin());
        assertNull(response.getAccessControlAllowCredentials());
        assertIsEmpty(response.getAccessControlAllowHeaders());
        assertIsEmpty(response.getAccessControlAllowMethods());
        assertIsEmpty(response.getAccessControlExposeHeaders());
    }

}
