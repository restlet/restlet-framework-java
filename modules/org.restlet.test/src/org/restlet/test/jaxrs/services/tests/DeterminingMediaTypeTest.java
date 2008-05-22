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

import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.test.jaxrs.services.resources.DeterminingMediaTypeTestService;

/**
 * This test class checks if the Request.evaluatePreconditions methods works
 * fine.
 * 
 * @author Stephan Koops
 */
public class DeterminingMediaTypeTest extends JaxRsTestCase {
    @Override
    protected Class<?> getRootResourceClass() {
        return DeterminingMediaTypeTestService.class;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testTextStar1() {
        Response response = get("textStar", MediaType.TEXT_ALL);
        assertEquals(Status.CLIENT_ERROR_NOT_ACCEPTABLE, response.getStatus());

        response = get("textStar", MediaType.ALL);
        assertEquals(Status.CLIENT_ERROR_NOT_ACCEPTABLE, response.getStatus());

        response = get("textStar", MediaType.IMAGE_GIF);
        assertEquals(Status.CLIENT_ERROR_NOT_ACCEPTABLE, response.getStatus());
    }

    public void testTextStar2() {
        Response response = get("textStar", MediaType.TEXT_HTML);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEqualMediaType(MediaType.TEXT_HTML, response);
    }

    public void testTextStar3() {
        Response response = get("textStar", MediaType.TEXT_PLAIN);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEqualMediaType(MediaType.TEXT_PLAIN, response);
    }

    public void testHtmlPlainGif1() {
        Response response = get("htmlPlainGif", MediaType.TEXT_ALL);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEqualMediaType(MediaType.TEXT_HTML, response);
    }

    public void testHtmlPlainGif2() {
        Response response = get("htmlPlainGif", MediaType.ALL);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEqualMediaType(MediaType.TEXT_HTML, response);
    }

    public void testHtmlPlainGif3() {
        Response response = get("htmlPlainGif", MediaType.IMAGE_GIF);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEqualMediaType(MediaType.IMAGE_GIF, response);
    }

    public void testHtmlPlainGif4() {
        Response response = get("htmlPlainGif", MediaType.TEXT_PLAIN);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEqualMediaType(MediaType.TEXT_PLAIN, response);
    }
}