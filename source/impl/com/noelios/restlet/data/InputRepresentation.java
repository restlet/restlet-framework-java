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

import java.io.IOException;
import java.io.InputStream;

import org.restlet.data.MediaType;

/**
 * Representation based on an input stream.
 */
public class InputRepresentation extends StreamRepresentation
{
   /** The representation's stream. */
   protected InputStream inputStream;

   /**
    * Constructor.
    * @param inputStream The representation's stream.
    * @param mediaType The representation's media type.
    */
   public InputRepresentation(InputStream inputStream, MediaType mediaType)
   {
      super(mediaType);
      this.inputStream = inputStream;
   }

   /**
    * Returns a stream with the representation's content.
    * @return A stream with the representation's content.
    */
   public InputStream getStream() throws IOException
   {
      return inputStream;
   }

}
