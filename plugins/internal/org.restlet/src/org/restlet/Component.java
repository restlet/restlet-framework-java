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
import org.restlet.util.ClientList;
import org.restlet.util.ServerList;


/**
 * Abstract unit of software instructions and internal state. "A component is an abstract
 * unit of software instructions and internal state that provides a transformation of data
 * via its interface." Roy T. Fielding
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/software_arch.htm#sec_1_2_1">Source
 * dissertation</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class Component implements UniformInterface
{
   /** The wrapped component. */
	private Component wrappedComponent;
   
	/**
	 * Constructor for wrappers.
	 * @param wrappedComponent The wrapped component.
	 */
	protected Component(Component wrappedComponent)
	{
		this.wrappedComponent = wrappedComponent;
	}

	/**
	 * Returns the wrapped component.
	 * @return The wrapped component.
	 */
	protected Component getWrappedComponent()
	{
		return this.wrappedComponent;
	}

   /**
    * Handles a call.
    * @param request The request to handle.
    * @param response The response to update.
    */
	public void handle(Request request, Response response)
   {
   	getWrappedComponent().handle(request, response);
   }

   /** Start hook. */
   public void start() throws Exception
   {
   	getWrappedComponent().start();
   }

   /** Stop hook. */
   public void stop() throws Exception
   {
   	getWrappedComponent().stop();
   }

   /**
    * Indicates if the Restlet is started.
    * @return True if the Restlet is started.
    */
   public boolean isStarted()
   {
   	return getWrappedComponent().isStarted();
   }

   /**
    * Indicates if the Restlet is stopped.
    * @return True if the Restlet is stopped.
    */
   public boolean isStopped()
   {
   	return getWrappedComponent().isStopped();
   }

   /**
    * Returns the context.
    * @return The context.
    */
   public Context getContext()
   {
      return getWrappedComponent().getContext();
   }

   /**
    * Sets the context.
    * @param context The context.
    */
   public void setContext(Context context)
   {
		getWrappedComponent().setContext(context);
   }

	/**
	 * Returns the modifiable list of client connectors.
	 * @return The modifiable list of client connectors.
	 */
	public ClientList getClients()
	{
		return getWrappedComponent().getClients();
	}

	/**
	 * Returns the modifiable list of server connectors.
	 * @return The modifiable list of server connectors.
	 */
	public ServerList getServers()
	{
		return getWrappedComponent().getServers();
	}
}
