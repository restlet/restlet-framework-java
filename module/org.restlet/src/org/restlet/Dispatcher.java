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

import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;

/**
 * Dispatcher of calls from a Restlet context to a set of connectors.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class Dispatcher {
	/**
	 * Deletes the identified resource.
	 * 
	 * @param resourceUri
	 *            The URI of the resource to delete.
	 * @return The response.
	 */
	public Response delete(String resourceUri) {
		return handle(new Request(Method.DELETE, resourceUri));
	}

	/**
	 * Gets the identified resource.
	 * 
	 * @param resourceUri
	 *            The URI of the resource to get.
	 * @return The response.
	 */
	public Response get(String resourceUri) {
		return handle(new Request(Method.GET, resourceUri));
	}

	/**
	 * Handles a call.
	 * 
	 * @param request
	 *            The request to handle.
	 * @return The returned response.
	 */
	public Response handle(Request request) {
		Response response = new Response(request);
		handle(request, response);
		return response;
	}

	/**
	 * Handles a call.
	 * 
	 * @param request
	 *            The request to handle.
	 * @param response
	 *            The response to update.
	 */
	public abstract void handle(Request request, Response response);

	/**
	 * Gets the identified resource without its representation's content.
	 * 
	 * @param resourceUri
	 *            The URI of the resource to get.
	 * @return The response.
	 */
	public Response head(String resourceUri) {
		return handle(new Request(Method.HEAD, resourceUri));
	}

	/**
	 * Gets the options for the identified resource.
	 * 
	 * @param resourceUri
	 *            The URI of the resource to get.
	 * @return The response.
	 */
	public Response options(String resourceUri) {
		return handle(new Request(Method.OPTIONS, resourceUri));
	}

	/**
	 * Posts a representation to the identified resource.
	 * 
	 * @param resourceUri
	 *            The URI of the resource to post to.
	 * @param entity
	 *            The entity to post.
	 * @return The response.
	 */
	public Response post(String resourceUri, Representation entity) {
		return handle(new Request(Method.POST, resourceUri, entity));
	}

	/**
	 * Puts a representation in the identified resource.
	 * 
	 * @param resourceUri
	 *            The URI of the resource to modify.
	 * @param entity
	 *            The entity to put.
	 * @return The response.
	 */
	public Response put(String resourceUri, Representation entity) {
		return handle(new Request(Method.PUT, resourceUri, entity));
	}

	/**
	 * Tests the identified resource.
	 * 
	 * @param resourceUri
	 *            The URI of the resource to delete.
	 * @return The response.
	 */
	public Response trace(String resourceUri) {
		return handle(new Request(Method.TRACE, resourceUri));
	}

}
