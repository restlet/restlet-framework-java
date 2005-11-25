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

import org.restlet.data.CharacterSet;

/**
 * Default character set implementation.
 */
public class CharacterSetImpl extends MetadataImpl implements CharacterSet
{
   /**
    * Constructor.
    * @param name The name.
    */
   public CharacterSetImpl(String name)
   {
      super(name.toUpperCase());
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Character set or range of character sets";
   }

   /**
    * Indicates if the character set is equal to a given one.
    * @param characterSet The character set to compare to.
    * @return True if the character set is equal to a given one.
    */
   public boolean equals(CharacterSet characterSet)
   {
      return getName().equalsIgnoreCase(characterSet.getName());
   }

}
