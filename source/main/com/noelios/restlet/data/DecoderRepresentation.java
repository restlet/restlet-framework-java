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
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;

import org.restlet.Resource;
import org.restlet.data.Encoding;
import org.restlet.data.Encodings;
import org.restlet.data.Representation;
import org.restlet.data.RepresentationMetadata;

/**
 * Representation that decodes a wrapped representation. 
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class DecoderRepresentation extends InputRepresentation
{
   /** Wrapped representation. */
   protected Representation wrappedRepresentation;

   /**
    * Constructor.
    * @param wrappedRepresentation The wrapped representation.
    */
   public DecoderRepresentation(Representation wrappedRepresentation)
   {
   	super(null, null);
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
   	result.setEncoding(null);
   	return result;
   }

   /**
    * Returns a stream with the representation's content.
    * @return A stream with the representation's content.
    */
   public InputStream getStream() throws IOException
   {
		InputStream result = null;

		if(getEncoding().equals(Encodings.GZIP))
		{
			result = new GZIPInputStream(this.wrappedRepresentation.getStream());
		}
		else if(getEncoding().equals(Encodings.DEFLATE))
		{
			result = new InflaterInputStream(this.wrappedRepresentation.getStream());
		}
		else if(getEncoding().equals(Encodings.ZIP))
		{
			result = new ZipInputStream(this.wrappedRepresentation.getStream());
		}
		else if(getEncoding().equals(Encodings.IDENTITY))
		{
			throw new IOException("Decoder unecessary for identity decoding");
		}
		else
		{
			throw new IOException("Unsupported decoding");
		}

		return result;
	}

	/**
	 * Returns the list of supported encodings.
	 * @return The list of supported encodings.
	 */
	public static List<Encoding> getSupportedEncodings()
	{
		List<Encoding> result = new ArrayList<Encoding>();
		result.add(Encodings.GZIP);
		result.add(Encodings.DEFLATE);
		result.add(Encodings.ZIP);
		result.add(Encodings.IDENTITY);
		return result;
	}
}
