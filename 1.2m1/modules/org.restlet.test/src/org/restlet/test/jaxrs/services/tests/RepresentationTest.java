/**
 * Copyright 2005-2009 Noelios Technologies.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.test.jaxrs.services.tests;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.test.jaxrs.services.others.Person;
import org.restlet.test.jaxrs.services.resources.RepresentationTestService;

/**
 * @author Stephan Koops
 * @see RepresentationTestService
 */
public class RepresentationTest extends JaxRsTestCase {

    @Override
    protected Application getApplication() {
        final Application appConfig = new Application() {
            @Override
            @SuppressWarnings("unchecked")
            public Set<Class<?>> getClasses() {
                return (Set) Collections.singleton(RepresentationTestService.class);
            }
        };
        return appConfig;
    }

    /** @throws IOException 
     *  @see RepresentationTestService#post(Representation) */
    public void testDecodePost() throws IOException {
        final Representation repr = new StringRepresentation("abcde");
        final Response response = post("reprDecode", repr);
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("abcde", response.getEntity().getText());
    }

    /** @throws IOException 
     *  @see RepresentationTestService#postJaxb(org.restlet.ext.jaxb.JaxbRepresentation) */
    public void testJaxbPost() throws IOException {
        final Response response = post("jaxb", (Representation) null);
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
        assertEquals(null, response.getEntity().getText());
    }

    /** @see RepresentationTestService#get() */
    public void testReprGet() {
        final Response response = get("repr");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
    }

    /** @throws IOException 
     *  @see RepresentationTestService#postJaxb(org.restlet.ext.jaxb.JaxbRepresentation) */
    public void testReprPost() throws IOException {
        Response response = post("jaxb", new StringRepresentation("abcdef"));
        assertEquals(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, response
                .getStatus());

        response = post("jaxb", new StringRepresentation(
                "<person firstname=\"Angela\" lastname=\"Merkel\"/>",
                MediaType.APPLICATION_XML));
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        String packageName = Person.class.getPackage().getName();
        assertEquals(packageName, response.getEntity().getText());
    }

    /** @see RepresentationTestService#getString() */
    public void testStringGet() {
        final Response response = get("reprString");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
    }
}