/*
 * Copyright 2005-2006 Jerome LOUVEL
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
 * @see org.restlet.DefaultRestlet
 * @see <a href="http://www.restlet.org/tutorial#part03">Tutorial: Listening to Web browsers</a>
 */
public abstract class AbstractRestlet implements Restlet
{
   /** Indicates if the restlet was started. */
   protected boolean started;

   /** The parent component. */
   protected Component parent;

   /**
    * Constructor.
    */
   public AbstractRestlet()
   {
      this(null);
   }

   /**
    * Constructor.
    * @param parent The parent component.
    */
   public AbstractRestlet(Component parent)
   {
   	this.parent = parent;
      this.started = false;
   }

   /**
    * Handles a call.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
		if(call.getMethod().equals(Methods.GET))
		{
			handleGet(call);
		}
		else if(call.getMethod().equals(Methods.POST))
		{
			handlePost(call);
		}
		else if(call.getMethod().equals(Methods.PUT))
		{
			handlePut(call);
		}
		else if(call.getMethod().equals(Methods.DELETE))
		{
			handleDelete(call);
		}
		else if(call.getMethod().equals(Methods.HEAD))
		{
			handleHead(call);
		}
		else if(call.getMethod().equals(Methods.CONNECT))
		{
			handleConnect(call);
		}
		else if(call.getMethod().equals(Methods.OPTIONS))
		{
			handleOptions(call);
		}
		else if(call.getMethod().equals(Methods.TRACE))
		{
			handleTrace(call);
		}
		else if(call.getMethod().equals(Methods.MOVE))
		{
			handleMove(call);
		}
		else if(call.getMethod().equals(Methods.COPY))
		{
			handleCopy(call);
		}
		else if(call.getMethod().equals(Methods.LOCK))
		{
			handleLock(call);
		}
		else if(call.getMethod().equals(Methods.MKCOL))
		{
			handleMakeCollection(call);
		}
		else if(call.getMethod().equals(Methods.PROPFIND))
		{
			handleFindProperties(call);
		}
		else if(call.getMethod().equals(Methods.PROPPATCH))
		{
			handlePatchProperties(call);
		}
		else if(call.getMethod().equals(Methods.UNLOCK))
		{
			handleUnlock(call);
		}
   }

   /**
    * Handles a CONNECT call.
    * @param call The call to handle.
    */
   public void handleConnect(Call call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a COPY call.
    * @param call The call to handle.
    */
   public void handleCopy(Call call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a DELETE call.
    * @param call The call to handle.
    */
   public void handleDelete(Call call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a GET call.
    * @param call The call to handle.
    */
   public void handleGet(Call call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a HEAD call.
    * @param call The call to handle.
    */
   public void handleHead(Call call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a LOCK call.
    * @param call The call to handle.
    */
   public void handleLock(Call call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a MKCOL call.
    * @param call The call to handle.
    */
   public void handleMakeCollection(Call call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a MOVE call.
    * @param call The call to handle.
    */
   public void handleMove(Call call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a OPTIONS call.
    * @param call The call to handle.
    */
   public void handleOptions(Call call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a POST call.
    * @param call The call to handle.
    */
   public void handlePost(Call call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a PROPFIND call.
    * @param call The call to handle.
    */
   public void handleFindProperties(Call call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a PROPPATCH call.
    * @param call The call to handle.
    */
   public void handlePatchProperties(Call call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a PUT call.
    * @param call The call to handle.
    */
   public void handlePut(Call call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a TRACE call.
    * @param call The call to handle.
    */
   public void handleTrace(Call call)
   {
   	defaultHandle(call);
   }

   /**
    * Handles a UNLOCK call.
    * @param call The call to handle.
    */
   public void handleUnlock(Call call)
   {
   	defaultHandle(call);
   }
   
   /**
    * Default implementation for the handle*() methods that simply throws
    * and "illegal access error" that is intercepted by the handle() method. 
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
		getParent().handle(call);
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
    * Returns the parent component.
    * @return The parent component.
    */
   public Component getParent()
   {
      return this.parent;
   }

   /**
    * Sets the parent component.
    * @param parent The parent component.
    */
   public void setParent(Component parent)
   {
      this.parent = parent;
   }

   /**
    * Compares this object with the specified object for order.
    * @param object The object to compare.
    * @return The result of the comparison.
    * @see java.lang.Comparable
    */
   public int compareTo(Object object)
   {
      return this.hashCode() - object.hashCode();
   }

}
