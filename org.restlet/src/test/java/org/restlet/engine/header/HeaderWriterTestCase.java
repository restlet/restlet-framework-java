/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.header;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.CharacterSet;

class HeaderWriterTestCase {

    private static HeaderWriter<String> newWriter() {
        return new HeaderWriter<>() {
            @Override
            public HeaderWriter<String> append(String value) {
                return append((CharSequence) value);
            }
        };
    }

    @Test
    void appendChar_appendsSingleCharacter() {
        assertEquals("a", newWriter().append('a').toString());
    }

    @Test
    void appendCharArray_appendsAllCharacters() {
        assertEquals("abc", newWriter().append(new char[] {'a', 'b', 'c'}).toString());
    }

    @Test
    void appendCharArray_null_appendsNothing() {
        assertEquals("", newWriter().append((char[]) null).toString());
    }

    @Test
    void appendCharSequence_appendsText() {
        assertEquals("hello", newWriter().append((CharSequence) "hello").toString());
    }

    @Test
    void appendCollection_nullOrEmpty_appendsNothing() {
        assertEquals("", newWriter().append((java.util.Collection<String>) null).toString());
        assertEquals("", newWriter().append(java.util.List.of()).toString());
    }

    @Test
    void appendCollection_multipleValues_joinsWithValueSeparator() {
        assertEquals("a, b", newWriter().append(Arrays.asList("a", "b")).toString());
    }

    @Test
    void appendCollection_skipsValuesRejectedByCanWrite() {
        HeaderWriter<String> writer =
                new HeaderWriter<>() {
                    @Override
                    public HeaderWriter<String> append(String value) {
                        return append((CharSequence) value);
                    }

                    @Override
                    protected boolean canWrite(String value) {
                        return !"skip".equals(value);
                    }
                };
        writer.append(Arrays.asList("a", "skip", "b"));
        assertEquals("a, b", writer.toString());
    }

    @Test
    void appendInt_appendsDecimalRepresentation() {
        assertEquals("42", newWriter().append(42).toString());
    }

    @Test
    void appendLong_appendsDecimalRepresentation() {
        assertEquals("123456789012", newWriter().append(123456789012L).toString());
    }

    @Test
    void appendComment_plainText_isWrappedInParentheses() {
        assertEquals("(hello)", newWriter().appendComment("hello").toString());
    }

    @Test
    void appendComment_withSpecialCharacter_isQuotedPair() {
        assertEquals("(a\\)b)", newWriter().appendComment("a)b").toString());
    }

    @Test
    void appendExtension_namedValueObject_delegatesToNameValueOverload() {
        org.restlet.data.Parameter parameter = new org.restlet.data.Parameter("name", "value");
        assertEquals("name=value", newWriter().appendExtension(parameter).toString());
    }

    @Test
    void appendExtension_nullNamedValue_appendsNothing() {
        assertEquals("", newWriter().appendExtension(null).toString());
    }

    @Test
    void appendExtension_nameOnly_appendsNameOnly() {
        assertEquals("name", newWriter().appendExtension("name", null).toString());
    }

    @Test
    void appendExtension_tokenValue_appendsUnquoted() {
        assertEquals("name=value", newWriter().appendExtension("name", "value").toString());
    }

    @Test
    void appendExtension_nonTokenValue_appendsQuoted() {
        assertEquals(
                "name=\"has space\"", newWriter().appendExtension("name", "has space").toString());
    }

    @Test
    void appendExtension_emptyName_appendsNothing() {
        assertEquals("", newWriter().appendExtension("", "value").toString());
    }

    @Test
    void appendParameterSeparator_appendsSemicolon() {
        assertEquals(";", newWriter().appendParameterSeparator().toString());
    }

    @Test
    void appendProduct_nameOnly_appendsNameOnly() {
        assertEquals("Restlet", newWriter().appendProduct("Restlet", null).toString());
    }

    @Test
    void appendProduct_nameAndVersion_joinsWithSlash() {
        assertEquals("Restlet/2.7", newWriter().appendProduct("Restlet", "2.7").toString());
    }

    @Test
    void appendQuotedPair_prefixesWithBackslash() {
        assertEquals("\\\"", newWriter().appendQuotedPair('"').toString());
    }

    @Test
    void appendQuotedString_nullOrEmpty_appendsNothing() {
        assertEquals("", newWriter().appendQuotedString(null).toString());
        assertEquals("", newWriter().appendQuotedString("").toString());
    }

    @Test
    void appendQuotedString_plainText_isWrappedInQuotes() {
        assertEquals("\"hello\"", newWriter().appendQuotedString("hello").toString());
    }

    @Test
    void appendQuotedString_withEmbeddedQuote_isEscaped() {
        assertEquals("\"a\\\"b\"", newWriter().appendQuotedString("a\"b").toString());
    }

    @Test
    void appendSpace_appendsSingleSpace() {
        assertEquals(" ", newWriter().appendSpace().toString());
    }

    @Test
    void appendToken_validToken_isAppended() {
        assertEquals("abc123", newWriter().appendToken("abc123").toString());
    }

    @Test
    void appendToken_invalidToken_throwsIllegalArgumentException() {
        HeaderWriter<String> stringHeaderWriter = newWriter();
        assertThrows(
                IllegalArgumentException.class, () -> stringHeaderWriter.appendToken("has space"));
    }

    @Test
    void appendUriEncoded_encodesReservedCharacters() {
        String result = newWriter().appendUriEncoded("a b", CharacterSet.UTF_8).toString();
        assertEquals("a%20b", result);
    }

    @Test
    void appendValueSeparator_appendsCommaAndSpace() {
        assertEquals(", ", newWriter().appendValueSeparator().toString());
    }

    @Test
    void canWrite_nonNullValue_returnsTrueByDefault() {
        assertEquals("value", newWriter().append(List.of("value")).toString());
    }

    @Test
    void append_chaining_returnsSameWriterInstance() {
        HeaderWriter<String> writer = newWriter();
        assertEquals(writer, writer.append('a'));
    }

    @Test
    void appendCollection_allValuesRejected_appendsNothing() {
        HeaderWriter<String> writer =
                new HeaderWriter<>() {
                    @Override
                    public HeaderWriter<String> append(String value) {
                        return append((CharSequence) value);
                    }

                    @Override
                    protected boolean canWrite(String value) {
                        return false;
                    }
                };
        writer.append(Arrays.asList("a", "b"));
        assertTrue(writer.toString().isEmpty());
    }
}
