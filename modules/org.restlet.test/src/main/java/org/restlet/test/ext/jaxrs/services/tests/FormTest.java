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

import javax.ws.rs.core.Application;

import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.test.ext.jaxrs.services.resources.FormTestResource;

/**
 * @author Stephan Koops
 * @see FormTestResource
 */
public class FormTest extends JaxRsTestCase {

    /**
     * @param subPath
     * @throws IOException
     */
    private void check(String subPath, boolean cPerhapsDouble)
            throws IOException {
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
     *            the variable c is given double in the entity. If this
     *            parameter is true, c must be returned double, if false, then
     *            only once.
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
    protected Application getApplication() {
        return new Application() {
            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Set<Class<?>> getClasses() {
                return (Set) Collections.singleton(FormTestResource.class);
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.emptySet();
            }
        };
    }

    /** @see FormTestResource#checkUnmodifiable(java.util.List) */
    public void testCheckUnmodifiable() {
        Form form = new Form();
        form.add("a", "b");
        form.add("a", "c");
        Response response = post("checkUnmodifiable",
                form.getWebRepresentation());
        sysOutEntityIfError(response);
        assertTrue(
                "The List annotated with @FormParam must not be modifiable. Status is "
                        + response.getStatus(), response.getStatus()
                        .isSuccess());
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
