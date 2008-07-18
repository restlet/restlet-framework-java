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

import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.test.jaxrs.services.resources.IllegalThingsTestService;

/**
 * Checks, if illegal things are forbidden.
 * 
 * @author Stephan Koops
 * @see IllegalThingsTestService
 */
public class IllegalThingsTest extends JaxRsTestCase {

    @Override
    protected Class<?> getRootResourceClass() {
        return IllegalThingsTestService.class;
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