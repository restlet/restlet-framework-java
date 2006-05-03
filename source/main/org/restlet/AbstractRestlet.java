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

/**
 * Abstract Restlet that can be easily subclassed. The start and stop state is managed by 
 * default but with no other action. Override the start and stop methods if needed.
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
