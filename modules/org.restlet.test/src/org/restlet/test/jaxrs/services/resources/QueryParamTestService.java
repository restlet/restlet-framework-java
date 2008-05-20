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

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.restlet.test.jaxrs.services.tests.QueryParamTest;

/**
 * @author Stephan Koops
 * @see QueryParamTest
 * @see QueryParam
 */
@Path("queryParamTest")
public class QueryParamTestService {

    @GET
    @ProduceMime("text/plain")
    @Path("encodedA")
    @Encoded
    public String encodedA(@QueryParam("firstname") String firstname,
            @QueryParam("lastname") String lastname) {
        return firstname + " " + lastname;
    }

    @GET
    @ProduceMime("text/plain")
    @Path("a")
    public String getA(@QueryParam("firstname") String firstname,
            @QueryParam("lastname") String lastname) {
        return firstname + " " + lastname;
    }

    @GET
    @ProduceMime(MediaType.TEXT_PLAIN)
    @Path("int")
    public String getInt(@QueryParam("n1") int n1,
            @QueryParam("n2") @DefaultValue("xx") int n2,
            @QueryParam("n3") @DefaultValue("99") int n3) {
        return n1+" "+n2+" "+n3;
    }

    @GET
    @ProduceMime(MediaType.TEXT_PLAIN)
    @Path("Integer")
    public String getInteger(@QueryParam("n1") Integer n1,
            @QueryParam("n2") @DefaultValue("xx") Integer n2,
            @QueryParam("n3") @DefaultValue("99") Integer n3) {
        return n1+" "+n2+" "+n3;
    }

    @GET
    @ProduceMime("text/plain")
    @Path("one")
    public String getOne(@QueryParam("name") String name) {
        if(name == null)
            return "[null]";
        if (name.equals(""))
            return "[empty]";
        return name;
    }
    
    @GET
    @ProduceMime("text/plain")
    @Path("qpDecoded")
    public String getQueryParamsDecoded(@Context UriInfo uriInfo) {
        String firstname = uriInfo.getQueryParameters().getFirst("firstname");
        String lastname = uriInfo.getQueryParameters().getFirst("lastname");
        return firstname + " " + lastname;
    }
    
    @GET
    @ProduceMime("text/plain")
    @Path("qpEncoded")
    public String getQueryParamsEncoded(@Context UriInfo uriInfo) {
        String firstn = uriInfo.getQueryParameters(false).getFirst("firstname");
        String lastn = uriInfo.getQueryParameters(false).getFirst("lastname");
        return firstn + " " + lastn;
    }

    @GET
    @ProduceMime("text/plain")
    @Path("array")
    public String getArrayQp(@QueryParam("qp") String[] qp) {
        return Arrays.toString(qp);
    }

    @GET
    @ProduceMime("text/plain")
    @Path("arrayWithDefault")
    public String getArrayQpDef(@QueryParam("qp") @DefaultValue("qv") String[] qp) {
        return Arrays.toString(qp);
    }

    @GET
    @ProduceMime("text/plain")
    @Path("list")
    public String getListQp(@QueryParam("qp") List<String> qp) {
        return qp.toString();
    }

    @GET
    @ProduceMime("text/plain")
    @Path("listWithDefault")
    public String getListQpDef(@QueryParam("qp") @DefaultValue("qv") List<String> qp) {
        return qp.toString();
    }
}