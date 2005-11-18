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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

import org.restlet.data.AbstractRepresentation;
import org.restlet.data.MediaType;

import com.noelios.restlet.util.ByteUtils;

/**
 * Representation based on a file.
 */
public class FileRepresentation extends AbstractRepresentation
{
   /** The file's path. */
   protected String filePath;

   /**
    * Constructor.
    * @param filePath		The file's path.
    * @param mediaType 		The representation's media type.
    */
   public FileRepresentation(String filePath, MediaType mediaType)
   {
      super(mediaType);
      this.filePath = filePath;
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
    * Writes the representation to a byte channel.
    * Optimizes using the file channel transferTo method.
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
         return new FileInputStream(filePath);
      }
      catch (FileNotFoundException fnfe)
      {
         throw new IOException("Couldn't get the stream. File not found");
      }
   }

   /**
    * Returns a readable byte channel.
    * If it is supported by a file a read-only instance of FileChannel is returned.
    * @return A readable byte channel.
    */
   public FileChannel getChannel() throws IOException
   {
      try
      {
         RandomAccessFile raf = new RandomAccessFile(filePath, "r");
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




