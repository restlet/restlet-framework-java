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

import javax.ws.rs.core.Application;

import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.test.ext.jaxrs.services.resources.IllegalThingsTestService;

/**
 * Checks, if illegal things are forbidden.
 * 
 * @author Stephan Koops
 * @see IllegalThingsTestService
 */
public class IllegalThingsTest extends JaxRsTestCase {

    @Override
    protected Application getApplication() {
        return new Application() {
            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Set<Class<?>> getClasses() {
                return (Set) Collections
                        .singleton(IllegalThingsTestService.class);
            }
        };
    }

    public void testNullSubResource() throws Exception {
        final Response response = get("nullSubResource");
        assertEquals(Status.SERVER_ERROR_INTERNAL, response.getStatus());
    }

    public void testPackage() throws Exception {
        final Response response = get("package");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    public void testPrivate() throws Exception {
        final Response response = get("private");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    public void testProtected() throws Exception {
        final Response response = get("protected");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }
}
