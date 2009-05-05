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
     * @throws IOException
     */
    public abstract void parse() throws IOException;

}
