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

package com.noelios.restlet.data;

import java.io.IOException;
import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.restlet.data.MediaType;

/**
 * Representation based on a writable NIO byte channel. The write(WritableByteChannel) 
 * method needs to be overriden in subclasses.
 */
public abstract class WritableRepresentation extends ChannelRepresentation
{
   /**
    * Constructor.
    * @param mediaType The representation's media type.
    */
   public WritableRepresentation(MediaType mediaType)
   {
      super(mediaType);
   }

   /**
    * Returns a readable byte channel. If it is supported by a file a read-only instance of FileChannel is
    * returned.
    * @return A readable byte channel.
    */
   public ReadableByteChannel getChannel() throws IOException
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
               wbc.close();
            }
            catch(IOException ioe)
            {
               ioe.printStackTrace();
            }
         }
      };

      // Start the writer thread
      writer.start();
      return pipe.source();
   }

}
