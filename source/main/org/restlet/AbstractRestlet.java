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
import java.util.logging.Logger;

import org.restlet.component.Component;
import org.restlet.data.DefaultStatus;
import org.restlet.data.Method;
import org.restlet.data.Methods;
import org.restlet.data.Statuses;

/**
 * Abstract Restlet that can easily be subclassed. Concrete classes must only implement the handle(Call)
 * method. Another option is to derive the DefaultRestlet to override only the supported handle methods. 
 * The start and stop state is managed by default but with no other action. Override the start and stop 
 * methods if needed.
 * @see <a href="http://www.restlet.org/tutorial#part03">Tutorial: Listening to Web browsers</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class AbstractRestlet implements Restlet
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger(AbstractRestlet.class.getCanonicalName());

   /** Indicates if the restlet was started. */
   protected boolean started;

   /** The owner component. */
   protected Component owner;

   /**
    * Constructor.
    */
   public AbstractRestlet()
   {
      this(null);
   }

   /**
    * Constructor.
    * @param owner The owner component.
    */
   public AbstractRestlet(Component owner)
   {
   	this.owner = owner;
      this.started = false;
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
   	else if(method.equals(Methods.GET))
		{
			handleGet(call);
		}
		else if(method.equals(Methods.POST))
		{
			handlePost(call);
		}
		else if(method.equals(Methods.PUT))
		{
			handlePut(call);
		}
		else if(method.equals(Methods.DELETE))
		{
			handleDelete(call);
		}
		else if(method.equals(Methods.HEAD))
		{
			handleHead(call);
		}
		else if(method.equals(Methods.CONNECT))
		{
			handleConnect(call);
		}
		else if(method.equals(Methods.OPTIONS))
		{
			handleOptions(call);
		}
		else if(method.equals(Methods.TRACE))
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
		call.setStatus(Statuses.CLIENT_ERROR_METHOD_NOT_ALLOWED);
   }

	/**
	 * Forwards a call to the parent component for processing. This can be useful when some sort of internal 
	 * redirection or dispatching is needed. Note that you can pass either an existing call or a fresh call 
	 * instance to this method. When the method returns, verification and further processing can still be 
	 * done, the client will only receive the response to the call when the Restlet handle method returns. 
	 * @param call The call to forward.
	 */
	public void forward(Call call)
	{
		call.setContextPath(null);
		getOwner().handle(call);
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

   /**
    * Returns the owner component.
    * @return The owner component.
    */
   public Component getOwner()
   {
      return this.owner;
   }

   /**
    * Sets the owner component.
    * @param owner The owner component.
    */
   public void setOwner(Component owner)
   {
      this.owner = owner;
   }

   /**
    * Compares this object with the specified object for order.
    * @param object The object to compare.
    * @return The result of the comparison.
    * @see java.lang.Comparable
    */
   public int compareTo(Restlet object)
   {
      return this.hashCode() - object.hashCode();
   }
   
   /**
	 * Handles a call with a given target Restlet. 
	 * @param call The call to handle.
	 * @param target The target Restlet.
	 */
	public static void handle(Call call, Restlet target)
	{
   	if(target != null)
   	{
			if(target.isStopped())
			{
				try
				{
					target.start();
				}
				catch (Exception e)
				{
					logger.log(Level.WARNING, "Unable to start the next handler and to invoke it", e);
					call.setStatus(new DefaultStatus(Statuses.SERVER_ERROR_INTERNAL, e.getMessage()));
				}
			}
			
			if(target.isStarted())
			{
				// Invoke the next handler
				target.handle(call);
			}
   	}
   	else
   	{
   		// No additional Restlet available
   		// stack moving up the stack of calls
   		// and apply the post-handle filters.
   	}
	}
}
