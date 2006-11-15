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

import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 * Restlet part of a processing chain. In addition to handling incoming calls like any Restlet, a Chainer
 * can also resolve, either statically or dynamically, the next Restlet that will continue the processing 
 * chain. Subclasses only have to implement the getNext() method.
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class Chainer extends Restlet
{
	/**
	 * Constructor.
	 */
	public Chainer()
	{
		this(null);
	}

	/**
	 * Constructor.
	 * @param context The context.
	 */
	public Chainer(Context context)
	{
		super(context);
	}

	/**
	 * Handles a call by invoking the next Restlet if it is available.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	public void handle(Request request, Response response)
	{
		init(request, response);

		Restlet next = getNext(request, response);
		if (next != null)
		{
			next.handle(request, response);
		}
		else
		{
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}
	}

	/**
	 * Returns the next Restlet if available.
	 * @param request The request to handle.
	 * @param response The response to update.
	 * @return The next Restlet if available or null.
	 */
	public abstract Restlet getNext(Request request, Response response);

}
