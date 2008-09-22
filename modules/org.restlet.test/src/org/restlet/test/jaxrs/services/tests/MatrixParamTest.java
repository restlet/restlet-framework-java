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
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.test.jaxrs.services.tests;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.ws.rs.MatrixParam;
import javax.ws.rs.core.Application;

import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.test.jaxrs.services.resources.MatrixParamTestService;
import org.restlet.test.jaxrs.services.resources.QueryParamTestService;

/**
 * @author Stephan Koops
 * @see MatrixParamTestService
 * @see MatrixParam
 */
public class MatrixParamTest extends JaxRsTestCase {

    public void checkBothGiven(String subPath) throws IOException {
        Response response = get(subPath + ";firstname=Angela;lastname=Merkel");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("Angela Merkel", response.getEntity().getText());

        response = get(subPath + ";lastname=Merkel;firstname=Angela");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("Angela Merkel", response.getEntity().getText());
    }

    public void checkOneGiven(String subPath) throws IOException {
        Response response = get(subPath + ";firstname=Goofy");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("Goofy null", response.getEntity().getText());

        response = get(subPath + ";lastname=Goofy");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("null Goofy", response.getEntity().getText());
    }

    @Override
    protected Application getAppConfig() {
        final Application appConfig = new Application() {
            @Override
            @SuppressWarnings("unchecked")
            public Set<Class<?>> getClasses() {
                return (Set) Collections.singleton(MatrixParamTestService.class);
            }
        };
        return appConfig;
    }
    public void testA() throws IOException {
        checkBothGiven("a");
        checkOneGiven("a");
    }

    public void testB() throws IOException {
        checkBothGiven("b");
        checkOneGiven("b");
    }

    public void testCheckUnmodifiable() {
        Response response = get("checkUnmodifiable");
        assertTrue(
                "The List annotated with @MatrixParam seems to be modifiable",
                response.getStatus().isSuccess());
    }

    public void testDecoded() throws IOException {
        final Response response = get("b;firstname=George%20U.;lastname=Bush");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("George U. Bush", response.getEntity().getText());
    }

    /** @see MatrixParamTestService#encoded(String, String) */
    public void testEncoded() throws IOException {
        final Response response = get("encoded;firstname=George%20U.;lastname=Bush");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("George%20U. Bush", response.getEntity().getText());
    }

    public void testOne1() throws Exception {
        final Response response = get("one;name");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("[empty]", response.getEntity().getText());
    }

    public void testOne2() throws Exception {
        final Response response = get("one;name=");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("[empty]", response.getEntity().getText());
    }

    public void testOne3() throws Exception {
        final Response response = get("one;name=x");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("x", response.getEntity().getText());
    }

    public void testOne4() throws Exception {
        final Response response = get("one;name2=sdf");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("[null]", response.getEntity().getText());
    }

    public void testSemicolon() {
        final Response response1 = get("semicolon");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response1.getStatus());
        sysOutEntityIfError(response1);

        final Response response2 = get("semicolon;mpA=6");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response2.getStatus());
        sysOutEntityIfError(response2);

        final Response response3 = get("semicolon;mpB=6");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response3.getStatus());
        sysOutEntityIfError(response3);

        final Response response4 = get("semicolon;mpB=6;mpA=5");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response4.getStatus());
        sysOutEntityIfError(response4);

        final Response response5 = get("semicolon;mpA=5;mpB=6");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response5.getStatus());
        sysOutEntityIfError(response5);
    }

    /** @see QueryParamTestService#getDecoded() */
    public void testSetterDecoded() throws Exception {
        final Response response1 = get(";decoded=abc/setterDecoded");
        sysOutEntityIfError(response1);
        assertEquals(Status.SUCCESS_OK, response1.getStatus());
        assertEquals("abc", response1.getEntity().getText());

        final Response response2 = get(";decoded=%20/setterDecoded");
        sysOutEntityIfError(response2);
        assertEquals(Status.SUCCESS_OK, response2.getStatus());
        assertEquals(" ", response2.getEntity().getText());
    }

    /** @see QueryParamTestService#getEncoded() */
    public void testSetterEncoded() throws Exception {
        final Response response1 = get(";encoded=abc/setterEncoded");
        sysOutEntityIfError(response1);
        assertEquals(Status.SUCCESS_OK, response1.getStatus());
        assertEquals("abc", response1.getEntity().getText());

        final Response response2 = get(";encoded=%20/setterEncoded");
        sysOutEntityIfError(response2);
        assertEquals(Status.SUCCESS_OK, response2.getStatus());
        assertEquals("%20", response2.getEntity().getText());
    }

    public void testSub1() throws Exception {
        final Response response2 = get("sub;name=abc/one");
        sysOutEntityIfError(response2);
        assertEquals(Status.SUCCESS_OK, response2.getStatus());
        assertEquals("abc", response2.getEntity().getText());

        final Response response3 = get("sub/one;name=def");
        sysOutEntityIfError(response3);
        assertEquals(Status.SUCCESS_OK, response3.getStatus());
        assertEquals("def", response3.getEntity().getText());

        final Response response4 = get("sub;name=abc/one;name=def");
        sysOutEntityIfError(response4);
        assertEquals(Status.SUCCESS_OK, response4.getStatus());
        assertEquals("def", response4.getEntity().getText());

        final Response response5 = get("sub;name=abc/allNames;name=def");
        sysOutEntityIfError(response5);
        assertEquals(Status.SUCCESS_OK, response5.getStatus());
        assertEquals("[abc, def]", response5.getEntity().getText());

        final Response response6 = get("allNames;name=abc");
        sysOutEntityIfError(response6);
        assertEquals(Status.SUCCESS_OK, response6.getStatus());
        assertEquals("[abc]", response6.getEntity().getText());
    }

    public void testWithDefault() throws IOException {
        Response response = get("withDefault;mp=abcde");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("abcde", response.getEntity().getText());

        response = get("withDefault;mp=");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("[empty]", response.getEntity().getText());

        response = get("withDefault");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("default", response.getEntity().getText());
    }

    public void testWithoutDefault() throws IOException {
        Response response = get("withoutDefault;mp=abcde");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("abcde", response.getEntity().getText());

        response = get("withoutDefault;mp=");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("[empty]", response.getEntity().getText());

        response = get("withoutDefault");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("[null]", response.getEntity().getText());
    }

    public void testWithoutPath() throws Exception {
        checkBothGiven("");
        checkOneGiven("");
    }
}