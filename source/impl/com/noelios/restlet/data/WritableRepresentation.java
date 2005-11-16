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
import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.restlet.RestletException;
import org.restlet.data.MediaType;

/**
 * Representation based on a writable byte channel.
 * The write(WritableByteChannel) method needs to be overriden in subclasses.
 */
public abstract class WritableRepresentation extends ChannelRepresentation
{
   /**
    * Constructor.
    * @param mediaType  The representation's media type.
    */
   public WritableRepresentation(MediaType mediaType)
   {
      super(mediaType);
   }

   /**
    * Returns a readable byte channel.
    * If it is supported by a file a read-only instance of FileChannel is returned.
    * @return A readable byte channel.
    */
   public ReadableByteChannel getChannel() throws RestletException
   {
      try
      {
         final Pipe pipe = Pipe.open();

         // Create a thread that will handle the task of continuously
         // writing the representation into the input side of the pipe
         Thread writer = new Thread()
         {
            public void run()
            {
               try
               {
                  WritableByteChannel wbc = pipe.sink();
                  write(wbc);
                  
                  try
                  {
                     wbc.close();
                  }
                  catch (IOException ioe)
                  {
                     throw new RestletException("Error while closing the output stream", ioe);
                  }
               }
               catch (RestletException re)
               {
                  re.printStackTrace();
               }
            }
         };
         
         // Start the writer thread
         writer.start();
         return pipe.source();
      }
      catch(IOException ioe)
      {
         throw new RestletException(ioe);
      }
   }
   
}
