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

import java.util.List;

import org.restlet.data.Language;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.util.ReferenceList;

/**
 * Restlet capable of handling calls using a target resource. At this point in the processing, all the 
 * necessary information should be ready in order to find the resource that is the actual target of the 
 * request and to handle the required method on it.<br/> 
 * <br/>
 * It is comparable to an HttpServlet class as it provides facility methods to handle the most common 
 * method names. The calls are then automatically dispatched to the appropriate handle*() method (where 
 * the '*' character corresponds to the method name, or to the handleOthers() method.<br/> 
 * <br/>
 * The handleGet(), handlePost(), handlePut(), handleDelete() and handleHead() have a useful 
 * implementation which is based on the target resource. See each method for details. 
 * <br/>
 * The other handle*() methods simply invoke the defaultHandle() method which simply set the status to 
 * SERVER_ERROR_NOT_IMPLEMENTED). 
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Handler extends Restlet
{
	/** The language to use if content negotiation fails. */
	private Language fallbackLanguage;

	/** Indicates if the best content is automatically negotiated. */
	private boolean negotiateContent;

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
		this.negotiateContent = true;
	}

	/** 
	 * Indicates if the best content is automatically negotiated. Default value is true.
	 * @return True if the best content is automatically negotiated.
	 */
	public boolean isNegotiateContent()
	{
		return this.negotiateContent;
	}

	/** 
	 * Indicates if the best content is automatically negotiated. Default value is true.
	 * @param negotiateContent True if the best content is automatically negotiated.
	 */
	public void setNegotiateContent(boolean negotiateContent)
	{
		this.negotiateContent = negotiateContent;
	}

	/**
	 * Default implementation for all the handle*() methods that simply returns a client error 
	 * indicating that the method is not allowed. 
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	protected void defaultHandle(Request request, Response response)
	{
		response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	/**
	 * Finds the target Resource if available. The default value is null, but this method is intended to
	 * be overriden in subclasses.
	 * @param request The request to handle.
	 * @param response The response to update.
	 * @return The target resource if available or null.
	 */
	public Resource findTarget(Request request, Response response)
	{
		return null;
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
	 * Handles a call.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	public void handle(Request request, Response response)
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
			else if (method.equals(Method.COPY))
			{
				handleCopy(request, response);
			}
			else if (method.equals(Method.MOVE))
			{
				handleMove(request, response);
			}
			else
			{
				handleOthers(request, response);
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
		defaultHandle(request, response);
	}

	/**
	 * Handles a COPY call.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	protected void handleCopy(Request request, Response response)
	{
		defaultHandle(request, response);
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
			if (target.allowDelete())
			{
				response.setStatus(target.delete().getStatus());
			}
			else
			{
				response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
			}
		}
		else
		{
			defaultHandle(request, response);
		}
	}

	/**
	 * Handles a GET call by automatically returning the best entity available from the target resource (as provided
	 * by the 'findTarget' method). The content negotiation is based on the client's preferences available in the 
	 * handled call and can be turned off using the "negotiateContent" property. If it is disabled and multiple 
	 * variants are available for the target resource, then a 300 (Multiple Choices) status will be 
	 * returned with the list of variants URI if available.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	protected void handleGet(Request request, Response response)
	{
		Resource target = findTarget(request, response);

		if (target != null)
		{
			if (target.allowGet())
			{
				if (isNegotiateContent())
				{
					response.setEntity(target, getFallbackLanguage());
				}
				else
				{
					List<Representation> variants = target.getVariants();
					if (variants.size() == 0)
					{
						response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
					}
					else if (variants.size() == 1)
					{
						response.setEntity(variants.get(0));
					}
					else
					{
						ReferenceList variantRefs = new ReferenceList();

						for (Representation variant : variants)
						{
							if (variant.getIdentifier() != null)
							{
								variantRefs.add(variant.getIdentifier());
							}
							else
							{
								getLogger()
										.warning(
												"A resource with multiple variants should provide and identifier for each variants when content negotiation is turned off");
							}
						}

						if (variantRefs.size() > 0)
						{
							// Return the list of variants
							response.setStatus(Status.REDIRECTION_MULTIPLE_CHOICES);
							response.setEntity(variantRefs.getRepresentation());
						}
						else
						{
							response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
						}
					}
				}
			}
			else
			{
				response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
			}
		}
		else
		{
			defaultHandle(request, response);
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
	 * Handles a MOVE call.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	protected void handleMove(Request request, Response response)
	{
		defaultHandle(request, response);
	}

	/**
	 * Handles a OPTIONS call.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	protected void handleOptions(Request request, Response response)
	{
		defaultHandle(request, response);
	}

	/**
	 * Handles a call with a method that is not directly supported by a special handle*() method.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	protected void handleOthers(Request request, Response response)
	{
		defaultHandle(request, response);
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
			if (target.allowPost())
			{
				if (request.isEntityAvailable())
				{
					response.setStatus(target.post(request.getEntity()).getStatus());
				}
				else
				{
					response.setStatus(new Status(Status.CLIENT_ERROR_NOT_ACCEPTABLE,
							"Missing request entity"));
				}
			}
			else
			{
				response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
			}
		}
		else
		{
			defaultHandle(request, response);
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
			if (target.allowPut())
			{
				if (request.isEntityAvailable())
				{
					response.setStatus(target.put(request.getEntity()).getStatus());
				}
				else
				{
					response.setStatus(new Status(Status.CLIENT_ERROR_NOT_ACCEPTABLE,
							"Missing request entity"));
				}
			}
			else
			{
				response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
			}
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
		defaultHandle(request, response);
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
