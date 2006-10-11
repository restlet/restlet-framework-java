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
import org.restlet.data.Method;
import org.restlet.data.Status;

/**
 * Restlet capable of finding a target Resource. It should have all the necessary 
 * information in order to find the resource that is the actual target of the call and to handle
 * the required method on it. 
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class Finder extends Restlet
{
	/** The language to use if content negotiation fails. */
	private Language fallbackLanguage;

	/**
	 * Constructor.
	 */
	public Finder()
	{
		this(null);
	}

	/**
	 * Constructor.
	 * @param context The context.
	 */
	public Finder(Context context)
	{
		super(context);
	}

	/**
	 * Finds the target Resource if available.
    * @param request The request to handle.
    * @param response The response to update.
	 * @return The target resource if available or null.
	 */
	public abstract Resource findTarget(Request request, Response response);

	/**
	 * Handles a GET call by automatically returning the best output available from the target resource (as provided
	 * by the 'findTarget' method). The content negotiation is based on the client's preferences available in the 
	 * handled call.
    * @param request The request to handle.
    * @param response The response to update.
	 */
	protected void handleGet(Request request, Response response)
	{
		Resource target = findTarget(request, response);

		if (target != null)
		{
			if(target.allowGet())
			{
				response.setOutput(target, getFallbackLanguage());
			}
			else
			{
				response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
			}

			setAllowedMethods(target, response);
		}
		else
		{
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}
	}
	
	/**
	 * Handles a HEAD call, using a logic similat to the handleGet method.
    * @param request The request to handle.
    * @param response The response to update.
	 */
	protected void handleHead(Request request, Response response)
	{
		handleGet(request, response);
	}

	/**
	 * Handles a DELETE call invoking the 'delete' method of the target resource (as provided by the 'findTarget' 
	 * method).
    * @param request The request to handle.
    * @param response The response to update.
	 */
	protected void handleDelete(Request request, Response response)
	{
		Resource target = findTarget(request, response);

		if (target != null)
		{
			if(target.allowDelete())
			{
				response.setStatus(target.delete().getStatus());
			}
			else
			{
				response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
			}

			setAllowedMethods(target, response);
		}
		else
		{
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}
	}

	/**
	 * Handles a POST call invoking the 'post' method of the target resource (as provided by the 'findTarget' method).
    * @param request The request to handle.
    * @param response The response to update.
	 */
	protected void handlePost(Request request, Response response)
	{
		Resource target = findTarget(request, response);

		if (target != null)
		{
			if(target.allowPost())
			{
				if (request.isInputAvailable())
				{
					response.setStatus(target.post(request.getInput()).getStatus());
				}
				else
				{
					response.setStatus(new Status(Status.CLIENT_ERROR_NOT_ACCEPTABLE,
					"Missing input representation"));
				}
			}
			else
			{
				response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
			}

			setAllowedMethods(target, response);
		}
		else
		{
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}
	}

	/**
	 * Handles a PUT call invoking the 'put' method of the target resource (as provided by the 'findTarget' method).
    * @param request The request to handle.
    * @param response The response to update.
	 */
	protected void handlePut(Request request, Response response)
	{
		Resource target = findTarget(request, response);

		if (target != null)
		{
			if(target.allowPut())
			{
				if (request.isInputAvailable())
				{
					response.setStatus(target.put(request.getInput()).getStatus());
				}
				else
				{
					response.setStatus(new Status(Status.CLIENT_ERROR_NOT_ACCEPTABLE,
							"Missing input representation"));
				}
			}
			else
			{
				response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
			}

			setAllowedMethods(target, response);
		}
		else
		{
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
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
	 * Sets the list of allowed methods on a resource to a response. 
	 * @param resource The resource to introspect.
	 * @param response The response to update.
	 */
	protected void setAllowedMethods(Resource resource, Response response)
	{
		// Clear the current set of allowed methods
		response.getAllowedMethods().clear();
		
		// Introspect the resource for allowed methods
		if(resource.allowGet()) 
		{
			response.getAllowedMethods().add(Method.HEAD);
			response.getAllowedMethods().add(Method.GET);
		}
		if(resource.allowDelete()) response.getAllowedMethods().add(Method.DELETE);
		if(resource.allowPost()) response.getAllowedMethods().add(Method.POST);
		if(resource.allowPut()) response.getAllowedMethods().add(Method.PUT);
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
