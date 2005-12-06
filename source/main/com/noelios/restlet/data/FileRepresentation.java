/*
 * Copyright 2005 Jérôme LOUVEL
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

package com.noelios.restlet.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Date;

import org.restlet.data.AbstractRepresentation;
import org.restlet.data.MediaType;

import com.noelios.restlet.util.ByteUtils;

/**
 * Representation based on a file.
 */
public class FileRepresentation extends AbstractRepresentation
{
   /** The file descriptor. */
   protected File file;

   /**
    * Constructor.
    * @param filePath The file's path.
    * @param mediaType The representation's media type.
    */
   public FileRepresentation(String filePath, MediaType mediaType)
   {
      super(mediaType);
      this.file = new File(filePath);
      this.modificationDate = new Date(file.lastModified());
      this.mediaType = mediaType;
   }

   /**
    * Writes the representation to a byte stream.
    * @param outputStream The output stream.
    */
   public void write(OutputStream outputStream) throws IOException
   {
      ByteUtils.write(getStream(), outputStream);
   }

   /**
    * Writes the representation to a byte channel. Optimizes using the file channel transferTo method.
    * @param writableChannel A writable byte channel.
    */
   public void write(WritableByteChannel writableChannel) throws IOException
   {
      FileChannel fc = getChannel();
      long position = 0;
      long count = fc.size();
      long written = 0;

      while(count > 0)
      {
         written = fc.transferTo(position, count, writableChannel);
         position += written;
         count -= written;
      }
   }

   /**
    * Returns a stream with the representation's content.
    * @return A stream with the representation's content.
    */
   public FileInputStream getStream() throws IOException
   {
      try
      {
         return new FileInputStream(file);
      }
      catch(FileNotFoundException fnfe)
      {
         throw new IOException("Couldn't get the stream. File not found");
      }
   }

   /**
    * Returns a readable byte channel. If it is supported by a file a read-only instance of FileChannel is
    * returned.
    * @return A readable byte channel.
    */
   public FileChannel getChannel() throws IOException
   {
      try
      {
         RandomAccessFile raf = new RandomAccessFile(file, "r");
         return raf.getChannel();
      }
      catch(FileNotFoundException fnfe)
      {
         throw new IOException("Couldn't get the channel. File not found");
      }
   }

   /**
    * Converts the representation to a string.
    * @return The representation as a string.
    */
   public String toString()
   {
      try
      {
         return ByteUtils.toString(getStream());
      }
      catch(IOException ioe)
      {
         ioe.printStackTrace();
         return null;
      }
   }

}
