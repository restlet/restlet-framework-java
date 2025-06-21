/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.header;

import org.restlet.data.Language;

import java.io.IOException;

/**
 * Language header reader.
 * 
 * @author Jerome Louvel
 */
public class LanguageReader extends HeaderReader<Language> {

	/**
	 * Constructor.
	 * 
	 * @param header The header to read.
	 */
	public LanguageReader(String header) {
		super(header);
	}

	@Override
	public Language readValue() throws IOException {
		return Language.valueOf(readRawValue());
	}

}
