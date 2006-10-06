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

import java.util.logging.Level;

import org.restlet.data.Method;
import org.restlet.data.Status;

/**
 * Generic call handler with a context. It has many subclasses that focus on a specific ways to handle 
 * calls like filtering, routing or finding the target resource. The context of a Restlet is typically 
 * provided by a parent container as a way to give access to features such as logging and client connectors.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Restlet implements UniformInterface
{
   /** Error message. */
   private static final String UNABLE_TO_START = "Unable to start the target Restlet";
   
   /** The context. */
	private Context context;

   /** Indicates if the restlet was started. */
   private boolean started;

   /**
    * Constructor.
    */
   public Restlet()
   {
   	this(null);
   }

   /**
    * Constructor.
    * @param context The context.
    */
   public Restlet(Context context)
   {
   	this.context = context;
      this.started = false;
   }

   /**
    * Returns the context.
    * @return The context.
    */
   public Context getContext()
   {
      return this.context;
   }

   /**
    * Sets the context.
    * @param context The context.
    */
   public void setContext(Context context)
   {
      this.context = context;
   }

   /**
    * Handles a call.
    * @param request The request to handle.
    * @param response The response to update.
    */
	public void handle(Request request, Response response)
   {
		// Check if the Restlet was started
		if(isStopped())
		{
			try
			{
				start();
			}
			catch (Exception e)
			{
				getContext().getLogger().log(Level.WARNING, UNABLE_TO_START, e);
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
			}
		}
		
		if(isStarted())
		{
	   	Method method = request.getMethod();
	   	
	   	if(method == null)
	   	{
	   		handleOthers(request, response);
	   	}
	   	else if(method.equals(Method.GET))
			{
				handleGet(request, response);
			}
			else if(method.equals(Method.POST))
			{
				handlePost(request, response);
			}
			else if(method.equals(Method.PUT))
			{
				handlePut(request, response);
			}
			else if(method.equals(Method.DELETE))
			{
				handleDelete(request, response);
			}
			else if(method.equals(Method.HEAD))
			{
				handleHead(request, response);
			}
			else if(method.equals(Method.CONNECT))
			{
				handleConnect(request, response);
			}
			else if(method.equals(Method.OPTIONS))
			{
				handleOptions(request, response);
			}
			else if(method.equals(Method.TRACE))
			{
				handleTrace(request, response);
			}
			else
			{
				handleOthers(request, response);
			}
		}
		else
		{
			getContext().getLogger().log(Level.WARNING, UNABLE_TO_START);
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
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
    * Handles a DELETE call.
    * @param request The request to handle.
    * @param response The response to update.
    */
   protected void handleDelete(Request request, Response response)
   {
   	defaultHandle(request, response);
   }

   /**
    * Handles a GET call.
    * @param request The request to handle.
    * @param response The response to update.
    */
   protected void handleGet(Request request, Response response)
   {
   	defaultHandle(request, response);
   }

   /**
    * Handles a HEAD call.
    * @param request The request to handle.
    * @param response The response to update.
    */
   protected void handleHead(Request request, Response response)
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
    * Handles a POST call.
    * @param request The request to handle.
    * @param response The response to update.
    */
   protected void handlePost(Request request, Response response)
   {
   	defaultHandle(request, response);
   }

   /**
    * Handles a PUT call.
    * @param request The request to handle.
    * @param response The response to update.
    */
   protected void handlePut(Request request, Response response)
   {
   	defaultHandle(request, response);
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
    * Handles a call with a method that is not directly supported by a special handle*() method.
    * @param request The request to handle.
    * @param response The response to update.
    */
   protected void handleOthers(Request request, Response response)
   {
   	defaultHandle(request, response);
   }
   
   /**
    * Default implementation for all the handle*() methods that simply returns a client error 
    * indicating that the method is not allowed. 
    * @param request The request to handle.
    * @param response The response to update.
    */
   protected void defaultHandle(Request request, Response response)
   {
		response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
   }
   
   /** Starts the Restlet. */
   public void start() throws Exception
   {
      this.started = true;
   }

   /** Stops the Restlet. */
   public void stop() throws Exception
   {
      this.started = false;
   }

   /**
    * Indicates if the Restlet is started.
    * @return True if the Restlet is started.
    */
   public boolean isStarted()
   {
      return this.started;
   }

   /**
    * Indicates if the Restlet is stopped.
    * @return True if the Restlet is stopped.
    */
   public boolean isStopped()
   {
      return !this.started;
   }
}
