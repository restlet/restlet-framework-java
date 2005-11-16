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

package com.noelios.restlet.data;

import org.restlet.data.Method;

/** 
 * Default method implementation. 
 * @see org.restlet.data.MethodEnum
 */
public class MethodImpl implements Method
{
   /** The technical name of the method. */
   private String name;
   
   /** The description of this REST element. */
   private String description;
   
   /** The URI of the specification describing the method. */
   private String uri;
   
   /**
    * Constructor.
    * @param name The technical name of the method.
    */
   public MethodImpl(String name)
   {
      this(name, null, null);
   }
   
   /**
    * Constructor.
    * @param name          The technical name of the method.
    * @param description   The description of this REST element.
    * @param uri           The URI of the specification describing the method.
    */
   public MethodImpl(String name, String description, String uri)
   {
      this.name = name;
      this.description = description;
      this.uri = uri;
   }
   
   /**
    * Returns the technical name of the method.
    * @return The technical name of the method.
    */
   public String getName()
   {
      return name;
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return description;
   }

   /**
    * Returns the URI of the specification describing the method.
    * @return The URI of the specification describing the method.
    */
   public String getUri()
   {
      return uri;
   }

   /**
    * Indicates if the method is equal to a given one.
    * @param method  The method to compare to.
    * @return        True if the method is equal to a given one.
    */
   public boolean equals(Method method)
   {
      return getName().equals(method.getName());
   }

}
