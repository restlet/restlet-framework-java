/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.ext.rdf.internal.turtle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.restlet.data.Reference;
import org.restlet.ext.rdf.Graph;
import org.restlet.ext.rdf.GraphHandler;
import org.restlet.ext.rdf.Literal;
import org.restlet.ext.rdf.internal.RdfConstants;
import org.restlet.ext.rdf.internal.ntriples.RdfNTriplesReader;
import org.restlet.representation.Representation;

/**
 * Handler of RDF content according to the RDF Turtle notation.
 * 
 * @author Thierry Boileau
 */
public class RdfTurtleReader extends RdfNTriplesReader {

    /** Increment used to identify inner blank nodes. */
    private int blankNodeId = 0;

    /** The current context object. */
    private Context context;

    /**
     * Constructor.
     * 
     * @param rdfRepresentation
     *            The representation to read.
     * @param graphHandler
     *            The graph handler invoked during the parsing.
     * @throws IOException
     */
    public RdfTurtleReader(Representation rdfN3Representation,
            GraphHandler graphHandler) throws IOException {
        super(rdfN3Representation, graphHandler);
        this.context = new Context();
        context.getKeywords().addAll(
                Arrays.asList("a", "is", "of", "this", "has"));
    }

    /**
     * Loops over the given list of lexical units and generates the adequat
     * calls to link* methods.
     * 
     * @see GraphHandler#link(Graph, Reference, Reference)
     * @see GraphHandler#link(Reference, Reference, Literal)
     * @see GraphHandler#link(Reference, Reference, Reference)
     * @param lexicalUnits
     *            The list of lexical units used to generate the links.
     */
    protected void generateLinks(List<LexicalUnit> lexicalUnits) {
        Object currentSubject = null;
        Reference currentPredicate = null;
        Object currentObject = null;
        int nbTokens = 0;
        boolean swapSubjectObject = false;

        for (int i = 0; i < lexicalUnits.size(); i++) {
            LexicalUnit lexicalUnit = lexicalUnits.get(i);

            nbTokens++;
            switch (nbTokens) {
            case 1:
                if (",".equals(lexicalUnit.getValue())) {
                    nbTokens++;
                } else if (!";".equals(lexicalUnit.getValue())) {
                    currentSubject = lexicalUnit.resolve();
                }
                break;
            case 2:
                if ("is".equalsIgnoreCase(lexicalUnit.getValue())) {
                    nbTokens--;
                    swapSubjectObject = true;
                } else if ("has".equalsIgnoreCase(lexicalUnit.getValue())) {
                    nbTokens--;
                } else if ("a".equalsIgnoreCase(lexicalUnit.getValue())) {
                    currentPredicate = RdfConstants.PREDICATE_TYPE;
                } else if ("!".equalsIgnoreCase(lexicalUnit.getValue())) {
                    currentObject = new BlankNodeToken(newBlankNodeId())
                            .resolve();
                    currentPredicate = getPredicate(lexicalUnits.get(++i));
                    this.link(currentSubject, currentPredicate, currentObject);
                    currentSubject = currentObject;
                    nbTokens = 1;
                } else if ("^".equalsIgnoreCase(lexicalUnit.getValue())) {
                    currentObject = currentSubject;
                    currentPredicate = getPredicate(lexicalUnits.get(++i));
                    currentSubject = new BlankNodeToken(newBlankNodeId())
                            .resolve();
                    this.link(currentSubject, currentPredicate, currentObject);
                    nbTokens = 1;
                } else {
                    currentPredicate = getPredicate(lexicalUnit);
                }
                break;
            case 3:
                if ("of".equalsIgnoreCase(lexicalUnit.getValue())) {
                    nbTokens--;
                } else {
                    if (swapSubjectObject) {
                        this.link(lexicalUnit.resolve(), currentPredicate,
                                currentSubject);
                    } else {
                        currentObject = lexicalUnit.resolve();
                        this.link(currentSubject, currentPredicate,
                                currentObject);
                    }
                    nbTokens = 0;
                    swapSubjectObject = false;
                }
                break;
            default:
                break;
            }
        }
    }

    /**
     * Returns the current context.
     * 
     * @return The current context.
     */
    protected Context getContext() {
        return context;
    }

    /**
     * Returns the given lexical unit as a predicate.
     * 
     * @param lexicalUnit
     *            The lexical unit to get as a predicate.
     * @return A RDF URI reference of the predicate.
     */
    private Reference getPredicate(LexicalUnit lexicalUnit) {
        Reference result = null;
        Object p = lexicalUnit.resolve();
        if (p instanceof Reference) {
            result = (Reference) p;
        } else if (p instanceof String) {
            result = new Reference((String) p);
        }

        return result;
    }

    /**
     * Returns true if the given character is a delimiter.
     * 
     * @param c
     *            The given character to check.
     * @return true if the given character is a delimiter.
     */
    @Override
    protected boolean isDelimiter(int c) {
        return isWhiteSpace(c) || c == '^' || c == '!' || c == '=' || c == '<'
                || c == '"' || c == '[' || c == ']' || c == '(' || c == ')'
                || c == '.' || c == ';' || c == ',' || c == '@';
    }

    /**
     * Callback method used when a link is parsed or written.
     * 
     * @param source
     *            The source or subject of the link.
     * @param typeRef
     *            The type reference of the link.
     * @param target
     *            The target or object of the link.
     */
    protected void link(Object source, Reference typeRef, Object target) {
        if (source instanceof Reference) {
            if (target instanceof Reference) {
                getGraphHandler().link((Reference) source, typeRef,
                        (Reference) target);
            } else if (target instanceof Literal) {
                getGraphHandler().link((Reference) source, typeRef,
                        (Literal) target);
            } else {
                org.restlet.Context
                        .getCurrentLogger()
                        .warning(
                                "The RDF Turtle document contains an object which is neither a Reference nor a literal: "
                                        + target);
                org.restlet.Context.getCurrentLogger().warning(
                        getParsingMessage());
            }
        } else if (source instanceof Graph) {
            if (target instanceof Reference) {
                getGraphHandler().link((Graph) source, typeRef,
                        (Reference) target);
            } else if (target instanceof Literal) {
                getGraphHandler().link((Graph) source, typeRef,
                        (Literal) target);
            } else {
                org.restlet.Context
                        .getCurrentLogger()
                        .warning(
                                "The RDF Turtle document contains an object which is neither a Reference nor a literal: "
                                        + target);
                org.restlet.Context.getCurrentLogger().warning(
                        getParsingMessage());
            }
        }
    }

    /**
     * Returns the identifier of a new blank node.
     * 
     * @return The identifier of a new blank node.
     */
    protected String newBlankNodeId() {
        return "#_bn" + blankNodeId++;
    }

    /**
     * Parses the current representation.
     * 
     * @throws IOException
     */
    @Override
    public void parse() throws Exception {
        // Init the reading.
        step();
        do {
            consumeWhiteSpaces();
            switch (getChar()) {
            case '@':
                parseDirective(this.context);
                break;
            case '#':
                parseComment();
                break;
            case '.':
                step();
                break;
            default:
                parseStatement(this.context);
                break;
            }
        } while (!isEndOfFile(getChar()));
    }

    /**
     * Parse the given blank node.
     * 
     * @param blankNode
     *            The blank node to parse.
     * @throws IOException
     */
    protected void parseBlankNode(BlankNodeToken blankNode) throws IOException {
        step();
        do {
            consumeWhiteSpaces();
            switch (getChar()) {
            case '(':
                blankNode.getLexicalUnits().add(
                        new ListToken(this, this.context));
                break;
            case '<':
                stepBack();
                blankNode.getLexicalUnits().add(
                        new UriToken(this, this.context));
                break;
            case '_':
                blankNode.getLexicalUnits().add(
                        new BlankNodeToken(this.parseToken()));
                break;
            case '"':
                blankNode.getLexicalUnits().add(
                        new StringToken(this, this.context));
                break;
            case '[':
                blankNode.getLexicalUnits().add(
                        new BlankNodeToken(this, this.context));
                break;
            case ']':
                break;
            default:
                if (!isEndOfFile(getChar())) {
                    blankNode.getLexicalUnits().add(
                            new Token(this, this.context));
                }

                break;
            }
        } while (!isEndOfFile(getChar()) && getChar() != ']');
        if (getChar() == ']') {
            // Set the cursor at the right of the list token.
            step();
        }
    }

    /**
     * Parse the current directive and update the context according to the kind
     * of directive ("base", "prefix", etc).
     * 
     * @param context
     *            The context to update.
     * @throws IOException
     */
    protected void parseDirective(Context context) throws IOException {
        // Remove the leading '@' character.
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
            } while (!isEndOfFile(c) && c != '.');
            String strKeywords = getCurrentToken();
            String[] keywords = strKeywords.split(",");
            context.getKeywords().clear();
            for (String keyword : keywords) {
                context.getKeywords().add(keyword.trim());
            }
            consumeStatement();
        } else {
            org.restlet.Context.getCurrentLogger().warning(
                    "@" + currentKeyword + " directive is not supported.");
            consumeStatement();
        }
    }

    /**
     * Parse the given list token.
     * 
     * @param listToken
     *            The list token to parse.
     * @throws IOException
     */
    protected void parseList(ListToken listToken) throws IOException {
        step();
        do {
            consumeWhiteSpaces();
            switch (getChar()) {
            case '(':
                listToken.getLexicalUnits().add(
                        new ListToken(this, this.context));
                break;
            case '<':
                stepBack();
                listToken.getLexicalUnits().add(
                        new UriToken(this, this.context));
                break;
            case '_':
                listToken.getLexicalUnits().add(
                        new BlankNodeToken(parseToken()));
                break;
            case '"':
                listToken.getLexicalUnits().add(
                        new StringToken(this, this.context));
                break;
            case '[':
                listToken.getLexicalUnits().add(
                        new BlankNodeToken(this, this.context));
                break;
            case ')':
                break;
            default:
                if (!isEndOfFile(getChar())) {
                    listToken.getLexicalUnits().add(
                            new Token(this, this.context));
                }
                break;
            }
        } while (!isEndOfFile(getChar()) && getChar() != ')');
        if (getChar() == ')') {
            // Set the cursor at the right of the list token.
            step();
        }
    }

    /**
     * Reads the current statement until its end, and parses it.
     * 
     * @param context
     *            The current context.
     * @throws IOException
     */
    protected void parseStatement(Context context) throws IOException {
        List<LexicalUnit> lexicalUnits = new ArrayList<LexicalUnit>();
        do {
            consumeWhiteSpaces();
            switch (getChar()) {
            case '(':
                lexicalUnits.add(new ListToken(this, context));
                break;
            case '<':
                stepBack();
                lexicalUnits.add(new UriToken(this, context));
                break;
            case '_':
                lexicalUnits.add(new BlankNodeToken(parseToken()));
                break;
            case '"':
                lexicalUnits.add(new StringToken(this, context));
                break;
            case '[':
                lexicalUnits.add(new BlankNodeToken(this, context));
                break;
            case '!':
                lexicalUnits.add(new Token("!"));
                step();
                discard();
                break;
            case '^':
                lexicalUnits.add(new Token("^"));
                step();
                discard();
                break;
            case '@':
                // Remove the leading '@' character.
                step();
                discard();
                lexicalUnits.add(new Token(this, context));
                discard();
                break;
            case ';':
                step();
                discard();
                lexicalUnits.add(new Token(";"));
                break;
            case ',':
                step();
                discard();
                lexicalUnits.add(new Token(","));
                break;
            case '#':
                parseComment();
                break;
            case '.':
                break;
            default:
                if (!isEndOfFile(getChar())) {
                    lexicalUnits.add(new Token(this, context));
                }
                break;
            }
        } while (!isEndOfFile(getChar()) && getChar() != '.');

        // Generate the links
        generateLinks(lexicalUnits);
    }

    /**
     * Parse the given String token.
     * 
     * @param stringToken
     *            The String token to parse.
     * @throws IOException
     */
    protected void parseString(StringToken stringToken) throws IOException {
        // Answer the question : is it multi lines or not?
        // That is to say, is it delimited by 3 quotes or not?
        int c1 = step();
        int c2 = step();

        if ((c1 == c2) && (c1 == '"')) {
            stringToken.setMultiLines(true);
            step();
            discard();
            int[] tab = new int[3];
            int cpt = 0; // Number of consecutives '"' characters.
            int c = getChar();
            while (!isEndOfFile(c)) {
                if (c == '"') {
                    tab[++cpt - 1] = c;
                } else {
                    cpt = 0;
                }
                if (cpt == 3) {
                    // End of the string reached.
                    stepBack(2);
                    stringToken.setValue(getCurrentToken());
                    step(3);
                    discard();
                    break;
                }
                c = step();
            }
        } else {
            stringToken.setMultiLines(false);
            stepBack(1);
            discard();
            int c = getChar();
            while (!isEndOfFile(c) && (c != '"')) {
                c = step();
            }
            stringToken.setValue(getCurrentToken());
            step();
            discard();
        }

        // Parse the type and language of literals
        int c = getChar();
        if (c == '@') {
            stringToken.setLanguage(parseToken());
        } else if (c == '^') {
            c = step();
            if (c == '^') {
                stringToken.setType(parseToken());
            } else {
                stepBack();
            }
        }

    }

    /**
     * Parses the given token.
     * 
     * @param token
     *            The token to parse.
     * @throws IOException
     */
    protected void parseToken(Token token) throws IOException {
        int c;
        do {
            c = step();
        } while (!isEndOfFile(c) && !isDelimiter(c));
        token.setValue(getCurrentToken());

    }

    /**
     * Parses the given URI token.
     * 
     * @param token
     *            The URI token to parse.
     * @throws IOException
     */
    protected void parseUri(UriToken uriToken) throws IOException {
        uriToken.setValue(parseUri());
    }
}
