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
 * Language used in representations.
 * @see org.restlet.data.Languages
 */
public interface Language extends Metadata
{
   /** All languages acceptable. */
   public final static String NAME_ALL = "*";

   /** English language. */
   public final static String NAME_ENGLISH = "en";

   /** English language spoken in USA. */
   public final static String NAME_ENGLISH_USA = "en-us";

   /** French language. */
   public final static String NAME_FRENCH = "fr";

   /** French language spoken in France. */
   public final static String NAME_FRENCH_FRANCE = "fr-fr";

   /** Spanish language. */
   public final static String NAME_SPANISH = "es";

   /**
    * Returns the main tag.
    * @return The main tag.
    */
   public String getMainTag();

   /**
    * Returns the sub tag.
    * @return The sub tag.
    */
   public String getSubTag();

   /**
    * Indicates if the language is equal to a given one.
    * @param language The language to compare to.
    * @return True if the language is equal to a given one.
    */
   public boolean equals(Language language);

}
