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
import org.restlet.data.Status;
import org.restlet.test.ext.jaxrs.services.resources.PathParamTestService2;

/**
 * @author Stephan Koops
 * @see PathParamTestService2
 * @see PathParam
 */
public class PathParamTest2 extends JaxRsTestCase {

    @Override
    protected Application getApplication() {
        return new Application() {
            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Set<Class<?>> getClasses() {
                return (Set) Collections.singleton(PathParamTestService2.class);
            }
        };
    }

    public void testDecoded1() throws Exception {
        Response response = get("decoded/x");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("x", response.getEntity().getText());

        response = get("decoded/sjkg");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("sjkg", response.getEntity().getText());
    }

    public void testDecoded2() throws Exception {
        final Response response = get("decoded/%20");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(" ", response.getEntity().getText());
    }

    /** @see PathParamTestService2#encoded(String) */
    public void testEncoded() throws Exception {
        Response response = get("encoded/x");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("x", response.getEntity().getText());

        response = get("encoded/sjkg");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("sjkg", response.getEntity().getText());

        response = get("encoded/%20");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("%20", response.getEntity().getText());
    }

    public void testGetBigDecimal() throws IOException {
        Response response = get("BigDecimal/413624654744743534745767");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("413624654744743534745767", response.getEntity().getText());

        response = get("BigDecimal/abc");
        assertTrue(response.getStatus().isError());
    }

    public void testGetInt() throws IOException {
        Response response = get("int/467");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("467", response.getEntity().getText());

        response = get("int/abc");
        assertTrue(response.getStatus().isError());
    }

    public void testGetInteger() throws IOException {
        Response response = get("Integer/4423467");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("4423467", response.getEntity().getText());

        response = get("Integer/423645365467345743734");
        assertTrue(response.getStatus().isError());
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());

        response = get("Integer/abc");
        assertTrue(response.getStatus().isError());
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    public void testGetMediaType() throws IOException {
        Response response = get("MediaType/467");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("467/*", response.getEntity().getText());

        response = get("MediaType/abc");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("abc/*", response.getEntity().getText());
    }

    public void testGetMn() throws IOException {
        Response response = get("mn467");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("467", response.getEntity().getText());

        response = get("mnabc");
        assertTrue(response.getStatus().isError());
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    /** @see PathParamTestService2#getMultSegment(String) */
    public void testGetWithSlashInUriParam() throws IOException {
        final Response response = get("multSegm/abc/def");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("abc/def", response.getEntity().getText());
    }

    public void testX() throws Exception {
        Response response = get("abc123");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("123", response.getEntity().getText());

        response = get("abcdef");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("def", response.getEntity().getText());
    }

    public void testX2() throws Exception {
        Response response = get("abcdef/1234");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("bcd\n12", response.getEntity().getText());

        response = get("aXYZef/AB34");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("XYZ\nAB", response.getEntity().getText());
    }
}
