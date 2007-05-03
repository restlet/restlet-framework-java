/*
 * Copyright 2005-2006 Jérôme LOUVEL
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

import java.util.List;

import org.restlet.data.Language;
import org.restlet.data.LanguagePref;
import org.restlet.data.Parameter;

/**
 * Language preference.
 */
public class LanguagePrefImpl extends PreferenceImpl implements LanguagePref
{
   /**
    * Constructor.
    * @param language The associated language.
    */
   public LanguagePrefImpl(Language language)
   {
      super(language, 1F, null);
   }

   /**
    * Constructor.
    * @param language The associated language.
    * @param quality The quality/preference level.
    */
   public LanguagePrefImpl(Language language, float quality)
   {
      super(language, quality, null);
   }

   /**
    * Constructor.
    * @param language The associated language.
    * @param quality The quality/preference level.
    * @param parameters The list of parameters.
    */
   public LanguagePrefImpl(Language language, float quality, List<Parameter> parameters)
   {
      super(language, quality, parameters);
   }

   /**
    * Returns the language associated with this preference.
    * @return The language associated with this preference.
    */
   public Language getLanguage()
   {
      return (Language)getMetadata();
   }
}
