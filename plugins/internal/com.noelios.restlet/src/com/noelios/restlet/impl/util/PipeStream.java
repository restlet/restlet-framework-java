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

package com.noelios.restlet.impl.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Pipe stream that pipes output streams into input streams. Implementation based on a shared synchronized
 * queue.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
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
