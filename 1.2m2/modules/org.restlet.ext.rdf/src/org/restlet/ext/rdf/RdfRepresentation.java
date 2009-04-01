/**
 * Copyright 2005-2009 Noelios Technologies.
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
import java.io.OutputStream;

import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;

/**
 * Base of all RDF representation classes. It knows how to serialize and
 * deserialize a {@link Graph}.
 * 
 * @author Jerome Louvel
 */
public abstract class RdfRepresentation extends OutputRepresentation {

    /** The inner graph of links. */
    private Graph graph;

    /**
     * Constructor with argument.
     * 
     * @param linkSet
     *            The graph of link.
     */
    public RdfRepresentation(Graph linkSet) {
        super(null);
        this.graph = linkSet;
    }

    /**
     * Constructor that parsed a given RDF representation into a link set.
     * 
     * @param rdfRepresentation
     *            The RDF representation to parse.
     * @param linkSet
     *            The link set to update.
     * @throws IOException
     */
    public RdfRepresentation(Representation rdfRepresentation, Graph linkSet)
            throws IOException {
        this(linkSet);
        if (MediaType.TEXT_RDF_N3.equals(rdfRepresentation.getMediaType())) {
            new RdfN3Representation(rdfRepresentation, linkSet);
        } else if (MediaType.TEXT_XML.equals(rdfRepresentation.getMediaType())) {
            new RdfXmlRepresentation(rdfRepresentation, linkSet);
        } else if (MediaType.APPLICATION_ALL_XML.includes(rdfRepresentation
                .getMediaType())) {
            new RdfXmlRepresentation(rdfRepresentation, linkSet);
        }
        // Parsing for other media types goes here.
    }

    /**
     * Returns the graph of links.
     * 
     * @return The graph of links.
     */
    public Graph getGraph() {
        return graph;
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

    @Override
    public void write(OutputStream outputStream) throws IOException {
        if (MediaType.TEXT_RDF_N3.equals(getMediaType())) {
            new RdfN3Representation(getGraph()).write(outputStream);
        } else if (MediaType.TEXT_XML.equals(getMediaType())) {
            new RdfXmlRepresentation(getGraph()).write(outputStream);
        } else if (MediaType.APPLICATION_ALL_XML.includes(getMediaType())) {
            new RdfXmlRepresentation(getGraph()).write(outputStream);
        }
        // Writing for other media types goes here.
    }
}
