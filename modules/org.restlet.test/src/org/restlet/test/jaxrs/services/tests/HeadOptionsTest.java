/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */
package org.restlet.test.jaxrs.services.tests;

import javax.ws.rs.HEAD;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.test.jaxrs.services.others.OPTIONS;
import org.restlet.test.jaxrs.services.resources.HeadOptionsTestService;

/**
 * Test to check if HTTP methods HEAD and OPTIONS work fine.
 * 
 * @author Stephan Koops
 * @see HeadOptionsTestService
 * @see HEAD
 * @see OPTIONS
 */
public class HeadOptionsTest extends JaxRsTestCase {

    @Override
    protected Class<?> getRootResourceClass() {
        return HeadOptionsTestService.class;
    }

    // NICE test if it works fine, for @GET first and for @HEAD first,
    // if the GET-Method is not prefered to the HEAD method.

    public void testHead1() throws Exception {
        final Response responseGett = get("headTest1", MediaType.TEXT_HTML);
        final Response responseHead = head("headTest1", MediaType.TEXT_HTML);
        if (responseGett.getStatus().isError()) {
            System.out.println(responseGett.getEntity().getText());
        }
        assertEquals(Status.SUCCESS_OK, responseGett.getStatus());
        if (responseHead.getStatus().isError()) {
            System.out.println(responseHead.getEntity().getText());
        }
        assertEquals(Status.SUCCESS_OK, responseHead.getStatus());
        final Representation entityGett = responseGett.getEntity();
        final Representation entityHead = responseHead.getEntity();
        assertNotNull(entityGett);
        assertNotNull("Must not be null to read the entity headers", entityHead);
        assertEqualMediaType(MediaType.TEXT_HTML, entityGett.getMediaType());
        assertEqualMediaType(MediaType.TEXT_HTML, entityHead.getMediaType());
        assertEquals("4711", entityGett.getText());
        assertEquals("The entity text of the head request must be null", null,
                entityHead.getText());
    }

    public void testHead2() throws Exception {
        final Response responseGett = get("headTest2", MediaType.TEXT_HTML);
        final Response responseHead = head("headTest2", MediaType.TEXT_HTML);
        if (responseGett.getStatus().isError()) {
            System.out.println(responseGett.getEntity().getText());
        }
        assertEquals(Status.SUCCESS_OK, responseGett.getStatus());
        if (responseHead.getStatus().isError()) {
            System.out.println(responseHead.getEntity().getText());
        }
        assertEquals(Status.SUCCESS_OK, responseHead.getStatus());
        final Representation entityGett = responseGett.getEntity();
        final Representation entityHead = responseHead.getEntity();
        assertNotNull(entityGett);
        assertNotNull("Must not be null to read the entity headers", entityHead);
        assertEqualMediaType(MediaType.TEXT_HTML, entityGett.getMediaType());
        assertEqualMediaType(MediaType.TEXT_HTML, entityHead.getMediaType());
        assertEquals("4711", entityGett.getText());
        assertEquals("The entity text of the head request must be null", null,
                entityHead.getText());
    }

    public void testHead2plain() throws Exception {
        final Response responseGett = get("headTest2", MediaType.TEXT_PLAIN);
        final Response responseHead = head("headTest2", MediaType.TEXT_PLAIN);
        if (responseGett.getStatus().isError()) {
            System.out.println(responseGett.getEntity().getText());
        }
        assertEquals(Status.SUCCESS_OK, responseGett.getStatus());
        if (responseHead.getStatus().isError()) {
            System.out.println(responseHead.getEntity().getText());
        }
        assertEquals(Status.SUCCESS_OK, responseHead.getStatus());
        final Representation entityGett = responseGett.getEntity();
        final Representation entityHead = responseHead.getEntity();
        assertNotNull(entityGett);
        assertNotNull("Must not be null to read the entity headers", entityHead);
        assertEqualMediaType(MediaType.TEXT_PLAIN, entityGett.getMediaType());
        assertEqualMediaType(MediaType.TEXT_PLAIN, entityHead.getMediaType());
        assertEquals("4711", entityGett.getText());
        assertEquals("The entity text of the head request must be null", null,
                entityHead.getText());
    }

    public void testOptions() throws Exception {
        Response response = options();
        assertAllowedMethod(response, Method.GET);

        response = options("headTest1");
        assertAllowedMethod(response, Method.GET, Method.HEAD, Method.POST);

        response = options("headTest2");
        assertAllowedMethod(response, Method.GET, Method.HEAD);

        response = options("xyz");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }
}