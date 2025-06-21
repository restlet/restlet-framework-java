/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.representation;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.engine.io.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Representation based on a BIO character stream.
 * 
 * @author Jerome Louvel
 */
public abstract class CharacterRepresentation extends Representation {
	/**
	 * Constructor.
	 * 
	 * @param mediaType The media type.
	 */
	public CharacterRepresentation(MediaType mediaType) {
		super(mediaType);
		setCharacterSet(CharacterSet.UTF_8);
	}

	@Override
	public InputStream getStream() throws IOException {
		return IoUtils.getStream(getReader(), getCharacterSet());
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		Writer writer = IoUtils.getWriter(outputStream, getCharacterSet());
		write(writer);
		writer.flush();
	}

}
