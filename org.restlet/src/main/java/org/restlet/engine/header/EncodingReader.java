/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.header;

import org.restlet.data.Encoding;

import java.io.IOException;
import java.util.Collection;

/**
 * Encoding header reader.
 * 
 * @author Jerome Louvel
 */
public class EncodingReader extends HeaderReader<Encoding> {

	/**
	 * Constructor.
	 * 
	 * @param header The header to read.
	 */
	public EncodingReader(String header) {
		super(header);
	}

	@Override
	protected boolean canAdd(Encoding value, Collection<Encoding> values) {
		return value != null && !Encoding.IDENTITY.equals(value);
	}

	@Override
	public Encoding readValue() throws IOException {
		return Encoding.valueOf(readToken());
	}

}
