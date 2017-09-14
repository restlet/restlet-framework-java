/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.rdf;

import org.restlet.data.Reference;

/**
 * Link between a source resource and a target resource or literal. This exactly
 * maps with the concepts of statement, triple or relationship defined by RDF,
 * the core specification of the Semantic Web. A link is composed of a source
 * node (or subject in RDF terminology), a type URI reference (or predicate in
 * RDF terminology) and a target node (or object in RDF terminology).
 * 
 * We use this class in Restlet to enhance resources and make them part of the
 * Web of data (also know as Linked Data and Hyperdata).
 * 
 * @author Jerome Louvel
 * @see <a href="http://www.w3.org/TR/rdf-concepts/">RDF concepts</a>
 */
public class Link {

    /**
     * Creates a reference to a blank node. In this API, we support RDF blank
     * nodes using the "_" namespace and local identifiers, in a way similar to
     * the RDF n3 serialization format.
     * 
     * @param identifier
     *            The blank node identifier.
     * @return A reference to a blank node.
     */
    public static Reference createBlankRef(String identifier) {
        return new Reference("_:" + identifier);
    }

    /**
     * Indicates if a reference is identifying a blank node.
     * 
     * @param reference
     *            The reference to test.
     * @return True if a reference is identifying a blank node.
     * @see #createBlankRef(String)
     */
    public static boolean isBlankRef(Reference reference) {
        return ((reference != null) && ("_".equals(reference.getScheme())));
    }

    /** The source object. */
    private volatile Object source;

    /** The target object. */
    private volatile Object target;

    /** The type URI reference. */
    private volatile Reference typeRef;

    /**
     * Constructor. Leverages n3 reification feature where a graph itself can be
     * the source node of a link.
     * 
     * @param sourceGraph
     *            The source graph or subject in RDF terminology.
     * @param typeRef
     *            The type reference or predicate in RDF terminology.
     * @param targetLit
     *            The target literal or object in RDF terminology.
     */
    public Link(Graph sourceGraph, Reference typeRef, Literal targetLit) {
        this((Object) sourceGraph, typeRef, (Object) targetLit);
    }

    /**
     * Constructor. Leverages n3 reification feature where a graph itself can be
     * the source node of a link.
     * 
     * @param sourceGraph
     *            The source graph or subject in RDF terminology.
     * @param typeRef
     *            The type reference or predicate in RDF terminology.
     * @param target
     *            The target node or object in RDF terminology.
     */
    public Link(Graph sourceGraph, Reference typeRef, Object target) {
        this((Object) sourceGraph, typeRef, target);
    }

    /**
     * Constructor. Leverages n3 reification feature where a graph itself can be
     * the source node of a link.
     * 
     * @param sourceGraph
     *            The source graph or subject in RDF terminology.
     * @param typeRef
     *            The type reference or predicate in RDF terminology.
     * @param targetRef
     *            The target reference or object in RDF terminology.
     */
    public Link(Graph sourceGraph, Reference typeRef, Reference targetRef) {
        this(sourceGraph, typeRef, (Object) targetRef);
    }

    /**
     * Constructor. Leverages n3 reification feature where a graph itself can be
     * the source node of a link.
     * 
     * @param sourceGraph
     *            The source graph or subject in RDF terminology.
     * @param typeRef
     *            The type reference or predicate in RDF terminology.
     * @param targetLit
     *            The target literal or object in RDF terminology.
     */
    public Link(Graph sourceGraph, String typeRef, Literal targetLit) {
        this(sourceGraph, new Reference(typeRef), targetLit);
    }

    /**
     * Constructor. Leverages n3 reification feature where a graph itself can be
     * the source node of a link.
     * 
     * @param sourceGraph
     *            The source graph or subject in RDF terminology.
     * @param typeRef
     *            The type reference or predicate in RDF terminology.
     * @param target
     *            The target node or object in RDF terminology.
     */
    public Link(Graph sourceGraph, String typeRef, Object target) {
        this(sourceGraph, new Reference(typeRef), target);
    }

    /**
     * Constructor. Leverages n3 reification feature where a graph itself can be
     * the source node of a link.
     * 
     * @param sourceGraph
     *            The source graph or subject in RDF terminology.
     * @param typeRef
     *            The type reference or predicate in RDF terminology.
     * @param targetRef
     *            The target reference or object in RDF terminology.
     */
    public Link(Graph sourceGraph, String typeRef, Reference targetRef) {
        this(sourceGraph, new Reference(typeRef), targetRef);
    }

    /**
     * Constructor by copy.
     * 
     * @param from
     *            The link to copy from.
     */
    public Link(Link from) {
        this(from.getSource(), from.getTypeRef(), from.getTarget());
    }

    /**
     * Constructor.
     * 
     * @param source
     *            The source node or subject in RDF terminology.
     * @param typeRef
     *            The type reference or predicate in RDF terminology.
     * @param target
     *            The target node or object in RDF terminology.
     */
    private Link(Object source, Reference typeRef, Object target) {
        this.source = source;
        this.typeRef = typeRef;
        this.target = target;
    }

    /**
     * Constructor.
     * 
     * @param sourceRef
     *            The source resource reference or subject in RDF terminology.
     * @param typeRef
     *            The type reference or predicate in RDF terminology.
     * @param targetLit
     *            The target literal node or object in RDF terminology.
     */
    public Link(Reference sourceRef, Reference typeRef, Literal targetLit) {
        this(sourceRef, typeRef, (Object) targetLit);
    }

    /**
     * Constructor.
     * 
     * @param sourceRef
     *            The source resource reference or subject in RDF terminology.
     * @param typeRef
     *            The type reference or predicate in RDF terminology.
     * @param targetRef
     *            The target resource reference or object in RDF terminology.
     */
    public Link(Reference sourceRef, Reference typeRef, Reference targetRef) {
        this(sourceRef, typeRef, (Object) targetRef);
    }

    /**
     * Constructor.
     * 
     * @param sourceRef
     *            The source resource reference or subject in RDF terminology.
     * @param typeRef
     *            The type reference or predicate in RDF terminology.
     * @param targetLit
     *            The target literal node or object in RDF terminology.
     */
    public Link(Reference sourceRef, String typeRef, Literal targetLit) {
        this(sourceRef, new Reference(typeRef), targetLit);
    }

    /**
     * Constructor.
     * 
     * @param sourceRef
     *            The source resource reference or subject in RDF terminology.
     * @param typeRef
     *            The type reference or predicate in RDF terminology.
     * @param targetRef
     *            The target resource reference or object in RDF terminology.
     */
    public Link(Reference sourceRef, String typeRef, Reference targetRef) {
        this(sourceRef, new Reference(typeRef), targetRef);
    }

    /**
     * Returns the source which can be either a reference or a link or a graph
     * or null. This maps with the concept of subject in RDF terminology.
     * 
     * @return The source.
     */
    public Object getSource() {
        return this.source;
    }

    /**
     * Returns the source graph. Supports RDF reification or N3 formulae.
     * 
     * @return The source graph.
     * @see #getSource()
     */
    public Graph getSourceAsGraph() {
        return hasGraphSource() ? (Graph) getSource() : null;
    }

    /**
     * Returns the source link. Supports RDF reification.
     * 
     * @return The source link.
     * @see #getSource()
     */
    public Link getSourceAsLink() {
        return hasLinkSource() ? (Link) getSource() : null;
    }

    /**
     * Returns the source resource reference.
     * 
     * @return The source resource reference.
     * @see #getSource()
     */
    public Reference getSourceAsReference() {
        return hasReferenceSource() ? (Reference) getSource() : null;
    }

    /**
     * Returns the target which can be either a literal or a reference or is
     * null. This maps with the concept of object in RDF terminology.
     * 
     * @return The target.
     */
    public Object getTarget() {
        return this.target;
    }

    /**
     * Returns the target graph.
     * 
     * @return The target graph.
     * @see #getTarget()
     */
    public Graph getTargetAsGraph() {
        return hasGraphTarget() ? (Graph) getTarget() : null;
    }

    /**
     * Returns the target link.
     * 
     * @return The target link.
     * @see #getTarget()
     */
    public Link getTargetAsLink() {
        return hasLinkTarget() ? (Link) getTarget() : null;
    }

    /**
     * Returns the target literal.
     * 
     * @return The target literal.
     * @see #getTarget()
     */
    public Literal getTargetAsLiteral() {
        return hasLiteralTarget() ? (Literal) getTarget() : null;
    }

    /**
     * Returns the target resource reference.
     * 
     * @return The target resource reference.
     * @see #getTarget()
     */
    public Reference getTargetAsReference() {
        return hasReferenceTarget() ? (Reference) getTarget() : null;
    }

    /**
     * Returns the type reference. This maps with the concept of predicate in
     * RDF terminology.
     * 
     * @return The type reference.
     */
    public Reference getTypeRef() {
        return this.typeRef;
    }

    /**
     * Indicates if the source is a graph.
     * 
     * @return True if the source is a graph.
     */
    public boolean hasGraphSource() {
        return getSource() instanceof Graph;
    }

    /**
     * Indicates if the target is a graph.
     * 
     * @return True if the target is a graph.
     */
    public boolean hasGraphTarget() {
        return getTarget() instanceof Graph;
    }

    /**
     * Indicates if the source is a link.
     * 
     * @return True if the source is a link.
     */
    public boolean hasLinkSource() {
        return getSource() instanceof Link;
    }

    /**
     * Indicates if the target is a link.
     * 
     * @return True if the target is a link.
     */
    public boolean hasLinkTarget() {
        return getTarget() instanceof Link;
    }

    /**
     * Indicates if the target is a literal.
     * 
     * @return True if the target is a literal.
     */
    public boolean hasLiteralTarget() {
        return getTarget() instanceof Literal;
    }

    /**
     * Indicates if the source is a reference.
     * 
     * @return True if the source is a reference.
     */
    public boolean hasReferenceSource() {
        return getSource() instanceof Reference;
    }

    /**
     * Indicates if the target is a reference.
     * 
     * @return True if the target is a reference.
     */
    public boolean hasReferenceTarget() {
        return getTarget() instanceof Reference;
    }

    /**
     * Sets the source as a graph. This maps with the concept of subject in RDF
     * terminology.
     * 
     * @param sourceGraph
     *            The source graph.
     */
    public void setSource(Graph sourceGraph) {
        this.source = sourceGraph;
    }

    /**
     * Sets the source as a link. This maps with the concept of subject in RDF
     * terminology.
     * 
     * @param sourceLink
     *            The source link.
     */
    public void setSource(Link sourceLink) {
        this.source = sourceLink;
    }

    /**
     * Sets the source resource reference. This maps with the concept of subject
     * in RDF terminology.
     * 
     * @param sourceRef
     *            The source resource reference.
     */
    public void setSource(Reference sourceRef) {
        this.source = sourceRef;
    }

    /**
     * Sets the target as a graph. This maps with the concept of object in RDF
     * terminology.
     * 
     * @param targetGraph
     *            The target graph.
     */
    public void setTarget(Graph targetGraph) {
        this.target = targetGraph;
    }

    /**
     * Sets the target as a link. This maps with the concept of object in RDF
     * terminology.
     * 
     * @param targetLink
     *            The target link.
     */
    public void setTarget(Link targetLink) {
        this.target = targetLink;
    }

    /**
     * Sets the target literal. This maps with the concept of object in RDF
     * terminology.
     * 
     * @param targetLit
     *            The target literal.
     */
    public void setTarget(Literal targetLit) {
        this.target = targetLit;
    }

    /**
     * Sets the target as a resource reference. This maps with the concept of
     * object in RDF terminology.
     * 
     * @param targetRef
     *            The target resource reference.
     */
    public void setTarget(Reference targetRef) {
        this.target = targetRef;
    }

    /**
     * Sets the type reference. This maps with the concept of predicate in RDF
     * terminology.
     * 
     * @param typeRef
     *            The type reference.
     */
    public void setTypeRef(Reference typeRef) {
        this.typeRef = typeRef;
    }

}
