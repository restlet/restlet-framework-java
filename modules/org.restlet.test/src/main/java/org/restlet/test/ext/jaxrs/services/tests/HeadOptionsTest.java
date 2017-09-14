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

package org.restlet.test.ext.jaxrs.services.tests;

import java.util.Collections;
import java.util.Set;

import javax.ws.rs.HEAD;
import javax.ws.rs.core.Application;

import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.test.ext.jaxrs.services.others.OPTIONS;
import org.restlet.test.ext.jaxrs.services.resources.HeadOptionsTestService;

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
    protected Application getApplication() {
        return new Application() {
            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Set<Class<?>> getClasses() {
                return (Set) Collections
                        .singleton(HeadOptionsTestService.class);
            }
        };
    }

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
