/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.header;

import org.restlet.data.Header;
import org.restlet.data.Tag;

import java.io.IOException;
import java.util.Collection;

/**
 * Tag header reader.
 * 
 * @author Thierry Boileau
 */
public class TagReader extends HeaderReader<Tag> {

	/**
	 * Adds values to the given collection.
	 * 
	 * @param header     The header to read.
	 * @param collection The collection to update.
	 */
	public static void addValues(Header header, Collection<Tag> collection) {
		new TagReader(header.getValue()).addValues(collection);
	}

	/**
	 * Constructor.
	 * 
	 * @param header The header to read.
	 */
	public TagReader(String header) {
		super(header);
	}

	@Override
	public Tag readValue() throws IOException {
		return Tag.parse(readRawValue());
	}

}
