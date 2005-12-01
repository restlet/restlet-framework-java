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

package com.noelios.restlet.data;

import org.restlet.data.CharacterSet;

/**
 * Default character set implementation.
 */
public class CharacterSetImpl extends MetadataImpl implements CharacterSet
{
   /**
    * Constructor.
    * @param name The name.
    */
   public CharacterSetImpl(String name)
   {
      super(name.toUpperCase());
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Character set or range of character sets";
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
