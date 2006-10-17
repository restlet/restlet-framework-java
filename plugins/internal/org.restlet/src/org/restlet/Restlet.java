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

import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 * Handler comparable to an HttpServlet class. It provides facility methods to handle the most common method
 * names. The calls are then automatically dispatched to the appropriate handle*() method (where the '*'
 * character corresponds to the method name, or to the handleOthers() method. By default, the implementation
 * of the handle*() or handleOthers() methods is to invoke the defaultHandle() method which should be 
 * overriden to change the default behavior (set the status to SERVER_ERROR_NOT_IMPLEMENTED).
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Restlet extends Handler
{
	/**
	 * Constructor.
	 */
	public Restlet()
	{
		this(new Context());
	}

	/**
	 * Constructor.
	 * @param context The context.
	 */
	public Restlet(Context context)
	{
		super(context);
	}

	/**
	 * Wrapper constructor.
	 * @param wrappedRestlet The wrapped Restlet.
	 */
	public Restlet(Restlet wrappedRestlet)
	{
		super(wrappedRestlet);
	}

	/**
	 * Returns the wrapped Restlet.
	 * @return The wrapped Restlet.
	 */
	private Restlet getWrappedRestlet()
	{
		return (Restlet)getWrappedHandler();
	}

	/**
	 * Default implementation for all the handle*() methods that simply returns a client error 
	 * indicating that the method is not allowed. 
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	protected void defaultHandle(Request request, Response response)
	{
		if(getWrappedRestlet() != null)
		{
			getWrappedRestlet().defaultHandle(request, response);
		}
		else
		{
			response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
		}
	}

	/**
	 * Handles a call.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	public void handle(Request request, Response response)
	{
		if(getWrappedRestlet() != null)
		{
			getWrappedRestlet().handle(request, response);
		}
		else
		{
	  		init(request, response);
	
			if (isStarted())
			{
				Method method = request.getMethod();
	
				if (method == null)
				{
					handleOthers(request, response);
				}
				else if (method.equals(Method.GET))
				{
					handleGet(request, response);
				}
				else if (method.equals(Method.POST))
				{
					handlePost(request, response);
				}
				else if (method.equals(Method.PUT))
				{
					handlePut(request, response);
				}
				else if (method.equals(Method.DELETE))
				{
					handleDelete(request, response);
				}
				else if (method.equals(Method.HEAD))
				{
					handleHead(request, response);
				}
				else if (method.equals(Method.CONNECT))
				{
					handleConnect(request, response);
				}
				else if (method.equals(Method.OPTIONS))
				{
					handleOptions(request, response);
				}
				else if (method.equals(Method.TRACE))
				{
					handleTrace(request, response);
				}
				else
				{
					handleOthers(request, response);
				}
			}
		}
	}

	/**
	 * Handles a CONNECT call.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	protected void handleConnect(Request request, Response response)
	{
		if(getWrappedRestlet() != null)
		{
			getWrappedRestlet().defaultHandle(request, response);
		}
		else
		{
			defaultHandle(request, response);
		}
	}

	/**
	 * Handles a DELETE call.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	protected void handleDelete(Request request, Response response)
	{
		if(getWrappedRestlet() != null)
		{
			getWrappedRestlet().defaultHandle(request, response);
		}
		else
		{
			defaultHandle(request, response);
		}
	}

	/**
	 * Handles a GET call.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	protected void handleGet(Request request, Response response)
	{
		if(getWrappedRestlet() != null)
		{
			getWrappedRestlet().defaultHandle(request, response);
		}
		else
		{
			defaultHandle(request, response);
		}
	}

	/**
	 * Handles a HEAD call.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	protected void handleHead(Request request, Response response)
	{
		if(getWrappedRestlet() != null)
		{
			getWrappedRestlet().defaultHandle(request, response);
		}
		else
		{
			defaultHandle(request, response);
		}
	}

	/**
	 * Handles a OPTIONS call.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	protected void handleOptions(Request request, Response response)
	{
		if(getWrappedRestlet() != null)
		{
			getWrappedRestlet().defaultHandle(request, response);
		}
		else
		{
			defaultHandle(request, response);
		}
	}

	/**
	 * Handles a call with a method that is not directly supported by a special handle*() method.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	protected void handleOthers(Request request, Response response)
	{
		if(getWrappedRestlet() != null)
		{
			getWrappedRestlet().defaultHandle(request, response);
		}
		else
		{
			defaultHandle(request, response);
		}
	}

	/**
	 * Handles a POST call.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	protected void handlePost(Request request, Response response)
	{
		if(getWrappedRestlet() != null)
		{
			getWrappedRestlet().defaultHandle(request, response);
		}
		else
		{
			defaultHandle(request, response);
		}
	}

	/**
	 * Handles a PUT call.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	protected void handlePut(Request request, Response response)
	{
		if(getWrappedRestlet() != null)
		{
			getWrappedRestlet().defaultHandle(request, response);
		}
		else
		{
			defaultHandle(request, response);
		}
	}

	/**
	 * Handles a TRACE call.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	protected void handleTrace(Request request, Response response)
	{
		if(getWrappedRestlet() != null)
		{
			getWrappedRestlet().defaultHandle(request, response);
		}
		else
		{
			defaultHandle(request, response);
		}
	}
}
