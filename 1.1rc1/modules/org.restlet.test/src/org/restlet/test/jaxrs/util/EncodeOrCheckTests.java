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
package org.restlet.test.jaxrs.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.restlet.ext.jaxrs.internal.util.EncodeOrCheck;

/**
 * @author Stephan Koops
 * @see EncodeOrCheck
 */
@SuppressWarnings("all")
public class EncodeOrCheckTests extends TestCase {

    static final Method FRAGMENT;

    static final Method FULL_MATRIX;

    static final Method FULL_QUERY;

    static final Method HOST;

    static final Method NAME_OR_VALUE;

    static final Method PATH_SEGMENT_WITH_MATRIX;

    static final Method PATH_WITHOUT_MATRIX;

    static final Method SCHEME;

    static final Method USER_INFO;

    static {
        try {
            FRAGMENT = EncodeOrCheck.class.getMethod("fragment",
                    CharSequence.class, Boolean.TYPE);
            FULL_MATRIX = EncodeOrCheck.class.getMethod("fullMatrix",
                    CharSequence.class, Boolean.TYPE);
            FULL_QUERY = EncodeOrCheck.class.getMethod("fullQuery",
                    CharSequence.class, Boolean.TYPE);
            HOST = EncodeOrCheck.class.getMethod("host", String.class);
            NAME_OR_VALUE = EncodeOrCheck.class.getMethod("nameOrValue",
                    Object.class, Boolean.TYPE, String.class);
            PATH_SEGMENT_WITH_MATRIX = EncodeOrCheck.class.getMethod(
                    "pathSegmentWithMatrix", CharSequence.class, Boolean.TYPE);
            PATH_WITHOUT_MATRIX = EncodeOrCheck.class.getMethod(
                    "pathWithoutMatrix", CharSequence.class);
            SCHEME = EncodeOrCheck.class.getMethod("scheme", String.class);
            USER_INFO = EncodeOrCheck.class.getMethod("userInfo",
                    CharSequence.class, Boolean.TYPE);
        } catch (final SecurityException e) {
            throw new RuntimeException(e);
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param method
     *            static method. The in value is the first parameter. If the
     *            method has two or three parameters, the second is the encode
     *            value. If it has three arguments, a generic error message is
     *            used for it.
     * @param in
     * @param encode
     *            must not be null, if the method has more than one argument.
     * @param out
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws RuntimeException
     */
    private void check(Method method, String in, Boolean encode, String out) {
        Object result;
        try {
            final int paramCount = method.getParameterTypes().length;
            if (paramCount == 0) {
                throw new AssertionFailedError(
                        ("The method " + method + " must have between 1 and 3 parameters"));
            } else if (paramCount == 1) {
                result = method.invoke(null, in);
            } else if ((paramCount == 2) && (encode != null)) {
                result = method.invoke(null, in, encode);
            } else if ((paramCount == 3) && (encode != null)) {
                result = method.invoke(null, in, encode,
                        "{generic test error message}");
            } else {
                throw new AssertionFailedError(
                        ("The method " + method + " has to much parameters"));
            }
            if (out == null) {
                fail("must throw an IllegalArgumentException for \"" + in
                        + "\" and encode = " + encode);
            }
            assertEquals(out, result != null ? result.toString() : null);
        } catch (final InvocationTargetException e) {
            if (!(e.getCause() instanceof IllegalArgumentException)) {
                throw (RuntimeException) e.getCause();
            }
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /** encoding necessary */
    void checkEncoding(Method method, String in, String encodedOut) {
        check(method, in, true, encodedOut);
        check(method, in, false, null);
    }

    void checkForInvalidCharFail(String uriPart) {
        try {
            EncodeOrCheck.checkForInvalidUriChars(uriPart, -1, "");
            fail("\"" + uriPart
                    + "\" contains an invalid char. The test must fail");
        } catch (final IllegalArgumentException e) {
            // wonderful
        }
    }

    /**
     * in String full invalid
     */
    void checkInvalid(Method method, String in) {
        check(method, in, true, null);
        check(method, in, false, null);
    }

    /**
     * No encoding necessary
     */
    void checkNoEncode(Method method, String testString) {
        check(method, testString, true, testString);
        check(method, testString, false, testString);
    }

    public void testCheckForInvalidUriChars() {
        final String allowed = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890{}";
        EncodeOrCheck.checkForInvalidUriChars(allowed, -1, "");
        EncodeOrCheck.checkForInvalidUriChars("aaaaa", -1, "");
        EncodeOrCheck.checkForInvalidUriChars("\\\\\\", -1, "");
        checkForInvalidCharFail("a:a");
        checkForInvalidCharFail("a:1");
        checkForInvalidCharFail("/a:");
        checkForInvalidCharFail("a:");
        checkForInvalidCharFail("/");
        checkForInvalidCharFail(" ");
        checkForInvalidCharFail("\0");

        checkForInvalidCharFail("abc{ }kg jj");
        EncodeOrCheck.checkForInvalidUriChars("abc{ }kgjj", -1, "test");
    }

    public void testEncodePathWithoutMatrix() {
        EncodeOrCheck.pathWithoutMatrix("");
        EncodeOrCheck.pathWithoutMatrix("%20");
    }

    public void testFragment() {
        checkNoEncode(FRAGMENT, EncodeOrCheck.UNRESERVED);
        checkInvalid(FRAGMENT, "{}");
        checkNoEncode(FRAGMENT, "%20%27HH");
        checkNoEncode(FRAGMENT, "dfd{  %K}7");
        checkInvalid(FRAGMENT, "dfd{ { %K}}}7");
        checkInvalid(FRAGMENT, "dfd{}7");
    }

    public void testFullMatrixes() {
        xtestFullQueryOrMatrix(FULL_MATRIX, ';', '&');
        checkEncoding(FULL_MATRIX, "jhg jk", "jhg%20jk");
    }

    public void testFullQueries() {
        xtestFullQueryOrMatrix(FULL_QUERY, '&', ';');
        checkEncoding(FULL_QUERY, "jhg jk", "jhg+jk");
    }

    public void testHost() throws Exception {
        checkNoEncode(HOST, "a");
        checkNoEncode(HOST, "a{sdf}f");
        checkNoEncode(HOST, "a{   }f");
        checkEncoding(HOST, "a{   } f", "a{   }%20f");
        checkNoEncode(HOST, "98.76.54.32");
        checkNoEncode(HOST, "9876:fg12::5432");
        // host = IP-literal / IPv4address / reg-name
        // IP-literal = "[" ( IPv6address / IPvFuture ) "]"
        // IPvFuture = "v" 1*HEXDIG "." 1*( unreserved / sub-delims / ":" )
        // NICE also allow national special chars etc. in a host
    }

    public void testNameOrValue() {
        checkNoEncode(NAME_OR_VALUE, "");
        checkNoEncode(NAME_OR_VALUE, "sdf");
        checkNoEncode(NAME_OR_VALUE, "sdf%20hfdf");
        checkEncoding(NAME_OR_VALUE, "abc def", "abc%20def");
        final StringBuilder reservedEnc = new StringBuilder();
        for (int i = 0; i < EncodeOrCheck.RESERVED.length(); i++) {
            EncodeOrCheck.toHex(EncodeOrCheck.RESERVED.charAt(i), reservedEnc);
        }
        checkEncoding(NAME_OR_VALUE, EncodeOrCheck.RESERVED, reservedEnc
                .toString());
    }

    public void testPathSegmentWithMatrix() {
        checkNoEncode(PATH_SEGMENT_WITH_MATRIX, "");
        checkNoEncode(PATH_SEGMENT_WITH_MATRIX, "sdf");
        checkEncoding(PATH_SEGMENT_WITH_MATRIX, "abc def", "abc%20def");
        checkNoEncode(PATH_SEGMENT_WITH_MATRIX, "abc/def");
        checkNoEncode(PATH_SEGMENT_WITH_MATRIX, "abc;1298=213/def");
    }

    public void testSchemeCheck() {
        // scheme = ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )
        EncodeOrCheck.scheme("f");
    }

    public void testUserInfo() {
        checkNoEncode(USER_INFO, "");
        // userinfo = *( unreserved / pct-encoded / sub-delims / ":" )
        checkNoEncode(USER_INFO, "a");
        checkNoEncode(USER_INFO, "a{g}a");
        checkNoEncode(USER_INFO, "a{g }a");
        checkNoEncode(USER_INFO, "user:password");
        checkEncoding(USER_INFO, "a{g } a", "a{g }%20a");
    }

    /**
     * @param delim
     *            ';' or '&'
     * @param nonDelim
     *            '&' or ';'
     */
    private void xtestFullQueryOrMatrix(Method method, char delim, char nonDelim) {
        final String nonDelimStr = (nonDelim == ';' ? "%3B" : "%26");
        final String str = "jshfk=kzi" + delim + "hk=" + delim + "k" + delim
                + delim;
        checkNoEncode(method, str);
        checkEncoding(method, str + nonDelim, str + nonDelimStr);
        checkNoEncode(method, "");
        checkNoEncode(method, "%20");
        checkEncoding(method, delim + "=" + nonDelim + "?", delim + "="
                + nonDelimStr + "%3F");
        checkNoEncode(method, "{s&?df}");
        checkNoEncode(method, "{sdf}");
        checkNoEncode(method, delim + "{sdf}");
        checkInvalid(method, "gg{nk{}}");
        checkInvalid(method, "gg{nk{");
        checkInvalid(method, "gg}ff");
    }
}