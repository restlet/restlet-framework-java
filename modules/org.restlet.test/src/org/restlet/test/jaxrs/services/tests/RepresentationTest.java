/*
 * Copyright 2005-2008 Noelios Technologies.
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
    protected Class<?> getRootResourceClass() {
        return RepresentationTestService.class;
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