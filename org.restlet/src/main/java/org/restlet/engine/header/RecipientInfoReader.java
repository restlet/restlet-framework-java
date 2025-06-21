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
import org.restlet.data.Protocol;
import org.restlet.data.RecipientInfo;

import java.io.IOException;
import java.util.Collection;

/**
 * Recipient info header reader.
 * 
 * @author Jerome Louvel
 */
public class RecipientInfoReader extends HeaderReader<RecipientInfo> {
	/**
	 * Adds values to the given collection.
	 * 
	 * @param header     The header to read.
	 * @param collection The collection to update.
	 */
	public static void addValues(Header header, Collection<RecipientInfo> collection) {
		new RecipientInfoReader(header.getValue()).addValues(collection);
	}

	/**
	 * Constructor.
	 * 
	 * @param header The header to read.
	 */
	public RecipientInfoReader(String header) {
		super(header);
	}

	@Override
	public RecipientInfo readValue() throws IOException {
		RecipientInfo result = new RecipientInfo();
		String protocolToken = readToken();

		if (protocolToken == null || protocolToken.isEmpty()) {
			throw new IOException(
					"Unexpected empty protocol token for while reading recipient info header, please check the value.");
		}

		if (peek() == '/') {
			read();
			result.setProtocol(new Protocol(protocolToken, protocolToken, null, -1, readToken()));
		} else {
			result.setProtocol(new Protocol("HTTP", "HTTP", null, -1, protocolToken));
		}

		// Move to the next text
		if (skipSpaces()) {
			result.setName(readRawText());

			// Move to the next text
			if (skipSpaces()) {
				result.setComment(readComment());
			}
		}

		return result;
	}
}
