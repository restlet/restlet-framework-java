/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.representation;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.engine.io.IoUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.logging.Level;

/**
 * Transient representation based on a BIO characters reader.
 * 
 * @author Jerome Louvel
 */
public class ReaderRepresentation extends CharacterRepresentation {

	/** The representation's reader. */
	private volatile Reader reader;

	/**
	 * Constructor.
	 * 
	 * @param reader The representation's stream.
	 */
	public ReaderRepresentation(Reader reader) {
		this(reader, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param reader    The representation's stream.
	 * @param mediaType The representation's media type.
	 */
	public ReaderRepresentation(Reader reader, MediaType mediaType) {
		this(reader, mediaType, UNKNOWN_SIZE);
	}

	/**
	 * Constructor.
	 * 
	 * @param reader       The representation's stream.
	 * @param mediaType    The representation's media type.
	 * @param expectedSize The expected reader size in bytes.
	 */
	public ReaderRepresentation(Reader reader, MediaType mediaType, long expectedSize) {
		super(mediaType);
		setSize(expectedSize);
		setTransient(true);
		setReader(reader);
	}

	@Override
	public Reader getReader() throws IOException {
		final Reader result = this.reader;
		setReader(null);
		return result;
	}

	/**
	 * Note that this method relies on {@link #getStream()}. This stream is closed
	 * once fully read.
	 */
	@Override
	public String getText() throws IOException {
		return IoUtils.toString(getStream(), getCharacterSet());
	}

	/**
	 * Closes and releases the input stream.
	 */
	@Override
	public void release() {
		if (this.reader != null) {
			try {
				this.reader.close();
			} catch (IOException e) {
				Context.getCurrentLogger().log(Level.WARNING, "Error while releasing the representation.", e);
			}

			this.reader = null;
		}

		super.release();
	}

	/**
	 * Sets the reader to use.
	 * 
	 * @param reader The reader to use.
	 */
	public void setReader(Reader reader) {
		this.reader = reader;
		setAvailable(reader != null);
	}

	@Override
	public void write(Writer writer) throws IOException {
		IoUtils.copy(getReader(), writer);
	}
}
