/*
 * Copyright 2005-2006 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet;

import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;

/**
 * Base class exposing a uniform REST interface.<br/> <br/> "The central
 * feature that distinguishes the REST architectural style from other
 * network-based styles is its emphasis on a uniform interface between
 * components. By applying the software engineering principle of generality to
 * the component interface, the overall system architecture is simplified and
 * the visibility of interactions is improved. Implementations are decoupled
 * from the services they provide, which encourages independent evolvability."
 * Roy T. Fielding<br/> <br/> It has many subclasses that focus on a specific
 * ways to handle calls like filtering, routing or finding a target resource.
 * The context property is typically provided by a parent component as a way to
 * give access to features such as logging and client connectors.
 * 
 * @see <a
 *      href="http://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm#sec_5_1_5">Source
 *      dissertation</a>
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
     * Deletes the identified resource.
     * 
     * @param resourceRef
     *            The reference of the resource to delete.
     * @return The response.
     */
    public Response delete(Reference resourceRef) {
        return handle(new Request(Method.DELETE, resourceRef));
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
     * Gets the identified resource.
     * 
     * @param resourceRef
     *            The reference of the resource to get.
     * @return The response.
     */
    public Response get(Reference resourceRef) {
        return handle(new Request(Method.GET, resourceRef));
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
     * Gets the identified resource without its representation's content.
     * 
     * @param resourceRef
     *            The reference of the resource to get.
     * @return The response.
     */
    public Response head(Reference resourceRef) {
        return handle(new Request(Method.HEAD, resourceRef));
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
     * Gets the options for the identified resource.
     * 
     * @param resourceRef
     *            The reference of the resource to get.
     * @return The response.
     */
    public Response options(Reference resourceRef) {
        return handle(new Request(Method.OPTIONS, resourceRef));
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
     * Posts a representation to the identified resource.
     * 
     * @param resourceRef
     *            The reference of the resource to post to.
     * @param entity
     *            The entity to post.
     * @return The response.
     */
    public Response post(Reference resourceRef, Representation entity) {
        return handle(new Request(Method.POST, resourceRef, entity));
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
     * Puts a representation in the identified resource.
     * 
     * @param resourceRef
     *            The reference of the resource to modify.
     * @param entity
     *            The entity to put.
     * @return The response.
     */
    public Response put(Reference resourceRef, Representation entity) {
        return handle(new Request(Method.PUT, resourceRef, entity));
    }

}
