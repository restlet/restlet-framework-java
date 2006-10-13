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

package org.restlet.util;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Wrapper used to enrich a Restlet with additional state or logic.
 * @see <a href="http://c2.com/cgi/wiki?DecoratorPattern">The decorator (aka wrapper) pattern</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class WrapperRestlet extends Restlet
{
	/** Wrapped resource. */
	private Restlet wrappedRestlet;

	/**
	 * Constructor.
	 * @param wrappedRestlet The wrapped Restlet.
	 */
	public WrapperRestlet(Restlet wrappedRestlet)
	{
		this.wrappedRestlet = wrappedRestlet;
	}

	/**
	 * Returns the wrapped Restlet.
	 * @return The wrapped Restlet.
	 */
	protected Restlet getWrappedRestlet()
	{
		return this.wrappedRestlet;
	}


   /**
    * Returns the context.
    * @return The context.
    */
   public Context getContext()
   {
      return getWrappedRestlet().getContext();
   }

   /**
    * Sets the context.
    * @param context The context.
    */
   public void setContext(Context context)
   {
   	getWrappedRestlet().setContext(context);
   }

   /**
    * Handles a call.
    * @param request The request to handle.
    * @param response The response to update.
    */
	public void handle(Request request, Response response)
   {
		getWrappedRestlet().handle(request, response);
   }
   
   /** Starts the Restlet. */
   public void start() throws Exception
   {
   	getWrappedRestlet().start();
   }

   /** Stops the Restlet. */
   public void stop() throws Exception
   {
   	getWrappedRestlet().stop();
   }

   /**
    * Indicates if the Restlet is started.
    * @return True if the Restlet is started.
    */
   public boolean isStarted()
   {
      return getWrappedRestlet().isStarted();
   }

   /**
    * Indicates if the Restlet is stopped.
    * @return True if the Restlet is stopped.
    */
   public boolean isStopped()
   {
      return getWrappedRestlet().isStopped();
   }
}
