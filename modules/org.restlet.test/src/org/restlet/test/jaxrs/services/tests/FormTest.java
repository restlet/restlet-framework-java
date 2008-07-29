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

import org.restlet.data.Form;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.test.jaxrs.services.resources.FormTestResource;

/**
 * @author Stephan Koops
 * @see FormTestResource
 */
public class FormTest extends JaxRsTestCase {

    /**
     * @param subPath
     * @throws IOException
     */
    private void check(String subPath, boolean cPerhapsDouble) throws IOException {
        check1(subPath);
        check2(subPath);
        check3(subPath, cPerhapsDouble);
    }

    /**
     * @param subPath
     * @return
     * @throws IOException
     */
    private Representation check1(String subPath) throws IOException {
        Form form = new Form();
        form.add("a", "b");
        Representation webRepresentation = form.getWebRepresentation();
        Response response = post(subPath, webRepresentation);
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("a -> b\n", response.getEntity().getText());
        return webRepresentation;
    }

    /**
     * @param subPath
     * @throws IOException
     */
    private void check2(String subPath) throws IOException {
        Response response;
        Form form = new Form();
        form.add("a", "b");
        form.add("c", "d");
        response = post(subPath, form.getWebRepresentation());
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("a -> b\nc -> d\n", response.getEntity().getText());
    }

    /**
     * @param subPath
     * @param cDouble
     *                the variable c is given double in the entity. If this
     *                parameter is true, c must be returned double, if false,
     *                then only once.
     * @throws IOException
     */
    private void check3(String subPath, boolean cDouble) throws IOException {
        Response response;
        Form form = new Form();
        form.add("a", "b");
        form.add("c", "d");
        form.add("c", "d2");
        response = post(subPath, form.getWebRepresentation());
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        String expectedEntity = "a -> b\nc -> d\n";
        if (cDouble)
            expectedEntity += "c -> d2\n";
        assertEquals(expectedEntity, response.getEntity().getText());
    }

    @Override
    protected Class<?> getRootResourceClass() {
        return FormTestResource.class;
    }

    public void testFormAndParam() throws IOException {
        check("formAndParam", true);
    }

    public void testFormOnly() throws IOException {
        check("formOnly", true);
    }

    public void testParamAndForm() throws IOException {
        check("paramAndForm", true);
    }

    /** @see FormTestResource#paramOnly(String, String) */
    public void testParamOnly() throws IOException {
        check("paramOnly", false);
    }
}