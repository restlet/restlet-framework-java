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

package org.restlet.data;

import java.io.ByteArrayOutputStream;

import org.restlet.Resource;

/**
 * Abstract resource representation.
 */
public abstract class AbstractRepresentation extends RepresentationMetadata implements Representation
{
	/** 
	 * The expected size. Dynamic representations can have any size, but sometimes we can know in 
	 * advance the expected size. If this expected size is specified by the user, it has a higher priority
	 * than any size that can be guessed by the representation (like a file size).
	 */
	protected long expectedSize;

	/**
    * The represented resource, if available.
    */
   protected Resource resource;

	/**
    * Constructor.
    * @param mediaType The representation's media type.
    */
   public AbstractRepresentation(MediaType mediaType)
   {
      this(mediaType, UNKNOWN_SIZE);
   }

	/**
    * Constructor.
    * @param mediaType The representation's media type.
    * @param expectedSize The expected stream size. 
    */
   public AbstractRepresentation(MediaType mediaType, long expectedSize)
   {
      super(mediaType);
      this.expectedSize = expectedSize;
   }

   /**
    * Returns the size in bytes if known, UNKNOWN_SIZE (-1) otherwise.
    * @return The size in bytes if known, UNKNOWN_SIZE (-1) otherwise.
    */
   public long getSize()
   {
      return this.expectedSize;
   }

   /**
    * Sets the expected size in bytes if known, -1 otherwise.
    * @param expectedSize The expected size in bytes if known, -1 otherwise.
    */
   public void setSize(long expectedSize)
   {
      this.expectedSize = expectedSize;
   }

   /**
    * Returns the represented resource if available.
    * @return The represented resource if available.
    */
   public Resource getResource()
   {
      return this.resource;
   }

   /**
    * Sets the represented resource.
    * @param resource The represented resource.
    */
   public void setResource(Resource resource)
   {
      this.resource = resource;
   }

   /**
    * Returns the metadata.
    * @return The metadata.
    */
   public RepresentationMetadata getMetadata()
   {
      return this;
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Resource representation";
   }

   /**
    * Returns the metadata name like "text/html" or "compress" or "iso-8851-1".
    * @return The metadata name like "text/html" or "compress" or "iso-8851-1".
    */
   public String getName()
   {
      return "representation";
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
