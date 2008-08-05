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

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.restlet.test.jaxrs.services.tests.InjectionTest;

/**
 * @author Stephan Koops
 * @see InjectionTest
 * @see Context
 */
@Path("/InjectionTestService/two/{idf}")
public class InjectionTestService2 {

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;

    @Context
    private Request request;

    @Context
    private HttpHeaders httpHeaders;

    @PathParam("idf")
    private Integer idf;

    @HeaderParam("host")
    private String hostHost;

    @GET
    @Produces("text/plain")
    public Response get() {
        String msg = "";
        if (this.securityContext == null) {
            msg += "\n* securityContext";
        }
        if (this.uriInfo == null) {
            msg += "\n* uriInfo";
        }
        if (this.request == null) {
            msg += "\n* request";
        }
        if (this.httpHeaders == null) {
            msg += "\n* httpHeaders";
        }
        if (this.idf == null) {
            msg += "\n* id";
        }
        if (this.hostHost == null) {
            msg += "\n* host";
        }
        if (msg.length() > 0) {
            return Response.serverError().entity("missing:" + msg).build();
        }
        return Response.ok(String.valueOf(this.idf)).build();
    }
}