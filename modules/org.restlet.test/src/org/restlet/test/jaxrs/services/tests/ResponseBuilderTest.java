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

import java.util.Set;

import org.restlet.data.Dimension;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.test.jaxrs.services.resources.ResponseBuilderService;

/**
 * @author Stephan Koops
 * @see ResponseBuilderService
 * @see javax.ws.rs.core.Response.ResponseBuilder
 */
public class ResponseBuilderTest extends JaxRsTestCase {

    @Override
    protected Class<?> getRootResourceClass() {
        return ResponseBuilderService.class;
    }

    public void test1() {
        Response response = get("1");
        Set<Dimension> dimensions = response.getDimensions();

        assertTrue("dimension must contain MediaType", dimensions
                .contains(Dimension.MEDIA_TYPE));
        assertTrue("dimension must contain Encoding", dimensions
                .contains(Dimension.ENCODING));
    }

    public void test2() {
        Response response = get("2");
        Set<Dimension> dimensions = response.getDimensions();

        assertTrue("dimension must contain Language", dimensions
                .contains(Dimension.LANGUAGE));
        assertTrue("dimension must contain CharacterSet", dimensions
                .contains(Dimension.CHARACTER_SET));
    }
    
    public void testDelete() {
        Response r = accessServer(Method.DELETE, getRootResourceClass(), null);
        
        assertEquals(Status.SUCCESS_OK, r.getStatus());
    }
}