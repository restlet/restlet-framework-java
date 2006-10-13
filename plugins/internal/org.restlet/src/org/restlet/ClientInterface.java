/*
 * Copyright 2005-2006 Noelios Consulting.
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

package org.restlet;

import org.restlet.data.Representation;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Uniform client interface.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public interface ClientInterface
{
	/**
	 * Handles a call.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	public void handle(Request request, Response response);
	
	/**
	 * Handles a call.
	 * @param request The request to handle.
	 * @return The returned response.
	 */
	public Response handle(Request request);
	
   /**
    * Deletes the identified resource.
    * @param resourceUri The URI of the resource to delete.
    * @return The response.
    */
   public Response delete(String resourceUri);

   /**
    * Gets the identified resource.
    * @param resourceUri The URI of the resource to get.
    * @return The response.
    */
   public Response get(String resourceUri);
   
   /**
    * Gets the identified resource without its representation's content.
    * @param resourceUri The URI of the resource to get.
    * @return The response.
    */
   public Response head(String resourceUri);
   
   /**
    * Gets the options for the identified resource.
    * @param resourceUri The URI of the resource to get.
    * @return The response.
    */
   public Response options(String resourceUri);
   
   /**
    * Posts a representation to the identified resource.
    * @param resourceUri The URI of the resource to post to.
    * @param input The input representation to post.
    * @return The response.
    */
   public Response post(String resourceUri, Representation input);

   /**
    * Puts a representation in the identified resource.
    * @param resourceUri The URI of the resource to modify.
    * @param input The input representation to put.
    * @return The response.
    */
   public Response put(String resourceUri, Representation input);
   
   /**
    * Tests the identified resource.
    * @param resourceUri The URI of the resource to delete.
    * @return The response.
    */
   public Response trace(String resourceUri);
}
