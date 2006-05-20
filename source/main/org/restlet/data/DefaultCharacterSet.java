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
 * Character set used to encode characters in textual representations.
 */
public class DefaultCharacterSet extends DefaultMetadata implements CharacterSet
{
   /**
    * Constructor.
    * @param name The name.
    */
   public DefaultCharacterSet(String name)
   {
      this(name, "Character set or range of character sets");
   }

   /**
    * Constructor.
    * @param name The name.
    * @param description The description. 
    */
   public DefaultCharacterSet(String name, String description)
   {
      super(name.toUpperCase(), description);
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
