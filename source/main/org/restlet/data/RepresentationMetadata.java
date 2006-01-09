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

import java.util.Date;

/**
 * Metadata describing a representation. Resources can have multiple representations called variants.
 */
public interface RepresentationMetadata extends Metadata
{
   /**
    * Returns the media type.
    * @return The media type.
    */
   public MediaType getMediaType();

   /**
    * Sets the media type.
    * @param mediaType The media type.
    */
   public void setMediaType(MediaType mediaType);

   /**
    * Returns the character set or null if not applicable.
    * @return The character set or null if not applicable.
    */
   public CharacterSet getCharacterSet();

   /**
    * Sets the character set or null if not applicable.
    * @param characterSet The character set or null if not applicable.
    */
   public void setCharacterSet(CharacterSet characterSet);

   /**
    * Returns the language or null if not applicable.
    * @return The language or null if not applicable.
    */
   public Language getLanguage();

   /**
    * Sets the language or null if not applicable.
    * @param language The language or null if not applicable.
    */
   public void setLanguage(Language language);

   /**
    * Returns the future date when this representation expire. If this information is not known, returns null.
    * @return The expiration date.
    */
   public Date getExpirationDate();

   /**
    * Sets the future date when this representation expire. If this information is not known, pass null.
    * @param expirationDate The expiration date.
    */
   public void setExpirationDate(Date expirationDate);

   /**
    * Returns the last date when this representation was modified. If this information is not known, returns
    * null.
    * @return The modification date.
    */
   public Date getModificationDate();

   /**
    * Sets the last date when this representation was modified. If this information is not known, pass null.
    * @param modificationDate The modification date.
    */
   public void setModificationDate(Date modificationDate);

   /**
    * Returns the tag.
    * @return The tag.
    */
   public Tag getTag();

   /**
    * Sets the tag.
    * @param tag The tag.
    */
   public void setTag(Tag tag);

}
