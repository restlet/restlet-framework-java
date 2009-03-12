/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
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

package org.restlet.ext.rdf;

import org.restlet.data.Reference;
import org.restlet.resource.Resource;

/**
 * Link between a source resource and a target resource or literal. This is
 * compatible with the concepts of statement, triple or relationship defined by
 * RDF, the core specification of the Semantic Web.
 * 
 * We use this class in Restlet to enhance {@link Resource} and make them part
 * of the Web of data (also know as Linked Data and Hyperdata).
 * 
 * @author Jerome Louvel
 * @see <a href="http://www.w3.org/TR/rdf-concepts/">RDF concepts</a>
 */
public class Link {

    /** The source or subject. */
    private Object source;

    /** The target or object. */
    private Object target;

    /** The type reference. */
    private Reference typeRef;

    /**
     * Constructor by copy.
     * 
     * @param from
     *            The link to copy from.
     */
    public Link(Link from) {
        this(from.source, from.typeRef, from.target);
    }

    /**
     * Constructor.
     * 
     * @param source
     *            The source.
     * @param typeRef
     *            The type reference.
     * @param target
     *            The target.
     */
    private Link(Object source, Reference typeRef, Object target) {
        this.source = source;
        this.target = target;
        this.typeRef = typeRef;
    }

    /**
     * Constructor.
     * 
     * @param sourceRef
     *            The source resource reference.
     * @param typeRef
     *            The type reference.
     * @param targetLit
     *            The target literal.
     */
    public Link(Reference sourceRef, Reference typeRef, Literal targetLit) {
        this(sourceRef, typeRef, (Object) targetLit);
    }

    /**
     * Constructor.
     * 
     * @param sourceRef
     *            The source resource reference.
     * @param typeRef
     *            The type reference.
     * @param targetRef
     *            The target resource reference.
     */
    public Link(Reference sourceRef, Reference typeRef, Reference targetRef) {
        this(sourceRef, typeRef, (Object) targetRef);
    }

    @Override
    public boolean equals(Object object) {
        boolean result = false;

        if (object instanceof Link) {
            Link link = (Link) object;

            result = ((getSourceAsReference() == null) || (getSourceAsReference()
                    .equals(link.getSourceAsReference())))
                    && ((getTarget() == null) || (getTarget().equals(link
                            .getTarget())))
                    && ((getTypeRef() == null) || (getTypeRef().equals(link
                            .getTypeRef())));
        }

        return result;
    }

    /**
     * Returns the source which can be either a reference or a link or a graph
     * or null.
     * 
     * @return The source.
     */
    public Object getSource() {
        return source;
    }

    /**
     * Returns the source graph. Supports RDF reification or N3 formulae.
     * 
     * @return The source graph.
     */
    public Graph getSourceAsGraph() {
        return hasGraphSource() ? (Graph) getSource() : null;
    }

    /**
     * Returns the source link. Supports RDF reification.
     * 
     * @return The source link.
     */
    public Link getSourceAsLink() {
        return hasLinkSource() ? (Link) getSource() : null;
    }

    /**
     * Returns the source resource reference.
     * 
     * @return The source resource reference.
     */
    public Reference getSourceAsReference() {
        return hasReferenceSource() ? (Reference) getSource() : null;
    }

    /**
     * Returns the target which can be either a literal or a reference or is
     * null.
     * 
     * @return The target.
     */
    public Object getTarget() {
        return this.target;
    }

    /**
     * Returns the target graph.
     * 
     * @return The target literal.
     */
    public Literal getTargetAsGraph() {
        return hasLiteralTarget() ? (Literal) getTarget() : null;
    }

    /**
     * Returns the target literal.
     * 
     * @return The target literal.
     */
    public Literal getTargetAsLiteral() {
        return hasLiteralTarget() ? (Literal) getTarget() : null;
    }

    /**
     * Returns the target resource reference.
     * 
     * @return The target resource reference.
     */
    public Reference getTargetAsReference() {
        return hasReferenceTarget() ? (Reference) getTarget() : null;
    }

    /**
     * Returns the type reference.
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
     * Sets the source as a graph.
     * 
     * @param sourceGraph
     *            The source graph.
     */
    public void setSource(Graph sourceGraph) {
        this.source = sourceGraph;
    }

    /**
     * Sets the source as a link.
     * 
     * @param sourceLink
     *            The source link.
     */
    public void setSource(Link sourceLink) {
        this.source = sourceLink;
    }

    /**
     * Sets the source resource reference.
     * 
     * @param sourceRef
     *            The source resource reference.
     */
    public void setSource(Reference sourceRef) {
        this.source = sourceRef;
    }

    /**
     * Sets the target as a graph.
     * 
     * @param targetGraph
     *            The target graph.
     */
    public void setTarget(Graph targetGraph) {
        this.target = targetGraph;
    }

    /**
     * Sets the target as a link.
     * 
     * @param targetLink
     *            The target link.
     */
    public void setTarget(Link targetLink) {
        this.target = targetLink;
    }

    /**
     * Sets the target literal.
     * 
     * @param targetLit
     *            The target literal.
     */
    public void setTarget(Literal targetLit) {
        this.target = targetLit;
    }

    /**
     * Sets the target as a resource reference.
     * 
     * @param targetRef
     *            The target resource reference.
     */
    public void setTarget(Reference targetRef) {
        this.target = targetRef;
    }

    /**
     * Sets the type reference.
     * 
     * @param typeRef
     *            The type reference.
     */
    public void setTypeRef(Reference typeRef) {
        this.typeRef = typeRef;
    }

}
