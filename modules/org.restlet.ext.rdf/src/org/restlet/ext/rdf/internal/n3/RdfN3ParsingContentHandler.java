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

package org.restlet.ext.rdf.internal.n3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.restlet.data.Reference;
import org.restlet.ext.rdf.Graph;
import org.restlet.ext.rdf.GraphHandler;
import org.restlet.ext.rdf.Literal;
import org.restlet.ext.rdf.RdfRepresentation;
import org.restlet.representation.Representation;

/**
 * Handler of RDF content according to the N3 notation.
 */
public class RdfN3ParsingContentHandler extends GraphHandler {
	/** Increment used to identify inner blank nodes. */
	private static int blankNodeId = 0;

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

	/**
	 * Returns the identifier of a new blank node.
	 * 
	 * @return The identifier of a new blank node.
	 */
	public static String newBlankNodeId() {
		return "#_bn" + blankNodeId++;
	}

	/** Internal buffered reader. */
	private BufferedReader br;

	/** The reading buffer. */
	private final char[] buffer;

	/** The current context object. */
	private Context context;

	/** The set of links to update when parsing. */
	private Graph linkSet;

	/** The representation to read. */
	private Representation rdfN3Representation;

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
	 * @param rdfN3Representation
	 *            The representation to read.
	 * @throws IOException
	 */
	public RdfN3ParsingContentHandler(Graph linkSet,
			Representation rdfN3Representation) throws IOException {
		super();
		this.linkSet = linkSet;
		this.rdfN3Representation = rdfN3Representation;

		// Initialize the buffer in two parts
		this.buffer = new char[(RdfN3ParsingContentHandler.BUFFER_SIZE + 1) * 2];
		// Mark the upper index of each part.
		this.buffer[RdfN3ParsingContentHandler.BUFFER_SIZE] = this.buffer[2 * RdfN3ParsingContentHandler.BUFFER_SIZE + 1] = EOF;
		this.scoutIndex = 2 * RdfN3ParsingContentHandler.BUFFER_SIZE;
		this.startTokenIndex = 0;

		this.br = new BufferedReader(new InputStreamReader(
				this.rdfN3Representation.getStream()));
		this.context = new Context();
		context.getKeywords().addAll(
				Arrays.asList("a", "is", "of", "this", "has"));
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
		while (c != RdfN3ParsingContentHandler.EOF && c != '.') {
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
		while (RdfN3ParsingContentHandler.isWhiteSpace(getChar())) {
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
	 * Loops over the given list of lexical units and generates the adequat
	 * calls to link* methods.
	 * 
	 * @see GraphHandler#link(Graph, Reference, Reference)
	 * @see GraphHandler#link(Reference, Reference, Literal)
	 * @see GraphHandler#link(Reference, Reference, Reference)
	 * @param lexicalUnits
	 *            The list of lexical units used to generate the links.
	 */
	public void generateLinks(List<LexicalUnit> lexicalUnits) {
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
				} else if ("=".equalsIgnoreCase(lexicalUnit.getValue())) {
					currentPredicate = RdfRepresentation.PREDICATE_SAME;
				} else if ("=>".equalsIgnoreCase(lexicalUnit.getValue())) {
					currentPredicate = RdfRepresentation.PREDICATE_IMPLIES;
				} else if ("<=".equalsIgnoreCase(lexicalUnit.getValue())) {
					swapSubjectObject = true;
					currentPredicate = RdfRepresentation.PREDICATE_IMPLIES;
				} else if ("a".equalsIgnoreCase(lexicalUnit.getValue())) {
					currentPredicate = RdfRepresentation.PREDICATE_TYPE;
				} else if ("!".equalsIgnoreCase(lexicalUnit.getValue())) {
					currentObject = new BlankNodeToken(
							RdfN3ParsingContentHandler.newBlankNodeId())
							.resolve();
					currentPredicate = getPredicate(lexicalUnits.get(++i));
					this.link(currentSubject, currentPredicate, currentObject);
					currentSubject = currentObject;
					nbTokens = 1;
				} else if ("^".equalsIgnoreCase(lexicalUnit.getValue())) {
					currentObject = currentSubject;
					currentPredicate = getPredicate(lexicalUnits.get(++i));
					currentSubject = new BlankNodeToken(
							RdfN3ParsingContentHandler.newBlankNodeId())
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
						currentObject = currentSubject;
						currentSubject = lexicalUnit.resolve();
					} else {
						currentObject = lexicalUnit.resolve();
					}
					this.link(currentSubject, currentPredicate, currentObject);
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
			if (scoutIndex <= RdfN3ParsingContentHandler.BUFFER_SIZE) {
				for (int i = startTokenIndex; i < scoutIndex; i++) {
					builder.append((char) buffer[i]);
				}
			} else {
				for (int i = startTokenIndex; i < RdfN3ParsingContentHandler.BUFFER_SIZE; i++) {
					builder.append((char) buffer[i]);
				}
				for (int i = RdfN3ParsingContentHandler.BUFFER_SIZE + 1; i < scoutIndex; i++) {
					builder.append((char) buffer[i]);
				}
			}
		} else {
			if (startTokenIndex <= RdfN3ParsingContentHandler.BUFFER_SIZE) {
				for (int i = startTokenIndex; i < RdfN3ParsingContentHandler.BUFFER_SIZE; i++) {
					builder.append((char) buffer[i]);
				}
				for (int i = RdfN3ParsingContentHandler.BUFFER_SIZE + 1; i < (2 * RdfN3ParsingContentHandler.BUFFER_SIZE + 1); i++) {
					builder.append((char) buffer[i]);
				}
				for (int i = 0; i < scoutIndex; i++) {
					builder.append((char) buffer[i]);
				}
			} else {
				for (int i = startTokenIndex; i < (2 * RdfN3ParsingContentHandler.BUFFER_SIZE + 1); i++) {
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

	@Override
	public void link(Graph source, Reference typeRef, Literal target) {
		this.linkSet.add(source, typeRef, target);
	}

	@Override
	public void link(Graph source, Reference typeRef, Reference target) {
		this.linkSet.add(source, typeRef, target);
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
	private void link(Object source, Reference typeRef, Object target) {
		if (source instanceof Reference) {
			if (target instanceof Reference) {
				link((Reference) source, typeRef, (Reference) target);
			} else if (target instanceof Literal) {
				link((Reference) source, typeRef, (Literal) target);
			} else {
				// Error?
			}
		} else if (source instanceof Graph) {
			if (target instanceof Reference) {
				link((Graph) source, typeRef, (Reference) target);
			} else if (target instanceof Literal) {
				link((Graph) source, typeRef, (Literal) target);
			} else {
				// Error?
			}
		}
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
		} while (getChar() != RdfN3ParsingContentHandler.EOF);

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
		} while (c != RdfN3ParsingContentHandler.EOF && c != '\n' && c != '\r');
		discard();
	}

	/**
	 * Parse the current directive and update the context according to the kind
	 * of directive ("base", "prefix", etc).
	 * 
	 * @param context
	 *            The context to update.
	 * @throws IOException
	 */
	public void parseDirective(Context context) throws IOException {
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
			} while (c != RdfN3ParsingContentHandler.EOF && c != '.');
			String strKeywords = getCurrentToken();
			String[] keywords = strKeywords.split(",");
			context.getKeywords().clear();
			for (String keyword : keywords) {
				context.getKeywords().add(keyword.trim());
			}
			consumeStatement();
		} else {
			// TODO @ForAll and @ForSome are not supported yet.
			consumeStatement();
		}
	}

	/**
	 * Reads the current statement until its end, and parses it.
	 * 
	 * @param context
	 *            The current context.
	 * @throws IOException
	 */
	public void parseStatement(Context context) throws IOException {
		List<LexicalUnit> lexicalUnits = new ArrayList<LexicalUnit>();
		do {
			consumeWhiteSpaces();
			switch (getChar()) {
			case '(':
				lexicalUnits.add(new ListToken(this, context));
				break;
			case '<':
				if (step() == '=') {
					lexicalUnits.add(new Token("<="));
					step();
					discard();
				} else {
					stepBack();
					lexicalUnits.add(new UriToken(this, context));
				}
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
			case '=':
				if (step() == '>') {
					lexicalUnits.add(new Token("=>"));
					step();
					discard();
				} else {
					lexicalUnits.add(new Token("="));
					discard();
				}
				break;
			case '@':
				// Remove the leading '@' character.
				step();
				discard();
				lexicalUnits.add(new Token(this, context));
				discard();
				break;
			case ';':
				// TODO
				step();
				discard();
				lexicalUnits.add(new Token(";"));
				break;
			case ',':
				// TODO
				step();
				discard();
				lexicalUnits.add(new Token(","));
				break;
			case '{':
				lexicalUnits.add(new FormulaToken(this, context));
				break;
			case '.':
				break;
			case RdfN3ParsingContentHandler.EOF:
				break;
			default:
				lexicalUnits.add(new Token(this, context));
				break;
			}
		} while (getChar() != RdfN3ParsingContentHandler.EOF
				&& getChar() != '.' && getChar() != '}');

		// Generate the links
		generateLinks(lexicalUnits);
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
		} while (c != RdfN3ParsingContentHandler.EOF && !isDelimiter(c));
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
		while (c != RdfN3ParsingContentHandler.EOF && c != '>') {
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
		if (buffer[scoutIndex] == RdfN3ParsingContentHandler.EOF) {
			if (scoutIndex == RdfN3ParsingContentHandler.BUFFER_SIZE) {
				// Reached the end of the first part of the buffer, read into
				// the second one.
				scoutIndex++;
				int len = this.br.read(buffer, 0,
						RdfN3ParsingContentHandler.BUFFER_SIZE);
				if (len == -1) {
					// End of the stream reached
					buffer[scoutIndex] = RdfN3ParsingContentHandler.EOF;
				} else {
					buffer[RdfN3ParsingContentHandler.BUFFER_SIZE + len + 1] = RdfN3ParsingContentHandler.EOF;
				}
			} else if (scoutIndex == (2 * RdfN3ParsingContentHandler.BUFFER_SIZE + 1)) {
				scoutIndex = 0;
				// Reached the end of the second part of the buffer, read into
				// the first one.
				int len = this.br.read(buffer, 0,
						RdfN3ParsingContentHandler.BUFFER_SIZE);
				if (len == -1) {
					// End of the stream reached
					buffer[scoutIndex] = RdfN3ParsingContentHandler.EOF;
				} else {
					buffer[len] = RdfN3ParsingContentHandler.EOF;
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
			scoutIndex = RdfN3ParsingContentHandler.BUFFER_SIZE * 2 + 1
					- scoutIndex;
		}
	}

}
