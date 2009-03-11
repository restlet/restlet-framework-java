/**
 * Copyright 2005-2009 Noelios Technologies.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.rdf.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.restlet.data.Reference;
import org.restlet.ext.rdf.Graph;
import org.restlet.ext.rdf.GraphHandler;
import org.restlet.ext.rdf.Literal;
import org.restlet.representation.Representation;

/**
 * Handler of content according to the RDF N3 notation.
 */
public class RdfN3ContentHandler extends GraphHandler {
    private static final int BUFFER_SIZE = 4096;

    public static final int EOF = 0;

    /**
     * Returns true if the given character is alphanumeric.
     * 
     * @param c
     *            The given character to check.
     * @return true if the given character is alphanumeric.
     */
    public static boolean isAlphaNum(int c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                || (c >= '0' && c <= '9');
    }

    /**
     * Returns true if the given character is a delimiter.
     * 
     * @param c
     *            The given character to check.
     * @return true if the given character is a delimiter.
     */
    public static boolean isDelimiter(int c) {
        return isWhiteSpace(c) || c == '^' || c == '!' || c == '=' || c == '<'
                || c == '"' || c == '{' || c == '}' || c == '[' || c == ']'
                || c == '(' || c == ')' || c == '.';
    }

    /**
     * Returns true if the given character is a whitespace.
     * 
     * @param c
     *            The given character to check.
     * @return true if the given character is a whitespace.
     */
    public static boolean isWhiteSpace(int c) {
        return c == ' ' || c == '\n' || c == '\r' || c == '\t';
    }

    /** Internal buffered reader. */
    private BufferedReader br;

    private final char[] buffer;

    /** The set of links to update when parsing, or to read when writing. */
    private Graph linkSet;

    /** The representation to read. */
    private Representation rdfN3Representation;

    /**
     * Index that discovers the end of the curren token and the beginning of the
     * futur one.
     */
    private int scoutIndex;

    /** Start index of current lexical unit. */
    private int startTokenIndex;

    /**
     * Constructor.
     * 
     * @param linkSet
     *            The set of links to update during the parsing.
     * @param rdfN3Representation
     *            The representation to read.
     * @throws IOException
     */
    public RdfN3ContentHandler(Graph linkSet, Representation rdfN3Representation)
            throws IOException {
        super();
        this.linkSet = linkSet;
        this.rdfN3Representation = rdfN3Representation;

        // Initialize the buffer in two parts
        this.buffer = new char[(RdfN3ContentHandler.BUFFER_SIZE + 1) * 2];
        // Mark the upper index of each part.
        this.buffer[RdfN3ContentHandler.BUFFER_SIZE] = this.buffer[2 * RdfN3ContentHandler.BUFFER_SIZE + 1] = EOF;
        this.scoutIndex = 2 * RdfN3ContentHandler.BUFFER_SIZE;
        this.startTokenIndex = 0;

        this.br = new BufferedReader(new InputStreamReader(rdfN3Representation
                .getStream()));
        parse();
    }

    /**
     * Discard all read characters. A call to {@link getCurrentToken} will
     * return a single character.
     */
    public void discard() {
        startTokenIndex = scoutIndex;
    }

    /**
     * Discard all read characters. A call to {@link getCurrentToken} will
     * return a single character.
     * 
     * @throws IOException
     */
    public void consumeWhiteSpaces() throws IOException {
        int c;
        do {
            c = step();
        } while (RdfN3ContentHandler.isWhiteSpace(c));
        discard();
    }

    /**
     * Discard all read characters until the end of the statement is reached
     * (maked by a '.').
     * 
     * @throws IOException
     */
    public void consumeStatement() throws IOException {
        if (getChar() != '.') {
            int c;
            do {
                c = step();
            } while (c != RdfN3ContentHandler.EOF && c != '.');
            step();
        }
        discard();
    }

    /**
     * Returns the current parsed character.
     * 
     * @return The current parsed character.
     */
    public char getChar() {
        return (char) buffer[scoutIndex];
    }

    /**
     * Returns the current token.
     * 
     * @return The current token.
     */
    public String getCurrentToken() {
        StringBuilder builder = new StringBuilder();
        if (startTokenIndex <= scoutIndex) {
            if (scoutIndex <= RdfN3ContentHandler.BUFFER_SIZE) {
                for (int i = startTokenIndex; i < scoutIndex; i++) {
                    builder.append((char) buffer[i]);
                }
            } else {
                for (int i = startTokenIndex; i < RdfN3ContentHandler.BUFFER_SIZE; i++) {
                    builder.append((char) buffer[i]);
                }
                for (int i = RdfN3ContentHandler.BUFFER_SIZE + 1; i < scoutIndex; i++) {
                    builder.append((char) buffer[i]);
                }
            }
        } else {
            if (startTokenIndex <= RdfN3ContentHandler.BUFFER_SIZE) {
                for (int i = startTokenIndex; i < RdfN3ContentHandler.BUFFER_SIZE; i++) {
                    builder.append((char) buffer[i]);
                }
                for (int i = RdfN3ContentHandler.BUFFER_SIZE + 1; i < (2 * RdfN3ContentHandler.BUFFER_SIZE + 1); i++) {
                    builder.append((char) buffer[i]);
                }
                for (int i = 0; i < scoutIndex; i++) {
                    builder.append((char) buffer[i]);
                }
            } else {
                for (int i = startTokenIndex; i < (2 * RdfN3ContentHandler.BUFFER_SIZE + 1); i++) {
                    builder.append((char) buffer[i]);
                }
                for (int i = 0; i < scoutIndex; i++) {
                    builder.append((char) buffer[i]);
                }
            }
        }
        return builder.toString();
    }

    @Override
    public void link(Object source, Reference typeRef, Reference target) {
        // TODO Auto-generated method stub

    }

    @Override
    public void link(Reference source, Reference typeRef, Literal target) {
        // TODO Auto-generated method stub

    }

    @Override
    public void link(Reference source, Reference typeRef, Reference target) {
        // TODO Auto-generated method stub

    }

    /**
     * @throws IOException
     * 
     */
    private void parse() throws IOException {
        parse(new Context());
    }

    /**
     * @throws IOException
     * 
     */
    private void parse(Context context) throws IOException {
        do {
            consumeWhiteSpaces();
            switch (getChar()) {
            case '@':
                parseDirective(context);
                break;
            case '#':
                parseComment();
                break;
            case '.':
                break;
            default:
                parseStatement(context);
                break;
            }
        } while (getChar() != RdfN3ContentHandler.EOF);
    }

    /**
     * 
     * @throws IOException
     */
    public void parseComment() throws IOException {
        int c;
        do {
            c = step();
        } while (c != RdfN3ContentHandler.EOF && c != '\n' && c != '\r');
        discard();
    }

    /**
     * Returns the value of the current URI.
     * 
     * @return The value of the current URI.
     * @throws IOException
     */
    public void parseDirective(Context context) throws IOException {
        // Remove the leading '@' character
        step();
        discard();
        String currentKeyword = parseToken();
        if ("base".equalsIgnoreCase(currentKeyword)) {
            consumeWhiteSpaces();
            String base = parseUri();
            Reference ref = new Reference(base);
            if (ref.isRelative()) {
                context.getBase().addSegment(base);
            } else {
                context.setBase(ref);
            }
            consumeStatement();
        } else if ("prefix".equalsIgnoreCase(currentKeyword)) {
            consumeWhiteSpaces();
            String prefix = parseToken();
            consumeWhiteSpaces();
            String uri = parseUri();
            context.getPrefixes().put(prefix, uri);
            consumeStatement();
        } else if ("keywords".equalsIgnoreCase(currentKeyword)) {
            consumeWhiteSpaces();
            int c;
            do {
                c = step();
            } while (c != RdfN3ContentHandler.EOF && c != '.');
            String strKeywords = getCurrentToken();
            String[] keywords = strKeywords.split(",");
            for (String keyword : keywords) {
                context.getKeywords().add(keyword.trim());
            }
            consumeStatement();
        }
    }

    /**
     * Returns the value of the current token.
     * 
     * @return The value of the current token.
     * @throws IOException
     */
    public String parseStatement(Context context) throws IOException {
        int c = step();
        while (c != RdfN3ContentHandler.EOF && !isDelimiter(c)) {
            c = step();
            // TODO parse statement
        }
        return getCurrentToken();
    }

    /**
     * Returns the value of the current token.
     * 
     * @return The value of the current token.
     * @throws IOException
     */
    public String parseToken() throws IOException {
        int c = step();
        while (c != RdfN3ContentHandler.EOF && !isDelimiter(c)) {
            c = step();
        }
        return getCurrentToken();
    }

    /**
     * Returns the value of the current URI.
     * 
     * @return The value of the current URI.
     * @throws IOException
     */
    public String parseUri() throws IOException {
        StringBuilder builder = new StringBuilder();
        // Suppose the current character is "<".
        int c = step();
        while (c != RdfN3ContentHandler.EOF && c != '>') {
            if (!isWhiteSpace(c)) {
                // Discard white spaces.
                builder.append((char) c);
            }
            c = step();
        }
        if (c == '>') {
            // Set the cursor at the right of the end of the Uri.
            step();
        }
        discard();

        return builder.toString();
    }

    /**
     * Read a new character.
     * 
     * @return The new read character.
     * @throws IOException
     */
    public int step() throws IOException {
        scoutIndex++;
        if (buffer[scoutIndex] == RdfN3ContentHandler.EOF) {
            if (scoutIndex == RdfN3ContentHandler.BUFFER_SIZE) {
                // Reached the end of the first part of the buffer, read into
                // the second one.
                scoutIndex++;
                int len = this.br.read(buffer, 0,
                        RdfN3ContentHandler.BUFFER_SIZE);
                if (len == -1) {
                    // End of the stream reached
                    buffer[scoutIndex] = RdfN3ContentHandler.EOF;
                } else {
                    buffer[RdfN3ContentHandler.BUFFER_SIZE + len + 1] = RdfN3ContentHandler.EOF;
                }
            } else if (scoutIndex == (2 * RdfN3ContentHandler.BUFFER_SIZE + 1)) {
                scoutIndex = 0;
                // Reached the end of the second part of the buffer, read into
                // the first one.
                int len = this.br.read(buffer, 0,
                        RdfN3ContentHandler.BUFFER_SIZE);
                if (len == -1) {
                    // End of the stream reached
                    buffer[scoutIndex] = RdfN3ContentHandler.EOF;
                } else {
                    buffer[len] = RdfN3ContentHandler.EOF;
                }
            } else {
                // Reached the end of the stream.
            }
        }

        return buffer[scoutIndex];
    }

    /**
     * Steps back of one step.
     * 
     */
    public void stepBack() {
        stepBack(1);
    }

    /**
     * Steps back.
     * 
     * @param n
     *            the number of steps to go back.
     */
    public void stepBack(int n) {
        scoutIndex -= n;
        if (scoutIndex < 0) {
            scoutIndex = RdfN3ContentHandler.BUFFER_SIZE * 2 + 1 - scoutIndex;
        }
    }

}
