/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.engine.header;

import static org.restlet.engine.util.DateUtils.FORMAT_ASC_TIME;
import static org.restlet.engine.util.DateUtils.FORMAT_RFC_1036;
import static org.restlet.engine.util.DateUtils.FORMAT_RFC_1123;

import java.io.IOException;
import java.util.Date;

import org.restlet.Context;
import org.restlet.data.CookieSetting;
import org.restlet.data.Parameter;
import org.restlet.engine.util.DateUtils;
import org.restlet.engine.util.StringUtils;

/**
 * Cookie setting header reader.
 * 
 * @author Jerome Louvel
 */
public class CookieSettingReader extends HeaderReader<CookieSetting> {

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

    /**
     * Parses the given String to a CookieSetting
     * 
     * @param cookieSetting
     * @return the CookieSetting parsed from the String
     * @throws IllegalArgumentException
     *             Thrown if the String can not be parsed as CookieSetting.
     */
    public static CookieSetting read(String cookieSetting)
            throws IllegalArgumentException {
        CookieSettingReader cr = new CookieSettingReader(cookieSetting);

        try {
            return cr.readValue();
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Could not read the cookie setting", e);
        }
    }

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
    public CookieSettingReader(String header) {
        super(header);
        this.cachedPair = null;
        this.globalVersion = -1;
    }

    /**
     * Reads the next pair as a parameter.
     * 
     * @return The next pair as a parameter.
     * @throws IOException
     */
    private Parameter readPair() throws IOException {
        if (this.cachedPair != null) {
            Parameter pair = this.cachedPair;
            this.cachedPair = null;
            return pair;
        }

        Parameter result = null;

        boolean readingName = true;
        StringBuilder nameBuffer = new StringBuilder();
        StringBuilder valueBuffer = new StringBuilder();
        int nextChar = 0;

        while ((result == null) && (nextChar != -1)) {
            nextChar = read();

            if (readingName) {
                if ((HeaderUtils.isSpace(nextChar)) && (nameBuffer.length() == 0)) {
                    // Skip spaces
                } else if ((nextChar == -1) || (nextChar == ';') || (nextChar == ',')) {
                    if (nameBuffer.length() > 0) {
                        // End of pair with no value
                        result = Parameter.create(nameBuffer, null);
                    } else if (nextChar == -1) {
                        // Do nothing return null preference
                    } else {
                        throw new IOException("Empty cookie name detected. Please check your cookies");
                    }
                } else if (nextChar == '=') {
                    readingName = false;
                } else if (HeaderUtils.isTokenChar(nextChar) || (this.globalVersion < 1)) {
                    nameBuffer.append((char) nextChar);
                } else {
                    throw new IOException(
                            "Separator and control characters are not allowed within a token. Please check your cookie header");
                }
            } else {
                // reading value
                if ((HeaderUtils.isSpace(nextChar)) && (valueBuffer.length() == 0)) {
                    // Skip spaces
                } else if ((nextChar == -1) || (nextChar == ';')) {
                    // End of pair
                    result = Parameter.create(nameBuffer, valueBuffer);
                } else if ((nextChar == '"') && (valueBuffer.length() == 0)) {
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
    public CookieSetting readValue() throws IOException {
        Parameter pair = null;

        // Unexpected special attributes
        // Silently ignore it as it may have been introduced by new specifications
        do {
            if ((pair = readPair()) == null) {
                return null;
            }
        } while (pair.getName().charAt(0) == '$');

        // Set the cookie name and value
        CookieSetting result = new CookieSetting(pair.getName(), pair.getValue());

        while ((pair = readPair()) != null) {
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
                Date expires = DateUtils.parse(pair.getValue(), FORMAT_RFC_1036);

                if (expires == null) {
                    expires = DateUtils.parse(pair.getValue(), FORMAT_RFC_1123);
                }

                if (expires == null) {
                    expires = DateUtils.parse(pair.getValue(), FORMAT_ASC_TIME);
                }

                if (expires != null) {
                    final Date current = new Date(System.currentTimeMillis());
                    if (DateUtils.after(current, expires)) {
                        result.setMaxAge((int) ((expires.getTime() - current.getTime()) / 1000));
                    } else {
                        result.setMaxAge(0);
                    }
                } else {
                    // Ignore the expires header
                    Context.getCurrentLogger()
                            .warning(
                                    "Ignoring cookie setting expiration date. Unable to parse the date: "
                                            + pair.getValue());
                }
            } else if (pair.getName().equalsIgnoreCase(NAME_SET_MAX_AGE)) {
                result.setMaxAge(Integer.valueOf(pair.getValue()));
            } else if (pair.getName().equalsIgnoreCase(NAME_SET_PORT)) {
                // No yet supported
            } else if (pair.getName().equalsIgnoreCase(NAME_SET_SECURE)) {
                if (StringUtils.isNullOrEmpty(pair.getValue())) {
                    result.setSecure(true);
                }
            } else if (pair.getName().equalsIgnoreCase(NAME_SET_ACCESS_RESTRICTED)) {
                if (StringUtils.isNullOrEmpty(pair.getValue())) {
                    result.setAccessRestricted(true);
                }
            } else if (pair.getName().equalsIgnoreCase(NAME_SET_VERSION)) {
                result.setVersion(Integer.valueOf(pair.getValue()));
            } else {
                // Unexpected special attribute
                // Silently ignore it as it may have been introduced by new specifications
            }
        }

        return result;
    }

}
