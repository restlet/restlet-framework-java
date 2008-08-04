/*
 * Copyright 2005-2008 Noelios Technologies.
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

import junit.framework.AssertionFailedError;

import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.test.jaxrs.services.resources.AppPlusXmlResource;

/**
 * @author Stephan Koops
 * @see AppPlusXmlResource
 */
public class AppPlusXmlTest extends JaxRsTestCase {

    private static final MediaType APP_PERSON_XML = new MediaType(
            "application/Person+xml");

    /**
     * @param mt
     */
    private void getAndCheck(MediaType mt) {
        final Response response = get(mt);
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        final MediaType mediaType = response.getEntity().getMediaType();
        assertEqualMediaType(mt, mediaType);
    }

    @Override
    protected Class<?> getRootResourceClass() {
        return AppPlusXmlResource.class;
    }

    public void testGet() throws Exception {
        final Response response = get();
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        final MediaType mediaType = response.getEntity().getMediaType();
        try {
            assertEqualMediaType(MediaType.TEXT_XML, mediaType);
        } catch (final AssertionFailedError afe) {
            try {
                assertEqualMediaType(MediaType.APPLICATION_XML, mediaType);
            } catch (final AssertionFailedError afe2) {
                assertEqualMediaType(APP_PERSON_XML, mediaType);
            }
        }
    }

    /**
     * @see AppPlusXmlResource#getPerson()
     */
    public void testGet2() throws Exception {
        getAndCheck(MediaType.TEXT_XML);
        getAndCheck(MediaType.APPLICATION_XML);
        getAndCheck(APP_PERSON_XML);
    }
}