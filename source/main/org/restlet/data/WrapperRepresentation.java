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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.restlet.Resource;

/**
 * Representation wrapper. Useful for application developer who need to enrich the representation 
 * with application related properties and behavior.
 * @see <a href="http://c2.com/cgi/wiki?DecoratorPattern">The decorator (aka wrapper) pattern</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class WrapperRepresentation implements Representation
{
   /** Wrapped representation. */
   protected Representation wrappedRepresentation;

   /**
    * Constructor.
    * @param wrappedRepresentation The wrapped representation.
    */
   public WrapperRepresentation(Representation wrappedRepresentation)
   {
      this.wrappedRepresentation = wrappedRepresentation;
   }
	
   /**
    * Returns the represented resource if available.
    * @return The represented resource if available.
    */
   public Resource getResource()
   {
   	return this.wrappedRepresentation.getResource();
   }

   /**
    * Sets the represented resource.
    * @param resource The represented resource.
    */
   public void setResource(Resource resource)
   {
   	this.wrappedRepresentation.setResource(resource);
   }

   /**
    * Returns the metadata.
    * @return The metadata.
    */
   public RepresentationMetadata getMetadata()
   {
   	return this.wrappedRepresentation.getMetadata();
   }

   /**
    * Returns a channel with the representation's content.<br/>
    * If it is supported by a file, a read-only instance of FileChannel is returned.
    * @return A channel with the representation's content.
    * @throws IOException
    */
   public ReadableByteChannel getChannel() throws IOException
   {
   	return this.wrappedRepresentation.getChannel();
   }

   /**
    * Returns a stream with the representation's content.
    * @return A stream with the representation's content.
    * @throws IOException
    */
   public InputStream getStream() throws IOException
   {
   	return this.wrappedRepresentation.getStream();
   }

   /**
    * Writes the representation to a byte channel.
    * @param writableChannel A writable byte channel.
    * @throws IOException
    */
   public void write(WritableByteChannel writableChannel) throws IOException
   {
   	this.wrappedRepresentation.write(writableChannel);
   }

   /**
    * Writes the representation to a byte stream.
    * @param outputStream The output stream.
    * @throws IOException
    */
   public void write(OutputStream outputStream) throws IOException
   {
   	this.wrappedRepresentation.write(outputStream);
   }

   /**
    * Returns the size in bytes if known, -1 otherwise.
    * @return The size in bytes if known, -1 otherwise.
    */
   public long getSize()
   {
   	return this.wrappedRepresentation.getSize();
   }

   /**
    * Sets the expected size in bytes if known, -1 otherwise.
    * @param expectedSize The expected size in bytes if known, -1 otherwise.
    */
   public void setSize(long expectedSize)
   {
   	this.wrappedRepresentation.setSize(expectedSize);
   }

   /**
    * Converts the representation to a string.
    * @return The representation as a string.
    */
   public String toString()
   {
   	return this.wrappedRepresentation.toString();
   }

}
