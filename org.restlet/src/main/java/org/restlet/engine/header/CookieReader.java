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
 * @author Jerome Louvel
 */
public class CookieReader extends HeaderReader<Cookie> {

    private static final String NAME_DOMAIN = "$Domain";

    private static final String NAME_PATH = "$Path";

    private static final String NAME_VERSION = "$Version";

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

    /** The global cookie specification version. */
    private volatile int globalVersion;

    /**
     * Constructor.
     *
     * @param header The header to read.
     */
    public CookieReader(String header) {
        super(header);
        this.globalVersion = -1;
    }

    /**
     * Reads the next pair as a parameter.
     *
     * @param readAttribute True, if the intention is to only read the cookie attribute.
     * @return The next pair as a parameter.
     * @throws IOException
     */
    private Parameter readPair(boolean readAttribute) throws IOException {
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
                } else if ((nextChar == -1) || (nextChar == ';') || (nextChar == ',')) {
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
                } else if (HeaderUtils.isTokenChar(nextChar) || (this.globalVersion < 1)) {
                    if (readAttribute && nextChar != '$' && (nameBuffer.isEmpty())) {
                        unread();
                        nextChar = -1;
                    } else {
                        nameBuffer.append((char) nextChar);
                    }
                } else {
                    throw new IOException(
                            "Separator and control characters are not allowed within a token. Please check your cookie header");
                }
            } else {
                // reading value
                if (valueBuffer.isEmpty() && HeaderUtils.isSpace(nextChar)) {
                    // Skip spaces
                } else if ((nextChar == -1) || (nextChar == ';')) {
                    // End of a pair
                    result = Parameter.create(nameBuffer, valueBuffer);
                } else if ((nextChar == '"') && (valueBuffer.isEmpty())) {
                    // Step back
                    unread();
                    valueBuffer.append(readQuotedString());
                } else if (HeaderUtils.isTokenChar(nextChar) || (this.globalVersion < 1)) {
                    valueBuffer.append((char) nextChar);
                } else {
                    throw new IOException(
                            "Separator and control characters are not allowed within a token. Please check your cookie header");
                }
            }
        }

        return result;
    }

    @Override
    public Cookie readValue() throws IOException {
        Cookie result = null;
        Parameter pair = readPair(false);

        if (pair != null && this.globalVersion == -1) {
            // Cookies version not yet detected
            this.globalVersion = getVersion(pair);
        }

        while ((pair != null) && (pair.getName().isEmpty() || pair.getName().charAt(0) == '$')) {
            // Unexpected special attribute
            // Silently ignore it as it may have been introduced by new specifications
            pair = readPair(false);
        }

        if (pair != null) {
            // Set the cookie name and value
            result = new Cookie(this.globalVersion, pair.getName(), pair.getValue());
            pair = readPair(true);
        }

        while ((pair != null) && (pair.getName().isEmpty() || pair.getName().charAt(0) == '$')) {
            if (pair.getName().equalsIgnoreCase(NAME_PATH)) {
                result.setPath(pair.getValue());
            } else if (pair.getName().equalsIgnoreCase(NAME_DOMAIN)) {
                result.setDomain(pair.getValue());
            } else {
                // Unexpected special attribute
                // Silently ignore it as it may have been introduced by new specifications
            }

            pair = readPair(true);
        }

        return result;
    }

    private int getVersion(final Parameter pair) throws IOException {
        if (NAME_VERSION.equalsIgnoreCase(pair.getName())) {
            if (pair.getValue() != null) {
                return Integer.parseInt(pair.getValue());
            } else {
                throw new IOException(
                        "Empty cookies version attribute detected. Please check your cookie header");
            }
        } else { // Set the default version for old Netscape cookies
            return 0;
        }
    }
}
