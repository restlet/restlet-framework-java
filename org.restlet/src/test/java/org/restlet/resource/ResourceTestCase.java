/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.representation.Representation;

/**
 * Unit tests for the {@link Resource} abstract class, exercised through a minimal local subclass.
 *
 * @author Jerome Louvel
 */
class ResourceTestCase {

    /** Minimal concrete subclass used to exercise the abstract base class. */
    private static class ConcreteResource extends Resource {

        private final Map<String, Object> attributes = new HashMap<>();

        private volatile boolean released;

        private volatile boolean failInit;

        @Override
        public String getAttribute(String name) {
            Object value = attributes.get(name);
            return (value == null) ? null : value.toString();
        }

        @Override
        public void setAttribute(String name, Object value) {
            attributes.put(name, value);
        }

        @Override
        public Representation handle() {
            return null;
        }

        @Override
        protected void doInit() throws ResourceException {
            if (failInit) {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
            }
        }

        @Override
        protected void doRelease() {
            released = true;
        }
    }

    private ConcreteResource resource;

    @BeforeEach
    void setUpEach() {
        Engine.clearThreadLocalVariables();
        resource = new ConcreteResource();
    }

    @AfterEach
    void tearDownEach() {
        Engine.clearThreadLocalVariables();
        resource = null;
    }

    @Test
    void toBoolean_convertsValidStringAndReturnsNullForNull() {
        assertEquals(Boolean.TRUE, Resource.toBoolean("true"));
        assertNull(Resource.toBoolean(null));
    }

    @Test
    void toByte_convertsValidStringAndReturnsNullForNull() {
        assertEquals(Byte.valueOf((byte) 5), Resource.toByte("5"));
        assertNull(Resource.toByte(null));
    }

    @Test
    void toDouble_convertsValidStringAndReturnsNullForNull() {
        assertEquals(Double.valueOf(3.14), Resource.toDouble("3.14"));
        assertNull(Resource.toDouble(null));
    }

    @Test
    void toFloat_convertsValidStringAndReturnsNullForNull() {
        assertEquals(Float.valueOf(1.5f), Resource.toFloat("1.5"));
        assertNull(Resource.toFloat(null));
    }

    @Test
    void toInteger_convertsValidStringAndReturnsNullForNull() {
        assertEquals(Integer.valueOf(42), Resource.toInteger("42"));
        assertNull(Resource.toInteger(null));
    }

    @Test
    void toLong_convertsValidStringAndReturnsNullForNull() {
        assertEquals(Long.valueOf(42L), Resource.toLong("42"));
        assertNull(Resource.toLong(null));
    }

    @Test
    void toShort_convertsValidStringAndReturnsNullForNull() {
        assertEquals(Short.valueOf((short) 7), Resource.toShort("7"));
        assertNull(Resource.toShort(null));
    }

    @Test
    void getRequestAndGetResponse_returnNullBeforeInit() {
        assertNull(resource.getRequest());
        assertNull(resource.getResponse());
        assertNull(resource.getContext());
    }

    @Test
    void init_setsContextRequestAndResponse() {
        Context context = new Context();
        Request request = new Request(Method.GET, "http://localhost/test");
        Response response = new Response(request);

        resource.init(context, request, response);

        assertEquals(context, resource.getContext());
        assertEquals(request, resource.getRequest());
        assertEquals(response, resource.getResponse());
    }

    @Test
    void init_catchesExceptionThrownByDoInit() {
        Context context = new Context();
        Request request = new Request(Method.GET, "http://localhost/test");
        Response response = new Response(request);
        resource.failInit = true;

        // Should not propagate the ResourceException thrown by doInit().
        resource.init(context, request, response);

        assertEquals(request, resource.getRequest());
    }

    @Test
    void getMethod_delegatesToRequest() {
        Request request = new Request(Method.PUT, "http://localhost/test");
        resource.setRequest(request);

        assertEquals(Method.PUT, resource.getMethod());
    }

    @Test
    void getStatus_delegatesToResponse() {
        Request request = new Request(Method.GET, "http://localhost/test");
        Response response = new Response(request);
        response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        resource.setResponse(response);

        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, resource.getStatus());
    }

    @Test
    void getReference_delegatesToRequest() {
        Request request = new Request(Method.GET, "http://localhost/test");
        resource.setRequest(request);

        assertEquals(new Reference("http://localhost/test"), resource.getReference());
    }

    @Test
    void getApplication_createsNewInstanceWhenNoneSet() {
        assertNotNull(resource.getApplication());
    }

    @Test
    void setApplication_returnsConfiguredInstance() {
        org.restlet.Application application = new org.restlet.Application();
        resource.setApplication(application);

        assertEquals(application, resource.getApplication());
    }

    @Test
    void setQueryValueThenGetQueryValue_roundTrips() {
        Request request = new Request(Method.GET, "http://localhost/test");
        resource.setRequest(request);

        resource.setQueryValue("name", "value");

        assertEquals("value", resource.getQueryValue("name"));
    }

    @Test
    void getQueryValue_returnsNullWhenNoRequest() {
        assertNull(resource.getQueryValue("name"));
    }

    @Test
    void release_invokesDoRelease() {
        assertFalse(resource.released);
        resource.release();
        assertTrue(resource.released);
    }

    @Test
    void toString_combinesRequestAndResponse() {
        Request request = new Request(Method.GET, "http://localhost/test");
        Response response = new Response(request);
        resource.init(new Context(), request, response);

        String result = resource.toString();

        assertTrue(result.contains(request.toString()));
    }

    @Test
    void toString_isEmptyWhenNoRequestOrResponse() {
        assertEquals("", resource.toString());
    }
}
