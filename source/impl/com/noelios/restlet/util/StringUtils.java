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

package com.noelios.restlet.util;

/**
 * String manipulation utilities.
 */
public class StringUtils
{
   /**
    * Strip a delimiter character from both ends of the source string.
    * @param source The source string to strip.
    * @param delimiter The character to remove.
    * @return The stripped string.
    */
   public static String strip(String source, char delimiter)
   {
      return strip(source, delimiter, true, true);
   }

   /**
    * Strip a delimiter character from a source string.
    * @param source The source string to strip.
    * @param delimiter The character to remove.
    * @param start Indicates if start of source should be stripped.
    * @param end Indicates if end of source should be stripped.
    * @return The stripped source string.
    */
   public static String strip(String source, char delimiter, boolean start, boolean end)
   {
      int beginIndex = 0;
      int endIndex = source.length();
      boolean stripping = true;

      // Strip beginning
      while(stripping && (beginIndex < endIndex))
      {
         if(source.charAt(beginIndex) == delimiter)
         {
            beginIndex++;
         }
         else
         {
            stripping = false;
         }
      }

      // Strip end
      stripping = true;
      while(stripping && (beginIndex < endIndex - 1))
      {
         if(source.charAt(endIndex - 1) == delimiter)
         {
            endIndex--;
         }
         else
         {
            stripping = false;
         }
      }

      return source.substring(beginIndex, endIndex);
   }

}
