/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.header;

import java.io.IOException;
import org.restlet.data.Cookie;
import org.restlet.data.Parameter;

/**
 * Cookie header reader.
 *
 * <p>Reads the RFC 6265 format: cookies are separated by semicolons. The pairs of the obsolete RFC
 * 2109 format ($Version, $Path and $Domain) are silently ignored.
 *
 * @author Jerome Louvel
 */
public class CookieReader extends HeaderReader<Cookie> {

    /**
     * Parses the given String to a Cookie
     *
     * @param cookie The cookie a string to parse.
     * @return the Cookie parsed from the String
     * @throws IllegalArgumentException Thrown if the String cannot be parsed as Cookie.
     */
    public static Cookie read(String cookie) throws IllegalArgumentException {
        CookieReader cr = new CookieReader(cookie);

        try {
            return cr.readValue();
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read the cookie", e);
        }
    }

    /**
     * Constructor.
     *
     * @param header The header to read.
     */
    public CookieReader(String header) {
        super(header);
    }

    /**
     * Reads the next pair as a parameter.
     *
     * @param skipUnlessLegacy True, if the pair must be left unread unless it is a legacy attribute
     *     whose name starts with '$'.
     * @return The next pair as a parameter.
     * @throws IOException
     */
    private Parameter readPair(boolean skipUnlessLegacy) throws IOException {
        Parameter result = null;

        boolean readingName = true;
        StringBuilder nameBuffer = new StringBuilder();
        StringBuilder valueBuffer = new StringBuilder();
        int nextChar = 0;

        while ((result == null) && (nextChar != -1)) {
            nextChar = read();

            if (readingName) {
                if (nameBuffer.isEmpty() && HeaderUtils.isSpace(nextChar)) {
                    // Skip spaces
                } else if ((nextChar == -1) || (nextChar == ';')) {
                    if (!nameBuffer.isEmpty()) {
                        // End of a pair with no value
                        result = Parameter.create(nameBuffer, null);
                    } else if (nextChar == -1) {
                        // Do nothing and return a null preference
                    } else {
                        throw new IOException(
                                "Empty cookie name detected. Please check your cookies");
                    }
                } else if (nextChar == '=') {
                    readingName = false;
                } else if (skipUnlessLegacy && nextChar != '$' && nameBuffer.isEmpty()) {
                    unread();
                    nextChar = -1;
                } else {
                    nameBuffer.append((char) nextChar);
                }
            } else {
                if (valueBuffer.isEmpty() && HeaderUtils.isSpace(nextChar)) {
                    // Skip spaces
                } else if ((nextChar == -1) || (nextChar == ';')) {
                    // End of a pair
                    result = Parameter.create(nameBuffer, valueBuffer);
                } else if ((nextChar == '"') && (valueBuffer.isEmpty())) {
                    // Step back, quotes are tolerated around the value
                    unread();
                    valueBuffer.append(readQuotedString());
                } else {
                    valueBuffer.append((char) nextChar);
                }
            }
        }

        return result;
    }

    @Override
    public Cookie readValue() throws IOException {
        Cookie result = null;
        Parameter pair = readPair(false);

        // Silently ignore the legacy $Version, $Path and $Domain pairs
        while ((pair != null) && (pair.getName().isEmpty() || pair.getName().charAt(0) == '$')) {
            pair = readPair(false);
        }

        if (pair != null) {
            result = new Cookie(pair.getName(), pair.getValue());

            // Skip the legacy attributes following the cookie
            do {
                pair = readPair(true);
            } while (pair != null);
        }

        return result;
    }
}
