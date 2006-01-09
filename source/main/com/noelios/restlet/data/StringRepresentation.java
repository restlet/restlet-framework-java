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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.restlet.data.MediaType;

/**
 * Representation based on a simple string.
 */
public class StringRepresentation extends StreamRepresentation
{
   /** The represented string. */
   protected String value;

   /**
    * Constructor.
    * @param value The represented string.
    * @param mediaType The representation's media type.
    */
   public StringRepresentation(String value, MediaType mediaType)
   {
      super(mediaType);
      this.value = value;
   }

   /**
    * Writes the datum as a stream of bytes.
    * @param outputStream The stream to use when writing.
    */
   public void write(OutputStream outputStream) throws IOException
   {
      outputStream.write(value.getBytes());
   }

   /**
    * Returns the size in bytes if known, -1 otherwise.
    * @return The size in bytes if known, -1 otherwise.
    */
   public long getSize()
   {
      if(value == null)
      {
         return -1L;
      }
      else
      {
         return (long)value.length();
      }
   }

   /**
    * Returns an inputstream that can read the representation's content.
    * @return An inputstream that can read the representation's content.
    */
   public InputStream getStream() throws IOException
   {
      return new ByteArrayInputStream(value.getBytes());
   }

   /**
    * Converts the representation to a string.
    * @return The representation as a string.
    */
   public String toString()
   {
      return this.value;
   }

}
