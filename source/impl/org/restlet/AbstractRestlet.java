/*
 * Copyright © 2005 Jérôme LOUVEL.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
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
