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

package org.restlet.component;

import org.restlet.Call;
import org.restlet.Context;
import org.restlet.Restlet;

/**
 * Abstract unit of software instructions and internal state. "A component is an abstract
 * unit of software instructions and internal state that provides a transformation of data
 * via its interface." Roy T. Fielding
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/software_arch.htm#sec_1_2_1">Source
 * dissertation</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Component extends Restlet
{
	/** The root Restlet. */
	private Restlet root;
   
   /**
    * Constructor.
    * @param context The context.
    */
   public Component(Context context)
   {
      this(context, null);
   }

   /**
    * Constructor.
    * @param context The context.
    * @param root The root Restlet.
    */
   public Component(Context context, Restlet root)
   {
   	super(context);
   	this.root = root;
   }

   /** Start hook. */
   public void start() throws Exception
   {
      super.start();

      if(getRoot() != null)
   	{
   		getRoot().start();
   	}
   }

   /** Stop hook. */
   public void stop() throws Exception
   {
   	if(getRoot() != null)
   	{
   		getRoot().stop();
   	}
   	
      super.stop();
   }

   /**
	 * Sets the root Restlet that will receive all incoming calls. In general, instance of Router, 
	 * Filter or Handler interfaces will be used as root of containers.
	 * @param root The root Restlet to use.
	 */
	public void setRoot(Restlet root)
	{
		this.root = root;
	}

	/**
	 * Returns the root Restlet.
	 * @return The root Restlet.
	 */
	public Restlet getRoot()
	{
		return this.root;
	}

	/**
	 * Indicates if a root Restlet is set. 
	 * @return True if a root Restlet is set. 
	 */
	public boolean hasRoot()
	{
		return getRoot() != null;
	}

   /**
    * Handles a direct call.
    * @param call The call to handle.
    */
	public void handle(Call call)
   {
		handle(call, getRoot());
   }

}
