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

package org.restlet.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.restlet.resource.Representation;

/**
 * Byte manipulation utilities.
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ByteUtils
{
	/**
	 * Returns a readable byte channel based on a given inputstream.
	 * If it is supported by a file a read-only instance of FileChannel is returned.
	 * @param inputStream The input stream to convert.
	 * @return A readable byte channel.
	 */
	public static ReadableByteChannel getChannel(InputStream inputStream)
			throws IOException
	{
		return (inputStream != null) ? Channels.newChannel(inputStream) : null;
	}

	/**
	 * Returns a writable byte channel based on a given output stream.
	 * @param outputStream The output stream.
	 */
	public static WritableByteChannel getChannel(OutputStream outputStream)
			throws IOException
	{
		return (outputStream != null) ? Channels.newChannel(outputStream) : null;
	}

	/**
	 * Returns an input stream based on a given readable byte channel.
	 * @param readableChannel The readable byte channel.
	 * @return An input stream based on a given readable byte channel.
	 */
	public static InputStream getStream(ReadableByteChannel readableChannel)
			throws IOException
	{
		return (readableChannel != null) ? Channels.newInputStream(readableChannel) : null;
	}

	/**
	 * Returns an output stream based on a given writable byte channel.
	 * @param writableChannel The writable byte channel.
	 * @return An output stream based on a given writable byte channel.
	 */
	public static OutputStream getStream(WritableByteChannel writableChannel)
	{
		return (writableChannel != null) ? Channels.newOutputStream(writableChannel) : null;
	}

	/**
	 * Returns an input stream based on the given representation's content and its write(OutputStream) method. 
	 * Internally, it uses a writer thread and a pipe stream.
	 * @return A stream with the representation's content.
	 */
	public static InputStream getStream(final Representation representation)
			throws IOException
	{
		if (representation != null)
		{
			final PipeStream pipe = new PipeStream();

			// Create a thread that will handle the task of continuously
			// writing the representation into the input side of the pipe
			Thread writer = new Thread()
			{
				public void run()
				{
					try
					{
						OutputStream os = pipe.getOutputStream();
						representation.write(os);
						os.write(-1);
						os.close();
					}
					catch (IOException ioe)
					{
						ioe.printStackTrace();
					}
				}
			};

			// Start the writer thread
			writer.start();
			return pipe.getInputStream();
		}
		else
		{
			return null;
		}
	}

	/**
	 * Returns a readable byte channel based on the given representation's content and its 
	 * write(WritableByteChannel) method. Internally, it uses a writer thread and a pipe stream.
	 * @return A readable byte channel.
	 */
	public static ReadableByteChannel getChannel(final Representation representation)
			throws IOException
	{
		final Pipe pipe = Pipe.open();

		// Create a thread that will handle the task of continuously
		// writing the representation into the input side of the pipe
		Thread writer = new Thread()
		{
			public void run()
			{
				try
				{
					WritableByteChannel wbc = pipe.sink();
					representation.write(wbc);
					wbc.close();
				}
				catch (IOException ioe)
				{
					ioe.printStackTrace();
				}
			}
		};

		// Start the writer thread
		writer.start();
		return pipe.source();
	}

	/**
	 * Converts an input stream to a string.
	 * @param inputStream The input stream.
	 * @return The converted string.
	 */
	public static String toString(InputStream inputStream)
	{
		String result = null;

		if (inputStream != null)
		{
			try
			{
				StringBuilder sb = new StringBuilder();
				InputStream is = new BufferedInputStream(inputStream);
				int nextByte = is.read();
				while (nextByte != -1)
				{
					sb.append((char) nextByte);
					nextByte = is.read();
				}
				is.close();
				result = sb.toString();
			}
			catch (Exception e)
			{
				// Return an empty string
			}
		}

		return result;
	}

	/**
	 * Writes an input stream to an output stream. When the reading is done, the input stream is closed. 
	 * @param inputStream The input stream.
	 * @param outputStream The output stream.
	 * @throws IOException
	 */
	public static void write(InputStream inputStream, OutputStream outputStream)
			throws IOException
	{
		int bytesRead;
		byte[] buffer = new byte[2048];
		while ((bytesRead = inputStream.read(buffer)) > 0)
		{
			outputStream.write(buffer, 0, bytesRead);
		}
		inputStream.close();
	}

	/**
	 * Writes a readable channel to a writable channel.
	 * @param readableChannel The readable channel.
	 * @param writableChannel The writable channel.
	 * @throws IOException
	 */
	public static void write(ReadableByteChannel readableChannel,
			WritableByteChannel writableChannel) throws IOException
	{
		if ((readableChannel != null) && (writableChannel != null))
		{
			write(Channels.newInputStream(readableChannel), Channels
					.newOutputStream(writableChannel));
		}
	}

	/**
	 * Private constructor to ensure that the class acts as a true utility class 
	 * i.e. it isn't instatiable and extensible.
	 */
	private ByteUtils()
	{

	}
}
