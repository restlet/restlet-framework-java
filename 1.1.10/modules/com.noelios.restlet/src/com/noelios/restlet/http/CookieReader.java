/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.Parameter;
import org.restlet.util.DateUtils;

/**
 * Cookie header reader.
 * 
 * @author Jerome Louvel
 */
public class CookieReader extends HeaderReader {
    private static final String NAME_DOMAIN = "$Domain";

    private static final String NAME_PATH = "$Path";

    private static final String NAME_SET_ACCESS_RESTRICTED = "httpOnly";

    private static final String NAME_SET_COMMENT = "comment";

    private static final String NAME_SET_COMMENT_URL = "commentURL";

    private static final String NAME_SET_DISCARD = "discard";

    private static final String NAME_SET_DOMAIN = "domain";

    private static final String NAME_SET_EXPIRES = "expires";

    private static final String NAME_SET_MAX_AGE = "max-age";

    private static final String NAME_SET_PATH = "path";

    private static final String NAME_SET_PORT = "port";

    private static final String NAME_SET_SECURE = "secure";

    private static final String NAME_SET_VERSION = "version";

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

    /**
     * Reads the next cookie available or null.
     * 
     * @return The next cookie available or null.
     * @throws IOException
     */
    public Cookie readCookie() throws IOException {
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
     * Reads the next cookie setting available or null.
     * 
     * @return The next cookie setting available or null.
     * @throws IOException
     */
    public CookieSetting readCookieSetting() throws IOException {
        CookieSetting result = null;
        Parameter pair = readPair();

        while ((pair != null) && (pair.getName().charAt(0) == '$')) {
            // Unexpected special attribute
            // Silently ignore it as it may have been introduced by new
            // specifications
            pair = readPair();
        }

        if (pair != null) {
            // Set the cookie name and value
            result = new CookieSetting(pair.getName(), pair.getValue());
            pair = readPair();
        }

        while (pair != null) {
            if (pair.getName().equalsIgnoreCase(NAME_SET_PATH)) {
                result.setPath(pair.getValue());
            } else if (pair.getName().equalsIgnoreCase(NAME_SET_DOMAIN)) {
                result.setDomain(pair.getValue());
            } else if (pair.getName().equalsIgnoreCase(NAME_SET_COMMENT)) {
                result.setComment(pair.getValue());
            } else if (pair.getName().equalsIgnoreCase(NAME_SET_COMMENT_URL)) {
                // No yet supported
            } else if (pair.getName().equalsIgnoreCase(NAME_SET_DISCARD)) {
                result.setMaxAge(-1);
            } else if (pair.getName().equalsIgnoreCase(NAME_SET_EXPIRES)) {
                final Date current = new Date(System.currentTimeMillis());
                Date expires = DateUtils.parse(pair.getValue(),
                        DateUtils.FORMAT_RFC_1036);

                if (expires == null) {
                    expires = DateUtils.parse(pair.getValue(),
                            DateUtils.FORMAT_RFC_1123);
                }

                if (expires == null) {
                    expires = DateUtils.parse(pair.getValue(),
                            DateUtils.FORMAT_ASC_TIME);
                }

                if (expires != null) {
                    if (DateUtils.after(current, expires)) {
                        result.setMaxAge((int) ((expires.getTime() - current
                                .getTime()) / 1000));
                    } else {
                        result.setMaxAge(0);
                    }
                } else {
                    // Ignore the expires header
                    Context.getCurrentLogger().log(
                            Level.WARNING,
                            "Ignoring cookie setting expiration date. Unable to parse the date: "
                                    + pair.getValue());
                }
            } else if (pair.getName().equalsIgnoreCase(NAME_SET_MAX_AGE)) {
                result.setMaxAge(Integer.valueOf(pair.getValue()));
            } else if (pair.getName().equalsIgnoreCase(NAME_SET_PORT)) {
                // No yet supported
            } else if (pair.getName().equalsIgnoreCase(NAME_SET_SECURE)) {
                if ((pair.getValue() == null)
                        || (pair.getValue().length() == 0)) {
                    result.setSecure(true);
                }
            } else if (pair.getName().equalsIgnoreCase(
                    NAME_SET_ACCESS_RESTRICTED)) {
                if ((pair.getValue() == null)
                        || (pair.getValue().length() == 0)) {
                    result.setAccessRestricted(true);
                }
            } else if (pair.getName().equalsIgnoreCase(NAME_SET_VERSION)) {
                result.setVersion(Integer.valueOf(pair.getValue()));
            } else {
                // Unexpected special attribute
                // Silently ignore it as it may have been introduced by new
                // specifications
            }

            pair = readPair();
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
            try {
                boolean readingName = true;
                boolean readingValue = false;
                final StringBuilder nameBuffer = new StringBuilder();
                final StringBuilder valueBuffer = new StringBuilder();

                int nextChar = 0;
                while ((result == null) && (nextChar != -1)) {
                    nextChar = read();

                    if (readingName) {
                        if ((HttpUtils.isSpace(nextChar))
                                && (nameBuffer.length() == 0)) {
                            // Skip spaces
                        } else if ((nextChar == -1) || (nextChar == ';')
                                || (nextChar == ',')) {
                            if (nameBuffer.length() > 0) {
                                // End of pair with no value
                                result = HttpUtils.createParameter(nameBuffer,
                                        null);
                            } else if (nextChar == -1) {
                                // Do nothing return null preference
                            } else {
                                throw new IOException(
                                        "Empty cookie name detected. Please check your cookies");
                            }
                        } else if (nextChar == '=') {
                            readingName = false;
                            readingValue = true;
                        } else if (HttpUtils.isTokenChar(nextChar)
                                || (this.globalVersion < 1)) {
                            nameBuffer.append((char) nextChar);
                        } else {
                            throw new IOException(
                                    "Separator and control characters are not allowed within a token. Please check your cookie header");
                        }
                    } else if (readingValue) {
                        if ((HttpUtils.isSpace(nextChar))
                                && (valueBuffer.length() == 0)) {
                            // Skip spaces
                        } else if ((nextChar == -1) || (nextChar == ';')) {
                            // End of pair
                            result = HttpUtils.createParameter(nameBuffer,
                                    valueBuffer);
                        } else if ((nextChar == '"')
                                && (valueBuffer.length() == 0)) {
                            valueBuffer.append(readQuotedString());
                        } else if (HttpUtils.isTokenChar(nextChar)
                                || (this.globalVersion < 1)) {
                            valueBuffer.append((char) nextChar);
                        } else {
                            throw new IOException(
                                    "Separator and control characters are not allowed within a token. Please check your cookie header");
                        }
                    }
                }
            } catch (UnsupportedEncodingException uee) {
                throw new IOException(
                        "Unsupported encoding. Please contact the administrator");
            }
        }

        return result;
    }

}
