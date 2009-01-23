/**
 * Copyright 2005-2009 Noelios Technologies.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.test.jaxrs.services.resources;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
        Set<Entry<String, List<String>>> entries;
        entries = lastPathSegment.getMatrixParameters().entrySet();
        final StringBuilder stb = new StringBuilder();
        for (final Map.Entry<String, List<String>> entry : entries) {
            stb.append(entry.getKey() + " : " + entry.getValue() + "\n");
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