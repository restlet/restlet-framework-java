/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
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
 * @see <a href=
 *      "http://roy.gbiv.com/pubs/dissertation/rest_arch_style.htm#sec_5_1_5">Source
 *      dissertation</a>
 * @author Jerome Louvel
 */
public interface Uniform {

	/**
	 * Handles a uniform call. It is important to realize that this interface can be
	 * used either on the client-side or on the server-side.
	 * 
	 * @param request  The request to handle.
	 * @param response The associated response.
	 */
	void handle(Request request, Response response);
}
