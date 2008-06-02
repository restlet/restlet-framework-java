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

import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.test.jaxrs.services.resources.QueryParamTestService;

/**
 * @author Stephan Koops
 * @see QueryParamTestService
 */
public class QueryParamTest extends JaxRsTestCase {

    public void checkBothGiven(String subPath) throws IOException {
        Response response = get(subPath + "?firstname=Angela&lastname=Merkel");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("Angela Merkel", response.getEntity().getText());

        response = get(subPath + "?lastname=Merkel&firstname=Angela");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("Angela Merkel", response.getEntity().getText());
    }

    /**
     * @param relPath
     * @param res0
     * @param res1
     * @param res2
     * @throws IOException
     */
    private void checkMult(String relPath, String res0, String res1, String res2)
            throws IOException {
        Response response = get(relPath);
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(res0, response.getEntity().getText());

        response = get(relPath + "?qp=1");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(res1, response.getEntity().getText());

        response = get(relPath + "?qp=1&qp=2");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(res2, response.getEntity().getText());
    }

    public void checkOneGiven(String subPath) throws IOException {
        Response response = get(subPath + "?firstname=Goofy");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("Goofy null", response.getEntity().getText());

        response = get(subPath + "?lastname=Goofy");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("null Goofy", response.getEntity().getText());
    }

    @Override
    protected Class<?> getRootResourceClass() {
        return QueryParamTestService.class;
    }

    public void testA() throws IOException {
        checkBothGiven("a");
        checkOneGiven("a");
    }

    public void testDecoded() throws IOException {
        Response response = get("qpDecoded?firstname=George%20U.&lastname=Bush");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("George U. Bush", response.getEntity().getText());
    }

    public void testEncodedA() throws IOException {
        Response response = get("encodedA?firstname=George%20U.&lastname=Bush");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("George%20U. Bush", response.getEntity().getText());
    }

    /** @see QueryParamTestService#getDecoded() */
    public void testFieldDecoded() throws Exception {
        Response response1 = get("decoded?decoded=abc");
        sysOutEntityIfError(response1);
        assertEquals(Status.SUCCESS_OK, response1.getStatus());
        assertEquals("abc", response1.getEntity().getText());

        Response response2 = get("decoded?decoded=%20");
        sysOutEntityIfError(response2);
        assertEquals(Status.SUCCESS_OK, response2.getStatus());
        assertEquals(" ", response2.getEntity().getText());
    }

    /** @see QueryParamTestService#getEncoded() */
    public void testFieldEncoded() throws Exception {
        Response response1 = get("encoded?encoded=abc");
        sysOutEntityIfError(response1);
        assertEquals(Status.SUCCESS_OK, response1.getStatus());
        assertEquals("abc", response1.getEntity().getText());

        Response response2 = get("encoded?encoded=%20");
        sysOutEntityIfError(response2);
        assertEquals(Status.SUCCESS_OK, response2.getStatus());
        assertEquals("%20", response2.getEntity().getText());
    }

    /**
     * @see QueryParamTestService#getInt(int, int, int)
     */
    public void testInt() throws Exception {
        Response response1 = get("int?n1=1&n2=2&n3=3");
        sysOutEntityIfError(response1);
        assertEquals(Status.SUCCESS_OK, response1.getStatus());
        assertEquals("1 2 3", response1.getEntity().getText());

        Response response2 = get("int?n1=1&n2=2");
        sysOutEntityIfError(response2);
        assertEquals(Status.SUCCESS_OK, response2.getStatus());
        assertEquals("1 2 99", response2.getEntity().getText());

        Response response5 = get("int?n2=2&n3=3");
        sysOutEntityIfError(response5);
        assertEquals(Status.SUCCESS_OK, response5.getStatus());
        assertEquals("0 2 3", response5.getEntity().getText());

        Response response4 = get("int?n1=1&n2=2&n3=");
        sysOutEntityIfError(response4);
        assertEquals(Status.SUCCESS_OK, response4.getStatus());
        assertEquals("1 2 99", response4.getEntity().getText());

        Response response6 = get("int?n1=1&n3=3");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response6.getStatus());

        Response response3 = get("int?n1=1&n2=2&n3");
        sysOutEntityIfError(response3);
        assertEquals(Status.SUCCESS_OK, response3.getStatus());
        assertEquals("1 2 99", response3.getEntity().getText());
    }

    /**
     * @see QueryParamTestService#getInteger(Integer, Integer, Integer)
     */
    public void testInteger() throws Exception {
        Response response1 = get("Integer?n1=1&n2=2&n3=3");
        sysOutEntityIfError(response1);
        assertEquals(Status.SUCCESS_OK, response1.getStatus());
        assertEquals("1 2 3", response1.getEntity().getText());

        Response response2 = get("Integer?n1=1&n2=2");
        sysOutEntityIfError(response2);
        assertEquals(Status.SUCCESS_OK, response2.getStatus());
        assertEquals("1 2 99", response2.getEntity().getText());

        Response response5 = get("Integer?n2=2&n3=3");
        sysOutEntityIfError(response5);
        assertEquals(Status.SUCCESS_OK, response5.getStatus());
        assertEquals("null 2 3", response5.getEntity().getText());

        Response response4 = get("Integer?n1=1&n2=2&n3=");
        sysOutEntityIfError(response4);
        assertEquals(Status.SUCCESS_OK, response4.getStatus());
        assertEquals("1 2 99", response4.getEntity().getText());

        Response response6 = get("Integer?n1=1&n3=3");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response6.getStatus());

        Response response3 = get("Integer?n1=1&n2=2&n3");
        sysOutEntityIfError(response3);
        assertEquals(Status.SUCCESS_OK, response3.getStatus());
        assertEquals("1 2 99", response3.getEntity().getText());
    }
    
    public void testMult1() throws Exception {
        checkMult("array", "[null]", "[1]", "[1, 2]");
        checkMult("arrayWithDefault", "[qv]", "[1]", "[1, 2]");

        checkMult("list", "[null]", "[1]", "[1, 2]");
        checkMult("listWithDefault", "[qv]", "[1]", "[1, 2]");
    }
    
    public void testOne1() throws Exception {
        Response response = get("one?name");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("[empty]", response.getEntity().getText());
    }

    public void testOne2() throws Exception {
        Response response = get("one?name=");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("[empty]", response.getEntity().getText());
    }

    public void testOne3() throws Exception {
        Response response = get("one?name=x");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("x", response.getEntity().getText());
    }

    public void testOne4() throws Exception {
        Response response = get("one?name2=sdf");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("[null]", response.getEntity().getText());
    }

    public void testQpDecoded() throws IOException {
        checkBothGiven("qpDecoded");
        checkOneGiven("qpDecoded");
    }

    public void testQpEncoded() throws IOException {
        Response response = get("qpEncoded?firstname=George%20U.&lastname=Bush");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("George%20U. Bush", response.getEntity().getText());

        response = get("qpEncoded?lastname=Bush&firstname=George%20U.");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("George%20U. Bush", response.getEntity().getText());
    }
}