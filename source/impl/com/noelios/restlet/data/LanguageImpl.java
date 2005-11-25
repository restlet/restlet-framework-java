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

import org.restlet.data.Language;

/**
 * Default language implementation.
 */
public class LanguageImpl extends MetadataImpl implements Language
{
   /**
    * Constructor.
    * @param name The language name;
    */
   public LanguageImpl(String name)
   {
      super(name.toLowerCase());
   }

   /**
    * Returns the main tag.
    * @return The main tag.
    */
   public String getMainTag()
   {
      int separator = getName().indexOf('-');

      if(separator == -1)
      {
         return getName();
      }
      else
      {
         return getName().substring(0, separator);
      }
   }

   /**
    * Returns the sub tag.
    * @return The sub tag.
    */
   public String getSubTag()
   {
      int separator = getName().indexOf('-');

      if(separator == -1)
      {
         return null;
      }
      else
      {
         return getName().substring(separator + 1);
      }
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Language or range of languages";
   }

   /**
    * Indicates if the language is equal to a given one.
    * @param language The language to compare to.
    * @return True if the language is equal to a given one.
    */
   public boolean equals(Language language)
   {
      return getName().equalsIgnoreCase(language.getName());
   }

}
