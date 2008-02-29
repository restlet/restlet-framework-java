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
import org.restlet.test.jaxrs.services.MatrixParamTestService;

/**
 * @author Stephan Koops
 */
public class MatrixParamTest extends JaxRsTestCase {

    @Override
    protected Class<?> getRootResourceClass() {
        return MatrixParamTestService.class;
    }

    public void testA() throws IOException
    {
        checkBothGiven("a");
        checkOneGiven("a");
    }
    
    public void testB() throws IOException
    {
        checkBothGiven("b");
        checkOneGiven("b");
    }
    
    public void checkBothGiven(String subPath) throws IOException {
        Response response = get(subPath+";firstname=Angela;lastname=Merkel");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("Angela Merkel", response.getEntity().getText());

        response = get(subPath+";lastname=Merkel;firstname=Angela");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("Angela Merkel", response.getEntity().getText());
    }
    
    public void checkOneGiven(String subPath) throws IOException {
        Response response = get(subPath+";firstname=Goofy");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("Goofy null", response.getEntity().getText());

        response = get(subPath+";lastname=Goofy");
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
}