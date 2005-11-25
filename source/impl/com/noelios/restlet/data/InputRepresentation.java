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

package com.noelios.restlet.data;

import java.io.IOException;
import java.io.InputStream;

import org.restlet.data.MediaType;

/**
 * Representation based on an input stream.
 */
public class InputRepresentation extends StreamRepresentation
{
   /** The representation's stream. */
   protected InputStream inputStream;

   /**
    * Constructor.
    * @param inputStream The representation's stream.
    * @param mediaType The representation's media type.
    */
   public InputRepresentation(InputStream inputStream, MediaType mediaType)
   {
      super(mediaType);
      this.inputStream = inputStream;
   }

   /**
    * Returns a stream with the representation's content.
    * @return A stream with the representation's content.
    */
   public InputStream getStream() throws IOException
   {
      return inputStream;
   }

}
