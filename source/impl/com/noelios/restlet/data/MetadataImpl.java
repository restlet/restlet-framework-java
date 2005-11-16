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

import org.restlet.data.Metadata;

/**
 * Default metadata implementation.
 */
public class MetadataImpl implements Metadata
{
   /** The unique name. */
   private String name;

   /**
    * Constructor.
    * @param name The unique name.
    */
   public MetadataImpl(String name)
   {
      this.name = name;
   }

   /**
    * Returns the metadata name like "text/html" or "compress" or "iso-8851-1".
    * @return The metadata name like "text/html" or "compress" or "iso-8851-1".
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
      return "Metadata or range of metadata";
   }

   /**
    * Returns the string representation.
    * @return The string representation.
    */
   public String toString()
   {
      return name;
   }

   /**
    * Returns a hash code value for the object.
    * @return A hash code value for the object.
    */
   public int hashCode()
   {
      return getName().hashCode(); // No parameters taken into account!!
   }

   /**
    * Indicates whether some other object is "equal to" this one.
    * @param object The reference object with which to compare.
    */
   public boolean equals(Object object)
   {
      return (object.hashCode() == this.hashCode());
   }

}

