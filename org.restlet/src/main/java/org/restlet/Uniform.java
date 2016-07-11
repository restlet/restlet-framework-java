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

package org.restlet;

/**
 * Uniform REST interface. "The central feature that distinguishes the REST
 * architectural style from other network-based styles is its emphasis on a
 * uniform interface between components. By applying the software engineering
 * principle of generality to the component interface, the overall system
 * architecture is simplified and the visibility of interactions is improved.
 * Implementations are decoupled from the services they provide, which
 * encourages independent evolvability." Roy T. Fielding
 * 
 * @see <a
 *      href="http://roy.gbiv.com/pubs/dissertation/rest_arch_style.htm#sec_5_1_5">Source
 *      dissertation</a>
 * @author Jerome Louvel
 */
public interface Uniform {

    /**
     * Handles a uniform call. It is important to realize that this interface
     * can be used either on the client-side or on the server-side.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The associated response.
     */
    void handle(Request request, Response response);
}
