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
 * Enumeration of common character sets.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
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
    * Returns the name (ex. "*" or "ISO-8859-1" or "US-ASCII").
    * @return The name (ex. "*" or "ISO-8859-1" or "US-ASCII").
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
    * Returns the description.
    * @return The description.
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
    * @param characterSet The character set to compare to.
    * @return True if the character set is equal to a given one.
    */
   public boolean equals(CharacterSet characterSet)
   {
      return getName().equalsIgnoreCase(characterSet.getName());
   }
   
}
