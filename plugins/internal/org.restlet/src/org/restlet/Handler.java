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

import org.restlet.data.Language;
import org.restlet.data.Resource;
import org.restlet.data.Status;

/**
 * Restlet capable of finding a target Resource. It should have all the necessary 
 * information in order to find the resource that is the actual target of the call and to handle
 * the required method on it. 
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Handler extends Restlet
{
	/** The language to use if content negotiation fails. */
	private Language fallbackLanguage;

	/**
	 * Constructor.
	 */
	public Handler()
	{
		this(null);
	}

	/**
	 * Constructor.
	 * @param context The context.
	 */
	public Handler(Context context)
	{
		super(context);
	}

	/**
	 * Finds the target Resource if available.
	 * @param call The current call.
	 * @return The target resource if available or null.
	 */
	public Resource findTarget(Call call)
	{
		return null;
	}

	/**
	 * Handles a GET call by automatically returning the best output available from the target resource (as provided
	 * by the 'findTarget' method). The content negotiation is based on the client's preferences available in the 
	 * handled call.
	 * @param call The call to handle.
	 */
	protected void handleGet(Call call)
	{
		call.setOutput(findTarget(call), getFallbackLanguage());
	}

	/**
	 * Handles a HEAD call, using a logic similat to the handleGet method.
	 * @param call The call to handle.
	 */
	protected void handleHead(Call call)
	{
		handleGet(call);
	}

	/**
	 * Handles a DELETE call invoking the 'delete' method of the target resource (as provided by the 'findTarget' 
	 * method).
	 * @param call The call to handle.
	 */
	protected void handleDelete(Call call)
	{
		Resource target = findTarget(call);

		if (target != null)
		{
			call.setStatus(target.delete());
		}
		else
		{
			call.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
		}
	}

	/**
	 * Handles a PUT call invoking the 'put' method of the target resource (as provided by the 'findTarget' method).
	 * @param call The call to handle.
	 */
	protected void handlePut(Call call)
	{
		Resource target = findTarget(call);

		if (target != null)
		{
			if (call.getInput() != null)
			{
				call.setStatus(target.put(call.getInput()));
			}
			else
			{
				call.setStatus(new Status(Status.CLIENT_ERROR_NOT_ACCEPTABLE,
						"Missing input representation"));
			}
		}
		else
		{
			call.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
		}
	}

	/**
	 * Returns the language to use if content negotiation fails.
	 * @return The language to use if content negotiation fails.
	 */
	public Language getFallbackLanguage()
	{
		return this.fallbackLanguage;
	}

	/**
	 * Sets the language to use if content negotiation fails.
	 * @param fallbackLanguage The language to use if content negotiation fails.
	 */
	public void setFallbackLanguage(Language fallbackLanguage)
	{
		this.fallbackLanguage = fallbackLanguage;
	}

}
