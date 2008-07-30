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
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.restlet.test.jaxrs.services.tests.MatrixParamTest;
import org.restlet.test.jaxrs.util.TestUtils;

/**
 * @author Stephan Koops
 * @see MatrixParamTest
 * @see MatrixParam
 */
@Path("matrixParamTest")
public class MatrixParamTestService {

    private String encoded;

    private String decoded;
    
    @GET
    @Produces("text/plain")
    @Path("checkUnmodifiable")
    public Object checkUnmodifiable(@MatrixParam("mp") List<String> mps) {
        try {
            mps.clear();
            throw new WebApplicationException(Response.serverError().entity(
                    "the List must be unmodifiable").build());
        } catch (UnsupportedOperationException use) {
            return null;
        }
    }

    @GET
    @Produces("text/plain")
    @Path("encoded")
    @Encoded
    public String encoded(@MatrixParam("firstname") String firstname,
            @MatrixParam("lastname") String lastname) {
        return firstname + " " + lastname;
    }

    @GET
    @Produces("text/plain")
    public String get(@MatrixParam("firstname") String firstname,
            @MatrixParam("lastname") String lastname) {
        return firstname + " " + lastname;
    }

    @GET
    @Produces("text/plain")
    @Path("a")
    public String getA(@MatrixParam("firstname") String firstname,
            @MatrixParam("lastname") String lastname) {
        return firstname + " " + lastname;
    }

    @GET
    @Produces("text/plain")
    @Path("allNames")
    public String getAllNames(@MatrixParam("name") List<String> name) {
        return name.toString();
    }

    @GET
    @Produces("text/plain")
    @Path("b")
    public String getB(@Context UriInfo uriInfo) {
        final PathSegment pSeg = TestUtils.getLastElement(uriInfo
                .getPathSegments());
        final String vorname = pSeg.getMatrixParameters().getFirst("firstname");
        final String nachname = pSeg.getMatrixParameters().getFirst("lastname");
        return vorname + " " + nachname;
    }

    @GET
    @Produces("text/plain")
    @Path("one")
    public String getOne(@MatrixParam("name") String name) {
        if (name == null) {
            return "[null]";
        }
        if (name.equals("")) {
            return "[empty]";
        }
        return name;
    }

    @GET
    @Produces("text/plain")
    @Path("setterDecoded")
    public String getSetterDecoded() {
        return this.decoded;
    }

    @GET
    @Produces("text/plain")
    @Path("setterEncoded")
    public String getSetterEncoded() {
        return this.encoded;
    }

    @Path("sub")
    public MatrixParamTestService getSub() {
        return new MatrixParamTestService();
    }

    @MatrixParam("decoded")
    public void setDecoded(String decoded) {
        this.decoded = decoded;
    }

    @Encoded
    @MatrixParam("encoded")
    public void setEncoded(String encoded) {
        this.encoded = encoded;
    }

    @GET
    @Produces("text/plain")
    @Path("withDefault")
    @Encoded
    public String withDefault(
            @MatrixParam("mp") @DefaultValue("default") String mp) {
        return withoutDefault(mp);
    }

    @GET
    @Produces("text/plain")
    @Path("withoutDefault")
    @Encoded
    public String withoutDefault(@MatrixParam("mp") String mp) {
        if (mp == null) {
            return "[null]";
        }
        if (mp.equals("")) {
            return "[empty]";
        }
        return mp;
    }

    @GET
    @Produces("text/plain")
    @Path("semicolon;mpA=")
    public Response withSemicolon(@MatrixParam("mpA") String mpA,
            @MatrixParam("mpB") String mpB) {
        final String entity = "this method must not be called\nmpA param is "
                + mpA + "\nmpB param is " + mpB;
        return Response.serverError().entity(entity).build();
    }
}