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

package org.restlet;

import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;

/**
 * Base class exposing the uniform REST interface. "The central feature that
 * distinguishes the REST architectural style from other network-based styles is
 * its emphasis on a uniform interface between components. By applying the
 * software engineering principle of generality to the component interface, the
 * overall system architecture is simplified and the visibility of interactions
 * is improved. Implementations are decoupled from the services they provide,
 * which encourages independent evolvability." Roy T. Fielding<br>
 * <br>
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @see <a
 *      href="http://roy.gbiv.com/pubs/dissertation/rest_arch_style.htm#sec_5_1_5">Source
 *      dissertation</a>
 * @author Jerome Louvel
 */
public abstract class Uniform {
    /**
     * Deletes the resource and all its representations at the target URI
     * reference.
     * 
     * @param resourceRef
     *            The reference of the resource to delete.
     * @return The response.
     */
    public final Response delete(Reference resourceRef) {
        return handle(new Request(Method.DELETE, resourceRef));
    }

    /**
     * Deletes the resource and all its representations at the target URI.
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
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.3">HTTP
     *      GET method</a>
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
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.3">HTTP
     *      GET method</a>
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
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.4">HTTP
     *      HEAD method</a>
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
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.4">HTTP
     *      HEAD method</a>
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
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.2">HTTP
     *      OPTIONS method</a>
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
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.2">HTTP
     *      OPTIONS method</a>
     */
    public final Response options(String resourceUri) {
        return handle(new Request(Method.OPTIONS, resourceUri));
    }

    /**
     * Posts a representation to the resource at the target URI reference.
     * 
     * @param resourceRef
     *            The reference of the resource to post to.
     * @param entity
     *            The posted entity.
     * @return The response.
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.5">HTTP
     *      POST method</a>
     */
    public final Response post(Reference resourceRef, Representation entity) {
        return handle(new Request(Method.POST, resourceRef, entity));
    }

    /**
     * Posts a representation to the resource at the target URI.
     * 
     * @param resourceUri
     *            The URI of the resource to post to.
     * @param entity
     *            The entity to post.
     * @return The response.
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.5">HTTP
     *      POST method</a>
     */
    public final Response post(String resourceUri, Representation entity) {
        return handle(new Request(Method.POST, resourceUri, entity));
    }

    /**
     * Creates or updates a resource at the target URI reference with the given
     * representation as new state to be stored.
     * 
     * @param resourceRef
     *            The reference of the resource to modify.
     * @param representation
     *            The representation to store.
     * @return The response.
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.6">HTTP
     *      PUT method</a>
     */
    public final Response put(Reference resourceRef,
            Representation representation) {
        return handle(new Request(Method.PUT, resourceRef, representation));
    }

    /**
     * Creates or updates a resource at the target URI with the given
     * representation as new state to be stored.
     * 
     * @param resourceUri
     *            The URI of the resource to modify.
     * @param representation
     *            The representation to store.
     * @return The response.
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.6">HTTP
     *      PUT method</a>
     */
    public final Response put(String resourceUri, Representation representation) {
        return handle(new Request(Method.PUT, resourceUri, representation));
    }

}
