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

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.restlet.test.jaxrs.services.tests.MatrixParamTest2;

/**
 * @author Stephan Koops
 * @see MatrixParamTest2
 * @see MatrixParam
 * @see UriInfo
 */
@Path("")
public class MatrixParamTestService2 {

    @GET
    @ProduceMime("text/plain")
    public String get(@MatrixParam("firstname") String firstname,
            @MatrixParam("lastname") String lastname, @Context UriInfo uriInfo) {
        List<PathSegment> pathSegents = uriInfo.getPathSegments();
        PathSegment lastPathSegm = pathSegents.get(0);
        MultivaluedMap<String, String> mp = lastPathSegm.getMatrixParameters();
        if(mp.isEmpty()) {
            ResponseBuilder rb = Response.status(Status.NOT_FOUND);
            rb.entity("matrix parameters are empty");
            throw new WebApplicationException(rb.build());
        }
        return firstname + " " + lastname;
    }

    @GET
    @ProduceMime(MediaType.TEXT_PLAIN)
    @Path("encodedWithDefault")
    public String encodedList(@Encoded @DefaultValue("default") 
            @MatrixParam("m") List<String> cc) {
        return cc.toString();
    }
}