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

package com.noelios.restlet;

import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.util.Dispatcher;

/**
 * Default call dispatcher.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class DefaultDispatcher extends Dispatcher
{
	/**
	 * Handles a call.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	public void handle(Request request, Response response)
	{
		Protocol protocol = request.getProtocol();
		if (protocol == null)
		{
			// Attempt to guess the protocol to use
			// from the target reference scheme
			protocol = request.getResourceRef().getSchemeProtocol();
		}

		if (protocol == null)
		{
			throw new UnsupportedOperationException(
					"Unable to determine the protocol to use for this call.");
		}
		else
		{
			handle(protocol, request, response);
		}
	}

	/**
	 * Handles a call.
	 * @param protocol The protocol to use for the handling.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	public abstract void handle(Protocol protocol, Request request, Response response);
}
