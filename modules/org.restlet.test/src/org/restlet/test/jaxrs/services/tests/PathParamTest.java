/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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