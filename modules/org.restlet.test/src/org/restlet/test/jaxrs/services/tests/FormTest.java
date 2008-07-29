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
import org.restlet.test.jaxrs.services.resources.FormTestResource;

/**
 * @author Stephan Koops
 * @see FormTestResource
 */
public class FormTest extends JaxRsTestCase {

    @Override
    protected Class<?> getRootResourceClass() {
        return FormTestResource.class;
    }

    public void testFormOnly() throws IOException {
        check("formOnly");
    }

    public void testFormAndParam() throws IOException {
        check("formAndParam");
    }

    /** @see FormTestResource#paramOnly(String, String) */
    public void testParamOnly() throws IOException {
        check("paramOnly");
    }

    public void testParamAndForm() throws IOException {
        check("paramAndForm");
    }

    /**
     * @param subPath
     * @throws IOException
     */
    private void check(String subPath) throws IOException {
        Form form = new Form();
        form.add("a", "b");
        Response response = post(subPath, form.getWebRepresentation());
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("a -> b\n", response.getEntity().getText());

        form.add("c", "d");
        response = post(subPath, form.getWebRepresentation());
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("a -> b\nc -> d\n", response.getEntity().getText());

        form.add("c", "d");
        response = post(subPath, form.getWebRepresentation());
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("a -> b\nc -> d\nc -> d\n", response.getEntity().getText());
    }
}