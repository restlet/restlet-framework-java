/*
 * Copyright 2005-2008 Noelios Consulting.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
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