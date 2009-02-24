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

package org.restlet.data;

import java.util.concurrent.CopyOnWriteArraySet;

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
     * Constructor that parses a given RDF representation into a set of links.
     * The following RDF media types are supported: RDF/XML, RDF/n3 and
     * RDF/Turtle.
     * 
     * NOT IMPLEMENTED YET
     * 
     * @param rdfRepresentation
     *            The RDF representation to parse.
     */
    public Graph(Representation rdfRepresentation) {
        // TODO : call the engine for parsing
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
     * NOT IMPLEMENTED YET
     * 
     * @return A representation in the RDF/n3 format.
     */
    public Representation getRdfN3Representation() {
        return null; // new RdfN3Representation(this);
    }

    /**
     * Returns a representation in the RDF/Turtle format.
     * 
     * NOT IMPLEMENTED YET
     * 
     * @return A representation in the RDF/Turtle format.
     */
    public Representation getRdfTurtleRepresentation() {
        return null;
    }

    /**
     * Returns a representation in the RDF/XML format.
     * 
     * NOT IMPLEMENTED YET
     * 
     * @return A representation in the RDF/XML format.
     */
    public Representation getRdfXmlRepresentation() {
        return null;
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
