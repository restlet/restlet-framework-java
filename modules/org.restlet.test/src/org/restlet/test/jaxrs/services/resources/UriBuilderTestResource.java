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
package org.restlet.test.jaxrs.services.resources;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.restlet.test.jaxrs.services.tests.UriBuilderByServiceTest;

/**
 * @author Stephan Koops
 * @see UriBuilderByServiceTest
 */
@Path("UriBuilder")
public class UriBuilderTestResource {
    @Context
    UriInfo uriInfo;

    // REQUEST rename UriInfo.getPlatonicRequestUriBuilder() to
    // .getPlatonicUriBuilder() or uriInfo.getPlatonicPathUriBuilder() ?

    @GET
    @Path("absolute")
    @ProduceMime( { MediaType.TEXT_PLAIN, MediaType.TEXT_HTML })
    public String getAbsoluteUriBuilder() {
        return uriInfo.getAbsolutePathBuilder().build().toString();
    }

    @GET
    @Path("base")
    @ProduceMime( { MediaType.TEXT_PLAIN, MediaType.TEXT_HTML })
    public String getBaseUriBuilder() {
        return uriInfo.getBaseUriBuilder().build().toString();
    }

    @GET
    @Path("platonic")
    @ProduceMime( { MediaType.TEXT_PLAIN, MediaType.TEXT_HTML })
    public String getPlatonicUriBuilder() {
        return uriInfo.getPlatonicRequestUriBuilder().build().toString();
    }

    @POST
    @Path("absolute")
    @ProduceMime( { MediaType.TEXT_PLAIN, MediaType.TEXT_HTML })
    public String postAbsoluteUriBuilder() {
        return uriInfo.getAbsolutePathBuilder().build().toString();
    }

    @POST
    @Path("base")
    @ProduceMime( { MediaType.TEXT_PLAIN, MediaType.TEXT_HTML })
    public String postBaseUriBuilder() {
        return uriInfo.getBaseUriBuilder().build().toString();
    }

    @POST
    @Path("platonic")
    @ProduceMime( { MediaType.TEXT_PLAIN, MediaType.TEXT_HTML })
    public String postPlatonicUriBuilder() {
        return uriInfo.getPlatonicRequestUriBuilder().build().toString();
    }
}