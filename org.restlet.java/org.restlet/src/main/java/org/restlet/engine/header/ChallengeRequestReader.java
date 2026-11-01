/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.header;

import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Parameter;

import java.io.IOException;

import static org.restlet.engine.header.HeaderUtils.isSpace;

/**
 * Challenge request header reader.
 * 
 * @author Thierry Boileau
 */
public class ChallengeRequestReader extends HeaderReader<ChallengeRequest> {

	/**
	 * Constructor.
	 * 
	 * @param header The header to read.
	 */
	public ChallengeRequestReader(String header) {
		super(header);
	}

	@Override
	public ChallengeRequest readValue() throws IOException {
		ChallengeRequest result = null;

		// The challenge is that this header is a comma separated lst of
		// challenges, and that each challenges is also a comma separated list,
		// but of parameters.
		skipSpaces();
		if (peek() != -1) {
			String scheme = readToken();
			result = new ChallengeRequest(new ChallengeScheme("HTTP_" + scheme, scheme));
			skipSpaces();

			// Header writer that will reconstruct the raw value of a challenge.
			HeaderWriter<Parameter> w = new HeaderWriter<Parameter>() {
				@Override
				public HeaderWriter<Parameter> append(Parameter value) {
					appendExtension(value);
					return this;
				}
			};

			// A challenge may carry a single token68 instead of parameters
			// (RFC 9110, section 11.2), such as a base64 Negotiate token.
			String token68 = readToken68();
			if (token68 != null) {
				w.append(token68);
			}

			boolean stop = false;
			while (peek() != -1 && !stop) {
				boolean sepSkipped = skipValueSeparator();
				// Record the start of the segment
				mark();
				// Read a token and the next character.
				readToken();
				int nextChar = read();
				reset();
				if (isSpace(nextChar)) {
					// A new scheme has been discovered.
					stop = true;
				} else {
					// The next segment is considered as a parameter
					if (sepSkipped) {
						// Add the skipped value separator.
						w.appendValueSeparator();
					}
					// Append the parameter
					w.append(readParameter());
				}
			}
			result.setRawValue(w.toString());
			w.close();
		}

		return result;
	}

	/**
	 * Reads a token68 ending with at least one "=" padding character, if the next
	 * characters are one. A token68 without padding is read as a regular parameter,
	 * as before.
	 * 
	 * @return The token68, or null (nothing consumed) if the next characters are
	 *         not one.
	 */
	private String readToken68() {
		mark();
		StringBuilder token = new StringBuilder();
		int next = peek();
		while (next != -1 && isToken68Char(next)) {
			token.append((char) read());
			next = peek();
		}

		int padding = 0;
		while (next == '=') {
			token.append((char) read());
			padding++;
			next = peek();
		}

		if (token.length() > padding && padding > 0 && (next == -1 || next == ',' || isSpace(next))) {
			return token.toString();
		}

		reset();
		return null;
	}

	private static boolean isToken68Char(int c) {
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '-' || c == '.'
				|| c == '_' || c == '~' || c == '+' || c == '/';
	}
}
