/*
 * Copyright 2005-2006 Jerome LOUVEL
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

import java.util.Date;

/**
 * Metadata describing a representation. Resources can have multiple representations called variants.
 */
public class RepresentationMetadata implements Metadata
{
   /** The character set or null if not applicable. */
   protected CharacterSet characterSet;

   /** The encoding or null if not identity encoding applies. */
   protected Encoding encoding;

   /** The expiration date. */
   protected Date expirationDate;
   
   /** The language or null if not applicable. */
   protected Language language;

   /** The media type. */
   protected MediaType mediaType;

   /** The modification date. */
   protected Date modificationDate;

   /** The tag. */
   protected Tag tag;

   /**
    * Constructor.
    * @param mediaType The media type.
    */
   public RepresentationMetadata(MediaType mediaType)
   {
      this.characterSet = null;
      this.encoding = null;
      this.expirationDate = null;
      this.language = null;
      this.mediaType = mediaType;
      this.modificationDate = null;
      this.tag = null;
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Representation variant";
   }

   /**
    * Returns the metadata name like "text/html" or "compress" or "iso-8851-1".
    * @return The metadata name like "text/html" or "compress" or "iso-8851-1".
    */
   public String getName()
   {
      return "Representation metadata";
   }

   /**
    * Returns the character set or null if not applicable.
    * @return The character set or null if not applicable.
    */
   public CharacterSet getCharacterSet()
   {
      return characterSet;
   }

   /**
    * Sets the character set or null if not applicable.
    * @param characterSet The character set or null if not applicable.
    */
   public void setCharacterSet(CharacterSet characterSet)
   {
      this.characterSet = characterSet;
   }

   /**
    * Returns the encoding or null if identity encoding applies.
    * @return The encoding or null if identity encoding applies.
    */
   public Encoding getEncoding()
   {
      return this.encoding;
   }

   /**
    * Sets the encoding or null if identity encoding applies.
    * @param encoding The encoding or null if identity encoding applies.
    */
   public void setEncoding(Encoding encoding)
   {
      this.encoding = encoding;
   }

   /**
    * Returns the future date when this representation expire. If this information is not known, returns null.
    * @return The expiration date.
    */
   public Date getExpirationDate()
   {
      return expirationDate;
   }

   /**
    * Sets the future date when this representation expire. If this information is not known, pass null.
    * @param expirationDate The expiration date.
    */
   public void setExpirationDate(Date expirationDate)
   {
      this.expirationDate = expirationDate;
   }

   /**
    * Returns the language or null if not applicable.
    * @return The language or null if not applicable.
    */
   public Language getLanguage()
   {
      return language;
   }

   /**
    * Sets the language or null if not applicable.
    * @param language The language or null if not applicable.
    */
   public void setLanguage(Language language)
   {
      this.language = language;
   }

   /**
    * Returns the media type.
    * @return The media type.
    */
   public MediaType getMediaType()
   {
      return mediaType;
   }

   /**
    * Sets the media type.
    * @param mediaType The media type.
    */
   public void setMediaType(MediaType mediaType)
   {
      this.mediaType = mediaType;
   }

   /**
    * Returns the last date when this representation was modified. If this information is not known, returns
    * null.
    * @return The modification date.
    */
   public Date getModificationDate()
   {
      return modificationDate;
   }

   /**
    * Sets the last date when this representation was modified. If this information is not known, pass null.
    * @param modificationDate The modification date.
    */
   public void setModificationDate(Date modificationDate)
   {
      this.modificationDate = modificationDate;
   }

   /**
    * Returns the tag.
    * @return The tag.
    */
   public Tag getTag()
   {
      return tag;
   }

   /**
    * Sets the tag.
    * @param tag The tag.
    */
   public void setTag(Tag tag)
   {
      this.tag = tag;
   }

}
