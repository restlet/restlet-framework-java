/*
 * Copyright © 2005 Jérôme LOUVEL.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.restlet.data;

import java.util.Date;

/**
 * Metadata describing a representation.
 * Resources can have multiple representations called variants.
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
    * Returns the future date when this representation expire.
    * If this information is not known, returns null.
    * @return The expiration date.
    */
   public Date getExpirationDate();

   /**
    * Sets the future date when this representation expire.
    * If this information is not known, pass null.
    * @param expirationDate The expiration date.
    */
   public void setExpirationDate(Date expirationDate);

   /**
    * Returns the last date when this representation was modified.
    * If this information is not known, returns null.
    * @return The modification date.
    */
   public Date getModificationDate();

   /**
    * Sets the last date when this representation was modified.
    * If this information is not known, pass null.
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




