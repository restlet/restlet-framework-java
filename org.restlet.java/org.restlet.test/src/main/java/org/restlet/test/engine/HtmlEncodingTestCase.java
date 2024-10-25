/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 *
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.restlet.engine.util.StringUtils;

import java.util.stream.Stream;

/**
 * Unit tests for the HTML encoding/decoding.
 *
 * @author Thierry Boileau
 */
public class HtmlEncodingTestCase {

    private static final String MIXED_ENCODED_STRING = "<test>azertyéè@à&%ù€®&#174;&reg;</test>&&&;&testest";
    private static final String ENCODED_STRING = "&lt;test&gt;azerty&eacute;&egrave;@&agrave;&amp;%&ugrave;&euro;&reg;&amp;#174;&amp;reg;&lt;/test&gt;&amp;&amp;&amp;;&amp;testest";
    private static final String DECODED_STRING = "<test>azertyéè@à&%ù€®®®</test>&&&;&testest";

    static Stream<Arguments> htmlEscapeArgumentsProvider() {
        return Stream.of(
                arguments("", ""),
                arguments(null, null),
                arguments(MIXED_ENCODED_STRING, ENCODED_STRING)
        );
    }

    @ParameterizedTest
    @MethodSource("htmlEscapeArgumentsProvider")
    public void testEncoding(final String toEscape, final String expected) {
        assertEquals(expected, StringUtils.htmlEscape(toEscape), "Error while escaping " + toEscape);
    }

    static Stream<Arguments> htmlUnescapeArgumentsProvider() {
        return Stream.of(
                arguments("", ""),
                arguments(null, null),
                arguments(MIXED_ENCODED_STRING, DECODED_STRING),
                arguments(ENCODED_STRING, MIXED_ENCODED_STRING),
                arguments(DECODED_STRING, DECODED_STRING)
        );
    }

    @ParameterizedTest
    @MethodSource("htmlUnescapeArgumentsProvider")
    public void testDecoding(final String toUnescape, final String expected) {
        assertEquals(expected, StringUtils.htmlUnescape(toUnescape), "Error while unescaping " + toUnescape);
    }

}
