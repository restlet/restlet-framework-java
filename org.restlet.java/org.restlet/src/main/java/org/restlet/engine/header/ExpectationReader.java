/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.header;

import org.restlet.data.ClientInfo;
import org.restlet.data.Expectation;

import java.io.IOException;

/**
 * Expectation header reader.
 * 
 * @author Jerome Louvel
 */
public class ExpectationReader extends HeaderReader<Expectation> {
	/**
	 * Adds values to the given collection.
	 * 
	 * @param header     The header to read.
	 * @param clientInfo The client info to update.
	 */
	public static void addValues(String header, ClientInfo clientInfo) {
		if (header != null) {
			new ExpectationReader(header).addValues(clientInfo.getExpectations());
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param header The header to read.
	 */
	public ExpectationReader(String header) {
		super(header);
	}

	@Override
	public Expectation readValue() throws IOException {
		Expectation result = readNamedValue(Expectation.class);

		while (skipParameterSeparator()) {
			result.getParameters().add(readParameter());
		}

		return result;
	}

}
