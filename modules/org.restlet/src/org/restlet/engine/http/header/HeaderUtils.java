/**
 * Copyright 2005-2009 Noelios Technologies.
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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.CharacterSet;
import org.restlet.data.CookieSetting;
import org.restlet.data.Digest;
import org.restlet.data.Dimension;
import org.restlet.data.Disposition;
import org.restlet.data.Encoding;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.data.Warning;
import org.restlet.engine.util.DateUtils;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

/**
 * Various HTTP utilities.
 * 
 * @author Jerome Louvel
 */
public class HeaderUtils {
    /**
     * Copies the entity headers from the {@link Representation} to the
     * {@link Series}.
     * 
     * @param entity
     *            The {@link Representation} to copy the headers from.
     * @param responseHeaders
     *            The {@link Series} to copy the headers to.
     */
    public static void addEntityHeaders(Representation entity,
            Series<Parameter> responseHeaders) {
        if (entity == null) {
            responseHeaders.add(HeaderConstants.HEADER_CONTENT_LENGTH, "0");
        } else {
            if (entity.getExpirationDate() != null) {
                responseHeaders.add(HeaderConstants.HEADER_EXPIRES, DateUtils
                        .format(entity.getExpirationDate()));
            }

            if (!entity.getEncodings().isEmpty()) {
                final StringBuilder value = new StringBuilder();
                for (final Encoding encoding : entity.getEncodings()) {
                    if (!encoding.equals(Encoding.IDENTITY)) {
                        if (value.length() > 0) {
                            value.append(", ");
                        }
                        value.append(encoding.getName());
                    }
                }
                if (value.length() > 0) {
                    responseHeaders.add(
                            HeaderConstants.HEADER_CONTENT_ENCODING, value
                                    .toString());
                }

            }

            if (!entity.getLanguages().isEmpty()) {
                final StringBuilder value = new StringBuilder();
                for (int i = 0; i < entity.getLanguages().size(); i++) {
                    if (i > 0) {
                        value.append(", ");
                    }
                    value.append(entity.getLanguages().get(i).getName());
                }
                responseHeaders.add(HeaderConstants.HEADER_CONTENT_LANGUAGE,
                        value.toString());
            }

            if (entity.getMediaType() != null) {
                final StringBuilder contentType = new StringBuilder(entity
                        .getMediaType().getName());

                if (entity.getCharacterSet() != null) {
                    // Specify the character set parameter
                    contentType.append("; charset=").append(
                            entity.getCharacterSet().getName());
                }

                responseHeaders.add(HeaderConstants.HEADER_CONTENT_TYPE,
                        contentType.toString());
            }

            if (entity.getModificationDate() != null) {
                responseHeaders.add(HeaderConstants.HEADER_LAST_MODIFIED,
                        HeaderUtils.formatDate(entity.getModificationDate(),
                                false));
            }

            if (entity.getTag() != null) {
                responseHeaders.add(HeaderConstants.HEADER_ETAG, entity
                        .getTag().format());
            }
            long size = entity.getAvailableSize();
            if (size != Representation.UNKNOWN_SIZE) {
                responseHeaders.add(HeaderConstants.HEADER_CONTENT_LENGTH, Long
                        .toString(size));
            }

            if (entity.getIdentifier() != null) {
                responseHeaders.add(HeaderConstants.HEADER_CONTENT_LOCATION,
                        entity.getIdentifier().toString());
            }

            if (entity.getDisposition() != null
                    && !Disposition.TYPE_NONE.equals(entity.getDisposition()
                            .getType())) {
                responseHeaders.add(HeaderConstants.HEADER_CONTENT_DISPOSITION,
                        DispositionUtils.format(entity.getDisposition()));
            }

            if (entity.getRange() != null) {
                try {
                    responseHeaders.add(HeaderConstants.HEADER_CONTENT_RANGE,
                            RangeUtils.formatContentRange(entity.getRange(),
                                    entity.getSize()));
                } catch (Exception e) {
                    Context
                            .getCurrentLogger()
                            .log(
                                    Level.WARNING,
                                    "Unable to format the HTTP Content-Range header",
                                    e);
                }
            }

            // [ifndef gwt]
            if (entity.getDigest() != null
                    && Digest.ALGORITHM_MD5.equals(entity.getDigest()
                            .getAlgorithm())) {
                responseHeaders.add(HeaderConstants.HEADER_CONTENT_MD5,
                        new String(org.restlet.engine.util.Base64.encode(entity
                                .getDigest().getValue(), false)));
            }
            // [enddef]
        }
    }

    // [ifndef gwt] method
    /**
     * Copies the headers from the {@link Response} to the given {@link Series}.
     * 
     * @param response
     *            The {@link Response} to copy the headers from.
     * @param responseHeaders
     *            The {@link Series} to copy the headers to.
     * @throws IllegalArgumentException
     */
    public static void addResponseHeaders(Response response,
            Series<Parameter> responseHeaders) throws IllegalArgumentException {
        if (response.getStatus().equals(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED)
                || Method.OPTIONS.equals(response.getRequest().getMethod())) {
            // Format the "Allow" header
            StringBuilder sb = new StringBuilder();
            boolean first = true;

            for (final Method method : response.getAllowedMethods()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }

                sb.append(method.getName());
            }

            responseHeaders.add(HeaderConstants.HEADER_ALLOW, sb.toString());
        }

        // Add the date
        response.setDate(new Date());
        responseHeaders.add(HeaderConstants.HEADER_DATE, DateUtils
                .format(response.getDate()));

        // Add the age
        if (response.getAge() > 0) {
            responseHeaders.add(HeaderConstants.HEADER_AGE, Integer
                    .toString(response.getAge()));
        }

        // Add the retry after date
        if (response.getRetryAfter() != null) {
            responseHeaders.add(HeaderConstants.HEADER_RETRY_AFTER, DateUtils
                    .format(response.getRetryAfter()));
        }

        // Add the cookie settings
        List<CookieSetting> cookies = response.getCookieSettings();
        for (int i = 0; i < cookies.size(); i++) {
            responseHeaders.add(HeaderConstants.HEADER_SET_COOKIE, CookieUtils
                    .format(cookies.get(i)));
        }

        // Set the location URI (for redirections or creations)
        if (response.getLocationRef() != null) {
            // The location header must contain an absolute URI.
            responseHeaders.add(HeaderConstants.HEADER_LOCATION, response
                    .getLocationRef().getTargetRef().toString());
        }

        // Set the security data
        if (response.getChallengeRequests() != null) {
            for (final ChallengeRequest challengeRequest : response
                    .getChallengeRequests()) {
                responseHeaders.add(HeaderConstants.HEADER_WWW_AUTHENTICATE,
                        org.restlet.client.engine.security.AuthenticatorUtils
                                .formatRequest(challengeRequest, response,
                                        responseHeaders));
            }
        }

        if (response.getProxyChallengeRequests() != null) {
            for (final ChallengeRequest challengeRequest : response
                    .getProxyChallengeRequests()) {
                responseHeaders.add(HeaderConstants.HEADER_PROXY_AUTHENTICATE,
                        org.restlet.client.engine.security.AuthenticatorUtils
                                .formatRequest(challengeRequest, response,
                                        responseHeaders));
            }
        }

        // Send the Vary header only to none-MSIE user agents as MSIE seems
        // to support partially and badly this header (cf issue 261).
        if (!((response.getRequest().getClientInfo().getAgent() != null) && response
                .getRequest().getClientInfo().getAgent().contains("MSIE"))) {
            // Add the Vary header if content negotiation was used
            final Set<Dimension> dimensions = response.getDimensions();
            final String vary = HeaderUtils.createVaryHeader(dimensions);
            if (vary != null) {
                responseHeaders.add(HeaderConstants.HEADER_VARY, vary);
            }
        }

        // Add the accept-ranges header
        if (response.getServerInfo().isAcceptingRanges()) {
            responseHeaders.add(HeaderConstants.HEADER_ACCEPT_RANGES, "bytes");
        }

        // Add the warning headers
        if (!response.getWarnings().isEmpty()) {
            for (Warning warning : response.getWarnings()) {
                responseHeaders.add(HeaderConstants.HEADER_WARNING,
                        WarningUtils.format(warning));
            }
        }

        // Add the Cache-control headers
        if (!response.getCacheDirectives().isEmpty()) {
            responseHeaders.add(HeaderConstants.HEADER_CACHE_CONTROL,
                    CacheControlUtils.format(response.getCacheDirectives()));
        }

        // Add the Authentication-Info header
        if (response.getAuthenticationInfo() != null) {
            try {
                responseHeaders.add(HeaderConstants.HEADER_AUTHENTICATION_INFO,
                        org.restlet.engine.security.AuthenticatorUtils
                                .formatAuthenticationInfo(response
                                        .getAuthenticationInfo()));
            } catch (IOException e) {
                Context.getCurrentLogger().log(Level.WARNING,
                        "Unable to write the Authentication-Info header", e);
            }
        }
    }

    /**
     * Appends a source string as an HTTP quoted string.
     * 
     * @param source
     *            The unquoted source string.
     * @param destination
     *            The destination to append to.
     * @throws IOException
     */
    public static Appendable appendQuote(CharSequence source,
            Appendable destination) throws IOException {
        destination.append('"');

        char c;
        for (int i = 0; i < source.length(); i++) {
            c = source.charAt(i);

            if (c == '"') {
                destination.append("\\\"");
            } else if (c == '\\') {
                destination.append("\\\\");
            } else {
                destination.append(c);
            }
        }

        destination.append('"');
        return destination;
    }

    /**
     * Appends a source string as an URI encoded string.
     * 
     * @param source
     *            The source string to format.
     * @param destination
     *            The appendable destination.
     * @param characterSet
     *            The supported character encoding.
     * @throws IOException
     */
    public static Appendable appendUriEncoded(CharSequence source,
            Appendable destination, CharacterSet characterSet)
            throws IOException {
        destination.append(Reference.encode(source.toString(), characterSet));
        return destination;
    }

    /**
     * Creates a vary header from the given dimensions.
     * 
     * @param dimensions
     *            The dimensions to copy to the response.
     * @return Returns the Vary header or null, if dimensions is null or empty.
     */
    public static String createVaryHeader(Collection<Dimension> dimensions) {
        String vary = null;
        if ((dimensions != null) && !dimensions.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
            boolean first = true;

            if (dimensions.contains(Dimension.CLIENT_ADDRESS)
                    || dimensions.contains(Dimension.TIME)
                    || dimensions.contains(Dimension.UNSPECIFIED)) {
                // From an HTTP point of view the representations can
                // vary in unspecified ways
                vary = "*";
            } else {
                for (final Dimension dim : dimensions) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(", ");
                    }

                    if (dim == Dimension.CHARACTER_SET) {
                        sb.append(HeaderConstants.HEADER_ACCEPT_CHARSET);
                    } else if (dim == Dimension.CLIENT_AGENT) {
                        sb.append(HeaderConstants.HEADER_USER_AGENT);
                    } else if (dim == Dimension.ENCODING) {
                        sb.append(HeaderConstants.HEADER_ACCEPT_ENCODING);
                    } else if (dim == Dimension.LANGUAGE) {
                        sb.append(HeaderConstants.HEADER_ACCEPT_LANGUAGE);
                    } else if (dim == Dimension.MEDIA_TYPE) {
                        sb.append(HeaderConstants.HEADER_ACCEPT);
                    } else if (dim == Dimension.AUTHORIZATION) {
                        sb.append(HeaderConstants.HEADER_AUTHORIZATION);
                    }
                }
                vary = sb.toString();
            }
        }
        return vary;
    }

    /**
     * Formats a date as a header string.
     * 
     * @param date
     *            The date to format.
     * @param cookie
     *            Indicates if the date should be in the cookie format.
     * @return The formatted date.
     */
    public static String formatDate(Date date, boolean cookie) {
        if (cookie) {
            return DateUtils.format(date, DateUtils.FORMAT_RFC_1036.get(0));
        }

        return DateUtils.format(date, DateUtils.FORMAT_RFC_1123.get(0));
    }

    /**
     * Formats a product description.
     * 
     * @param nameToken
     *            The product name token.
     * @param versionToken
     *            The product version token.
     * @param destination
     *            The appendable destination;
     * @throws IOException
     */
    public static void formatProduct(CharSequence nameToken,
            CharSequence versionToken, Appendable destination)
            throws IOException {
        if (!isToken(nameToken)) {
            throw new IllegalArgumentException(
                    "Invalid product name detected. Only token characters are allowed.");
        }

        destination.append(nameToken);

        if (versionToken != null) {
            if (!isToken(versionToken)) {
                throw new IllegalArgumentException(
                        "Invalid product version detected. Only token characters are allowed.");
            }

            destination.append('/').append(versionToken);
        }
    }

    /**
     * Returns the content length of the request entity if know,
     * {@link Representation#UNKNOWN_SIZE} otherwise.
     * 
     * @return The request content length.
     */
    public static long getContentLength(Series<Parameter> headers) {
        long contentLength = Representation.UNKNOWN_SIZE;

        // Extract the content length header
        for (Parameter header : headers) {
            if (header.getName().equalsIgnoreCase(
                    HeaderConstants.HEADER_CONTENT_LENGTH)) {
                try {
                    contentLength = Long.parseLong(header.getValue());
                } catch (NumberFormatException e) {
                    contentLength = Representation.UNKNOWN_SIZE;
                }
            }
        }

        return contentLength;
    }

    /**
     * Indicates if the given character is alphabetical (a-z or A-Z).
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is alphabetical (a-z or A-Z).
     */
    public static boolean isAlpha(int character) {
        return isUpperCase(character) || isLowerCase(character);
    }

    /**
     * Indicates if the given character is in ASCII range.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is in ASCII range.
     */
    public static boolean isAsciiChar(int character) {
        return (character >= 0) && (character <= 127);
    }

    /**
     * Indicates if the given character is a carriage return.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a carriage return.
     */
    public static boolean isCarriageReturn(int character) {
        return (character == 13);
    }

    /**
     * Indicates if the entity is chunked.
     * 
     * @return True if the entity is chunked.
     */
    public static boolean isChunkedEncoding(Series<Parameter> headers) {
        final String header = headers.getFirstValue(
                HeaderConstants.HEADER_TRANSFER_ENCODING, true);
        return "chunked".equalsIgnoreCase(header);
    }

    /**
     * Indicates if the given character is a comment text. It means
     * {@link #isText(int)} returns true and the character is not '(' or ')'.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a quoted text.
     */
    public static boolean isCommentText(int character) {
        return isText(character) && (character != '(') && (character != ')');
    }

    /**
     * Indicates if the connection must be closed.
     * 
     * @param headers
     *            The headers to test.
     * @return True if the connection must be closed.
     */
    public static boolean isConnectionClose(Series<Parameter> headers) {
        String header = headers.getFirstValue(
                HeaderConstants.HEADER_CONNECTION, true);
        return "close".equalsIgnoreCase(header);
    }

    /**
     * Indicates if the given character is a control character.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a control character.
     */
    public static boolean isControlChar(int character) {
        return ((character >= 0) && (character <= 31)) || (character == 127);
    }

    /**
     * Indicates if the given character is a digit (0-9).
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a digit (0-9).
     */
    public static boolean isDigit(int character) {
        return (character >= '0') && (character <= '9');
    }

    /**
     * Indicates if the given character is a double quote.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a double quote.
     */
    public static boolean isDoubleQuote(int character) {
        return (character == 34);
    }

    /**
     * Indicates if the given character is an horizontal tab.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is an horizontal tab.
     */
    public static boolean isHorizontalTab(int character) {
        return (character == 9);
    }

    /**
     * Indicates if the given character is a value separator.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a value separator.
     */
    public static boolean isLinearWhiteSpace(int character) {
        return (HeaderUtils.isCarriageReturn(character)
                || HeaderUtils.isSpace(character)
                || HeaderUtils.isLineFeed(character) || HeaderUtils
                .isHorizontalTab(character));
    }

    /**
     * Indicates if the given character is a line feed.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a line feed.
     */
    public static boolean isLineFeed(int character) {
        return (character == 10);
    }

    /**
     * Indicates if the given character is lower case (a-z).
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is lower case (a-z).
     */
    public static boolean isLowerCase(int character) {
        return (character >= 'a') && (character <= 'z');
    }

    /**
     * Indicates if the given character is a quoted text. It means
     * {@link #isText(int)} returns true and {@link #isDoubleQuote(int)} returns
     * false.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a quoted text.
     */
    public static boolean isQuotedText(int character) {
        return isText(character) && !isDoubleQuote(character);
    }

    /**
     * Indicates if the given character is a separator.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a separator.
     */
    public static boolean isSeparator(int character) {
        switch (character) {
        case '(':
        case ')':
        case '<':
        case '>':
        case '@':
        case ',':
        case ';':
        case ':':
        case '\\':
        case '"':
        case '/':
        case '[':
        case ']':
        case '?':
        case '=':
        case '{':
        case '}':
        case ' ':
        case '\t':
            return true;

        default:
            return false;
        }
    }

    /**
     * Indicates if the given character is a space.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a space.
     */
    public static boolean isSpace(int character) {
        return (character == 32);
    }

    /**
     * Indicates if the given character is textual (ASCII and not a control
     * character).
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is textual (ASCII and not a control
     *         character).
     */
    public static boolean isText(int character) {
        return isAsciiChar(character) && !isControlChar(character);
    }

    /**
     * Indicates if the token is valid.<br>
     * Only contains valid token characters.
     * 
     * @param token
     *            The token to check
     * @return True if the token is valid.
     */
    public static boolean isToken(CharSequence token) {
        for (int i = 0; i < token.length(); i++) {
            if (!isTokenChar(token.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Indicates if the given character is a token character (text and not a
     * separator).
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a token character (text and not a
     *         separator).
     */
    public static boolean isTokenChar(int character) {
        return isText(character) && !isSeparator(character);
    }

    /**
     * Indicates if the given character is upper case (A-Z).
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is upper case (A-Z).
     */
    public static boolean isUpperCase(int character) {
        return (character >= 'A') && (character <= 'Z');
    }

    /**
     * Indicates if the given character is a value separator.
     * 
     * @param character
     *            The character to test.
     * @return True if the given character is a value separator.
     */
    public static boolean isValueSeparator(int character) {
        return (character == ',');
    }

    /**
     * Parses a date string.
     * 
     * @param date
     *            The date string to parse.
     * @param cookie
     *            Indicates if the date is in the cookie format.
     * @return The parsed date.
     */
    public static Date parseDate(String date, boolean cookie) {
        if (cookie) {
            return DateUtils.parse(date, DateUtils.FORMAT_RFC_1036);
        }

        return DateUtils.parse(date, DateUtils.FORMAT_RFC_1123);
    }

    /**
     * Read a header. Return null if the last header was already read.
     * 
     * @param is
     *            The message input stream.
     * @param sb
     *            The string builder to reuse.
     * @return The header read or null.
     * @throws IOException
     */
    public static Parameter readHeader(InputStream is, StringBuilder sb)
            throws IOException {
        Parameter result = null;

        // Detect the end of headers
        int next = is.read();
        if (HeaderUtils.isCarriageReturn(next)) {
            next = is.read();
            if (!HeaderUtils.isLineFeed(next)) {
                throw new IOException(
                        "Invalid end of headers. Line feed missing after the carriage return.");
            }
        } else {
            result = new Parameter();

            // Parse the header name
            while ((next != -1) && (next != ':')) {
                sb.append((char) next);
                next = is.read();
            }

            if (next == -1) {
                throw new IOException(
                        "Unable to parse the header name. End of stream reached too early.");
            }

            result.setName(sb.toString());
            sb.delete(0, sb.length());

            next = is.read();
            while (HeaderUtils.isSpace(next)) {
                // Skip any separator space between colon and header value
                next = is.read();
            }

            // Parse the header value
            while ((next != -1) && (!HeaderUtils.isCarriageReturn(next))) {
                sb.append((char) next);
                next = is.read();
            }

            if (next == -1) {
                throw new IOException(
                        "Unable to parse the header value. End of stream reached too early.");
            }
            next = is.read();

            if (HeaderUtils.isLineFeed(next)) {
                result.setValue(sb.toString());
                sb.delete(0, sb.length());
            } else {
                throw new IOException(
                        "Unable to parse the HTTP header value. The carriage return must be followed by a line feed.");
            }
        }

        return result;
    }

    // [ifndef gwt] method
    /**
     * Writes a new line.
     * 
     * @param os
     *            The output stream.
     * @throws IOException
     */
    public static void writeCRLF(OutputStream os) throws IOException {
        os.write(13); // CR
        os.write(10); // LF
    }

    // [ifndef gwt] method
    /**
     * Writes a header line.
     * 
     * @param header
     *            The header to write.
     * @param os
     *            The output stream.
     * @throws IOException
     */
    public static void writeHeader(Parameter header, OutputStream os)
            throws IOException {
        os.write(header.getName().getBytes());
        os.write(':');
        os.write(' ');

        if (header.getValue() != null) {
            os.write(header.getValue().getBytes());
        }

        os.write(13); // CR
        os.write(10); // LF
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private HeaderUtils() {
    }
}
