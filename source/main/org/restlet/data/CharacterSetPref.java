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

import java.util.List;

/**
 * Character set preference.
 */
public class CharacterSetPref extends Preference
{
   /**
    * Constructor.
    * @param characterSet The associated character set.
    */
   public CharacterSetPref(CharacterSet characterSet)
   {
      super(characterSet, 1F, null);
   }

   /**
    * Constructor.
    * @param characterSet The associated character set.
    * @param quality The quality/preference level.
    */
   public CharacterSetPref(CharacterSet characterSet, float quality)
   {
      super(characterSet, quality, null);
   }

   /**
    * Constructor.
    * @param characterSet The associated character set.
    * @param quality The quality/preference level.
    * @param parameters The list of parameters.
    */
   public CharacterSetPref(CharacterSet characterSet, float quality, List<Parameter> parameters)
   {
      super(characterSet, quality, parameters);
   }

   /**
    * Returns the character set associated with this preference.
    * @return The character set associated with this preference.
    */
   public CharacterSet getCharacterSet()
   {
      return (CharacterSet)getMetadata();
   }
}
