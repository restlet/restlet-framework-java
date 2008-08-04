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

import java.io.IOException;

import javax.ws.rs.PathParam;

import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.test.jaxrs.services.resources.PathParamTestService;

/**
 * @author Stephan Koops
 * @see PathParamTestService
 * @see PathParam
 */
public class PathParamTest extends JaxRsTestCase {

    /**
     * @param subPath
     *                without beginning '/'
     * @return
     */
    private Reference createReference(String subPath) {
        final String baseRef = createBaseRef() + "/pathParamTest/" + subPath;
        return new Reference(createBaseRef(), baseRef);
    }

    @Override
    protected Class<?> getRootResourceClass() {
        return PathParamTestService.class;
    }

    /** @see PathParamTestService#checkUnmodifiable(java.util.List) */
    public void htestCheckUnmodifiable() {
        // LATER @PathParam(..) PathSegment testen
        final Response response = get(createReference("4711/checkUnmodifiable/1667"));
        assertTrue(
                "The List annotated with @PathParam must not be modifiable. Status is "
                        + response.getStatus(), response.getStatus()
                        .isSuccess());
    }

    public void testGet1() throws IOException {
        final Response response = get(createReference("4711"));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("4711", response.getEntity().getText());
    }

    public void testGet2() throws IOException {
        final Response response = get(createReference("4711/abc/677/def"));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("4711\n677", response.getEntity().getText());
    }

    public void testGet4() throws IOException {
        final Response response = get(createReference("12/st/34"));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("34", response.getEntity().getText());
    }
}