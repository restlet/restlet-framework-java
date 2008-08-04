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
package org.restlet.test.jaxrs.services.resources;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.test.jaxrs.services.others.Person;
import org.restlet.test.jaxrs.services.tests.RepresentationTest;

import com.noelios.restlet.application.DecodeRepresentation;

/**
 * This tests ensures, that it is also working with a Restlet
 * {@link Representation} and subclasses.
 * 
 * @author Stephan Koops
 * @see RepresentationTest
 * @see Representation
 */
@Path("representationTest")
public class RepresentationTestService {

    /**@return*/
    @GET
    @Path("repr")
    @Produces("text/plain")
    public Representation get() {
        return getString();
    }

    /**@return*/
    @GET
    @Path("reprString")
    @Produces("text/plain")
    public StringRepresentation getString() {
        return new StringRepresentation("jgkghkg");
    }

    /**@return
     * @param representation
     * @throws IOException */
    @POST
    @Path("repr")
    public Response post(Representation representation) throws IOException {
        final String type = representation.getMediaType().toString();
        final String entity = representation.getText();
        return Response.ok(entity).type(type).build();
    }

    /**@return
     * @param representation
     * @throws IOException */
    @POST
    @Path("reprDecode")
    public Response postDecode(DecodeRepresentation representation)
            throws IOException {
        final String type = representation.getMediaType().toString();
        final String entity = representation.getText();
        return Response.ok(entity).type(type).build();
    }

    /**@return
     * @param personRepr
     * @throws IOException */
    @POST
    @Path("jaxb")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    public String postJaxb(JaxbRepresentation<Person> personRepr)
            throws IOException {
        if (personRepr == null) {
            return null;
        }
        personRepr.getObject();
        return personRepr.getContextPath();
    }
}