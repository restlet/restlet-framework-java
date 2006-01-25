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

package org.restlet.data;

import java.util.List;

/**
 * Preference data related to a uniform call.
 */
public interface PreferenceData extends ControlData
{
   /**
    * Returns the character set preferences.
    * @return The character set preferences.
    */
   public List<CharacterSetPref> getCharacterSets();

   /**
    * Returns the encoding preferences.
    * @return The encoding preferences.
    */
   public List<EncodingPref> getEncodings();

   /**
    * Returns the language preferences.
    * @return The language preferences.
    */
   public List<LanguagePref> getLanguages();

   /**
    * Returns the media type preferences.
    * @return The media type preferences.
    */
   public List<MediaTypePref> getMediaTypes();
}
