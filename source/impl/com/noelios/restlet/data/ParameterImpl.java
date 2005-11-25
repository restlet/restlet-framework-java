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

import org.restlet.RestletException;
import org.restlet.data.Parameter;

/**
 * Default parameter implementation.
 */
public class ParameterImpl implements Parameter
{
   /** The name. */
   protected String name;

   /** The value. */
   protected String value;

   /**
    * Default constructor.
    * @throws RestletException
    */
   public ParameterImpl() throws RestletException
   {
      this(null, null);
   }

   /**
    * Preferred constructor.
    * @param name The name.
    * @param value The value.
    */
   public ParameterImpl(String name, String value)
   {
      this.name = name;
      this.value = value;
   }

   /**
    * Returns the name.
    * @return The name.
    */
   public String getName()
   {
      return this.name;
   }

   /**
    * Sets the name.
    * @param name The name.
    */
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    * Returns the value.
    * @return The value.
    */
   public String getValue()
   {
      return this.value;
   }

   /**
    * Sets the value.
    * @param value The value.
    */
   public void setValue(String value)
   {
      this.value = value;
   }

   /**
    * Compares two parameters.
    * @param otherParam The other parameter to compare.
    * @return True if the parameters are identical (name and value).
    */
   public boolean equals(Parameter otherParam)
   {
      boolean result = true;

      if(getName() == null)
      {
         result = (otherParam.getName() == null);
      }
      else
      {
         result = getName().equals(otherParam.getName());
      }

      if(getValue() == null)
      {
         result &= (otherParam.getValue() == null);
      }
      else
      {
         result &= getValue().equals(otherParam.getValue());
      }

      return result;
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Parameter";
   }

   /**
    * Compares this object with the specified object for order.
    * @param o The object to be compared.
    * @return A negative integer, zero, or a positive integer as this object is less than, equal to, or
    * greater than the specified object.
    */
   public int compareTo(Parameter o)
   {
      return getName().compareTo(o.getName());
   }

}
