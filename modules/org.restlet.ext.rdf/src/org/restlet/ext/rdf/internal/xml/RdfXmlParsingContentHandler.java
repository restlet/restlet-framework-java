package org.restlet.ext.rdf.internal.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.data.Language;
import org.restlet.data.Reference;
import org.restlet.ext.rdf.Graph;
import org.restlet.ext.rdf.GraphHandler;
import org.restlet.ext.rdf.LinkReference;
import org.restlet.ext.rdf.Literal;
import org.restlet.ext.rdf.RdfXmlRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.SaxRepresentation;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class RdfXmlParsingContentHandler extends GraphHandler {

	/**
	 * Content reader part.
	 */
	private static class ContentReader extends DefaultHandler {
		public enum State {
			LITERAL, NONE, OBJECT, PREDICATE, SUBJECT
		}

		/** Increment used to identify inner blank nodes. */
		private static int blankNodeId = 0;

		/**
		 * Returns the identifier of a new blank node.
		 * 
		 * @return The identifier of a new blank node.
		 */
		private static String newBlankNodeId() {
			return "#_bn" + blankNodeId++;
		}

		/** The value of the "base" reference. */
		private Reference base;

		/** Container for string content. */
		private StringBuilder builder;

		/** Current data type. */
		private String currentDataType;

		/** Current language. */
		private String currentLanguage;

		/** Current object. */
		private Object currentObject;

		/** Current predicate. */
		private Reference currentPredicate;

		/** The graph handler to call when a link is detected. */
		private GraphHandler graphHandler;

		/** Used to get the content of XMl literal. */
		private int nodeDepth;

		/** The list of known prefixes. */
		private Map<String, String> prefixes;

		/** Heap of states. */
		private List<State> states;

		/** Heap of subjects. */
		private List<Reference> subjects;

		/**
		 * Constructor.
		 * 
		 * @param graphHandler
		 *            The graph handler to call when a link is detected.
		 */
		public ContentReader(GraphHandler graphHandler) {
			super();
			this.graphHandler = graphHandler;
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if (getCurrentState() == State.LITERAL
					|| getCurrentState() == State.PREDICATE) {
				builder.append(ch, start, length);
			}
		}

		@Override
		public void endDocument() throws SAXException {
			this.builder = null;
			this.currentObject = null;
			this.currentPredicate = null;
			this.graphHandler = null;
			this.prefixes.clear();
			this.prefixes = null;
			this.states.clear();
			this.states = null;
			this.subjects.clear();
			this.subjects = null;
		}

		@Override
		public void endElement(String uri, String localName, String name)
				throws SAXException {
			State state = popState();

			if (state == State.SUBJECT) {
				popSubject();
			} else if (state == State.PREDICATE) {
				if (this.builder.length() > 0) {
					this.graphHandler.link(getCurrentSubject(),
							this.currentPredicate, getLiteral(builder
									.toString(), null, this.currentLanguage));
				}
			} else if (state == State.OBJECT) {
				if (this.builder.length() > 0) {
					this.currentObject = getLiteral(builder.toString(), null,
							this.currentLanguage);
				}
			} else if (state == State.LITERAL) {
				if (nodeDepth == 0) {
					// End of the XML literal
					this.graphHandler.link(getCurrentSubject(),
							this.currentPredicate, getLiteral(builder
									.toString(), this.currentDataType,
									this.currentLanguage));
				} else {
					// Still gleaning the content of an XML literal
					// Glean the XML content
					this.builder.append("</");
					if (uri != null && !"".equals(uri)) {
						this.builder.append(uri).append(":");
					}
					this.builder.append(localName).append(">");
					nodeDepth--;
					pushState(state);
				}
			}
		}

		@Override
		public void endPrefixMapping(String prefix) throws SAXException {
			this.prefixes.remove(prefix);
		}

		/**
		 * Indicates if the given qualified name is equals to a parsed element
		 * represented by its uri, localName and name.
		 * 
		 * @param qName
		 *            The qualified name to compare to.
		 * @param uri
		 *            The URI of the parsed element.
		 * @param localName
		 *            The local name of the parsed element.
		 * @param name
		 *            The (probably qualified) name of the parsed element.
		 * @return true if the qualified name and the parsed element are equal.
		 */
		private boolean equals(String qName, String uri, String localName,
				String name) {
			boolean result = qName.equals(name);
			return result;
		}

		/**
		 * Returns the state at the top of the heap.
		 * 
		 * @return The state at the top of the heap.
		 */
		private State getCurrentState() {
			State result = null;
			int size = this.states.size();

			if (size > 0) {
				result = this.states.get(size - 1);
			}

			return result;
		}

		/**
		 * Returns the subject at the top of the heap.
		 * 
		 * @return The subject at the top of the heap.
		 */
		private Reference getCurrentSubject() {
			Reference result = null;
			int size = this.subjects.size();

			if (size > 0) {
				result = this.subjects.get(size - 1);
			}

			return result;
		}

		private Literal getLiteral(String value, String datatype,
				String language) {
			Literal literal = new Literal(value);
			if (datatype != null) {
				literal.setDatatypeRef(new Reference(datatype));
			}
			if (language != null) {
				literal.setLanguage(Language.valueOf(language));
			}
			return literal;
		}

		/**
		 * Returns the absolute reference of a parsed element according to its
		 * URI, local name and name.
		 * 
		 * @param uri
		 *            The URI (maybe null) of the parsed element.
		 * @param localName
		 *            The local name of the parsed element.
		 * @param name
		 *            The (maybe qualified name of the parsed element.
		 * @return Returns the absolute reference of a parsed element.
		 */
		private Reference getReference(String uri, String localName, String name) {
			Reference result = null;

			if (uri != null) {
				Reference base = new Reference(uri);
				if (base.isRelative()) {
					base = new Reference(this.base, uri);
				}

				if (localName != null) {
					result = new Reference(base, localName);
				} else {
					result = base;
				}
			} else if (name != null) {
				int index = name.indexOf(":");
				if (index != -1) {
					// Get the prefix and return the URI.
					String prefix = name.substring(0, index);
					if (this.prefixes.containsKey(prefix)) {
						result = new Reference(this.prefixes.get(prefix),
								localName);
					}
				}
			} else {
				result = new Reference(this.base, localName);
			}

			return result.getTargetRef();
		}

		/**
		 * A new statement has been detected with the current subject, predicate
		 * and object.
		 */
		private void link() {
			Reference currentSubject = getCurrentSubject();
			if (currentSubject instanceof Reference) {
				if (this.currentObject instanceof Reference) {
					this.graphHandler.link((Reference) currentSubject,
							this.currentPredicate,
							(Reference) this.currentObject);
				} else if (this.currentObject instanceof Literal) {
					this.graphHandler
							.link((Reference) currentSubject,
									this.currentPredicate,
									(Literal) this.currentObject);
				} else {
					// TODO Error.
				}
			} else {
				// TODO Error.
			}
		}

		/**
		 * Returns the RDF URI of the given node represented by its namespace
		 * uri, local name, name, and attributes. It also generates the
		 * available statements, thanks to some shortcuts provided by the RDF
		 * XML syntax.
		 * 
		 * @param uri
		 * @param localName
		 * @param name
		 * @param attributes
		 * @return The RDF URI of the given node.
		 */
		private Reference parseNode(String uri, String localName, String name,
				Attributes attributes) {
			Reference result = null;
			// Stores the arcs
			List<String[]> arcs = new ArrayList<String[]>();
			boolean found = false;
			// Get the RDF URI of this node
			for (int i = 0; i < attributes.getLength(); i++) {
				String qName = attributes.getQName(i);
				if ("rdf:about".equals(qName)) {
					found = true;
					result = getReference(null, attributes.getValue(i), null);
				} else if ("rdf:nodeID".equals(qName)) {
					found = true;
					result = LinkReference.createBlank(attributes.getValue(i));
				} else if ("rdf:ID".equals(qName)) {
					found = true;
					result = getReference(null, "#" + attributes.getValue(i),
							null);
				} else {
					String[] arc = { qName, attributes.getValue(i) };
					arcs.add(arc);
				}
			}
			if (!found) {
				// Blank node with no given ID
				result = LinkReference.createBlank(ContentReader
						.newBlankNodeId());
			}

			// Create the available statements
			if (!"rdf:Description".equals(name)) {
				// Handle typed node
				this.graphHandler.link(result,
						RdfXmlRepresentation.PREDICATE_TYPE, getReference(uri,
								localName, name));
			}
			for (String[] arc : arcs) {
				this.graphHandler.link(result,
						getReference(null, null, arc[0]), new Literal(arc[1]));
			}

			return result;
		}

		/**
		 * Returns the state at the top of the heap and removes it from the
		 * heap.
		 * 
		 * @return The state at the top of the heap.
		 */
		private State popState() {
			State result = null;
			int size = this.states.size();

			if (size > 0) {
				result = this.states.remove(size - 1);
			}

			return result;
		}

		/**
		 * Returns the subject at the top of the heap and removes it from the
		 * heap.
		 * 
		 * @return The subject at the top of the heap.
		 */
		private Reference popSubject() {
			Reference result = null;
			int size = this.subjects.size();

			if (size > 0) {
				result = this.subjects.remove(size - 1);
			}

			return result;
		}

		/**
		 * Adds a new state at the top of the heap.
		 * 
		 * @param state
		 *            The state to add.
		 */
		private void pushState(State state) {
			this.states.add(state);
		}

		/**
		 * Adds a new subject at the top of the heap.
		 * 
		 * @param subject
		 *            The subject to add.
		 */
		private void pushSubject(Reference subject) {
			this.subjects.add(subject);
		}

		@Override
		public void startDocument() throws SAXException {
			this.prefixes = new HashMap<String, String>();
			this.builder = new StringBuilder();
			this.states = new ArrayList<State>();
			this.subjects = new ArrayList<Reference>();
			nodeDepth = 0;
			pushState(State.NONE);
		}

		@Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {

			State state = getCurrentState();
			if (state != State.LITERAL && this.builder.length() > 0) {
				// Reset the gleaner of text content.
				this.builder = new StringBuilder();
			}
			if (state == State.NONE) {
				if (equals("rdf:RDF", uri, localName, name)) {
					// Top element
					String base = attributes.getValue("xml:base");
					if (base != null) {
						this.base = new Reference(base);
					}
				} else {
					// Parse the current subject
					pushSubject(parseNode(uri, localName, name, attributes));
					pushState(State.SUBJECT);
				}
			} else if (state == State.SUBJECT) {
				List<String[]> arcs = new ArrayList<String[]>();
				// Parse the current predicate
				pushState(State.PREDICATE);
				this.currentPredicate = getReference(uri, localName, name);
				for (int i = 0; i < attributes.getLength(); i++) {
					String qName = attributes.getQName(i);
					if ("rdf:resource".equals(qName)) {
						this.graphHandler.link(getCurrentSubject(),
								this.currentPredicate, getReference(attributes
										.getValue(i), null, null));
						break;
					} else if ("rdf:datatype".equals(qName)) {
						// The object is a literal
						popState();
						pushState(State.LITERAL);
						this.currentDataType = attributes.getValue(i);
					} else if ("rdf:parseType".equals(qName)) {
						String value = attributes.getValue(i);
						if ("Literal".equals(value)) {
							// The object is an XML literal
							popState();
							pushState(State.LITERAL);
							this.currentDataType = RdfXmlRepresentation.RDF_SYNTAX
									+ "XMLLiteral";
							nodeDepth = 0;
						} else if ("Resource".equals(value)) {
							// Create a new blank node
							Reference ref = LinkReference
									.createBlank(ContentReader.newBlankNodeId());
							this.graphHandler.link(getCurrentSubject(),
									this.currentPredicate, ref);
							popState();
							pushSubject(ref);
							// TODO Do we really have to stop?
							break;
						} else {
							// Error
						}
					} else if ("rdf:nodeID".equals(qName)) {
						this.graphHandler.link(getCurrentSubject(),
								this.currentPredicate, LinkReference
										.createBlank(attributes.getValue(i)));
					} else {
						if (!qName.startsWith("xlmns")) {
							// Add arcs.
							String[] arc = { qName, attributes.getValue(i) };
							arcs.add(arc);
						}
					}
				}

				if (!arcs.isEmpty()) {
					// Create arcs that starts from a blank node and ends to
					// literal values. This blank node is the object of the
					// current statement
					Reference blankNode = LinkReference
							.createBlank(ContentReader.newBlankNodeId());
					this.graphHandler.link(getCurrentSubject(),
							this.currentPredicate, blankNode);
					for (String[] arc : arcs) {
						this.graphHandler.link(blankNode, getReference(arc[0],
								null, null), new Literal(arc[1]));
					}
				}
				// TODO Caution, what about the scope of the language attribute?
				this.currentLanguage = attributes.getValue("xml:lang");
			} else if (state == State.PREDICATE) {
				// Parse the current object, create the current link
				Reference object = parseNode(uri, localName, name, attributes);
				this.currentObject = object;
				link();
				pushSubject(object);
			} else if (state == State.OBJECT) {
			} else if (state == State.LITERAL) {
				// Glean the XML content
				nodeDepth++;
				this.builder.append("<");
				if (uri != null && !"".equals(uri)) {
					this.builder.append(uri).append(":");
				}
				this.builder.append(localName).append(">");
			}
		}

		@Override
		public void startPrefixMapping(String prefix, String uri)
				throws SAXException {
			this.prefixes.put(prefix, uri);
		}

	}

	/** The set of links to update when parsing, or to read when writing. */
	private Graph linkSet;

	/** The representation to read. */
	private SaxRepresentation rdfXmlRepresentation;

	/**
	 * Constructor.
	 * 
	 * @param linkSet
	 *            The set of links to update during the parsing.
	 * @param rdfXmlRepresentation
	 *            The representation to read.
	 * @throws IOException
	 */
	public RdfXmlParsingContentHandler(Graph linkSet,
			Representation rdfXmlRepresentation) throws IOException {
		super();
		this.linkSet = linkSet;
		if (rdfXmlRepresentation instanceof SaxRepresentation) {
			this.rdfXmlRepresentation = (SaxRepresentation) rdfXmlRepresentation;
		} else {
			this.rdfXmlRepresentation = new SaxRepresentation(
					rdfXmlRepresentation);
		}

		parse();
	}

	@Override
	public void link(Graph source, Reference typeRef, Literal target) {
		this.linkSet.add(source, typeRef, target);
	}

	@Override
	public void link(Graph source, Reference typeRef, Reference target) {
		this.linkSet.add(source, typeRef, target);
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
		this.rdfXmlRepresentation.parse(new ContentReader(this));
	}

}
