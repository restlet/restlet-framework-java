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

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.restlet.test.ext.jaxrs.services.tests.InjectionTest;

/**
 * @author Stephan Koops
 * @see InjectionTest
 * @see Context
 */
@Path("/InjectionTestService")
public class InjectionTestService {

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;

    @Context
    private Request request;

    private HttpHeaders httpHeaders;

    @HeaderParam("host")
    private String host;

    private String qp1;

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
        if (this.host == null) {
            msg += "\n* host";
        }
        if (this.qp1 == null) {
            msg += "\n* qp1";
        }
        if (msg.length() > 0) {
            return Response.serverError().entity("missing:" + msg).build();
        }
        return Response.ok("ok").build();
    }

    @Context
    void setHeaders(HttpHeaders httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    @QueryParam("qp1")
    void setQp1(String qp1) {
        this.qp1 = qp1;
    }
}
