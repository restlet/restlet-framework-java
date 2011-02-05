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

import java.io.IOException;

import org.restlet.data.Reference;

/**
 * Handler for the content of a Graph. List of callbacks used when parsing or
 * writing a representation of a RDF graph.
 */
public abstract class GraphHandler {

    /**
     * Callback method used after the graph is parsed or written. Does nothing
     * by default.
     * 
     * @throws IOException
     */
    public void endGraph() throws IOException {

    }

    /**
     * Callback method used at the end of a Namespace mapping. Does nothing by
     * default.
     * 
     * @param prefix
     *            The Namespace prefix.
     */
    public void endPrefixMapping(String prefix) {

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
    public abstract void link(Graph source, Reference typeRef, Literal target);

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
    public abstract void link(Graph source, Reference typeRef, Reference target);

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
    public abstract void link(Reference source, Reference typeRef,
            Literal target);

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
    public abstract void link(Reference source, Reference typeRef,
            Reference target);

    /**
     * Callback method used before the graph is parsed or written. Does nothing
     * by default.
     * 
     * @throws IOException
     */
    public void startGraph() throws IOException {

    }

    /**
     * Callback method used at the start of a Namespace mapping. Does nothing by
     * default.
     * 
     * @param prefix
     *            The Namespace prefix being declared.
     * @param reference
     *            The Namespace URI mapped to the prefix.
     */
    public void startPrefixMapping(String prefix, Reference reference) {

    }

}
