/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.service.MetadataService;

class RestletHelperTestCase {

    private static class TestRestletHelper extends RestletHelper<Restlet> {
        TestRestletHelper(Restlet helped) {
            super(helped);
        }

        @Override
        public void start() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void stop() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void update() {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    void getHelpedAndSetHelped_roundTrip() {
        Restlet restlet = new Restlet() {};
        TestRestletHelper helper = new TestRestletHelper(restlet);
        assertEquals(restlet, helper.getHelped());

        Restlet other = new Restlet() {};
        helper.setHelped(other);
        assertEquals(other, helper.getHelped());
    }

    @Test
    void getAttributes_returnsMutableMap() {
        TestRestletHelper helper = new TestRestletHelper(new Restlet() {});
        helper.getAttributes().put("key", "value");
        assertEquals("value", helper.getAttributes().get("key"));
    }

    @Test
    void getContext_delegatesToHelpedRestlet() {
        Context context = new Context();
        Restlet restlet = new Restlet(context) {};
        TestRestletHelper helper = new TestRestletHelper(restlet);
        assertEquals(context, helper.getContext());
    }

    @Test
    void getHelpedParameters_withContext_returnsContextParameters() {
        Context context = new Context();
        Restlet restlet = new Restlet(context) {};
        TestRestletHelper helper = new TestRestletHelper(restlet);
        assertEquals(context.getParameters(), helper.getHelpedParameters());
    }

    @Test
    void getHelpedParameters_withoutContext_returnsEmptySeries() {
        Restlet restlet = new Restlet() {};
        TestRestletHelper helper = new TestRestletHelper(restlet);
        assertTrue(helper.getHelpedParameters().isEmpty());
    }

    @Test
    void getLogger_withContext_returnsContextLogger() {
        Context context = new Context();
        Restlet restlet = new Restlet(context) {};
        TestRestletHelper helper = new TestRestletHelper(restlet);
        assertEquals(context.getLogger(), helper.getLogger());
    }

    @Test
    void getLogger_withoutContext_returnsCurrentLogger() {
        Restlet restlet = new Restlet() {};
        TestRestletHelper helper = new TestRestletHelper(restlet);
        assertNotNull(helper.getLogger());
    }

    @Test
    void getMetadataService_withoutApplication_returnsNewInstance() {
        Restlet restlet = new Restlet() {};
        TestRestletHelper helper = new TestRestletHelper(restlet);
        assertNotNull(helper.getMetadataService());
    }

    @Test
    void getMetadataService_withApplication_returnsApplicationMetadataService() {
        org.restlet.Application application = new org.restlet.Application(new Context());
        org.restlet.Application.setCurrent(application);
        try {
            TestRestletHelper helper = new TestRestletHelper(application);
            MetadataService result = helper.getMetadataService();
            assertEquals(application.getMetadataService(), result);
        } finally {
            org.restlet.Application.setCurrent(null);
        }
    }

    @Test
    void handle_setsCurrentResponseAndContext() {
        Context context = new Context();
        Restlet restlet = new Restlet(context) {};
        TestRestletHelper helper = new TestRestletHelper(restlet);
        Request request = new Request(Method.GET, "http://localhost/test");
        Response response = new Response(request);

        helper.handle(request, response);

        assertEquals(response, Response.getCurrent());
        assertEquals(context, Context.getCurrent());

        Response.setCurrent(null);
    }
}
