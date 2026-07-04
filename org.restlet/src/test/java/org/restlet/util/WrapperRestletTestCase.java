/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;

/** Unit tests for {@link WrapperRestlet}. */
class WrapperRestletTestCase {

    @Test
    void delegatesAllOperationsToWrappedRestlet() throws Exception {
        Restlet wrapped = new Restlet() {};
        WrapperRestlet wrapper = new WrapperRestlet(wrapped);

        Context context = new Context();
        wrapper.setContext(context);
        assertSame(context, wrapper.getContext());
        assertSame(wrapped.getContext(), wrapper.getContext());

        wrapper.setAuthor("author");
        assertEquals("author", wrapper.getAuthor());

        wrapper.setDescription("description");
        assertEquals("description", wrapper.getDescription());

        wrapper.setName("name");
        assertEquals("name", wrapper.getName());

        wrapper.setOwner("owner");
        assertEquals("owner", wrapper.getOwner());

        assertSame(wrapped.getLogger(), wrapper.getLogger());
        assertSame(wrapped.getApplication(), wrapper.getApplication());

        assertFalse(wrapper.isStarted());
        assertTrue(wrapper.isStopped());

        wrapper.start();
        assertTrue(wrapper.isStarted());
        assertFalse(wrapper.isStopped());

        wrapper.stop();
        assertTrue(wrapper.isStopped());

        Request request = new Request();
        Response response = new Response(request);
        wrapper.handle(request, response);
    }
}
