/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.header;

import org.restlet.data.CacheDirective;
import org.restlet.data.Header;

import java.io.IOException;
import java.util.Collection;

/**
 * Cache directive header reader.
 * 
 * @author Jerome Louvel
 */
public class CacheDirectiveReader extends HeaderReader<CacheDirective> {

	/**
	 * Adds values to the given collection.
	 * 
	 * @param header     The header to read.
	 * @param collection The collection to update.
	 */
	public static void addValues(Header header, Collection<CacheDirective> collection) {
		new CacheDirectiveReader(header.getValue()).addValues(collection);
	}

	/**
	 * Constructor.
	 * 
	 * @param header The header to read.
	 */
	public CacheDirectiveReader(String header) {
		super(header);
	}

	@Override
	public CacheDirective readValue() throws IOException {
		return readNamedValue(CacheDirective.class);
	}

}
