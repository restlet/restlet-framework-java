package org.restlet.ext.rdf;

import org.restlet.data.Reference;

/**
 * Handler for the content of a Graph. List of callbacks used when parsing a
 * representation of a RDF graph.
 */
public abstract class GraphHandler {

    /**
     * Callback method used when a link has been parsed.
     * 
     * @param source
     *            The source or subject of the link.
     * @param typeRef
     *            The type reference of the link.
     * @param target
     *            The target or object of the link.
     */
    public abstract void link(Reference source, Reference typeRef,
            Literal target);

    /**
     * Callback method used when a link has been parsed.
     * 
     * @param source
     *            The source or subject of the link.
     * @param typeRef
     *            The type reference of the link.
     * @param target
     *            The target or object of the link.
     */
    public abstract void link(Reference source, Reference typeRef,
            Reference target);

    /**
     * Callback method used when a link has been parsed.
     * 
     * @param source
     *            The source or subject of the link.
     * @param typeRef
     *            The type reference of the link.
     * @param target
     *            The target or object of the link.
     */
    public abstract void link(Object source, Reference typeRef, Reference target);
}
