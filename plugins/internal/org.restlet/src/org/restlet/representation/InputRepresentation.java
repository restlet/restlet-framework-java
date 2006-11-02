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

package org.restlet.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.restlet.data.MediaType;
import org.restlet.util.ByteUtils;

/**
 * Transient representation based on a BIO input stream.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class InputRepresentation extends StreamRepresentation
{
	/** The representation's stream. */
	private InputStream inputStream;

	/**
	 * Constructor.
	 * @param inputStream The representation's stream.
	 * @param mediaType The representation's media type.
	 */
	public InputRepresentation(InputStream inputStream, MediaType mediaType)
	{
		this(inputStream, mediaType, UNKNOWN_SIZE);
	}

	/**
	 * Constructor.
	 * @param inputStream The representation's stream.
	 * @param mediaType The representation's media type.
	 * @param expectedSize The expected input stream size.
	 */
	public InputRepresentation(InputStream inputStream, MediaType mediaType,
			long expectedSize)
	{
		super(mediaType);
		setSize(expectedSize);
		this.inputStream = inputStream;
		setAvailable(inputStream != null);
		setTransient(true);
	}

	/**
	 * Returns a stream with the representation's content.
	 * @return A stream with the representation's content.
	 */
	public synchronized InputStream getStream() throws IOException
	{
		InputStream result = this.inputStream;
		this.inputStream = null;
		setAvailable(false);
		return result;
	}

	/**
	 * Writes the representation to a byte stream.
	 * @param outputStream The output stream.
	 */
	public void write(OutputStream outputStream) throws IOException
	{
		ByteUtils.write(getStream(), outputStream);
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
			result = ByteUtils.toString(getStream());
		}
		catch (IOException ioe)
		{
		}

		return result;
	}

}
