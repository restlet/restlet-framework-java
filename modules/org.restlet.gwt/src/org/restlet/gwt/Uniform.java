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

package org.restlet.gwt;

import org.restlet.gwt.data.Method;
import org.restlet.gwt.data.Reference;
import org.restlet.gwt.data.Request;
import org.restlet.gwt.data.Response;
import org.restlet.gwt.resource.Representation;
import org.restlet.gwt.resource.StringRepresentation;

/**
 * Base class exposing the uniform REST interface. "The central feature that
 * distinguishes the REST architectural style from other network-based styles is
 * its emphasis on a uniform interface between components. By applying the
 * software engineering principle of generality to the component interface, the
 * overall system architecture is simplified and the visibility of interactions
 * is improved. Implementations are decoupled from the services they provide,
 * which encourages independent evolvability." Roy T. Fielding
 * 
 * @see <a
 *      href="http://roy.gbiv.com/pubs/dissertation/rest_arch_style.htm#sec_5_1_5"
 *      >Source dissertation</a>
 * @author Jerome Louvel
 */
public abstract class Uniform {
    /**
     * Deletes the identified resource.
     * 
     * @param resourceRef
     *            The reference of the resource to delete.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void delete(Reference resourceRef, Callback callback) {
        handle(new Request(Method.DELETE, resourceRef), callback);
    }

    /**
     * Deletes the identified resource.
     * 
     * @param resourceUri
     *            The URI of the resource to delete.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void delete(String resourceUri, Callback callback) {
        handle(new Request(Method.DELETE, resourceUri), callback);
    }

    /**
     * Gets the identified resource.
     * 
     * @param resourceRef
     *            The reference of the resource to get.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void get(Reference resourceRef, Callback callback) {
        handle(new Request(Method.GET, resourceRef), callback);
    }

    /**
     * Gets the identified resource.
     * 
     * @param resourceUri
     *            The URI of the resource to get.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void get(String resourceUri, Callback callback) {
        handle(new Request(Method.GET, resourceUri), callback);
    }

    /**
     * Handles a call.
     * 
     * @param request
     *            The request to handle.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void handle(Request request, Callback callback) {
        final Response response = new Response(request);
        handle(request, response, callback);
    }

    /**
     * Handles a call.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public abstract void handle(Request request, Response response,
            Callback callback);

    /**
     * Gets the identified resource without its representation's content.
     * 
     * @param resourceRef
     *            The reference of the resource to get.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void head(Reference resourceRef, Callback callback) {
        handle(new Request(Method.HEAD, resourceRef), callback);
    }

    /**
     * Gets the identified resource without its representation's content.
     * 
     * @param resourceUri
     *            The URI of the resource to get.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void head(String resourceUri, Callback callback) {
        handle(new Request(Method.HEAD, resourceUri), callback);
    }

    /**
     * Gets the options for the identified resource.
     * 
     * @param resourceRef
     *            The reference of the resource to get.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void options(Reference resourceRef, Callback callback) {
        handle(new Request(Method.OPTIONS, resourceRef), callback);
    }

    /**
     * Gets the options for the identified resource.
     * 
     * @param resourceUri
     *            The URI of the resource to get.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void options(String resourceUri, Callback callback) {
        handle(new Request(Method.OPTIONS, resourceUri), callback);
    }

    /**
     * Posts a representation to the identified resource.
     * 
     * @param resourceRef
     *            The reference of the resource to post to.
     * @param entity
     *            The entity to post.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void post(Reference resourceRef, Representation entity,
            Callback callback) {
        handle(new Request(Method.POST, resourceRef, entity), callback);
    }

    /**
     * Posts a representation to the identified resource.
     * 
     * @param resourceUri
     *            The URI of the resource to post to.
     * @param entity
     *            The entity to post.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void post(String resourceUri, Representation entity,
            Callback callback) {
        handle(new Request(Method.POST, resourceUri, entity), callback);
    }

    /**
     * Posts a representation to the identified resource.
     * 
     * @param resourceUri
     *            The URI of the resource to modify.
     * @param entity
     *            The entity to post.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void post(String resourceUri, String entity, Callback callback) {
        post(resourceUri, new StringRepresentation(entity), callback);
    }

    /**
     * Puts a representation in the identified resource.
     * 
     * @param resourceRef
     *            The reference of the resource to modify.
     * @param entity
     *            The entity to put.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void put(Reference resourceRef, Representation entity,
            Callback callback) {
        handle(new Request(Method.PUT, resourceRef, entity), callback);
    }

    /**
     * Puts a representation in the identified resource.
     * 
     * @param resourceUri
     *            The URI of the resource to modify.
     * @param entity
     *            The entity to put.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void put(String resourceUri, Representation entity,
            Callback callback) {
        handle(new Request(Method.PUT, resourceUri, entity), callback);
    }

    /**
     * Puts a representation in the identified resource.
     * 
     * @param resourceUri
     *            The URI of the resource to modify.
     * @param entity
     *            The entity to put.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public final void put(String resourceUri, String entity, Callback callback) {
        put(resourceUri, new StringRepresentation(entity), callback);
    }

}
