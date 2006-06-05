/*
 * Copyright 2005-2006 Noelios Consulting.
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

package com.noelios.restlet.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Byte manipulation utilities.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ByteUtils
{
   /**
    * Writes an input stream to an output stream.
    * @param inputStream The input stream.
    * @param outputStream The output stream.
    * @throws IOException
    */
   public static void write(InputStream inputStream, OutputStream outputStream) throws IOException
   {
      InputStream is = (inputStream instanceof BufferedInputStream) ? inputStream : 
      	new BufferedInputStream(inputStream);
      OutputStream os = (outputStream instanceof BufferedOutputStream) ? outputStream : 
      	new BufferedOutputStream(outputStream);
      int nextByte = is.read();
      while(nextByte != -1)
      {
         os.write(nextByte);
         nextByte = is.read();
      }
      is.close();
   }

   /**
    * Writes a readable channel to a writable channel.
    * @param readableChannel The readable channel.
    * @param writableChannel The writable channel.
    * @throws IOException
    */
   public static void write(ReadableByteChannel readableChannel, WritableByteChannel writableChannel)
         throws IOException
   {
      write(Channels.newInputStream(readableChannel), Channels.newOutputStream(writableChannel));
   }

   /**
    * Converts an input stream to a string.
    * @param inputStream The input stream.
    * @return The converted string.
    */
   public static String toString(InputStream inputStream)
   {
      String result = null;

      try
      {
         StringBuilder sb = new StringBuilder();
         InputStream is = new BufferedInputStream(inputStream);
         int nextByte = is.read();
         while(nextByte != -1)
         {
            sb.append((char)nextByte);
            nextByte = is.read();
         }
         is.close();
         result = sb.toString();
      }
      catch(Exception e)
      {
         // Return an empty string
      }

      return result;
   }

}
