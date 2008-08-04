/**
 * Copyright 2005-2008 Noelios Technologies.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.test.jaxrs.services.resources;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.restlet.test.jaxrs.services.tests.QueryParamTest;

/**
 * @author Stephan Koops
 * @see QueryParamTest
 * @see QueryParam
 */
@Path("queryParamTest")
public class QueryParamTestService {

    @QueryParam("decoded")
    private String decoded;

    @Encoded
    @QueryParam("encoded")
    private String encoded;

    @GET
    @Produces("text/plain")
    @Path("checkUnmodifiable")
    public Object checkUnmodifiable(@QueryParam("a") List<String> as) {
        try {
            as.clear();
            throw new WebApplicationException(Response.serverError().entity(
                    "the List must be unmodifiable").build());
        } catch (UnsupportedOperationException uoe) {
            return null;
        }
    }
    
    @GET
    @Produces("text/plain")
    @Path("encodedA")
    @Encoded
    public String encodedA(@QueryParam("firstname") String firstname,
            @QueryParam("lastname") String lastname) {
        return firstname + " " + lastname;
    }

    @GET
    @Produces("text/plain")
    @Path("a")
    public String getA(@QueryParam("firstname") String firstname,
            @QueryParam("lastname") String lastname) {
        return firstname + " " + lastname;
    }

    @GET
    @Produces("text/plain")
    @Path("array")
    public String getArrayQp(@QueryParam("qp") String[] qp) {
        return Arrays.toString(qp);
    }

    @GET
    @Produces("text/plain")
    @Path("arrayWithDefault")
    public String getArrayQpDef(
            @QueryParam("qp") @DefaultValue("qv") String[] qp) {
        return Arrays.toString(qp);
    }

    @GET
    @Produces("text/plain")
    @Path("decoded")
    public String getFieldDecoded() {
        return this.decoded;
    }

    @GET
    @Produces("text/plain")
    @Path("encoded")
    public String getFieldEncoded() {
        return this.encoded;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("int")
    public String getInt(@QueryParam("n1") int n1,
            @QueryParam("n2") @DefaultValue("xx") int n2,
            @QueryParam("n3") @DefaultValue("99") int n3) {
        return n1 + " " + n2 + " " + n3;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("Integer")
    public String getInteger(@QueryParam("n1") Integer n1,
            @QueryParam("n2") @DefaultValue("xx") Integer n2,
            @QueryParam("n3") @DefaultValue("99") Integer n3) {
        return n1 + " " + n2 + " " + n3;
    }

    @GET
    @Produces("text/plain")
    @Path("list")
    public String getListQp(@QueryParam("qp") List<String> qp) {
        return qp.toString();
    }

    @GET
    @Produces("text/plain")
    @Path("listWithDefault")
    public String getListQpDef(
            @QueryParam("qp") @DefaultValue("qv") List<String> qp) {
        return qp.toString();
    }

    @GET
    @Produces("text/plain")
    @Path("one")
    public String getOne(@QueryParam("name") String name) {
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
    @Path("qpDecoded")
    public String getQueryParamsDecoded(@Context UriInfo uriInfo) {
        final String firstname = uriInfo.getQueryParameters().getFirst(
                "firstname");
        final String lastname = uriInfo.getQueryParameters().getFirst(
                "lastname");
        return firstname + " " + lastname;
    }

    @GET
    @Produces("text/plain")
    @Path("qpEncoded")
    public String getQueryParamsEncoded(@Context UriInfo uriInfo) {
        final String firstn = uriInfo.getQueryParameters(false).getFirst(
                "firstname");
        final String lastn = uriInfo.getQueryParameters(false).getFirst(
                "lastname");
        return firstn + " " + lastn;
    }
}