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
 * Default Restlet that can be easily subclassed to override only the supported methods.
 * By default any call using the other methods will return a "method not allowed" error to the client. 
 */
public class DefaultRestlet extends AbstractRestlet
{
   /**
    * Constructor.
    */
   public DefaultRestlet()
   {
      super();
   }

   /**
    * Constructor.
    * @param parent The parent component.
    */
   public DefaultRestlet(Component parent)
   {
   	super(parent);
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
   
}
