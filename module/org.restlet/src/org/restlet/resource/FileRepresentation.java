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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Date;

import org.restlet.data.MediaType;
import org.restlet.util.ByteUtils;

/**
 * Representation based on a file.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class FileRepresentation extends Representation {
	/** The file descriptor. */
	private File file;

	/**
	 * Constructor.
	 * 
	 * @param file
	 *            The represented file.
	 * @param mediaType
	 *            The representation's media type.
	 * @param timeToLive
	 *            The time to live before it expires (in seconds).
	 */
	public FileRepresentation(File file, MediaType mediaType, int timeToLive) {
		super(mediaType);
		this.file = file;
		setModificationDate(new Date(file.lastModified()));
		setExpirationDate(new Date(System.currentTimeMillis()
				+ (1000L * timeToLive)));
		setMediaType(mediaType);
	}

	/**
	 * Constructor.
	 * 
	 * @param filePath
	 *            The path of the represented file.
	 * @param mediaType
	 *            The representation's media type.
	 * @param timeToLive
	 *            The time to live before it expires (in seconds).
	 */
	public FileRepresentation(String filePath, MediaType mediaType,
			int timeToLive) {
		this(new File(filePath), mediaType, timeToLive);
	}

	/**
	 * Returns a readable byte channel. If it is supported by a file a read-only
	 * instance of FileChannel is returned.
	 * 
	 * @return A readable byte channel.
	 */
	public FileChannel getChannel() throws IOException {
		try {
			// Alternative method
			// return new FileInputStream(getFile()).getChannel();

			RandomAccessFile raf = new RandomAccessFile(file, "r");
			return raf.getChannel();
		} catch (FileNotFoundException fnfe) {
			throw new IOException("Couldn't get the channel. File not found");
		}
	}

	/**
	 * Returns the size in bytes if known, UNKNOWN_SIZE (-1) otherwise.
	 * 
	 * @return The size in bytes if known, UNKNOWN_SIZE (-1) otherwise.
	 */
	public long getSize() {
		if (super.getSize() != UNKNOWN_SIZE) {
			return super.getSize();
		} else {
			return this.file.length();
		}
	}

	/**
	 * Returns a stream with the representation's content.
	 * 
	 * @return A stream with the representation's content.
	 */
	public FileInputStream getStream() throws IOException {
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException fnfe) {
			throw new IOException("Couldn't get the stream. File not found");
		}
	}

	/**
	 * Converts the representation to a string value. Be careful when using this
	 * method as the conversion of large content to a string fully stored in
	 * memory can result in OutOfMemoryErrors being thrown.
	 * 
	 * @return The representation as a string value.
	 */
	public String getValue() throws IOException {
		return ByteUtils.toString(getStream());
	}

	/**
	 * Writes the representation to a byte stream.
	 * 
	 * @param outputStream
	 *            The output stream.
	 */
	public void write(OutputStream outputStream) throws IOException {
		ByteUtils.write(getStream(), outputStream);
	}

	/**
	 * Writes the representation to a byte channel. Optimizes using the file
	 * channel transferTo method.
	 * 
	 * @param writableChannel
	 *            A writable byte channel.
	 */
	public void write(WritableByteChannel writableChannel) throws IOException {
		FileChannel fc = getChannel();
		long position = 0;
		long count = fc.size();
		long written = 0;

		while (count > 0) {
			written = fc.transferTo(position, count, writableChannel);
			position += written;
			count -= written;
		}
	}

}
