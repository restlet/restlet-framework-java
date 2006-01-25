/*
 * Copyright 2005-2006 Jérôme LOUVEL
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.restlet.data.MediaType;

import com.noelios.restlet.util.PipeStream;

/**
 * Representation based on an output stream.<br/>
 * The write(OutputStream) method needs to be overriden in subclasses.
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
