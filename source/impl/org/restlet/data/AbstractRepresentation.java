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

/**
 * Abstract resource representation.
 */
public abstract class AbstractRepresentation extends DefaultRepresentationMetadata implements Representation
{
   /**
    * Constructor.
    * @param mediaType The media type.
    */
   public AbstractRepresentation(MediaType mediaType)
   {
      super(mediaType);
   }

   /**
    * Returns the metadata.
    * @return The metadata.
    */
   public RepresentationMetadata getMetadata()
   {
      return this;
   }
   
   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Resource representation";
   }

   /**
    * Returns the metadata name like "text/html" or "compress" or "iso-8851-1".
    * @return The metadata name like "text/html" or "compress" or "iso-8851-1".
    */
   public String getName()
   {
      return "representation";
   }

}




