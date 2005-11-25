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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Pipe stream that pipes output streams into input streams. Implementation based on a shared synchronized
 * queue.
 */
public class PipeStream
{
   /** The supporting synchronized queue. */
   private final BlockingQueue<Integer> queue;

   /** Constructor. */
   public PipeStream()
   {
      this.queue = new ArrayBlockingQueue<Integer>(1024);
   }

   /**
    * Returns a new output stream that can write into the pipe.
    * @return A new output stream that can write into the pipe.
    */
   public OutputStream getOutputStream()
   {
      return new OutputStream()
      {
         public void write(int b) throws IOException
         {
            try
            {
               queue.put(new Integer(b));
            }
            catch(InterruptedException ie)
            {
               throw new IOException("Interruption occurred while writing in the queue");
            }
         }
      };
   }

   /**
    * Returns a new input stream that can read from the pipe.
    * @return A new input stream that can read from the pipe.
    */
   public InputStream getInputStream()
   {
      return new InputStream()
      {
         private boolean endReached = false;

         public int read() throws IOException
         {
            try
            {
               if(endReached) return -1;
               int value = ((Integer)queue.take()).intValue();
               endReached = (value == -1);
               return value;
            }
            catch(InterruptedException ie)
            {
               throw new IOException("Interruption occurred while writing in the queue");
            }
         }
      };
   }

}
