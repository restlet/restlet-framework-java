/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for the HTML encoding/decoding.
 *
 * @author Thierry Boileau
 */
class HtmlEncodingTestCase {

    private static final String MIXED_ENCODED_STRING =
            "<test>azertyéè@à&%ù€®&#174;&reg;</test>&&&;&testest";
    private static final String ENCODED_STRING =
            "&lt;test&gt;azerty&eacute;&egrave;@&agrave;&amp;%&ugrave;&euro;&reg;&amp;#174;&amp;reg;&lt;/test&gt;&amp;&amp;&amp;;&amp;testest";
    private static final String DECODED_STRING = "<test>azertyéè@à&%ù€®®®</test>&&&;&testest";

    static Stream<Arguments> htmlEscapeArgumentsProvider() {
        return Stream.of(
                arguments("", ""),
                arguments(null, null),
                arguments(MIXED_ENCODED_STRING, ENCODED_STRING));
    }

    @ParameterizedTest
    @MethodSource("htmlEscapeArgumentsProvider")
    void testEncoding(final String toEscape, final String expected) {
        assertEquals(
                expected, StringUtils.htmlEscape(toEscape), "Error while escaping " + toEscape);
    }

    static Stream<Arguments> htmlUnescapeArgumentsProvider() {
        return Stream.of(
                arguments("", ""),
                arguments(null, null),
                arguments(MIXED_ENCODED_STRING, DECODED_STRING),
                arguments(ENCODED_STRING, MIXED_ENCODED_STRING),
                arguments(DECODED_STRING, DECODED_STRING));
    }

    @ParameterizedTest
    @MethodSource("htmlUnescapeArgumentsProvider")
    void testDecoding(final String toUnescape, final String expected) {
        assertEquals(
                expected,
                StringUtils.htmlUnescape(toUnescape),
                "Error while unescaping " + toUnescape);
    }
}
