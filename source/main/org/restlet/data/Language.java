/*
 * Copyright 2005 Jérôme LOUVEL
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
