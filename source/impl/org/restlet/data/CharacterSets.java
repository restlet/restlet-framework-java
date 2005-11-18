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
 * Enumeration of common character sets.
 */
public enum CharacterSets implements CharacterSet
{
   /** All character sets acceptable. */
   ALL,

   /**
    * The ISO/IEC 8859-1 or Latin 1 character set.
    * @see <a href="http://en.wikipedia.org/wiki/ISO_8859-1">Wikipedia page</a>
    */
   ISO_8859_1,

   /**
    * The US-ASCII character set.
    * @see <a href="http://en.wikipedia.org/wiki/US-ASCII">Wikipedia page</a>
    */
   US_ASCII,

   /**
    * The UTF-8 character set.
    * @see <a href="http://en.wikipedia.org/wiki/UTF-8">Wikipedia page</a>
    */
   UTF_8,
   
   /**
    * The UTF-16 character set.
    * @see <a href="http://en.wikipedia.org/wiki/UTF-16">Wikipedia page</a>
    */
   UTF_16;

   /**
    * Returns the metadata name like "text/html" or "compress" or "iso-8851-1".
    * @return The metadata name like "text/html" or "compress" or "iso-8851-1".
    */
   public String getName()
   {
      String result = null;
      
      switch(this)
      {
         case ALL:
            result = "*";
            break;
         case ISO_8859_1:
            result = "ISO-8859-1";
            break;
         case US_ASCII:
            result = "US-ASCII";
            break;
         case UTF_8:
            result = "UTF-8";
            break;
         case UTF_16:
            result = "UTF-16";
            break;
      }
      
      return result;
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      String result = null;
      
      switch(this)
      {
         case ALL:
            result = "All character sets";
            break;
         case ISO_8859_1:
            result = "ISO/IEC 8859-1 or Latin 1 character set";
            break;
         case US_ASCII:
            result = "US ASCII character set";
            break;
         case UTF_8:
            result = "UTF 8 character set";
            break;
         case UTF_16:
            result = "UTF 16 character set";
            break;
      }
      
      return result;
   }

   /**
    * Indicates if the character set is equal to a given one.
    * @param characterSet  The character set to compare to.
    * @return              True if the character set is equal to a given one.
    */
   public boolean equals(CharacterSet characterSet)
   {
      return getName().equalsIgnoreCase(characterSet.getName());
   }

}
