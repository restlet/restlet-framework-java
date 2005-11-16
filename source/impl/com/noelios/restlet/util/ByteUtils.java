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

package com.noelios.restlet.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.restlet.RestletException;

/**
 * Byte manipulation utilities.
 */
public class ByteUtils
{
   /**
    * Writes an input stream to an output stream.
    * @param inputStream   The input stream.
    * @param outputStream  The output stream.
    */
   public static void write(InputStream inputStream, OutputStream outputStream) throws RestletException
   {
      try
      {
         InputStream is = (inputStream instanceof BufferedInputStream) ? inputStream : new BufferedInputStream(inputStream);
         int nextByte = is.read();
         while (nextByte != -1)
         {
            outputStream.write(nextByte);
            nextByte = is.read();
         }
         is.close();
      }
      catch (IOException ioe)
      {
         throw new RestletException("Error while creating the file input stream", ioe);
      }
   }
   /**
    * Writes a readable channel to a writable channel.
    * @param readableChannel  The readable channel.
    * @param writableChannel  The writable channel.
    */
   public static void write(ReadableByteChannel readableChannel, WritableByteChannel writableChannel) throws RestletException
   {
      write(Channels.newInputStream(readableChannel), Channels.newOutputStream(writableChannel));
   }
   
   /**
    * Converts an input stream to a string.
    * @param inputStream   The input stream.
    * @return              The converted string.
    */
   public static String toString(InputStream inputStream)
   {
      String result = null;

      try
      {
         StringBuilder sb = new StringBuilder();
         InputStream is = new BufferedInputStream(inputStream);
         int nextByte = is.read();
         while (nextByte != -1)
         {
            sb.append((char)nextByte);
            nextByte = is.read();
         }
         is.close();
         result = sb.toString();
      }
      catch (Exception e)
      {
         // Return an empty string
      }

      return result;
   }
  
}
