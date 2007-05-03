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

import org.restlet.component.Component;
import org.restlet.data.Methods;
import org.restlet.data.Statuses;

/**
 * Abstract Restlet that can be easily subclassed. Concrete classes must only implement the handle(Call)
 * method. Another option is to derive the DefaultRestlet to override only the supported handle methods. 
 * The start and stop state is managed by default but with no other action. Override the start and stop 
 * methods if needed.
 * @see <a href="http://www.restlet.org/tutorial#part03">Tutorial: Listening to Web browsers</a>
 */
public abstract class AbstractRestlet<T extends Call> implements Restlet
{
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
   @SuppressWarnings("unchecked")
	public void handle(Call call)
   {
   	T genericCall = (T)call;
   	
		if(call.getMethod().equals(Methods.GET))
		{
			handleGet(genericCall);
		}
		else if(call.getMethod().equals(Methods.POST))
		{
			handlePost(genericCall);
		}
		else if(call.getMethod().equals(Methods.PUT))
		{
			handlePut(genericCall);
		}
		else if(call.getMethod().equals(Methods.DELETE))
		{
			handleDelete(genericCall);
		}
		else if(call.getMethod().equals(Methods.HEAD))
		{
			handleHead(genericCall);
		}
		else if(call.getMethod().equals(Methods.CONNECT))
		{
			handleConnect(genericCall);
		}
		else if(call.getMethod().equals(Methods.OPTIONS))
		{
			handleOptions(genericCall);
		}
		else if(call.getMethod().equals(Methods.TRACE))
		{
			handleTrace(genericCall);
		}
		else if(call.getMethod().equals(Methods.MOVE))
		{
			handleMove(genericCall);
		}
		else if(call.getMethod().equals(Methods.COPY))
		{
			handleCopy(genericCall);
		}
		else if(call.getMethod().equals(Methods.LOCK))
		{
			handleLock(genericCall);
		}
		else if(call.getMethod().equals(Methods.MKCOL))
		{
			handleMakeCollection(genericCall);
		}
		else if(call.getMethod().equals(Methods.PROPFIND))
		{
			handleFindProperties(genericCall);
		}
		else if(call.getMethod().equals(Methods.PROPPATCH))
		{
			handlePatchProperties(genericCall);
		}
		else if(call.getMethod().equals(Methods.UNLOCK))
		{
			handleUnlock(genericCall);
		}
   }

   /**
    * Handles a CONNECT call.
    * @param call The call to handle.
    */
   public void handleConnect(T call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a COPY call.
    * @param call The call to handle.
    */
   public void handleCopy(T call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a DELETE call.
    * @param call The call to handle.
    */
   public void handleDelete(T call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a GET call.
    * @param call The call to handle.
    */
   public void handleGet(T call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a HEAD call.
    * @param call The call to handle.
    */
   public void handleHead(T call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a LOCK call.
    * @param call The call to handle.
    */
   public void handleLock(T call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a MKCOL call.
    * @param call The call to handle.
    */
   public void handleMakeCollection(T call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a MOVE call.
    * @param call The call to handle.
    */
   public void handleMove(T call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a OPTIONS call.
    * @param call The call to handle.
    */
   public void handleOptions(T call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a POST call.
    * @param call The call to handle.
    */
   public void handlePost(T call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a PROPFIND call.
    * @param call The call to handle.
    */
   public void handleFindProperties(T call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a PROPPATCH call.
    * @param call The call to handle.
    */
   public void handlePatchProperties(T call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a PUT call.
    * @param call The call to handle.
    */
   public void handlePut(T call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a TRACE call.
    * @param call The call to handle.
    */
   public void handleTrace(T call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a UNLOCK call.
    * @param call The call to handle.
    */
   public void handleUnlock(T call)
   {
   	defaultHandle(call);
   }
   
   /**
    * Default implementation for the handle*() methods that simply throws
    * and "illegal access error" that is intercepted by the handle() method. 
    * @param call The call to handle.
    */
   protected void defaultHandle(T call)
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
	public void forward(T call)
	{
		call.setContextPath(null);
		getOwner().handle(call);
	}
   
   /** Starts the restlet. */
   public void start() throws Exception
   {
      this.started = true;
   }

   /** Stops the restlet. */
   public void stop() throws Exception
   {
      this.started = false;
   }

   /**
    * Indicates if the restlet is started.
    * @return True if the restlet is started.
    */
   public boolean isStarted()
   {
      return this.started;
   }

   /**
    * Indicates if the restlet is stopped.
    * @return True if the restlet is stopped.
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

}
