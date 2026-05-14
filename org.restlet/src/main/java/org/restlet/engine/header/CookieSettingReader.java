/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.header;

import static org.restlet.engine.util.DateUtils.FORMAT_ASC_TIME;
import static org.restlet.engine.util.DateUtils.FORMAT_RFC_1036;
import static org.restlet.engine.util.DateUtils.FORMAT_RFC_1123;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import org.restlet.Context;
import org.restlet.data.CookieSetting;
import org.restlet.data.Parameter;
import org.restlet.engine.util.DateUtils;
import org.restlet.engine.util.StringUtils;

/**
 * Cookie setting header reader.
 *
 * <p>The commentURL and port attributes are not supported yet.
 *
 * @author Jerome Louvel
 */
public class CookieSettingReader extends HeaderReader<CookieSetting> {

    private static final String NAME_SET_ACCESS_RESTRICTED = "httpOnly";

    private static final String NAME_SET_COMMENT = "comment";

    private static final String NAME_SET_DISCARD = "discard";

    private static final String NAME_SET_DOMAIN = "domain";

    private static final String NAME_SET_EXPIRES = "expires";

    private static final String NAME_SET_MAX_AGE = "max-age";

    private static final String NAME_SET_PATH = "path";

    private static final String NAME_SET_SECURE = "secure";

    private static final String NAME_SET_VERSION = "version";

    /**
     * Parses the given String to a CookieSetting
     *
     * @param cookieSetting The cookie setting as String to parse.
     * @return the CookieSetting parsed from the String
     * @throws IllegalArgumentException Thrown if the String cannot be parsed as CookieSetting.
     */
    public static CookieSetting read(String cookieSetting) throws IllegalArgumentException {
        CookieSettingReader cr = new CookieSettingReader(cookieSetting);

        try {
            return cr.readValue();
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read the cookie setting", e);
        }
    }

    /** The cached pair. Used by the readPair() method. */
    private volatile Parameter cachedPair;

    /** The global cookie specification version. */
    private final int globalVersion;

    /**
     * Constructor.
     *
     * @param header The header to read.
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
                    nameBuffer.append((char) nextChar);
                } else {
                    throw new IOException(
                            "Separator and control characters are not allowed within a token. Please check your cookie header");
                }
            } else {
                // reading value
                if (valueBuffer.isEmpty() && HeaderUtils.isSpace(nextChar)) {
                    // Skip spaces
                } else if ((nextChar == -1) || (nextChar == ';')) {
                    // End of pair
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
    public CookieSetting readValue() throws IOException {
        Parameter pair;

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
            if (NAME_SET_PATH.equalsIgnoreCase(pair.getName())) {
                result.setPath(pair.getValue());
            } else if (NAME_SET_DOMAIN.equalsIgnoreCase(pair.getName())) {
                result.setDomain(pair.getValue());
            } else if (NAME_SET_COMMENT.equalsIgnoreCase(pair.getName())) {
                result.setComment(pair.getValue());
            } else if (NAME_SET_DISCARD.equalsIgnoreCase(pair.getName())) {
                result.setMaxAge(-1);
            } else if (NAME_SET_EXPIRES.equalsIgnoreCase(pair.getName())) {
                Date expires = parseExpiresDate(pair);
                if (expires == null) {
                    // Ignore the "expires" header
                    Context.getCurrentLogger()
                            .warning(
                                    "Ignoring cookie setting expiration date. Unable to parse the date: "
                                            + pair.getValue());
                } else {
                    result.setMaxAge(fromExpirationDateToMaxAge(expires));
                }
            } else if (NAME_SET_MAX_AGE.equalsIgnoreCase(pair.getName())) {
                try {
                    result.setMaxAge(Integer.parseInt(pair.getValue()));
                } catch (NumberFormatException numberFormatException) {
                    result.setMaxAge(Integer.MAX_VALUE);
                    Context.getCurrentLogger()
                            .warning(
                                    "Unable to parse the cookie setting max-age value \""
                                            + pair.getValue()
                                            + "\", used Integer.MAX_VALUE instead: "
                                            + Integer.MAX_VALUE);
                }
            } else if (NAME_SET_SECURE.equalsIgnoreCase(pair.getName())) {
                if (StringUtils.isNullOrEmpty(pair.getValue())) {
                    result.setSecure(true);
                }
            } else if (NAME_SET_ACCESS_RESTRICTED.equalsIgnoreCase(pair.getName())) {
                if (StringUtils.isNullOrEmpty(pair.getValue())) {
                    result.setAccessRestricted(true);
                }
            } else if (NAME_SET_VERSION.equalsIgnoreCase(pair.getName())) {
                result.setVersion(Integer.parseInt(pair.getValue()));
            } else {
                // Silently ignore the parameter as it may have been introduced by new
                // specifications
                Context.getCurrentLogger()
                        .log(
                                Level.FINE,
                                "The \"{0}\" attribute is not supported yet for cookie settings. Ignoring it.",
                                pair.getName());
            }
        }

        return result;
    }

    private static int fromExpirationDateToMaxAge(final Date expires) {
        final Date current = new Date(System.currentTimeMillis());

        return DateUtils.after(current, expires)
                ? (int) ((expires.getTime() - current.getTime()) / 1000)
                : 0;
    }

    private static Date parseExpiresDate(final Parameter pair) {
        Date expires = DateUtils.parse(pair.getValue(), FORMAT_RFC_1036);
        if (expires == null) {
            expires = DateUtils.parse(pair.getValue(), FORMAT_RFC_1123);
        }
        if (expires == null) {
            expires = DateUtils.parse(pair.getValue(), FORMAT_ASC_TIME);
        }
        return expires;
    }
}
