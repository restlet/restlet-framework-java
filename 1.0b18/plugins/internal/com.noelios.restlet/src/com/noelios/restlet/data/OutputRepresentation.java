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
import java.io.InputStream;
import java.io.OutputStream;

import org.restlet.data.MediaType;

import com.noelios.restlet.util.ByteUtils;

/**
 * Representation based on a BIO output stream. The write(OutputStream) 
 * method needs to be overriden in subclasses.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
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
    * Constructor.
    * @param mediaType The representation's mediaType.
    * @param expectedSize The expected input stream size.
    */
   public OutputRepresentation(MediaType mediaType, long expectedSize)
   {
      super(mediaType);
      setSize(expectedSize);
   }
   
   /**
    * Writes the representation to a byte stream.
    * @param outputStream The output stream.
    */
   public abstract void write(OutputStream outputStream) throws IOException;

   /**
    * Returns a stream with the representation's content. 
    * Internally, it uses a writer thread and a pipe stream.
    * @return A stream with the representation's content.
    */
   public InputStream getStream() throws IOException
   {
   	return ByteUtils.getStream(this);
   }

}
