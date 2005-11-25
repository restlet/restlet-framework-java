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
import java.nio.channels.ReadableByteChannel;

import org.restlet.data.MediaType;

/**
 * Representation based on a readable byte channel.
 */
public class ReadableRepresentation extends ChannelRepresentation
{
   /** The representation's input stream. */
   protected ReadableByteChannel readableChannel;

   /**
    * Constructor.
    * @param readableChannel The representation's channel.
    * @param mediaType The representation's media type.
    */
   public ReadableRepresentation(ReadableByteChannel readableChannel, MediaType mediaType)
   {
      super(mediaType);
      this.readableChannel = readableChannel;
   }

   /**
    * Returns a readable byte channel. If it is supported by a file a read-only instance of FileChannel is
    * returned.
    * @return A readable byte channel.
    */
   public ReadableByteChannel getChannel() throws IOException
   {
      return readableChannel;
   }

}
