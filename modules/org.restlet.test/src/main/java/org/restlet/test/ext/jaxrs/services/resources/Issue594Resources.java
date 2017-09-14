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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.restlet.test.ext.jaxrs.services.tests.Issue594Test;

/**
 * @author Roman Geus
 * @author Stephan Koops
 * @see Issue594Test
 */
@Path("admin")
public class Issue594Resources {

    /**
     * Provides both static and dynamic, per-request information, about the
     * components of a request URI.
     */
    @Context
    UriInfo uriInfo;

    @GET
    @Produces("text/plain")
    public String root() {
        return "root";
    }

    @GET
    @Path("{project}")
    @Produces("text/plain")
    public String project(@PathParam("project")
    String project) {
        return "project=" + project;
    }

    @GET
    @Path("{project}/{repository}")
    @Produces("text/plain")
    public String repository(@PathParam("project")
    String project, @PathParam("repository")
    String repository) {
        return "project=" + project + "\nrepository=" + repository;
    }

    @GET
    @Path("{project}/{repository}/schema")
    @Produces("text/plain")
    public String schemaDir(@PathParam("project")
    String project, @PathParam("repository")
    String repository) {
        return "project=" + project + "\nrepository=" + repository + "\nschema";
    }

    @GET
    @Path("{project}/{repository}/schema/{schema}")
    @Produces("text/plain")
    public String schema(@PathParam("project")
    String project, @PathParam("repository")
    String repository, @PathParam("schema")
    String schema) {
        return "project=" + project + "\nrepository=" + repository
                + "\nschema\nschema=" + schema;
    }
}
