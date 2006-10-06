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

/**
 * Uniform REST interface. "The central feature that distinguishes
 * the REST architectural style from other network-based styles is its emphasis on a uniform interface between
 * components. By applying the software engineering principle of generality to the component interface, the
 * overall system architecture is simplified and the visibility of interactions is improved. Implementations
 * are decoupled from the services they provide, which encourages independent evolvability." Roy T. Fielding
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm#sec_5_1_5">Source
 * dissertation</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public interface UniformInterface
{
   /**
    * Handles a call.
    * @param request The request to handle.
    * @param response The response to update.
    */
	public void handle(Request request, Response response);
}
