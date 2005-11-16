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

package org.restlet.connector;

/**
 * Abstract connector implementation.
 */
public abstract class AbstractConnector implements Connector
{
   /** The name of this REST connector. */
   private String name;

   /**
    * Constructor.
    * @param name The name of this REST connector.
    */
   public AbstractConnector(String name)
   {
      this.name = name;
   }

   /**
    * Returns the name of this REST connector.
    * @return The name of this REST connector.
    */
   public String getName()
   {
      return this.name;
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Default connector";
   }

   /** Start hook. */
   public void start()
   {
      // No default action
   }

   /** Stop hook. */
   public void stop()
   {
      // No default action
   }

}




