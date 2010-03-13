/**
 * Copyright 2005-2010 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine.http.header;

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

    /** The cached pair. Used by the readPair() method. */
    private volatile Parameter cachedPair;

    /** The global cookie specification version. */
    private volatile int globalVersion;

    /**
     * Constructor.
     * 
     * @param header
     *            The header to read.
     */
    public CookieReader(String header) {
        super(header);
        this.cachedPair = null;
        this.globalVersion = -1;
    }

    @Override
    public Cookie readValue() throws IOException {
        Cookie result = null;
        Parameter pair = readPair();

        if (this.globalVersion == -1) {
            // Cookies version not yet detected
            if (pair.getName().equalsIgnoreCase(NAME_VERSION)) {
                if (pair.getValue() != null) {
                    this.globalVersion = Integer.parseInt(pair.getValue());
                } else {
                    throw new IOException(
                            "Empty cookies version attribute detected. Please check your cookie header");
                }
            } else {
                // Set the default version for old Netscape cookies
                this.globalVersion = 0;
            }
        }

        while ((pair != null) && (pair.getName().charAt(0) == '$')) {
            // Unexpected special attribute
            // Silently ignore it as it may have been introduced by new
            // specifications
            pair = readPair();
        }

        if (pair != null) {
            // Set the cookie name and value
            result = new Cookie(this.globalVersion, pair.getName(), pair
                    .getValue());
            pair = readPair();
        }

        while ((pair != null) && (pair.getName().charAt(0) == '$')) {
            if (pair.getName().equalsIgnoreCase(NAME_PATH)) {
                result.setPath(pair.getValue());
            } else if (pair.getName().equalsIgnoreCase(NAME_DOMAIN)) {
                result.setDomain(pair.getValue());
            } else {
                // Unexpected special attribute
                // Silently ignore it as it may have been introduced by new
                // specifications
            }

            pair = readPair();
        }

        if (pair != null) {
            // We started to read the next cookie
            // So let's put it back into the stream
            this.cachedPair = pair;
        }

        return result;
    }

    /**
     * Reads the next pair as a parameter.
     * 
     * @return The next pair as a parameter.
     * @throws IOException
     */
    private Parameter readPair() throws IOException {
        Parameter result = null;

        if (this.cachedPair != null) {
            result = this.cachedPair;
            this.cachedPair = null;
        } else {
            boolean readingName = true;
            boolean readingValue = false;
            StringBuilder nameBuffer = new StringBuilder();
            StringBuilder valueBuffer = new StringBuilder();
            int nextChar = 0;

            while ((result == null) && (nextChar != -1)) {
                nextChar = read();

                if (readingName) {
                    if ((HeaderUtils.isSpace(nextChar))
                            && (nameBuffer.length() == 0)) {
                        // Skip spaces
                    } else if ((nextChar == -1) || (nextChar == ';')
                            || (nextChar == ',')) {
                        if (nameBuffer.length() > 0) {
                            // End of pair with no value
                            result = Parameter.create(nameBuffer, null);
                        } else if (nextChar == -1) {
                            // Do nothing return null preference
                        } else {
                            throw new IOException(
                                    "Empty cookie name detected. Please check your cookies");
                        }
                    } else if (nextChar == '=') {
                        readingName = false;
                        readingValue = true;
                    } else if (HeaderUtils.isTokenChar(nextChar)
                            || (this.globalVersion < 1)) {
                        nameBuffer.append((char) nextChar);
                    } else {
                        throw new IOException(
                                "Separator and control characters are not allowed within a token. Please check your cookie header");
                    }
                } else if (readingValue) {
                    if ((HeaderUtils.isSpace(nextChar))
                            && (valueBuffer.length() == 0)) {
                        // Skip spaces
                    } else if ((nextChar == -1) || (nextChar == ';')) {
                        // End of pair
                        result = Parameter.create(nameBuffer, valueBuffer);
                    } else if ((nextChar == '"') && (valueBuffer.length() == 0)) {
                        // Step back
                        unread();
                        valueBuffer.append(readQuotedString());
                    } else if (HeaderUtils.isTokenChar(nextChar)
                            || (this.globalVersion < 1)) {
                        valueBuffer.append((char) nextChar);
                    } else {
                        throw new IOException(
                                "Separator and control characters are not allowed within a token. Please check your cookie header");
                    }
                }
            }
        }

        return result;
    }

}
