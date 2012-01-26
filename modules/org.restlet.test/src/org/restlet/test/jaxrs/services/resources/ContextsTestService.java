/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.jaxrs.services.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;

import org.restlet.test.jaxrs.services.tests.ContextsTest;

/**
 * @author Stephan Koops
 * @see ContextsTest
 */
@Path("contextTest")
public class ContextsTestService {

    @Context
    Providers providers;

    void setProviders(Providers providers) {
        this.contextResolver = providers.getContextResolver(Integer.class,
                MediaType.WILDCARD_TYPE);
    }

    ContextResolver<Integer> contextResolver;

    @Context
    UriInfo uriInfo;

    @GET
    @Produces("text/plain")
    @Path("fields")
    public String fieldsAvailable() {
        final StringBuilder stb = new StringBuilder();
        if (this.providers != null) {
            stb.append("providers\n");
        }
        if (this.contextResolver != null) {
            stb.append("contextResolver\n");
        }
        if (this.uriInfo != null) {
            stb.append("uriInfo\n");
        }
        return stb.toString();
    }

    @GET
    @Produces("text/plain")
    @Path("lastPathSegm")
    public String getPathSegm(@Context PathSegment lastPathSegment) {
        final StringBuilder stb = new StringBuilder();
        for (String key : lastPathSegment.getMatrixParameters().keySet()) {
            stb.append(key + " : "
                    + lastPathSegment.getMatrixParameters().get(key) + "\n");
        }
        return stb.toString();
    }

    @GET
    @Produces("text/plain")
    @Path("params")
    public String getResources(@Context UriInfo uriInfo,
            @Context Providers providers) {
        final StringBuilder stb = new StringBuilder();
        if (providers != null) {
            stb.append("providers\n");
        }
        if (contextResolver != null) {
            stb.append("contextResolver\n");
        }
        if (uriInfo != null) {
            stb.append("uriInfo\n");
        }
        return stb.toString();
    }
}
