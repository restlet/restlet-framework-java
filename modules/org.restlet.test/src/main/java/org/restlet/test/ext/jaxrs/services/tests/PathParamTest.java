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

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Application;

import org.restlet.Response;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.test.ext.jaxrs.services.resources.PathParamTestService;

/**
 * @author Stephan Koops
 * @see PathParamTestService
 * @see PathParam
 */
public class PathParamTest extends JaxRsTestCase {

    /**
     * @param subPath
     *            without beginning '/'
     * @return
     */
    private Reference createReference2(String subPath) {
        final String baseRef = createBaseRef() + "/pathParamTest/" + subPath;
        return new Reference(createBaseRef(), baseRef);
    }

    @Override
    protected Application getApplication() {
        return new Application() {
            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Set<Class<?>> getClasses() {
                return (Set) Collections.singleton(PathParamTestService.class);
            }
        };
    }

    /** @see PathParamTestService#checkUnmodifiable(java.util.List) */
    public void htestCheckUnmodifiable() {
        final Response response = get(createReference2("4711/checkUnmodifiable/1667"));
        assertTrue(
                "The List annotated with @PathParam must not be modifiable. Status is "
                        + response.getStatus(), response.getStatus()
                        .isSuccess());
    }

    public void testGet1() throws IOException {
        final Response response = get(createReference2("4711"));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("4711", response.getEntity().getText());
    }

    public void testGet2() throws IOException {
        final Response response = get(createReference2("4711/abc/677/def"));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("4711\n677", response.getEntity().getText());
    }

    public void testGet4() throws IOException {
        final Response response = get(createReference2("12/st/34"));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("34", response.getEntity().getText());
    }

    public void testGetRegExpPathEinBuchstabe() throws IOException {
        Response response = get("regExp/a");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("ein Buchstabe: a", response.getEntity().getText());
    }

    public void testGetRegExpPathLangerString() throws IOException {
        Response response = get("regExp/aa");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("anderes: aa", response.getEntity().getText());
    }

    public void testGetRegExpPathZahl() throws IOException {
        Response response = get("regExp/1");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("Zahl: 1", response.getEntity().getText());

        response = get("regExp/112");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("Zahl: 112", response.getEntity().getText());
    }

    public void testGetRegExpPathZahlMinus() throws IOException {
        Response response = get("regExp/-1");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("Zahl: -1", response.getEntity().getText());
    }
}
