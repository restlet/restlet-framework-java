/*
 * Copyright 2005-2006 Noelios Consulting.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.data;

/**
 * Language used in representations.
 */
public enum Languages implements Language
{
   /** All languages acceptable. */
   ALL,

   /** English language. */
   ENGLISH,

   /** English language spoken in USA. */
   ENGLISH_US,

   /** French language. */
   FRENCH,

   /** French language spoken in France. */
   FRENCH_FRANCE,

   /** Spanish language. */
   SPANISH;

   /**
    * Returns the name (ex: "*" or "en" or "en-us").
    * @return The name (ex: "*" or "en" or "en-us").
    */
   public String getName()
   {
      String result = null;

      switch(this)
      {
         case ALL:
            result = "*";
            break;
         case ENGLISH:
            result = "en";
            break;
         case ENGLISH_US:
            result = "en-us";
            break;
         case FRENCH:
            result = "fr";
            break;
         case FRENCH_FRANCE:
            result = "fr-fr";
            break;
         case SPANISH:
            result = "es";
            break;
      }

      return result;
   }

   /**
    * Returns the description.
    * @return The description.
    */
   public String getDescription()
   {
      String result = null;

      switch(this)
      {
         case ALL:
            result = "All languages";
            break;
         case ENGLISH:
            result = "English language";
            break;
         case ENGLISH_US:
            result = "English language in the USA";
            break;
         case FRENCH:
            result = "French language";
            break;
         case FRENCH_FRANCE:
            result = "French language in France";
            break;
         case SPANISH:
            result = "Spanish language";
            break;
      }

      return result;
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
    * Indicates if the language is equal to a given one.
    * @param language The language to compare to.
    * @return True if the language is equal to a given one.
    */
   public boolean equals(Language language)
   {
      return getName().equalsIgnoreCase(language.getName());
   }

}
