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
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.restlet.Resource;
import org.restlet.data.Encodings;
import org.restlet.data.MediaTypes;
import org.restlet.data.Representation;
import org.restlet.data.RepresentationMetadata;

/**
 * Representation that compresses a wrapped representation using the GZIP algorithm. 
 * @author Lars Heuer (heuer[at]semagia.com) <a href="http://semagia.com/">Semagia</a>
 * @author Jerome Louvel (contact[at]noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class GzipRepresentation extends OutputRepresentation
{
   /** Wrapped representation. */
   protected Representation wrappedRepresentation;

   /**
    * Constructor.
    * @param wrappedRepresentation The wrapped representation.
    */
   public GzipRepresentation(Representation wrappedRepresentation)
   {
   	super(MediaTypes.APPLICATION_ZIP);
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
   	RepresentationMetadata result = new RepresentationMetadata(this.wrappedRepresentation.getMetadata());
   	result.setEncoding(Encodings.GZIP);
   	return result;
   }
   
   /**
    * Returns the name of this REST element.
    * @return The name of this REST element.
    */
   public String getName()
   {
   	return this.wrappedRepresentation.getName();
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
   	return this.wrappedRepresentation.getDescription();
   }

   /**
    * Writes the representation to a byte stream.
    * @param outputStream The output stream.
    */
	public void write(OutputStream out) throws IOException
	{
		GZIPOutputStream gzipOutput = new GZIPOutputStream(out);
		this.wrappedRepresentation.write(gzipOutput);
		gzipOutput.finish();
	}

}
