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

import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 * Handler part of a processing chain. In addition to handling incoming calls like any Restlet, a handler 
 * can also resolve, either statically or dynamically, the next Restlet that will continue the processing chain.
 * Subclasses only have to implement the findNext() method.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Chainer extends Handler 
{
	/**
    * Constructor.
    * @param context The context.
    */
   public Chainer(Context context)
   {
   	super(context);
   }
	
	/**
    * Constructor.
    * @param wrappedChainer The chainer to wrap.
    */
   public Chainer(Chainer wrappedChainer)
   {
   	super(wrappedChainer);
   }

	/**
	 * Handles a call by invoking the next handler if it is available.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	public void handle(Request request, Response response)
	{
		if(getWrappedChainer() != null)
		{
			getWrappedChainer().handle(request, response);
		}
		else
		{
	  		init(request, response);

	  		Handler next = getNext(request, response);
	   	if(next != null)
	   	{
	      	next.handle(request, response);
	   	}
	   	else
	   	{
	   		response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
	   	}
		}
   }

	/** 
	 * Returns the wrapped chainer.
	 * @return The wrapped chainer.
	 */
	private Chainer getWrappedChainer()
	{
		return (Chainer)getWrappedHandler();
	}

   /**
	 * Returns the next handler if available.
    * @param request The request to handle.
    * @param response The response to update.
	 * @return The next handler if available or null.
	 */
	public Handler getNext(Request request, Response response)
	{
		return getWrappedChainer() != null ? getWrappedChainer().getNext(request, response) : null;
	}
	
}
