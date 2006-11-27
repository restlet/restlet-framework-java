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

package org.restlet.util;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Delegate used by API classes to get support from the implementation classes.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public interface Helper {
	/**
	 * Creates a new context.
	 * 
	 * @return The new context.
	 */
	public Context createContext();

	/**
	 * Handles a call.
	 * 
	 * @param request
	 *            The request to handle.
	 * @param response
	 *            The response to update.
	 */
	public void handle(Request request, Response response);

	/** Start callback. */
	public void start() throws Exception;

	/** Stop callback. */
	public void stop() throws Exception;
}
