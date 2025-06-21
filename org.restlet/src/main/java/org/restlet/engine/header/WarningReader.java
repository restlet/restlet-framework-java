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
import org.restlet.data.Status;
import org.restlet.data.Warning;
import org.restlet.engine.util.DateUtils;

import java.io.IOException;
import java.util.Collection;

/**
 * Warning header reader.
 * 
 * @author Thierry Boileau
 */
public class WarningReader extends HeaderReader<Warning> {

	/**
	 * Adds values to the given collection.
	 * 
	 * @param header     The header to read.
	 * @param collection The collection to update.
	 */
	public static void addValues(Header header, Collection<Warning> collection) {
		new WarningReader(header.getValue()).addValues(collection);
	}

	/**
	 * Constructor.
	 * 
	 * @param header The header to read.
	 */
	public WarningReader(String header) {
		super(header);
	}

	@Override
	public Warning readValue() throws IOException {
		Warning result = new Warning();

		String code = readToken();
		skipSpaces();
		String agent = readRawText();
		skipSpaces();
		String text = readQuotedString();
		// The date is not mandatory
		skipSpaces();
		String date = null;
		if (peek() != -1) {
			date = readQuotedString();
		}

		if ((code == null) || (agent == null) || (text == null)) {
			throw new IOException("Warning header malformed.");
		}

		result.setStatus(Status.valueOf(Integer.parseInt(code)));
		result.setAgent(agent);
		result.setText(text);
		if (date != null) {
			result.setDate(DateUtils.parse(date));
		}

		return result;
	}

}
