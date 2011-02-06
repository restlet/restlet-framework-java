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

package org.restlet.ext.rdf;

import java.util.concurrent.CopyOnWriteArraySet;

import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;

/**
 * Graph composed of links. This also called a set of RDF statements or a RDF
 * model.
 * 
 * @author Jerome Louvel
 */
public class Graph extends CopyOnWriteArraySet<Link> {

    /** The serialization unique identifier. */
    private static final long serialVersionUID = 1L;

    /** The default link that is used to complete new links. */
    private Link defaultLink;

    /**
     * Default constructor.
     */
    public Graph() {
        this((Link) null);
    }

    /**
     * Constructor with a default link.
     * 
     * @param defaultLink
     *            The link to use when adding links with missing properties.
     */
    public Graph(Link defaultLink) {
        this.defaultLink = defaultLink;
    }

    /**
     * Creates then adds a link. If one of the parameter is null, the value from
     * {@link #getDefaultLink()} is used instead if possible.
     * 
     * @param sourceGraph
     *            The source graph.
     * @param typeRef
     *            The type reference.
     * @param targetLit
     *            The target literal.
     * @return The created link.
     */
    public Link add(Graph sourceGraph, Reference typeRef, Literal targetLit) {
        Link result = new Link(getSourceAsGraph(sourceGraph),
                getTypeRef(typeRef), getTargetAsLiteral(targetLit));
        add(result);
        return result;
    }

    /**
     * Creates then adds a link. If one of the parameter is null, the value from
     * {@link #getDefaultLink()} is used instead if possible.
     * 
     * @param sourceGraph
     *            The source graph.
     * @param typeRef
     *            The type reference.
     * @param targetRef
     *            The target reference.
     * @return The created link.
     */
    public Link add(Graph sourceGraph, Reference typeRef, Reference targetRef) {
        Link result = new Link(getSourceAsGraph(sourceGraph),
                getTypeRef(typeRef), getTargetAsReference(targetRef));
        add(result);
        return result;
    }

    /**
     * Creates then adds a link. If one of the parameter is null, the value from
     * {@link #getDefaultLink()} is used instead if possible.
     * 
     * @param sourceRef
     *            The source resource reference.
     * @param typeRef
     *            The type reference.
     * @param targetLit
     *            The target literal.
     * @return The created link.
     */
    public Link add(Reference sourceRef, Reference typeRef, Literal targetLit) {
        Link result = new Link(getSourceAsReference(sourceRef),
                getTypeRef(typeRef), getTargetAsLiteral(targetLit));
        add(result);
        return result;
    }

    /**
     * Creates then adds a link. If one of the parameter is null, the value from
     * {@link #getDefaultLink()} is used instead if possible.
     * 
     * @param sourceRef
     *            The source resource reference.
     * @param typeRef
     *            The type reference.
     * @param targetRef
     *            The target resource reference.
     * @return The created link.
     */
    public Link add(Reference sourceRef, Reference typeRef, Reference targetRef) {
        Link result = new Link(getSourceAsReference(sourceRef),
                getTypeRef(typeRef), getTargetAsReference(targetRef));
        add(result);
        return result;
    }

    /**
     * Returns the default link that is used to complete new links.
     * 
     * @return The default link that is used to complete new links.
     */
    public Link getDefaultLink() {
        return defaultLink;
    }

    /**
     * Returns a representation in the RDF/n3 format.
     * 
     * @return A representation in the RDF/n3 format.
     */
    public Representation getRdfN3Representation() {
        return new RdfRepresentation(this, MediaType.TEXT_RDF_N3);
    }

    /**
     * Returns a representation in the RDF/N-Triples format.
     * 
     * @return A representation in the RDF/N-Triples format.
     */
    public Representation getRdfNTriplesRepresentation() {
        return new RdfRepresentation(this, MediaType.TEXT_PLAIN);
    }

    /**
     * Returns a representation in the RDF/Turtle format.
     * 
     * @return A representation in the RDF/Turtle format.
     */
    public Representation getRdfTurtleRepresentation() {
        return new RdfRepresentation(this, MediaType.APPLICATION_RDF_TURTLE);
    }

    /**
     * Returns a representation in the RDF/XML format.
     * 
     * @return A representation in the RDF/XML format.
     */
    public Representation getRdfXmlRepresentation() {
        return new RdfRepresentation(this, MediaType.TEXT_XML);
    }

    /**
     * Returns the source reference, either the one given in the sourceRef
     * parameter or if it is null, the source reference of the default link.
     * 
     * @param sourceRef
     *            The source reference to check.
     * @return The source reference.
     */
    private Graph getSourceAsGraph(Graph sourceGraph) {
        Graph result = sourceGraph;

        if ((result == null) && (getDefaultLink() != null)) {
            result = getDefaultLink().getSourceAsGraph();
        }

        return result;
    }

    /**
     * Returns the source reference, either the one given in the sourceRef
     * parameter or if it is null, the source reference of the default link.
     * 
     * @param sourceRef
     *            The source reference to check.
     * @return The source reference.
     */
    private Reference getSourceAsReference(Reference sourceRef) {
        Reference result = sourceRef;

        if ((result == null) && (getDefaultLink() != null)) {
            result = getDefaultLink().getSourceAsReference();
        }

        return result;
    }

    /**
     * Returns the target literal, either the one given in the targetLit
     * parameter or if it is null, the target literal of the default link.
     * 
     * @param targetLit
     *            The target literal to check.
     * @return The target literal.
     */
    private Literal getTargetAsLiteral(Literal targetLit) {
        Literal result = targetLit;

        if ((result == null) && (getDefaultLink() != null)) {
            result = getDefaultLink().getTargetAsLiteral();
        }

        return result;
    }

    /**
     * Returns the target reference, either the one given in the targetRef
     * parameter or if it is null, the target reference of the default link.
     * 
     * @param targetRef
     *            The target reference to check.
     * @return The target reference.
     */
    private Reference getTargetAsReference(Reference targetRef) {
        Reference result = targetRef;

        if ((result == null) && (getDefaultLink() != null)) {
            result = getDefaultLink().getTargetAsReference();
        }

        return result;
    }

    /**
     * Returns the type reference, either the one given in the typeRef parameter
     * or if it is null, the type reference of the default link.
     * 
     * @param typeRef
     *            The type reference to check.
     * @return The type reference.
     */
    private Reference getTypeRef(Reference typeRef) {
        Reference result = typeRef;

        if ((result == null) && (getDefaultLink() != null)) {
            result = getDefaultLink().getTypeRef();
        }

        return result;
    }

    /**
     * Sets the default link that is used to complete new links.
     * 
     * @param defaultLink
     *            The default link that is used to complete new links.
     */
    public void setDefaultLink(Link defaultLink) {
        this.defaultLink = defaultLink;
    }

}
