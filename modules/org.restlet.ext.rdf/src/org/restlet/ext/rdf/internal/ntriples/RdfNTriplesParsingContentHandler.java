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
import org.restlet.representation.Representation;

/**
 * Handler of RDF content according to the N-Triples notation.
 * 
 * @author Thierry Boileau
 */
public class RdfNTriplesParsingContentHandler extends GraphHandler {

    /** Internal buffered reader. */
    private BufferedReader br;

    /** The reading buffer. */
    private final char[] buffer;

    /** Size of the reading buffer. */
    private final int BUFFER_SIZE = 4096;

    /** End of reading buffer marker. */
    public final int EOF = 0;

    /** The set of links to update when parsing. */
    private Graph linkSet;

    /** The representation to read. */
    private Representation rdfRepresentation;

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
     * @param rdfRepresentation
     *            The representation to read.
     * @throws IOException
     */
    public RdfNTriplesParsingContentHandler(Graph linkSet,
            Representation rdfRepresentation) throws IOException {
        super();
        this.linkSet = linkSet;
        this.rdfRepresentation = rdfRepresentation;

        // Initialize the buffer in two parts
        this.buffer = new char[(BUFFER_SIZE + 1) * 2];
        // Mark the upper index of each part.
        this.buffer[BUFFER_SIZE] = this.buffer[2 * BUFFER_SIZE + 1] = EOF;
        this.scoutIndex = 2 * BUFFER_SIZE;
        this.startTokenIndex = 0;

        this.br = new BufferedReader(new InputStreamReader(
                this.rdfRepresentation.getStream()));
    }

    /**
     * Discard all read characters until the end of the statement is reached
     * (marked by a '.').
     * 
     * @throws IOException
     */
    protected void consumeStatement() throws IOException {
        int c = getChar();
        while (!isEndOfFile(c) && c != '.') {
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
    protected void consumeWhiteSpaces() throws IOException {
        while (isWhiteSpace(getChar())) {
            step();
        }
        discard();
    }

    /**
     * Discard all read characters. A call to {@link getCurrentToken} will
     * return a single character.
     */
    protected void discard() {
        startTokenIndex = scoutIndex;
    }

    /**
     * Returns the current parsed character.
     * 
     * @return The current parsed character.
     */
    protected char getChar() {
        return (char) buffer[scoutIndex];
    }

    /**
     * Returns the current token.
     * 
     * @return The current token.
     */
    protected String getCurrentToken() {
        StringBuilder builder = new StringBuilder();
        if (startTokenIndex <= scoutIndex) {
            if (scoutIndex <= BUFFER_SIZE) {
                for (int i = startTokenIndex; i < scoutIndex; i++) {
                    builder.append((char) buffer[i]);
                }
            } else {
                for (int i = startTokenIndex; i < BUFFER_SIZE; i++) {
                    builder.append((char) buffer[i]);
                }
                for (int i = BUFFER_SIZE + 1; i < scoutIndex; i++) {
                    builder.append((char) buffer[i]);
                }
            }
        } else {
            if (startTokenIndex <= BUFFER_SIZE) {
                for (int i = startTokenIndex; i < BUFFER_SIZE; i++) {
                    builder.append((char) buffer[i]);
                }
                for (int i = BUFFER_SIZE + 1; i < (2 * BUFFER_SIZE + 1); i++) {
                    builder.append((char) buffer[i]);
                }
                for (int i = 0; i < scoutIndex; i++) {
                    builder.append((char) buffer[i]);
                }
            } else {
                for (int i = startTokenIndex; i < (2 * BUFFER_SIZE + 1); i++) {
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

    public Graph getLinkSet() {
        return linkSet;
    }

    /**
     * Returns true if the given character is alphanumeric.
     * 
     * @param c
     *            The given character to check.
     * @return true if the given character is alphanumeric.
     */
    protected boolean isAlphaNum(int c) {
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
    protected boolean isDelimiter(int c) {
        return isWhiteSpace(c) || c == '"' || c == '.';
    }

    protected boolean isEndOfFile(int c) {
        return c == EOF;
    }

    /**
     * Returns true if the given character is a whitespace.
     * 
     * @param c
     *            The given character to check.
     * @return true if the given character is a whitespace.
     */
    protected boolean isWhiteSpace(int c) {
        return c == ' ' || c == '\n' || c == '\r' || c == '\t';
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
    public void parse() throws IOException {
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
        } while (!isEndOfFile(getChar()));
    }

    /**
     * Parses a comment.
     * 
     * @throws IOException
     */
    protected void parseComment() throws IOException {
        int c;
        do {
            c = step();
        } while (!isEndOfFile(getChar()) && c != '\n' && c != '\r');
        discard();
    }

    /**
     * Reads the current statement until its end, and parses it.
     * 
     * @param context
     *            The current context.
     * @throws IOException
     */
    protected void parseStatement() throws IOException {
        List<Reference> lexicalUnits = new ArrayList<Reference>();
        String object = null;
        do {
            consumeWhiteSpaces();
            switch (getChar()) {
            case '<':
                lexicalUnits.add(new Reference(parseUri()));
                break;
            case '_':
                lexicalUnits.add(LinkReference.createBlank(parseToken()));
                break;
            case '"':
                int c = step();
                discard();
                while (!isEndOfFile(c) && (c != '"')) {
                    c = step();
                }
                object = getCurrentToken();
                step();
                discard();
                break;
            case '.':
                break;
            case EOF:
                break;
            default:
                break;
            }
        } while (!isEndOfFile(getChar()) && getChar() != '.'
                && getChar() != '}');

        // Generate the links
        if (!lexicalUnits.isEmpty()) {
            if (object != null) {
                link(lexicalUnits.get(0), lexicalUnits.get(1), new Literal(
                        object));
            } else {
                link(lexicalUnits.get(0), lexicalUnits.get(1), lexicalUnits
                        .get(2));
            }
        }
    }

    /**
     * Returns the value of the current token.
     * 
     * @return The value of the current token.
     * @throws IOException
     */
    protected String parseToken() throws IOException {
        int c;
        do {
            c = step();
        } while (!isEndOfFile(c) && !isDelimiter(c));
        String result = getCurrentToken();
        return result;
    }

    /**
     * Returns the value of the current URI.
     * 
     * @return The value of the current URI.
     * @throws IOException
     */
    protected String parseUri() throws IOException {
        StringBuilder builder = new StringBuilder();
        // Suppose the current character is "<".
        int c = step();
        while (c != EOF && c != '>') {
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
    protected int step() throws IOException {
        scoutIndex++;
        if (buffer[scoutIndex] == EOF) {
            if (scoutIndex == BUFFER_SIZE) {
                // Reached the end of the first part of the buffer, read into
                // the second one.
                scoutIndex++;
                int len = this.br.read(buffer, 0, BUFFER_SIZE);
                if (len == -1) {
                    // End of the stream reached
                    buffer[scoutIndex] = EOF;
                } else {
                    buffer[BUFFER_SIZE + len + 1] = EOF;
                }
            } else if (scoutIndex == (2 * BUFFER_SIZE + 1)) {
                scoutIndex = 0;
                // Reached the end of the second part of the buffer, read into
                // the first one.
                int len = this.br.read(buffer, 0, BUFFER_SIZE);
                if (len == -1) {
                    // End of the stream reached
                    buffer[scoutIndex] = EOF;
                } else {
                    buffer[len] = EOF;
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
     *            The number of steps to go forward.
     * @throws IOException
     */
    protected void step(int n) throws IOException {
        for (int i = 0; i < n; i++) {
            step();
        }
    }

    /**
     * Steps back of one step.
     * 
     */
    protected void stepBack() {
        stepBack(1);
    }

    /**
     * Steps back.
     * 
     * @param n
     *            The number of steps to go back.
     */
    protected void stepBack(int n) {
        scoutIndex -= n;
        if (scoutIndex < 0) {
            scoutIndex = BUFFER_SIZE * 2 + 1 - scoutIndex;
        }
    }
}
