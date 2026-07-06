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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.restlet.data.Header;
import org.restlet.data.Parameter;

class HeaderReaderTestCase {

    @Test
    void readDate_notCookieFormat_parsesRfc1123() {
        Date date = HeaderReader.readDate("Sun, 06 Nov 1994 08:49:37 GMT", false);
        assertEquals(784111777000L, date.getTime());
    }

    @Test
    void readDate_cookieFormat_parsesRfc1036() {
        Date date = HeaderReader.readDate("Sunday, 06-Nov-94 08:49:37 GMT", true);
        assertEquals(784111777000L, date.getTime());
    }

    @Test
    void readHeaderFromCharSequence_endOfHeaders_returnsNull() throws IOException {
        assertNull(HeaderReader.readHeader(""));
    }

    @Test
    void readHeaderFromCharSequence_crlfOnly_returnsNull() throws IOException {
        assertNull(HeaderReader.readHeader("\r\n"));
    }

    @Test
    void readHeaderFromCharSequence_crWithoutLf_throws() {
        assertThrows(IOException.class, () -> HeaderReader.readHeader("\rX"));
    }

    @Test
    void readHeaderFromCharSequence_missingColon_throws() {
        assertThrows(IOException.class, () -> HeaderReader.readHeader("NameOnly"));
    }

    @Test
    void readHeaderFromCharSequence_nameOnly_returnsHeaderWithNullValue() throws IOException {
        Header header = HeaderReader.readHeader("Name:");
        assertEquals("Name", header.getName());
    }

    @Test
    void readHeaderFromCharSequence_nameAndValue_parsesBoth() throws IOException {
        Header header = HeaderReader.readHeader("Content-Type: text/plain");
        assertEquals("Content-Type", header.getName());
        assertEquals("text/plain", header.getValue());
    }

    @Test
    void readHeaderFromInputStream_nameAndValue_parsesBoth() throws IOException {
        ByteArrayInputStream is =
                new ByteArrayInputStream("Content-Type: text/plain\r\n".getBytes());
        Header header = HeaderReader.readHeader(is, new StringBuilder());
        assertEquals("Content-Type", header.getName());
        assertEquals("text/plain", header.getValue());
    }

    @Test
    void readHeaderFromInputStream_endOfHeaders_returnsNull() throws IOException {
        ByteArrayInputStream is = new ByteArrayInputStream("\r\n".getBytes());
        assertNull(HeaderReader.readHeader(is, new StringBuilder()));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
        "crWithoutLf,\rX",
        "missingColon,NameOnly",
        "missingTrailingLineFeed,Name: value\r"
    })
    void readHeaderFromInputStream_throws(final String label, final String header) {
        ByteArrayInputStream is = new ByteArrayInputStream(header.getBytes());
        assertThrows(IOException.class, () -> HeaderReader.readHeader(is, new StringBuilder()));
    }

    @Test
    void peek_atStart_returnsFirstCharacterWithoutAdvancing() {
        HeaderReader<String> reader = new HeaderReader<>("abc");
        assertEquals('a', reader.peek());
        assertEquals('a', reader.peek());
    }

    @Test
    void peek_emptyHeader_returnsMinusOne() {
        HeaderReader<String> reader = new HeaderReader<>("");
        assertEquals(-1, reader.peek());
    }

    @Test
    void read_advancesIndexUntilEnd() {
        HeaderReader<String> reader = new HeaderReader<>("ab");
        assertEquals('a', reader.read());
        assertEquals('b', reader.read());
        assertEquals(-1, reader.read());
    }

    @Test
    void unread_movesIndexBackOneCharacter() {
        HeaderReader<String> reader = new HeaderReader<>("abc");
        reader.read();
        reader.read();
        reader.unread();
        assertEquals('b', reader.read());
    }

    @Test
    void markAndReset_repositionsToMarkedIndex() {
        HeaderReader<String> reader = new HeaderReader<>("abc");
        reader.read();
        reader.mark();
        reader.read();
        reader.reset();
        assertEquals('b', reader.read());
    }

    @Test
    void readToken_stopsAtNonTokenCharacter() {
        HeaderReader<String> reader = new HeaderReader<>("abc=def");
        assertEquals("abc", reader.readToken());
        assertEquals('=', reader.read());
    }

    @Test
    void readDigits_stopsAtNonTokenCharacter() {
        HeaderReader<String> reader = new HeaderReader<>("123;abc");
        assertEquals("123", reader.readDigits());
        assertEquals(';', reader.read());
    }

    @Test
    void readQuotedString_simpleValue_returnsUnquoted() throws IOException {
        HeaderReader<String> reader = new HeaderReader<>("\"hello\"");
        assertEquals("hello", reader.readQuotedString());
    }

    @Test
    void readQuotedString_withEscapedCharacter_unescapesIt() throws IOException {
        HeaderReader<String> reader = new HeaderReader<>("\"a\\\"b\"");
        assertEquals("a\"b", reader.readQuotedString());
    }

    @Test
    void readQuotedString_withoutLeadingQuote_throws() {
        HeaderReader<String> reader = new HeaderReader<>("hello\"");
        assertThrows(IOException.class, reader::readQuotedString);
    }

    @Test
    void readQuotedString_unterminated_throws() {
        HeaderReader<String> reader = new HeaderReader<>("\"hello");
        assertThrows(IOException.class, reader::readQuotedString);
    }

    @Test
    void readActualNamedValue_quotedString_returnsUnquoted() throws IOException {
        HeaderReader<String> reader = new HeaderReader<>("\"quoted value\"");
        assertEquals("quoted value", reader.readActualNamedValue());
    }

    @Test
    void readActualNamedValue_token_returnsToken() throws IOException {
        HeaderReader<String> reader = new HeaderReader<>("token123");
        assertEquals("token123", reader.readActualNamedValue());
    }

    @Test
    void readComment_simpleComment_returnsContent() throws IOException {
        HeaderReader<String> reader = new HeaderReader<>("(a comment)");
        assertEquals("a comment", reader.readComment());
    }

    @Test
    void readComment_nestedComment_returnsFullContent() throws IOException {
        HeaderReader<String> reader = new HeaderReader<>("(outer (inner) end)");
        assertEquals("outer (inner) end", reader.readComment());
    }

    @Test
    void readComment_withoutLeadingParenthesis_throws() {
        HeaderReader<String> reader = new HeaderReader<>("no parenthesis)");
        assertThrows(IOException.class, reader::readComment);
    }

    @Test
    void readComment_unterminated_throws() {
        HeaderReader<String> reader = new HeaderReader<>("(unterminated");
        assertThrows(IOException.class, reader::readComment);
    }

    @Test
    void readRawText_stopsAtSpace() {
        HeaderReader<String> reader = new HeaderReader<>("abc def");
        assertEquals("abc", reader.readRawText());
    }

    @Test
    void readRawText_emptyInput_returnsNull() {
        HeaderReader<String> reader = new HeaderReader<>("");
        assertNull(reader.readRawText());
    }

    @Test
    void readRawValue_trimsTrailingSpacesAndStopsAtComma() {
        HeaderReader<String> reader = new HeaderReader<>("  value with spaces  , next");
        assertEquals("value with spaces", reader.readRawValue());
    }

    @Test
    void readRawValue_emptyInput_returnsNull() {
        HeaderReader<String> reader = new HeaderReader<>("");
        assertNull(reader.readRawValue());
    }

    @Test
    void readParameter_nameOnly_returnsNullValueParameter() throws IOException {
        HeaderReader<Parameter> reader = new HeaderReader<>("flag");
        Parameter parameter = reader.readParameter();
        assertEquals("flag", parameter.getName());
        assertNull(parameter.getValue());
    }

    @Test
    void readParameter_nameAndValue_parsesBoth() throws IOException {
        HeaderReader<Parameter> reader = new HeaderReader<>("name=value");
        Parameter parameter = reader.readParameter();
        assertEquals("name", parameter.getName());
        assertEquals("value", parameter.getValue());
    }

    @Test
    void readParameter_emptyName_throws() {
        HeaderReader<Parameter> reader = new HeaderReader<>("=value");
        assertThrows(IOException.class, reader::readParameter);
    }

    @Test
    void skipParameterSeparator_semicolonPresent_returnsTrueAndSkipsSpaces() {
        HeaderReader<String> reader = new HeaderReader<>(" ;  rest");
        assertTrue(reader.skipParameterSeparator());
        assertEquals('r', reader.read());
    }

    @Test
    void skipParameterSeparator_noSeparator_returnsFalseAndDoesNotConsume() {
        HeaderReader<String> reader = new HeaderReader<>("rest");
        assertFalse(reader.skipParameterSeparator());
        assertEquals('r', reader.read());
    }

    @Test
    void skipValueSeparator_commaPresent_returnsTrue() {
        HeaderReader<String> reader = new HeaderReader<>(" ,  rest");
        assertTrue(reader.skipValueSeparator());
        assertEquals('r', reader.read());
    }

    @Test
    void skipValueSeparator_noSeparator_returnsFalse() {
        HeaderReader<String> reader = new HeaderReader<>("rest");
        assertFalse(reader.skipValueSeparator());
    }

    @Test
    void skipSpaces_leadingSpaces_returnsTrueAndSkipsThem() {
        HeaderReader<String> reader = new HeaderReader<>("   rest");
        assertTrue(reader.skipSpaces());
        assertEquals('r', reader.read());
    }

    @Test
    void skipSpaces_noSpaces_returnsFalse() {
        HeaderReader<String> reader = new HeaderReader<>("rest");
        assertFalse(reader.skipSpaces());
    }

    @Test
    void readValue_defaultImplementation_returnsNull() throws IOException {
        HeaderReader<String> reader = new HeaderReader<>("anything");
        assertNull(reader.readValue());
    }

    @Test
    void readValues_defaultImplementation_returnsEmptyList() {
        HeaderReader<String> reader = new HeaderReader<>("anything");
        List<String> values = reader.readValues();
        assertTrue(values.isEmpty());
    }

    @Test
    void addValues_customReaderWithTokenValues_addsEachNonNullValue() {
        HeaderReader<String> reader =
                new HeaderReader<>("a, b, a") {
                    @Override
                    public String readValue() {
                        return readToken();
                    }
                };
        List<String> values = new ArrayList<>();
        reader.addValues(values);
        assertEquals(List.of("a", "b"), values);
    }

    @Test
    void canAdd_duplicateValue_isExcludedByDefault() {
        HeaderReader<String> reader = new HeaderReader<>("");
        List<String> existing = new ArrayList<>(List.of("a"));
        assertFalse(reader.canAdd("a", existing));
        assertTrue(reader.canAdd("b", existing));
        assertFalse(reader.canAdd(null, existing));
    }

    @Test
    void constructor_nullHeader_behavesAsEmpty() {
        HeaderReader<String> reader = new HeaderReader<>(null);
        assertEquals(-1, reader.peek());
        assertEquals(-1, reader.read());
    }
}
