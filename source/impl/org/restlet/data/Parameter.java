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

package org.restlet.data;

/**
 * Multi-usage parameter.
 */
public interface Parameter extends Data, Comparable<Parameter>
{
   /**
    * Returns the name.
    * @return The name.
    */
   public String getName();

   /**
    * Sets the name.
    * @param name The name.
    */
   public void setName(String name);

   /**
    * Returns the value.
    * @return The value.
    */
   public String getValue();

   /**
    * Sets the value.
    * @param value The value.
    */
   public void setValue(String value);

   /**
    * Compares two parameters.
    * @param otherParam The other parameter to compare.
    * @return 				True if the parameters are identical (name and value).
    */
   public boolean equals(Parameter otherParam);
}




