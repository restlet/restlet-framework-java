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
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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
    @Produces(MediaType.TEXT_PLAIN)
    @Path("encodedWithDefault")
    public String encodedList(
            @Encoded @DefaultValue("default") @MatrixParam("m") List<String> cc) {
        return cc.toString();
    }

    @GET
    @Produces("text/plain")
    public String get(@MatrixParam("firstname") String firstname,
            @MatrixParam("lastname") String lastname, @Context UriInfo uriInfo) {
        final List<PathSegment> pathSegents = uriInfo.getPathSegments();
        final PathSegment lastPathSegm = pathSegents.get(0);
        final MultivaluedMap<String, String> mp = lastPathSegm
                .getMatrixParameters();
        if (mp.isEmpty()) {
            final ResponseBuilder rb = Response.status(Status.NOT_FOUND);
            rb.entity("matrix parameters are empty");
            throw new WebApplicationException(rb.build());
        }
        return firstname + " " + lastname;
    }
}