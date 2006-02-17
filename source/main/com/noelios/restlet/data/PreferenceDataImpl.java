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

import java.util.ArrayList;
import java.util.List;

import org.restlet.data.CharacterSetPref;
import org.restlet.data.EncodingPref;
import org.restlet.data.LanguagePref;
import org.restlet.data.MediaTypePref;
import org.restlet.data.PreferenceData;

/**
 * Client preference data related to a call.
 */
public class PreferenceDataImpl implements PreferenceData
{
   /** The character set preferences. */
   protected List<CharacterSetPref> characterSets;

   /** The encoding preferences. */
   protected List<EncodingPref> encodings;

   /** The language preferences. */
   protected List<LanguagePref> languages;

   /** The media preferences. */
   protected List<MediaTypePref> mediaTypes;

   /**
    * Constructor.
    */
   public PreferenceDataImpl()
   {
      this.characterSets = null;
      this.encodings = null;
      this.languages = null;
      this.mediaTypes = null;
   }

   /**
    * Returns the character set preferences.
    * @return The character set preferences.
    */
   public List<CharacterSetPref> getCharacterSets()
   {
      if(this.characterSets == null) this.characterSets = new ArrayList<CharacterSetPref>();
      return this.characterSets;
   }

   /**
    * Returns the encoding preferences.
    * @return The encoding preferences.
    */
   public List<EncodingPref> getEncodings()
   {
      if(this.encodings == null) this.encodings = new ArrayList<EncodingPref>();
      return this.encodings;      
   }

   /**
    * Returns the language preferences.
    * @return The language preferences.
    */
   public List<LanguagePref> getLanguages()
   {
      if(this.languages == null) this.languages = new ArrayList<LanguagePref>();
      return this.languages;
   }

   /**
    * Returns the media type preferences.
    * @return The media type preferences.
    */
   public List<MediaTypePref> getMediaTypes()
   {
      if(this.mediaTypes == null) this.mediaTypes = new ArrayList<MediaTypePref>();
      return this.mediaTypes;
   }

   /**
    * Returns the name of this REST element.
    * @return The name of this REST element.
    */
   public String getName()
   {
      return "Preference data";
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Client preference data related to a call";
   }

}
