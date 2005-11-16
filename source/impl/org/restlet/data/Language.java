/*
 * Copyright © 2005 Jérôme LOUVEL.  All Rights Reserved.
 */

package org.restlet.data;

/**
 * Language used in representations.
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
    * @param language   The language to compare to.
    * @return           True if the language is equal to a given one.
    */
   public boolean equals(Language language);

}




