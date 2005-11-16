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

import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.restlet.RestletException;
import org.restlet.data.MediaType;
import org.restlet.data.AbstractRepresentation;

import com.noelios.restlet.util.ByteUtils;

/**
 * Representation based on a stream.
 */
public abstract class StreamRepresentation extends AbstractRepresentation
{
   /**
    * Constructor.
    * @param mediaType     The representation's media type.
    */
   public StreamRepresentation(MediaType mediaType)
   {
      super(mediaType);
   }

   /**
    * Writes the representation to a byte stream.
    * @param outputStream The output stream.
    */
   public void write(OutputStream outputStream) throws RestletException
   {
      ByteUtils.write(getStream(), outputStream);
   }

   /**
    * Writes the representation to a byte channel.
    * @param writableChannel A writable byte channel.
    */
   public void write(WritableByteChannel writableChannel) throws RestletException
   {
      write(Channels.newOutputStream(writableChannel));
   }

   /**
    * Returns a readable byte channel.
    * If it is supported by a file a read-only instance of FileChannel is returned.
    * @return A readable byte channel.
    */
   public ReadableByteChannel getChannel() throws RestletException
   {
      return Channels.newChannel(getStream());
   }

   /**
    * Converts the representation to a string.
    * @return The representation as a string.
    */
   public String toString()
   {
      String result = null;
      
      try
      {
         result = ByteUtils.toString(getStream());
      }
      catch(RestletException re)
      {
         re.printStackTrace();
      }

      return result;
   }

}
