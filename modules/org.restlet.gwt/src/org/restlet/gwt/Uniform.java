/*
 * Copyright 2005-2008 Noelios Consulting.
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

package org.restlet.gwt;

import org.restlet.gwt.data.Method;
import org.restlet.gwt.data.Reference;
import org.restlet.gwt.data.Request;
import org.restlet.gwt.data.Response;
import org.restlet.gwt.resource.Representation;

/**
 * Base class exposing the uniform REST interface. "The central feature that
 * distinguishes the REST architectural style from other network-based styles is
 * its emphasis on a uniform interface between components. By applying the
 * software engineering principle of generality to the component interface, the
 * overall system architecture is simplified and the visibility of interactions
 * is improved. Implementations are decoupled from the services they provide,
 * which encourages independent evolvability." Roy T. Fielding
 * 
 * @see <a *
 *      href="http://roy.gbiv.com/pubs/dissertation/rest_arch_style.htm#sec_5_1_5"
 *      >Source * dissertation< /a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class Uniform {
    /**
     * Deletes the identified resource.
     * 
     * @param resourceRef
     *            The reference of the resource to delete.
     * @return The response.
     */
    public final Response delete(Reference resourceRef) {
        return handle(new Request(Method.DELETE, resourceRef));
    }

    /**
     * Deletes the identified resource.
     * 
     * @param resourceUri
     *            The URI of the resource to delete.
     * @return The response.
     */
    public final Response delete(String resourceUri) {
        return handle(new Request(Method.DELETE, resourceUri));
    }

    /**
     * Gets the identified resource.
     * 
     * @param resourceRef
     *            The reference of the resource to get.
     * @return The response.
     */
    public final Response get(Reference resourceRef) {
        return handle(new Request(Method.GET, resourceRef));
    }

    /**
     * Gets the identified resource.
     * 
     * @param resourceUri
     *            The URI of the resource to get.
     * @return The response.
     */
    public final Response get(String resourceUri) {
        return handle(new Request(Method.GET, resourceUri));
    }

    /**
     * Handles a call.
     * 
     * @param request
     *            The request to handle.
     * @return The returned response.
     */
    public final Response handle(Request request) {
        final Response response = new Response(request);
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
     * @param resourceRef
     *            The reference of the resource to get.
     * @return The response.
     */
    public final Response head(Reference resourceRef) {
        return handle(new Request(Method.HEAD, resourceRef));
    }

    /**
     * Gets the identified resource without its representation's content.
     * 
     * @param resourceUri
     *            The URI of the resource to get.
     * @return The response.
     */
    public final Response head(String resourceUri) {
        return handle(new Request(Method.HEAD, resourceUri));
    }

    /**
     * Gets the options for the identified resource.
     * 
     * @param resourceRef
     *            The reference of the resource to get.
     * @return The response.
     */
    public final Response options(Reference resourceRef) {
        return handle(new Request(Method.OPTIONS, resourceRef));
    }

    /**
     * Gets the options for the identified resource.
     * 
     * @param resourceUri
     *            The URI of the resource to get.
     * @return The response.
     */
    public final Response options(String resourceUri) {
        return handle(new Request(Method.OPTIONS, resourceUri));
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
    public final Response post(Reference resourceRef, Representation entity) {
        return handle(new Request(Method.POST, resourceRef, entity));
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
    public final Response post(String resourceUri, Representation entity) {
        return handle(new Request(Method.POST, resourceUri, entity));
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
    public final Response put(Reference resourceRef, Representation entity) {
        return handle(new Request(Method.PUT, resourceRef, entity));
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
    public final Response put(String resourceUri, Representation entity) {
        return handle(new Request(Method.PUT, resourceUri, entity));
    }

}
