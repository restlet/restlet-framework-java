/*
 * Copyright 2005-2006 Jérôme LOUVEL
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
 * Abstract Restlet that can be easily subclassed.<br/>
 * The start and stop state is managed by default but with no other action.<br/>
 * Override the start and stop methods if needed.
 * @see <a href="http://www.restlet.org/tutorial#part03">Tutorial: Listening to Web browsers</a>
 */
public abstract class AbstractRestlet implements Restlet
{
   /** Indicates if the handler was started. */
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

   /** Starts the handler. */
   public void start() throws Exception
   {
      this.started = true;
   }

   /** Stops the handler. */
   public void stop() throws Exception
   {
      this.started = false;
   }

   /**
    * Indicates if the handler is started.
    * @return True if the handler is started.
    */
   public boolean isStarted()
   {
      return this.started;
   }

   /**
    * Indicates if the handler is stopped.
    * @return True if the handler is stopped.
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
