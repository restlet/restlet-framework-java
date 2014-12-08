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

package org.restlet.test.ext.jaxrs.services.resources;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.restlet.engine.application.DecodeRepresentation;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.test.ext.jaxrs.services.others.Person;
import org.restlet.test.ext.jaxrs.services.tests.RepresentationTest;

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

    /** @return */
    @GET
    @Path("repr")
    @Produces("text/plain")
    public Representation get() {
        return getString();
    }

    /** @return */
    @GET
    @Path("reprString")
    @Produces("text/plain")
    public StringRepresentation getString() {
        return new StringRepresentation("jgkghkg");
    }

    /**
     * @return
     * @param representation
     * @throws IOException
     */
    @POST
    @Path("repr")
    public Response post(Representation representation) throws IOException {
        final String type = representation.getMediaType().toString();
        final String entity = representation.getText();
        return Response.ok(entity).type(type).build();
    }

    /**
     * @return
     * @param representation
     * @throws IOException
     */
    @POST
    @Path("reprDecode")
    public Response postDecode(DecodeRepresentation representation)
            throws IOException {
        final String type = representation.getMediaType().toString();
        final String entity = representation.getText();
        return Response.ok(entity).type(type).build();
    }

    /**
     * @return
     * @param personRepr
     * @throws IOException
     */
    @POST
    @Path("jaxb")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public String postJaxb(JaxbRepresentation<Person> personRepr)
            throws IOException {
        if (personRepr == null) {
            return null;
        }
        personRepr.getObject();
        return personRepr.getContextPath();
    }
}
