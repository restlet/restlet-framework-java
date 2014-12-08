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

package org.restlet.ext.rdf.internal;

import java.io.IOException;

import org.restlet.ext.rdf.GraphHandler;
import org.restlet.representation.Representation;

/**
 * Super class of all RDF readers.
 * 
 */
public abstract class RdfReader {
    /** The graph handler invoked when parsing. */
    private GraphHandler graphHandler;

    /** The representation to read. */
    private Representation rdfRepresentation;

    /**
     * Constructor.
     * 
     * @param rdfRepresentation
     *            The representation to read.
     * @param graphHandler
     *            The graph handler invoked during the parsing.
     * @throws IOException
     */
    public RdfReader(Representation rdfRepresentation, GraphHandler graphHandler) {
        super();
        this.rdfRepresentation = rdfRepresentation;
        this.graphHandler = graphHandler;
    }

    /**
     * Returns the graph handler invoked when parsing.
     * 
     * @return The graph handler invoked when parsing.
     */
    public GraphHandler getGraphHandler() {
        return graphHandler;
    }

    /**
     * Returns the representation to read.
     * 
     * @return The representation to read.
     */
    public Representation getRdfRepresentation() {
        return rdfRepresentation;
    }

    /**
     * Parses the content.
     * 
     * @throws Exception
     */
    public abstract void parse() throws Exception;

}
