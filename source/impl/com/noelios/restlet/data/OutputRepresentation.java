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

import org.restlet.data.MediaType;

import com.noelios.restlet.util.PipeStream;

/**
 * Representation based on an output stream. The write(OutputStream) method needs to be overriden in
 * subclasses.
 */
public abstract class OutputRepresentation extends StreamRepresentation
{
   /**
    * Constructor.
    * @param mediaType The representation's mediaType.
    */
   public OutputRepresentation(MediaType mediaType)
   {
      super(mediaType);
   }

   /**
    * Writes the representation to a byte stream.
    * @param outputStream The output stream.
    */
   public abstract void write(OutputStream outputStream) throws IOException;

   /**
    * Returns a stream with the representation's content. Internally, it uses a writer thread and a pipe
    * stream.
    * @return A stream with the representation's content.
    */
   public InputStream getStream() throws IOException
   {
      final PipeStream pipe = new PipeStream();

      // Create a thread that will handle the task of continuously
      // writing the representation into the input side of the pipe
      Thread writer = new Thread()
      {
         public void run()
         {
            try
            {
               OutputStream os = pipe.getOutputStream();
               write(os);
               os.write(-1);
               os.close();
            }
            catch(IOException ioe)
            {
               ioe.printStackTrace();
            }
         }
      };

      // Start the writer thread
      writer.start();
      return pipe.getInputStream();
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
      catch(Exception ioe)
      {
         // Return an empty string
      }

      return result;
   }

}
