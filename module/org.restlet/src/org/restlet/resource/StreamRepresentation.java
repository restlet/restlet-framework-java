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
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.restlet.data.MediaType;
import org.restlet.util.ByteUtils;

/**
 * Representation based on a BIO stream.
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class StreamRepresentation extends Representation
{
	/**
	 * Constructor.
	 * @param mediaType The media type.
	 */
	public StreamRepresentation(MediaType mediaType)
	{
		super(mediaType);
	}

	/**
	 * Returns a readable byte channel. If it is supported by a file a read-only instance of 
	 * FileChannel is returned.
	 * @return A readable byte channel.
	 */
	public ReadableByteChannel getChannel() throws IOException
	{
		return ByteUtils.getChannel(getStream());
	}

	/**
	 * Writes the representation to a byte channel.
	 * @param writableChannel A writable byte channel.
	 */
	public void write(WritableByteChannel writableChannel) throws IOException
	{
		write(ByteUtils.getStream(writableChannel));
	}

}
