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
 * Character set used to encode characters representations.
 * @see org.restlet.data.CharacterSets
 */
public interface CharacterSet extends Metadata
{
   /**
    * Indicates if the character set is equal to a given one.
    * @param characterSet The character set to compare to.
    * @return True if the character set is equal to a given one.
    */
   public boolean equals(CharacterSet characterSet);

}
