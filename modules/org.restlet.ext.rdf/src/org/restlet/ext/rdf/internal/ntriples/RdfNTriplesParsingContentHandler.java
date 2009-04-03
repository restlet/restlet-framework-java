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

package org.restlet.ext.rdf.internal.ntriples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.restlet.data.Reference;
import org.restlet.ext.rdf.Graph;
import org.restlet.ext.rdf.GraphHandler;
import org.restlet.ext.rdf.LinkReference;
import org.restlet.ext.rdf.Literal;
import org.restlet.ext.rdf.internal.n3.RdfN3ParsingContentHandler;
import org.restlet.representation.Representation;

/**
 * Handler of RDF content according to the N-Triples notation.
 * 
 * @author Thierry Boileau
 */
public class RdfNTriplesParsingContentHandler extends GraphHandler {

    /** Size of the reading buffer. */
    private static final int BUFFER_SIZE = 4096;

    /** End of reading buffer marker. */
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
                || c == '(' || c == ')' || c == '.' || c == ';' || c == ','
                || c == '@';
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

    /** The reading buffer. */
    private final char[] buffer;

    /** The set of links to update when parsing. */
    private Graph linkSet;

    /** The representation to read. */
    private Representation rdfNTriplesRepresentation;

    /**
     * Index that discovers the end of the current token and the beginning of
     * the futur one.
     */
    private int scoutIndex;

    /** Start index of current lexical unit. */
    private int startTokenIndex;

    /**
     * Constructor.
     * 
     * @param linkSet
     *            The set of links to update during the parsing.
     * @param rdfNTriplesRepresentation
     *            The representation to read.
     * @throws IOException
     */
    public RdfNTriplesParsingContentHandler(Graph linkSet,
            Representation rdfNTriplesRepresentation) throws IOException {
        super();
        this.linkSet = linkSet;
        this.rdfNTriplesRepresentation = rdfNTriplesRepresentation;

        // Initialize the buffer in two parts
        this.buffer = new char[(RdfNTriplesParsingContentHandler.BUFFER_SIZE + 1) * 2];
        // Mark the upper index of each part.
        this.buffer[RdfNTriplesParsingContentHandler.BUFFER_SIZE] = this.buffer[2 * RdfNTriplesParsingContentHandler.BUFFER_SIZE + 1] = EOF;
        this.scoutIndex = 2 * RdfNTriplesParsingContentHandler.BUFFER_SIZE;
        this.startTokenIndex = 0;

        this.br = new BufferedReader(new InputStreamReader(
                this.rdfNTriplesRepresentation.getStream()));
        parse();
    }

    /**
     * Discard all read characters until the end of the statement is reached
     * (marked by a '.').
     * 
     * @throws IOException
     */
    public void consumeStatement() throws IOException {
        int c = getChar();
        while (c != RdfNTriplesParsingContentHandler.EOF && c != '.') {
            c = step();
        }
        if (getChar() == '.') {
            // A further step at the right of the statement.
            step();
        }
        discard();
    }

    /**
     * Discard all read characters. A call to {@link getCurrentToken} will
     * return a single character.
     * 
     * @throws IOException
     */
    public void consumeWhiteSpaces() throws IOException {
        while (RdfNTriplesParsingContentHandler.isWhiteSpace(getChar())) {
            step();
        }
        discard();
    }

    /**
     * Discard all read characters. A call to {@link getCurrentToken} will
     * return a single character.
     */
    public void discard() {
        startTokenIndex = scoutIndex;
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
            if (scoutIndex <= RdfNTriplesParsingContentHandler.BUFFER_SIZE) {
                for (int i = startTokenIndex; i < scoutIndex; i++) {
                    builder.append((char) buffer[i]);
                }
            } else {
                for (int i = startTokenIndex; i < RdfNTriplesParsingContentHandler.BUFFER_SIZE; i++) {
                    builder.append((char) buffer[i]);
                }
                for (int i = RdfNTriplesParsingContentHandler.BUFFER_SIZE + 1; i < scoutIndex; i++) {
                    builder.append((char) buffer[i]);
                }
            }
        } else {
            if (startTokenIndex <= RdfNTriplesParsingContentHandler.BUFFER_SIZE) {
                for (int i = startTokenIndex; i < RdfNTriplesParsingContentHandler.BUFFER_SIZE; i++) {
                    builder.append((char) buffer[i]);
                }
                for (int i = RdfNTriplesParsingContentHandler.BUFFER_SIZE + 1; i < (2 * RdfNTriplesParsingContentHandler.BUFFER_SIZE + 1); i++) {
                    builder.append((char) buffer[i]);
                }
                for (int i = 0; i < scoutIndex; i++) {
                    builder.append((char) buffer[i]);
                }
            } else {
                for (int i = startTokenIndex; i < (2 * RdfNTriplesParsingContentHandler.BUFFER_SIZE + 1); i++) {
                    builder.append((char) buffer[i]);
                }
                for (int i = 0; i < scoutIndex; i++) {
                    builder.append((char) buffer[i]);
                }
            }
        }
        // the current token is consumed.
        startTokenIndex = scoutIndex;
        return builder.toString();
    }

    @Override
    public void link(Graph source, Reference typeRef, Literal target) {
        org.restlet.Context.getCurrentLogger().warning(
                "Subjects as Graph are not supported in N-Triples.");
    }

    @Override
    public void link(Graph source, Reference typeRef, Reference target) {
        org.restlet.Context.getCurrentLogger().warning(
                "Subjects as Graph are not supported in N-Triples.");
    }

    @Override
    public void link(Reference source, Reference typeRef, Literal target) {
        this.linkSet.add(source, typeRef, target);
    }

    @Override
    public void link(Reference source, Reference typeRef, Reference target) {
        this.linkSet.add(source, typeRef, target);
    }

    /**
     * Parses the current representation.
     * 
     * @throws IOException
     */
    private void parse() throws IOException {
        // Init the reading.
        step();
        do {
            consumeWhiteSpaces();
            switch (getChar()) {
            case '#':
                parseComment();
                break;
            case '.':
                step();
                break;
            default:
                parseStatement();
                break;
            }
        } while (getChar() != RdfNTriplesParsingContentHandler.EOF);

    }

    /**
     * Parses a comment.
     * 
     * @throws IOException
     */
    public void parseComment() throws IOException {
        int c;
        do {
            c = step();
        } while (c != RdfNTriplesParsingContentHandler.EOF && c != '\n'
                && c != '\r');
        discard();
    }

    /**
     * Reads the current statement until its end, and parses it.
     * 
     * @param context
     *            The current context.
     * @throws IOException
     */
    public void parseStatement() throws IOException {
        List<Reference> lexicalUnits = new ArrayList<Reference>();
        String object = null;
        do {
            consumeWhiteSpaces();
            switch (getChar()) {
            case '<':
                stepBack();
                lexicalUnits.add(new Reference(parseUri()));
                break;
            case '_':
                lexicalUnits.add(LinkReference.createBlank(parseToken()));
                break;
            case '"':
                stepBack(1);
                discard();
                int c = getChar();
                while (c != RdfN3ParsingContentHandler.EOF && (c != '"')) {
                    c = step();
                }
                object = getCurrentToken();
                step();
                discard();
                break;
            case '.':
                break;
            case RdfNTriplesParsingContentHandler.EOF:
                break;
            default:
                break;
            }
        } while (getChar() != RdfNTriplesParsingContentHandler.EOF
                && getChar() != '.' && getChar() != '}');

        // Generate the links
        if (object != null) {
            link(lexicalUnits.get(0), lexicalUnits.get(1), new Literal(object));
        } else {
            link(lexicalUnits.get(0), lexicalUnits.get(1), lexicalUnits.get(2));
        }

    }

    /**
     * Returns the value of the current token.
     * 
     * @return The value of the current token.
     * @throws IOException
     */
    public String parseToken() throws IOException {
        int c;
        do {
            c = step();
        } while (c != RdfNTriplesParsingContentHandler.EOF && !isDelimiter(c));
        String result = getCurrentToken();
        return result;
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
        while (c != RdfNTriplesParsingContentHandler.EOF && c != '>') {
            if (!isWhiteSpace(c)) {
                // Discard white spaces.
                builder.append((char) c);
            }
            c = step();
        }
        if (c == '>') {
            // Set the cursor at the right of the uri.
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
        if (buffer[scoutIndex] == RdfNTriplesParsingContentHandler.EOF) {
            if (scoutIndex == RdfNTriplesParsingContentHandler.BUFFER_SIZE) {
                // Reached the end of the first part of the buffer, read into
                // the second one.
                scoutIndex++;
                int len = this.br.read(buffer, 0,
                        RdfNTriplesParsingContentHandler.BUFFER_SIZE);
                if (len == -1) {
                    // End of the stream reached
                    buffer[scoutIndex] = RdfNTriplesParsingContentHandler.EOF;
                } else {
                    buffer[RdfNTriplesParsingContentHandler.BUFFER_SIZE + len
                            + 1] = RdfNTriplesParsingContentHandler.EOF;
                }
            } else if (scoutIndex == (2 * RdfNTriplesParsingContentHandler.BUFFER_SIZE + 1)) {
                scoutIndex = 0;
                // Reached the end of the second part of the buffer, read into
                // the first one.
                int len = this.br.read(buffer, 0,
                        RdfNTriplesParsingContentHandler.BUFFER_SIZE);
                if (len == -1) {
                    // End of the stream reached
                    buffer[scoutIndex] = RdfNTriplesParsingContentHandler.EOF;
                } else {
                    buffer[len] = RdfNTriplesParsingContentHandler.EOF;
                }
            } else {
                // Reached the end of the stream.
            }
        }

        return buffer[scoutIndex];
    }

    /**
     * Steps forward.
     * 
     * @param n
     *            the number of steps to go forward.
     * @throws IOException
     */
    public void step(int n) throws IOException {
        for (int i = 0; i < n; i++) {
            step();
        }
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
            scoutIndex = RdfNTriplesParsingContentHandler.BUFFER_SIZE * 2 + 1
                    - scoutIndex;
        }
    }
}
