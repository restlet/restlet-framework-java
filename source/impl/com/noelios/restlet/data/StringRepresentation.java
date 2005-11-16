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


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.restlet.RestletException;
import org.restlet.data.MediaType;

/**
 * Representation based on a simple string.
 */
public class StringRepresentation extends StreamRepresentation
{
   /** The represented string. */
   protected String value;

   /**
    * Constructor.
    * @param value 		The represented string.
    * @param mediaType	The representation's media type.
    */
   public StringRepresentation(String value, MediaType mediaType)
   {
      super(mediaType);
      this.value = value;
   }

   /**
    * Writes the datum as a stream of bytes.
    * @param outputStream The stream to use when writing.
    */
   public void write(OutputStream outputStream) throws RestletException
   {
      try
      {
         outputStream.write(value.getBytes());
      }
      catch (IOException ioe)
      {
         throw new RestletException("Unexpected I/O exception", "Please contact the administrator");

      }
   }

   /**
    * Returns an inputstream that can read the representation's content.
    * @return An inputstream that can read the representation's content.
    */
   public InputStream getStream() throws RestletException
   {
      return new ByteArrayInputStream(value.getBytes());
   }

   /**
    * Converts the representation to a string.
    * @return The representation as a string.
    */
   public String toString()
   {
      return this.value;
   }

}




