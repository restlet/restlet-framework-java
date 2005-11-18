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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import org.restlet.data.AbstractRepresentation;
import org.restlet.data.MediaType;

import com.noelios.restlet.util.ByteUtils;

/**
 * Representation based on a byte channel.
 */
public abstract class ChannelRepresentation extends AbstractRepresentation
{
   /**
    * Constructor.
    * @param mediaType  The representation's media type.
    */
   public ChannelRepresentation(MediaType mediaType)
   {
      super(mediaType);
   }

   /**
    * Writes the representation to a byte stream.
    * @param outputStream The output stream.
    */
   public void write(OutputStream outputStream) throws IOException
   {
      write(Channels.newChannel(outputStream));
   }

   /**
    * Writes the representation to a byte channel.
    * @param writableChannel A writable byte channel.
    */
   public void write(WritableByteChannel writableChannel) throws IOException
   {
      ByteUtils.write(getChannel(), writableChannel);
   }

   /**
    * Returns a stream with the representation's content.
    * @return A stream with the representation's content.
    */
   public InputStream getStream() throws IOException
   {
      return Channels.newInputStream(getChannel());
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
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         write(baos);
         result = baos.toString();
      }
      catch (Exception ioe)
      {
         // Return an empty string
      }

      return result;
   }
   
}
