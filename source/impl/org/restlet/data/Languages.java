/*
 * Copyright © 2005 Jérôme LOUVEL.  All Rights Reserved.
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
    * Returns the description of this REST element.
    * @return The description of this REST element.
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

      if (separator == -1)
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

      if (separator == -1)
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
    * @param language   The language to compare to.
    * @return           True if the language is equal to a given one.
    */
   public boolean equals(Language language)
   {
      return getName().equalsIgnoreCase(language.getName());
   }

}




