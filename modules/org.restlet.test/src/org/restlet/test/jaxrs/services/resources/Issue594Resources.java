/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.restlet.test.jaxrs.services.tests.Issue594Test;

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
   public String project(@PathParam("project") String project) {
       return "project="+project;
   }

   @GET
   @Path("{project}/{repository}")
   @Produces("text/plain")
   public String repository(@PathParam("project") String project,
           @PathParam("repository") String repository) {
       return "project="+project+"\nrepository="+repository;
   }

   @GET
   @Path("{project}/{repository}/schema")
   @Produces("text/plain")
   public String schemaDir(@PathParam("project") String project,
           @PathParam("repository") String repository) {
       return "project="+project+"\nrepository="+repository+"\nschema";
   }

   @GET
   @Path("{project}/{repository}/schema/{schema}")
   @Produces("text/plain")
   public String schema(@PathParam("project") String project,
           @PathParam("repository") String repository,
           @PathParam("schema") String schema) {
       return "project="+project+"\nrepository="+repository+"\nschema\nschema="+schema;
   }
}