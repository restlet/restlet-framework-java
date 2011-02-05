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

package org.restlet.ext.rdf.internal.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.data.Language;
import org.restlet.data.Reference;
import org.restlet.ext.rdf.GraphHandler;
import org.restlet.ext.rdf.Link;
import org.restlet.ext.rdf.Literal;
import org.restlet.ext.rdf.RdfRepresentation;
import org.restlet.ext.rdf.internal.RdfConstants;
import org.restlet.representation.Representation;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Content reader part.
 * 
 * @author Thierry Boileau
 */
class ContentReader extends DefaultHandler {
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

    /** The value of the "base" URI. */
    private ScopedProperty<Reference> base;

    /** Container for string content. */
    private StringBuilder builder;

    /** Indicates if the string content must be consumed. */
    private boolean consumingContent;

    /** Current data type. */
    private String currentDataType;

    /** Current language. */
    private ScopedProperty<Language> language;

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

    /**
     * True if {@link RdfRepresentation#RDF_SYNTAX} is the default namespace.
     */
    private boolean rdfDefaultNamespace;

    /** Used when a statement must be reified. */
    private Reference reifiedRef;

    /** Heap of states. */
    private List<ContentReader.State> states;

    /** Heap of subjects. */
    private List<Reference> subjects;

    /**
     * 
     * 
     * @param graphHandler
     * 
     */
    /**
     * Constructor.
     * 
     * @param graphHandler
     *            The graph handler to call when a link is detected.
     * @param representation
     *            The input representation.
     */
    public ContentReader(Representation representation,
            GraphHandler graphHandler) {
        super();
        this.graphHandler = graphHandler;
        this.base = new ScopedProperty<Reference>();
        this.language = new ScopedProperty<Language>();
        if (representation.getLocationRef() != null) {
            this.base.add(representation.getLocationRef().getTargetRef());
            this.base.incrDepth();
        }
        if (representation.getLanguages().size() == 1) {
            this.language.add(representation.getLanguages().get(1));
            this.language.incrDepth();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (consumingContent) {
            builder.append(ch, start, length);
        }
    }

    /**
     * Returns true if the given qualified name is in the RDF namespace and is
     * equal to the given local name.
     * 
     * @param localName
     *            The local name to compare to.
     * @param qName
     *            The qualified name
     */
    private boolean checkRdfQName(String localName, String qName) {
        boolean result = qName.equals("rdf:" + localName);
        if (!result) {
            int index = qName.indexOf(":");
            // The qualified name has no prefix.
            result = rdfDefaultNamespace && (index == -1)
                    && localName.equals(qName);
        }

        return result;
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
        this.nodeDepth = 0;
    }

    @Override
    public void endElement(String uri, String localName, String name)
            throws SAXException {
        ContentReader.State state = popState();

        if (state == State.SUBJECT) {
            popSubject();
        } else if (state == State.PREDICATE) {
            if (this.consumingContent) {
                link(getCurrentSubject(), this.currentPredicate, getLiteral(
                        builder.toString(), null, this.language.getValue()));
                this.consumingContent = false;
            }
        } else if (state == State.OBJECT) {
        } else if (state == State.LITERAL) {
            if (nodeDepth == 0) {
                // End of the XML literal
                link(getCurrentSubject(), this.currentPredicate, getLiteral(
                        builder.toString(), this.currentDataType, this.language
                                .getValue()));
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
        this.base.decrDepth();
        this.language.decrDepth();
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        this.prefixes.remove(prefix);
    }

    /**
     * Returns the state at the top of the heap.
     * 
     * @return The state at the top of the heap.
     */
    private ContentReader.State getCurrentState() {
        ContentReader.State result = null;
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

    /**
     * Returns a Literal object according to the given parameters.
     * 
     * @param value
     *            The value of the literal.
     * @param datatype
     *            The datatype of the literal.
     * @param language
     *            The language of the literal.
     * @return A Literal object
     */
    private Literal getLiteral(String value, String datatype, Language language) {
        Literal literal = new Literal(value);
        if (datatype != null) {
            literal.setDatatypeRef(new Reference(datatype));
        }
        if (language != null) {
            literal.setLanguage(language);
        }
        return literal;
    }

    /**
     * A new statement has been detected with the current subject, predicate and
     * object.
     */
    private void link() {
        Reference currentSubject = getCurrentSubject();
        if (currentSubject != null) {
            if (this.currentObject instanceof Reference) {
                link(currentSubject, this.currentPredicate,
                        (Reference) this.currentObject);
            } else if (this.currentObject instanceof Literal) {
                link(currentSubject, this.currentPredicate,
                        (Literal) this.currentObject);
            } else {
                org.restlet.Context
                        .getCurrentLogger()
                        .warning(
                                "Cannot write the representation of a statement due to the fact that the object is neither a Reference nor a literal.");
            }
        } else {
            org.restlet.Context
                    .getCurrentLogger()
                    .warning(
                            "Cannot write the representation of a statement due to the fact that the subject is not a Reference.");
        }
    }

    /**
     * Creates a statement and reify it, if necessary.
     * 
     * @param subject
     *            The subject of the statement.
     * @param predicate
     *            The predicate of the statement.
     * @param object
     *            The object of the statement.
     */
    private void link(Reference subject, Reference predicate, Literal object) {
        this.graphHandler.link(subject, predicate, object);
        if (this.reifiedRef != null) {
            this.graphHandler.link(this.reifiedRef,
                    RdfConstants.PREDICATE_SUBJECT, subject);
            this.graphHandler.link(this.reifiedRef,
                    RdfConstants.PREDICATE_PREDICATE, predicate);
            this.graphHandler.link(this.reifiedRef,
                    RdfConstants.PREDICATE_OBJECT, object);
            this.graphHandler.link(reifiedRef, RdfConstants.PREDICATE_TYPE,
                    RdfConstants.PREDICATE_STATEMENT);
            this.reifiedRef = null;
        }
    }

    /**
     * Creates a statement and reify it, if necessary.
     * 
     * @param subject
     *            The subject of the statement.
     * @param predicate
     *            The predicate of the statement.
     * @param object
     *            The object of the statement.
     */
    private void link(Reference subject, Reference predicate, Reference object) {
        this.graphHandler.link(subject, predicate, object);

        if (this.reifiedRef != null) {
            this.graphHandler.link(this.reifiedRef,
                    RdfConstants.PREDICATE_SUBJECT, subject);
            this.graphHandler.link(this.reifiedRef,
                    RdfConstants.PREDICATE_PREDICATE, predicate);
            this.graphHandler.link(this.reifiedRef,
                    RdfConstants.PREDICATE_OBJECT, object);
            this.graphHandler.link(reifiedRef, RdfConstants.PREDICATE_TYPE,
                    RdfConstants.PREDICATE_STATEMENT);
            this.reifiedRef = null;
        }
    }

    /**
     * Returns the RDF URI of the given node represented by its namespace uri,
     * local name, name, and attributes. It also generates the available
     * statements, thanks to some shortcuts provided by the RDF XML syntax.
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
        if (attributes.getIndex("xml:base") != -1) {
            this.base.add(new Reference(attributes.getValue("xml:base")));
        }
        // Get the RDF URI of this node
        for (int i = 0; i < attributes.getLength(); i++) {
            String qName = attributes.getQName(i);
            if (checkRdfQName("about", qName)) {
                found = true;
                result = resolve(attributes.getValue(i), false);
            } else if (checkRdfQName("nodeID", qName)) {
                found = true;
                result = Link.createBlankRef(attributes.getValue(i));
            } else if (checkRdfQName("ID", qName)) {
                found = true;
                result = resolve(attributes.getValue(i), true);
            } else if ("xml:lang".equals(qName)) {
                this.language.add(Language.valueOf(attributes.getValue(i)));
            } else if ("xml:base".equals(qName)) {
                // Already stored
            } else {
                if (!qName.startsWith("xmlns")) {
                    String[] arc = { qName, attributes.getValue(i) };
                    arcs.add(arc);
                }
            }
        }
        if (!found) {
            // Blank node with no given ID
            result = Link.createBlankRef(ContentReader.newBlankNodeId());
        }

        // Create the available statements
        if (!checkRdfQName("Description", name)) {
            // Handle typed node
            this.graphHandler.link(result, RdfConstants.PREDICATE_TYPE,
                    resolve(uri, name));
        }
        for (String[] arc : arcs) {
            this.graphHandler.link(result, resolve(null, arc[0]), getLiteral(
                    arc[1], null, this.language.getValue()));
        }

        return result;
    }

    /**
     * Returns the state at the top of the heap and removes it from the heap.
     * 
     * @return The state at the top of the heap.
     */
    private ContentReader.State popState() {
        ContentReader.State result = null;
        int size = this.states.size();

        if (size > 0) {
            result = this.states.remove(size - 1);
        }

        return result;
    }

    /**
     * Returns the subject at the top of the heap and removes it from the heap.
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
    private void pushState(ContentReader.State state) {
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

    /**
     * Returns the absolute reference for a given value. If this value is not an
     * absolute URI, then the base URI is used.
     * 
     * @param value
     *            The value.
     * @param fragment
     *            True if the value is a fragment to add to the base reference.
     * @return Returns the absolute reference for a given value.
     */
    private Reference resolve(String value, boolean fragment) {
        Reference result = null;

        if (fragment) {
            result = new Reference(this.base.getValue());
            result.setFragment(value);
        } else {
            result = new Reference(value);
            if (result.isRelative()) {
                result = new Reference(this.base.getValue(), value);
            }
        }
        return result.getTargetRef();
    }

    /**
     * Returns the absolute reference of a parsed element according to its URI,
     * qualified name. In case the base URI is null or empty, then an attempt is
     * made to look for the mapped URI according to the qName.
     * 
     * @param uri
     *            The base URI.
     * @param qName
     *            The qualified name of the parsed element.
     * @return Returns the absolute reference of a parsed element.
     */
    private Reference resolve(String uri, String qName) {
        Reference result = null;

        int index = qName.indexOf(":");
        String prefix = null;
        String localName = null;
        if (index != -1) {
            prefix = qName.substring(0, index);
            localName = qName.substring(index + 1);
        } else {
            localName = qName;
            prefix = "";
        }

        if (uri != null && !"".equals(uri)) {
            if (!uri.endsWith("#") && !uri.endsWith("/")) {
                result = new Reference(uri + "/" + localName);
            } else {
                result = new Reference(uri + localName);
            }
        } else {
            String baseUri = this.prefixes.get(prefix);
            if (baseUri != null) {
                result = new Reference(baseUri + localName);
            }
        }

        return (result == null) ? null : result.getTargetRef();
    }

    @Override
    public void startDocument() throws SAXException {
        this.prefixes = new HashMap<String, String>();
        this.builder = new StringBuilder();
        this.states = new ArrayList<ContentReader.State>();
        this.subjects = new ArrayList<Reference>();
        nodeDepth = 0;
        pushState(State.NONE);
    }

    @Override
    public void startElement(String uri, String localName, String name,
            Attributes attributes) throws SAXException {
        ContentReader.State state = getCurrentState();
        this.base.incrDepth();
        this.language.incrDepth();
        if (!this.consumingContent && this.builder != null) {
            this.builder = null;
        }
        if (state == State.NONE) {
            if (checkRdfQName("RDF", name)) {
                // Top element
                String base = attributes.getValue("xml:base");
                if (base != null) {
                    this.base.add(new Reference(base));
                }
            } else {
                // Parse the current subject
                pushSubject(parseNode(uri, localName, name, attributes));
                pushState(State.SUBJECT);
            }
        } else if (state == State.SUBJECT) {
            // Parse the current predicate
            List<String[]> arcs = new ArrayList<String[]>();
            pushState(State.PREDICATE);
            this.consumingContent = true;
            Reference currentSubject = getCurrentSubject();
            this.currentPredicate = resolve(uri, name);
            Reference currentObject = null;
            for (int i = 0; i < attributes.getLength(); i++) {
                String qName = attributes.getQName(i);
                if (checkRdfQName("resource", qName)) {
                    this.consumingContent = false;
                    currentObject = resolve(attributes.getValue(i), false);
                    popState();
                    pushState(State.OBJECT);
                } else if (checkRdfQName("datatype", qName)) {
                    // The object is a literal
                    this.consumingContent = true;
                    popState();
                    pushState(State.LITERAL);
                    this.currentDataType = attributes.getValue(i);
                } else if (checkRdfQName("parseType", qName)) {
                    String value = attributes.getValue(i);
                    if ("Literal".equals(value)) {
                        this.consumingContent = true;
                        // The object is an XML literal
                        popState();
                        pushState(State.LITERAL);
                        this.currentDataType = RdfConstants.RDF_SYNTAX
                                + "XMLLiteral";
                        nodeDepth = 0;
                    } else if ("Resource".equals(value)) {
                        this.consumingContent = false;
                        // Create a new blank node
                        currentObject = Link.createBlankRef(ContentReader
                                .newBlankNodeId());
                        popState();
                        pushState(State.SUBJECT);
                        pushSubject(currentObject);
                    } else {
                        this.consumingContent = false;
                        // Error
                    }
                } else if (checkRdfQName("nodeID", qName)) {
                    this.consumingContent = false;
                    currentObject = Link.createBlankRef(attributes.getValue(i));
                    popState();
                    pushState(State.SUBJECT);
                    pushSubject(currentObject);
                } else if (checkRdfQName("ID", qName)) {
                    // Reify the statement
                    reifiedRef = resolve(attributes.getValue(i), true);
                } else if ("xml:lang".equals(qName)) {
                    this.language.add(Language.valueOf(attributes.getValue(i)));
                } else {
                    if (!qName.startsWith("xmlns")) {
                        // Add arcs.
                        String[] arc = { qName, attributes.getValue(i) };
                        arcs.add(arc);
                    }
                }
            }
            if (currentObject != null) {
                link(currentSubject, this.currentPredicate, currentObject);
            }

            if (!arcs.isEmpty()) {
                // Create arcs that starts from a blank node and ends to
                // literal values. This blank node is the object of the
                // current statement.

                boolean found = false;
                Reference blankNode = Link.createBlankRef(ContentReader
                        .newBlankNodeId());
                for (String[] arc : arcs) {
                    Reference pRef = resolve(null, arc[0]);
                    // Silently remove unrecognized attributes
                    if (pRef != null) {
                        found = true;
                        this.graphHandler.link(blankNode, pRef, new Literal(
                                arc[1]));
                    }
                }
                if (found) {
                    link(currentSubject, this.currentPredicate, blankNode);
                    popState();
                    pushState(State.OBJECT);
                }
            }
            if (this.consumingContent) {
                builder = new StringBuilder();
            }
        } else if (state == State.PREDICATE) {
            this.consumingContent = false;
            // Parse the current object, create the current link
            Reference object = parseNode(uri, localName, name, attributes);
            this.currentObject = object;
            link();
            pushSubject(object);
            pushState(State.SUBJECT);
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
        this.rdfDefaultNamespace = this.rdfDefaultNamespace
                || ((prefix == null || "".equals(prefix)
                        && RdfConstants.RDF_SYNTAX.toString(true, true).equals(
                                uri)));

        if (!uri.endsWith("#") && !uri.endsWith("/")) {
            this.prefixes.put(prefix, uri + "/");
        } else {
            this.prefixes.put(prefix, uri);
        }
    }
}