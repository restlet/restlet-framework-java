/*
 * Copyright 2005 Jérôme LOUVEL
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

import org.restlet.component.RestletContainer;

/**
 * Abstract restlet that can be easily subclassed.
 */
public abstract class AbstractRestlet implements Restlet, Comparable
{
   /**
    * The container.
    * @link aggregation
    * @label parent container
    */
   protected RestletContainer container;

   /**
    * Creates a new restlet in the given container.
    * @param container The parent container.
    */
   public AbstractRestlet(RestletContainer container)
   {
      this.container = container;
   }

   /**
    * Returns the container.
    * @return The container.
    */
   public RestletContainer getContainer()
   {
      return this.container;
   }

   /**
    * Sets the container.
    * @param container The container.
    */
   protected void setContainer(RestletContainer container)
   {
      this.container = container;
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
