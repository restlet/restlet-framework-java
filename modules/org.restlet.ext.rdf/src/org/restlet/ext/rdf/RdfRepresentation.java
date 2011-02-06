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
import java.io.Writer;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.ext.rdf.internal.n3.RdfN3Reader;
import org.restlet.ext.rdf.internal.n3.RdfN3Writer;
import org.restlet.ext.rdf.internal.ntriples.RdfNTriplesReader;
import org.restlet.ext.rdf.internal.ntriples.RdfNTriplesWriter;
import org.restlet.ext.rdf.internal.turtle.RdfTurtleReader;
import org.restlet.ext.rdf.internal.turtle.RdfTurtleWriter;
import org.restlet.ext.rdf.internal.xml.RdfXmlReader;
import org.restlet.ext.rdf.internal.xml.RdfXmlWriter;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;

/**
 * Generic RDF representation. Provides support for the Resource Description
 * Framework (RDF) Semantic Web standard. It supports major RDF serialization
 * formats (n3, Turtle, N-Triples and RDF/XML) and is able to both serialize and
 * deserialize a {@link Graph}.
 * 
 * @author Jerome Louvel
 */
public class RdfRepresentation extends WriterRepresentation {

    /** The inner graph of links. */
    private Graph graph;

    /** The inner RDF representation. */
    private Representation rdfRepresentation;

    /**
     * Constructor.
     */
    public RdfRepresentation() {
        super(MediaType.TEXT_XML);
    }

    /**
     * Constructor with argument.
     * 
     * @param linkSet
     *            The graph of links.
     * @param mediaType
     *            The representation's mediaType.
     */
    public RdfRepresentation(Graph linkSet, MediaType mediaType) {
        super(mediaType);
        this.graph = linkSet;
    }

    /**
     * Constructor with argument.
     * 
     * @param mediaType
     *            The representation's mediaType.
     */
    public RdfRepresentation(MediaType mediaType) {
        super(mediaType);
    }

    /**
     * Constructor that parsed a given RDF representation into a link set.
     * 
     * @param rdfRepresentation
     *            The RDF representation to parse.
     * @throws IOException
     */
    public RdfRepresentation(Representation rdfRepresentation)
            throws IOException {
        super(rdfRepresentation.getMediaType());
        this.rdfRepresentation = rdfRepresentation;
    }

    /**
     * Returns an instance of a graph handler used when parsing the inner RDF
     * representation.
     * 
     * @param graph
     *            The graph to build.
     * @return An instance of a graph handler used when parsing the inner RDF
     *         representation.
     */
    public GraphHandler createBuilder(Graph graph) {
        return new GraphBuilder(this.graph);
    }

    /**
     * Returns an instance of a graph handler used when writing the inner set of
     * links.
     * 
     * @param mediaType
     *            The given media type of the parsed RDF representation.
     * @param writer
     *            The character writer to write to.
     * @return An instance of a graph handler used when writing the inner set of
     *         links.
     * @throws IOException
     */
    public GraphHandler createWriter(MediaType mediaType, Writer writer)
            throws IOException {
        if (MediaType.TEXT_RDF_N3.equals(getMediaType())) {
            return new RdfN3Writer(writer);
        } else if (MediaType.TEXT_XML.equals(getMediaType())) {
            return new RdfXmlWriter(writer);
        } else if (MediaType.APPLICATION_ALL_XML.includes(getMediaType())) {
            return new RdfXmlWriter(writer);
        } else if (MediaType.TEXT_PLAIN.equals(getMediaType())) {
            return new RdfNTriplesWriter(writer);
        } else if (MediaType.TEXT_RDF_NTRIPLES.equals(getMediaType())) {
            return new RdfNTriplesWriter(writer);
        } else if (MediaType.APPLICATION_RDF_TURTLE.equals(getMediaType())) {
            return new RdfTurtleWriter(writer);
        }

        // Writing for other media types goes here.
        return null;
    }

    /**
     * Updates the list of known namespaces for the given graph of links.
     * 
     * @param linkset
     *            The given graph of links.
     * @param GraphHandler
     *            the graph handler.
     */
    private void discoverNamespaces(Graph linkset, GraphHandler graphHandler) {
        for (Link link : linkset) {
            discoverNamespaces(link, graphHandler);
        }
    }

    /**
     * Updates the list of known namespaces of the XML writer for the given
     * link.
     * 
     * @param link
     *            The given link.
     * @param GraphHandler
     *            the graph handler.
     */
    private void discoverNamespaces(Link link, GraphHandler graphHandler) {
        // The subject of the link is not discovered, it is generated as the
        // value of an "about" attribute.
        if (link.hasLinkSource()) {
            discoverNamespaces(link.getSourceAsLink(), graphHandler);
        } else if (link.hasGraphSource()) {
            discoverNamespaces(link.getSourceAsGraph(), graphHandler);
        }
        discoverNamespaces(link.getTypeRef(), graphHandler);
        if (link.hasLinkTarget()) {
            discoverNamespaces(link.getTargetAsLink(), graphHandler);
        } else if (link.hasGraphSource()) {
            discoverNamespaces(link.getSourceAsGraph(), graphHandler);
        }
    }

    /**
     * Updates the list of known namespaces of the XML writer for the given
     * reference.
     * 
     * @param reference
     *            The given reference.
     * @param xmlWriter
     *            the XML writer.
     */
    private void discoverNamespaces(Reference reference,
            GraphHandler graphHandler) {
        if (!Link.isBlankRef(reference)) {
            graphHandler.startPrefixMapping(null, reference);
        }
    }

    /**
     * Returns the graph of links.
     * 
     * @return The graph of links.
     * @throws Exception
     */
    public Graph getGraph() throws Exception {
        if (this.graph == null) {
            this.graph = new Graph();
            parse(createBuilder(this.graph));
        }
        return this.graph;
    }

    /**
     * Parses the inner RDF representation. The given graph handler is invoked
     * each time a link is detected.
     * 
     * @param graphHandler
     *            The graph handler.
     * @throws Exception
     */
    public void parse(GraphHandler graphHandler) throws Exception {
        if (rdfRepresentation != null) {
            if (MediaType.TEXT_RDF_N3.equals(rdfRepresentation.getMediaType())) {
                new RdfN3Reader(rdfRepresentation, graphHandler).parse();
            } else if (MediaType.TEXT_XML.equals(rdfRepresentation
                    .getMediaType())) {
                new RdfXmlReader(rdfRepresentation, graphHandler).parse();
            } else if (MediaType.APPLICATION_ALL_XML.includes(rdfRepresentation
                    .getMediaType())) {
                new RdfXmlReader(rdfRepresentation, graphHandler).parse();
            } else if (MediaType.TEXT_PLAIN.equals(rdfRepresentation
                    .getMediaType())) {
                new RdfNTriplesReader(rdfRepresentation, graphHandler).parse();
            } else if (MediaType.TEXT_RDF_NTRIPLES.equals(rdfRepresentation
                    .getMediaType())) {
                new RdfNTriplesReader(rdfRepresentation, graphHandler).parse();
            } else if (MediaType.APPLICATION_RDF_TURTLE
                    .equals(rdfRepresentation.getMediaType())) {
                new RdfTurtleReader(rdfRepresentation, graphHandler).parse();
            } else if (MediaType.valueOf("text/rdf+n3").equals(
                    rdfRepresentation.getMediaType())) {
                // Deprecated media type still in usage
                new RdfN3Reader(rdfRepresentation, graphHandler).parse();
            }
            // Parsing for other media types goes here.
        }
    }

    /**
     * Sets the graph of links.
     * 
     * @param linkSet
     *            The graph of links.
     */
    public void setGraph(Graph linkSet) {
        this.graph = linkSet;
    }

    /**
     * Writes the
     * 
     * @param graphHandler
     * @throws IOException
     */
    public void write(GraphHandler graphHandler) throws IOException {
        if (graph != null) {
            discoverNamespaces(graph, graphHandler);
            graphHandler.startGraph();
            for (Link link : graph) {
                if (link.hasReferenceSource()) {
                    if (link.hasReferenceTarget()) {
                        graphHandler.link(link.getSourceAsReference(), link
                                .getTypeRef(), link.getTargetAsReference());
                    } else if (link.hasLiteralTarget()) {
                        graphHandler.link(link.getSourceAsReference(), link
                                .getTypeRef(), link.getTargetAsLiteral());
                    } else if (link.hasLinkTarget()) {
                        Context
                                .getCurrentLogger()
                                .warning(
                                        "Cannot write the representation of a statement due to the fact that the object is neither a Reference nor a literal.");
                    } else {
                        Context
                                .getCurrentLogger()
                                .warning(
                                        "Cannot write the representation of a statement due to the fact that the object is neither a Reference nor a literal.");
                    }
                } else if (link.hasGraphSource()) {
                    if (link.hasReferenceTarget()) {
                        graphHandler.link(link.getSourceAsGraph(), link
                                .getTypeRef(), link.getTargetAsReference());
                    } else if (link.hasLiteralTarget()) {
                        graphHandler.link(link.getSourceAsGraph(), link
                                .getTypeRef(), link.getTargetAsLiteral());
                    } else if (link.hasLinkTarget()) {
                        Context
                                .getCurrentLogger()
                                .warning(
                                        "Cannot write the representation of a statement due to the fact that the object is neither a Reference nor a literal.");
                    } else {
                        Context
                                .getCurrentLogger()
                                .warning(
                                        "Cannot write the representation of a statement due to the fact that the object is neither a Reference nor a literal.");
                    }
                }
            }
            graphHandler.endGraph();
        }
    }

    @Override
    public void write(Writer writer) throws IOException {
        write(createWriter(getMediaType(), writer));
    }

}
