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
import org.restlet.data.Status;

/**
 * Generic call processor. "The central feature that distinguishes
 * the REST architectural style from other network-based styles is its emphasis on a uniform interface between
 * components. By applying the software engineering principle of generality to the component interface, the
 * overall system architecture is simplified and the visibility of interactions is improved. Implementations
 * are decoupled from the services they provide, which encourages independent evolvability." Roy T. Fielding
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm#sec_5_1_5">Source
 * dissertation</a>
 * @see <a href="http://www.restlet.org/tutorial#part03">Tutorial: Listening to Web browsers</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Restlet implements UniformInterface
{
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
    * @param call The call to handle.
    */
	public void handle(Call call)
   {
   	Method method = call.getMethod();
   	
   	if(method == null)
   	{
   		handleOthers(call);
   	}
   	else if(method.equals(Method.GET))
		{
			handleGet(call);
		}
		else if(method.equals(Method.POST))
		{
			handlePost(call);
		}
		else if(method.equals(Method.PUT))
		{
			handlePut(call);
		}
		else if(method.equals(Method.DELETE))
		{
			handleDelete(call);
		}
		else if(method.equals(Method.HEAD))
		{
			handleHead(call);
		}
		else if(method.equals(Method.CONNECT))
		{
			handleConnect(call);
		}
		else if(method.equals(Method.OPTIONS))
		{
			handleOptions(call);
		}
		else if(method.equals(Method.TRACE))
		{
			handleTrace(call);
		}
		else
		{
			handleOthers(call);
		}
   }

   /**
    * Handles a CONNECT call.
    * @param call The call to handle.
    */
   protected void handleConnect(Call call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a DELETE call.
    * @param call The call to handle.
    */
   protected void handleDelete(Call call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a GET call.
    * @param call The call to handle.
    */
   protected void handleGet(Call call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a HEAD call.
    * @param call The call to handle.
    */
   protected void handleHead(Call call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a OPTIONS call.
    * @param call The call to handle.
    */
   protected void handleOptions(Call call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a POST call.
    * @param call The call to handle.
    */
   protected void handlePost(Call call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a PUT call.
    * @param call The call to handle.
    */
   protected void handlePut(Call call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a TRACE call.
    * @param call The call to handle.
    */
   protected void handleTrace(Call call)
   {
   	defaultHandle(call);
   }
   
   /**
    * Handles a call with a method that is not directly supported by a special handle*() method.
    * @param call The call to handle.
    */
   protected void handleOthers(Call call)
   {
   	defaultHandle(call);
   }
   
   /**
    * Default implementation for all the handle*() methods that simply returns a client error 
    * indicating that the method is not allowed. 
    * @param call The call to handle.
    */
   protected void defaultHandle(Call call)
   {
		call.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
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
