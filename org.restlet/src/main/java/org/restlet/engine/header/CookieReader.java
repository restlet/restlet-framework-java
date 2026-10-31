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

    /** Returned by {@link #readName} when the next pair is not a legacy attribute. */
    private static final int NOT_LEGACY = -2;

    /**
     * Reads the next pair as a parameter.
     *
     * @param legacyOnly True, if the pair must be left unread unless it is a legacy attribute whose
     *     name starts with '$'.
     * @return The next pair as a parameter.
     * @throws IOException
     */
    private Parameter readPair(boolean legacyOnly) throws IOException {
        StringBuilder name = new StringBuilder();
        int end = readName(name, legacyOnly);

        if (end == NOT_LEGACY) {
            return null;
        } else if (end == '=') {
            return Parameter.create(name, readPairValue());
        } else if (!name.isEmpty()) {
            // End of a pair with no value
            return Parameter.create(name, null);
        } else if (end == ';') {
            throw new IOException("Empty cookie name detected. Please check your cookies");
        }

        // End of the header
        return null;
    }

    /**
     * Reads the name of a pair, skipping the leading spaces.
     *
     * @param name The buffer receiving the name.
     * @param legacyOnly True, if the pair must be left unread unless its name starts with '$'.
     * @return The character ending the name: '=', ';' or -1 at the end of the header, or {@link
     *     #NOT_LEGACY}.
     */
    private int readName(StringBuilder name, boolean legacyOnly) {
        int next;

        while (((next = read()) != -1) && (next != ';') && (next != '=')) {
            if (name.isEmpty() && HeaderUtils.isSpace(next)) {
                // Skip spaces
            } else if (legacyOnly && name.isEmpty() && next != '$') {
                unread();
                return NOT_LEGACY;
            } else {
                name.append((char) next);
            }
        }

        return next;
    }

    /**
     * Reads the value of a pair, up to the next semicolon. Quotes are tolerated around the value.
     *
     * @return The value.
     * @throws IOException
     */
    private StringBuilder readPairValue() throws IOException {
        StringBuilder value = new StringBuilder();
        int next;

        while (((next = read()) != -1) && (next != ';')) {
            if (value.isEmpty() && HeaderUtils.isSpace(next)) {
                // Skip spaces
            } else if ((next == '"') && value.isEmpty()) {
                // Step back
                unread();
                value.append(readQuotedString());
            } else {
                value.append((char) next);
            }
        }

        return value;
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
