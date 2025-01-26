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
import org.restlet.data.Method;

import java.io.IOException;
import java.util.Collection;

/**
 * Method header reader.
 * 
 * @author Jerome Louvel
 */
public class MethodReader extends HeaderReader<Method> {

	/**
	 * Adds values to the given collection.
	 * 
	 * @param header     The header to read.
	 * @param collection The collection to update.
	 */
	public static void addValues(Header header, Collection<Method> collection) {
		new MethodReader(header.getValue()).addValues(collection);
	}

	/**
	 * Constructor.
	 * 
	 * @param header The header to read.
	 */
	public MethodReader(String header) {
		super(header);
	}

	@Override
	public Method readValue() throws IOException {
		return Method.valueOf(readToken());
	}

}
