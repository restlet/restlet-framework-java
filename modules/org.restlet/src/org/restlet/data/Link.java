/**
 * Copyright 2005-2008 Noelios Technologies.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.data;

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

    /** The source resource reference. */
    private Reference sourceRef;

    /** The target. */
    private Object target;

    /** The type reference. */
    private Reference typeRef;

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
     * @param target
     *            The target.
     */
    public Link(Reference sourceRef, Reference typeRef, Object target) {
        this.sourceRef = sourceRef;
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
     * @param targetRef
     *            The target resource reference.
     */
    public Link(Reference sourceRef, Reference typeRef, Reference targetRef) {
        this(sourceRef, typeRef, (Object) targetRef);
    }

    /**
     * Returns the source resource reference.
     * 
     * @return The source resource reference.
     */
    public Reference getSourceRef() {
        return sourceRef;
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
     * Indicates if the target is a literal.
     * 
     * @return True if the target is a literal.
     */
    public boolean hasLiteralTarget() {
        return getTarget() instanceof Literal;
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
     * Sets the source resource reference.
     * 
     * @param sourceRef
     *            The source resource reference.
     */
    public void setSourceRef(Reference sourceRef) {
        this.sourceRef = sourceRef;
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
