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
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;

import org.restlet.data.Encoding;
import org.restlet.data.Representation;
import org.restlet.util.WrapperRepresentation;

import com.noelios.restlet.util.ByteUtils;

/**
 * Representation that decodes a wrapped representation if its encoding is supported. 
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class DecoderRepresentation extends WrapperRepresentation
{
	/** Indicates if the decoding can happen. */
	private boolean canDecode;
	
   /**
    * Constructor.
    * @param wrappedRepresentation The wrapped representation.
    */
   public DecoderRepresentation(Representation wrappedRepresentation)
   {
   	super(wrappedRepresentation);
   	this.canDecode = getSupportedEncodings().contains(wrappedRepresentation.getEncoding());
   }
   
   /**
    * Indicates if the decoding can happen.
    * @return True if the decoding can happen.
    */
   public boolean canDecode()
   {
   	return this.canDecode;
   }
   
   /**
    * Returns the encoding or null if identity encoding applies.
    * @return The encoding or null if identity encoding applies.
    */
   public Encoding getEncoding()
   {
   	if(canDecode())
   	{
   		return null;
   	}
   	else
   	{
   		return getWrappedRepresentation().getEncoding();
   	}
   }
   
   /**
    * Sets the encoding or null if identity encoding applies.
    * @param encoding The encoding or null if identity encoding applies.
    */
   public void setEncoding(Encoding encoding)
   {
   	throw new IllegalArgumentException("The encoding can't be changed for a decoder representation");
   }

   /**
    * Returns a readable byte channel. If it is supported by a file a read-only instance of 
    * FileChannel is returned.
    * @return A readable byte channel.
    */
   public ReadableByteChannel getChannel() throws IOException
   {
		if(canDecode())
		{
			return ByteUtils.getChannel(getStream());
		}
		else
		{
			return getWrappedRepresentation().getChannel();
		}
   }

   /**
    * Returns a stream with the representation's content.
    * @return A stream with the representation's content.
    */
   public InputStream getStream() throws IOException
   {
		InputStream result = null;

		if(canDecode())
		{
			Encoding we = getWrappedRepresentation().getEncoding();
			
			if(we.equals(Encoding.GZIP))
			{
				result = new GZIPInputStream(getWrappedRepresentation().getStream());
			}
			else if(we.equals(Encoding.DEFLATE))
			{
				result = new InflaterInputStream(getWrappedRepresentation().getStream());
			}
			else if(we.equals(Encoding.ZIP))
			{
				result = new ZipInputStream(getWrappedRepresentation().getStream());
			}
			else if(we.equals(Encoding.IDENTITY))
			{
				throw new IOException("Decoder unecessary for identity decoding");
			}
		}
		
		return result;
	}
   
   /**
    * Writes the representation to a byte channel.
    * @param writableChannel A writable byte channel.
    */
   public void write(WritableByteChannel writableChannel) throws IOException
   {
		if(canDecode())
		{
			write(ByteUtils.getStream(writableChannel));
		}
		else
		{
			getWrappedRepresentation().write(writableChannel);
		}
   }
   
   /**
    * Writes the representation to a byte stream.
    * @param outputStream The output stream.
    */
   public void write(OutputStream outputStream) throws IOException
   {
		if(canDecode())
		{
			ByteUtils.write(getStream(), outputStream);
		}
		else
		{
			getWrappedRepresentation().write(outputStream);
		}
   }
   
   /**
    * Converts the representation to a string.
    * @return The representation as a string.
    */
   public String toString()
   {
		String result = null;

		if(canDecode())
		{
	      try
	      {
	         result = ByteUtils.toString(getStream());
	      }
	      catch(IOException ioe)
	      {
	      }
		}
		else
		{
			result = getWrappedRepresentation().toString();
		}

      return result;
   }

	/**
	 * Returns the list of supported encodings.
	 * @return The list of supported encodings.
	 */
	public static List<Encoding> getSupportedEncodings()
	{
		return Arrays.<Encoding>asList(Encoding.GZIP, Encoding.DEFLATE, Encoding.ZIP, Encoding.IDENTITY);
	}
}
