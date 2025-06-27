/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.io;

import org.restlet.data.CharacterSet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Output stream wrapping a character writer.
 * 
 * @author Kevin Conaway
 */
public class WriterOutputStream extends OutputStream {

	/** The character set to use when parsing byte arrays. */
	private final Charset charSet;

	/** The wrapped writer. */
	private final Writer writer;

	/**
	 * Constructor.
	 * 
	 * @param writer       The wrapped writer.
	 * @param characterSet The character set. Use {@link CharacterSet#ISO_8859_1} by
	 *                     default if a null value is given.
	 */
	public WriterOutputStream(Writer writer, CharacterSet characterSet) {
		this.writer = writer;
		this.charSet = (characterSet == null) ? StandardCharsets.ISO_8859_1 : characterSet.toCharset();
	}

	@Override
	public void close() throws IOException {
		super.close();
		this.writer.close();
	}

	@Override
	public void flush() throws IOException {
		super.flush();
		this.writer.flush();
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		CharBuffer charBuffer = this.charSet.decode(ByteBuffer.wrap(b, off, len));
		this.writer.write(charBuffer.toString());
	}

	@Override
	public void write(int b) throws IOException {
		this.writer.write(b);
	}
}
