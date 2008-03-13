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

import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.test.jaxrs.services.tests.MatrixParamTest;

/**
 * @author Stephan Koops
 * @see MatrixParamTest
 */
@Path("matrixParamTest")
public class MatrixParamTestService {

    @GET
    @ProduceMime("text/plain")
    @Path("a")
    public String getA(@MatrixParam("firstname") String firstname,
            @MatrixParam("lastname") String lastname) {
        return firstname + " " + lastname;
    }

    @GET
    @ProduceMime("text/plain")
    @Path("b")
    public String getB(@Context UriInfo uriInfo) {
        PathSegment pSeg = Util.getLastElement(uriInfo.getPathSegments());
        String vorname = pSeg.getMatrixParameters().getFirst("firstname");
        String nachname = pSeg.getMatrixParameters().getFirst("lastname");
        return vorname + " " + nachname;
    }

    @GET
    @ProduceMime("text/plain")
    @Path("encoded")
    @Encoded
    public String encoded(@MatrixParam("firstname") String firstname,
            @MatrixParam("lastname") String lastname) {
        return firstname + " " + lastname;
    }

    @GET
    @ProduceMime("text/plain")
    @Path("withDefault")
    @Encoded
    public String withDefault(@MatrixParam("mp") @DefaultValue("default") String mp) {
        return withoutDefault(mp);
    }

    @GET
    @ProduceMime("text/plain")
    @Path("withoutDefault")
    @Encoded
    public String withoutDefault(@MatrixParam("mp") String mp) {
        if (mp == null)
            return "[null]";
        if (mp.equals(""))
            return "[empty]";
        return mp;
    }

    @GET
    @ProduceMime("text/plain")
    @Path("semicolon;mpA=")
    public Response withSemicolon(@MatrixParam("mpA") String mpA,
            @MatrixParam("mpB") String mpB) {
        String entity = "this method must not be called\nmpA param is " + mpA
                + "\nmpB param is " + mpB;
        return Response.serverError().entity(entity).build();
    }

    @GET
    @ProduceMime("text/plain")
    @Path("one")
    public String getOne(@MatrixParam("name") String name) {
        if(name == null)
            return "[null]";
        if (name.equals(""))
            return "[empty]";
        return name;
    }
}