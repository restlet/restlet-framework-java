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

import java.io.IOException;

import javax.ws.rs.MatrixParam;

import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.test.jaxrs.services.resources.MatrixParamTestService;

/**
 * @author Stephan Koops
 * @see MatrixParamTestService
 * @see MatrixParam
 */
public class MatrixParamTest extends JaxRsTestCase {

    @Override
    protected Class<?> getRootResourceClass() {
        return MatrixParamTestService.class;
    }

    public void testWithoutPath() throws Exception {
        checkBothGiven("");
        checkOneGiven("");
    }
    
    public void testA() throws IOException {
        checkBothGiven("a");
        checkOneGiven("a");
    }

    public void testB() throws IOException {
        checkBothGiven("b");
        checkOneGiven("b");
    }

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

    public void testEncoded() throws IOException {
        Response response = get("encoded;firstname=George%20U.;lastname=Bush");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("George%20U. Bush", response.getEntity().getText());
    }

    public void testDecoded() throws IOException {
        Response response = get("b;firstname=George%20U.;lastname=Bush");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("George U. Bush", response.getEntity().getText());
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

    public void testSemicolon() {
        Response response1 = get("semicolon");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response1.getStatus());
        sysOutEntityIfError(response1);

        Response response2 = get("semicolon;mpA=6");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response2.getStatus());
        sysOutEntityIfError(response2);

        Response response3 = get("semicolon;mpB=6");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response3.getStatus());
        sysOutEntityIfError(response3);

        Response response4 = get("semicolon;mpB=6;mpA=5");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response4.getStatus());
        sysOutEntityIfError(response4);

        Response response5 = get("semicolon;mpA=5;mpB=6");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response5.getStatus());
        sysOutEntityIfError(response5);
    }

    public void testOne1() throws Exception {
        Response response = get("one;name");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("[empty]", response.getEntity().getText());
    }

    public void testOne2() throws Exception {
        Response response = get("one;name=");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("[empty]", response.getEntity().getText());
    }

    public void testOne3() throws Exception {
        Response response = get("one;name=x");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("x", response.getEntity().getText());
    }

    public void testOne4() throws Exception {
        Response response = get("one;name2=sdf");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("[null]", response.getEntity().getText());
    }

    public void testSub1() throws Exception {
        Response response2 = get("sub;name=abc/one");
        sysOutEntityIfError(response2);
        assertEquals(Status.SUCCESS_OK, response2.getStatus());
        assertEquals("abc", response2.getEntity().getText());

        Response response3 = get("sub/one;name=def");
        sysOutEntityIfError(response3);
        assertEquals(Status.SUCCESS_OK, response3.getStatus());
        assertEquals("def", response3.getEntity().getText());

        Response response4 = get("sub;name=abc/one;name=def");
        sysOutEntityIfError(response4);
        assertEquals(Status.SUCCESS_OK, response4.getStatus());
        assertEquals("def", response4.getEntity().getText());

        Response response5 = get("sub;name=abc/allNames;name=def");
        sysOutEntityIfError(response5);
        assertEquals(Status.SUCCESS_OK, response5.getStatus());
        assertEquals("[abc, def]", response5.getEntity().getText());

        Response response6 = get("allNames;name=abc");
        sysOutEntityIfError(response6);
        assertEquals(Status.SUCCESS_OK, response6.getStatus());
        assertEquals("[abc]", response6.getEntity().getText());
    }
}