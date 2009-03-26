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

import org.restlet.data.Reference;
import org.restlet.ext.rdf.internal.xml.RdfXmlParsingContentHandler;
import org.restlet.ext.rdf.internal.xml.RdfXmlWritingContentHandler;
import org.restlet.representation.Representation;

/**
 * Representation for RDF/n3 documents. It knows how to serialize and
 * deserialize a {@link Graph}.
 * 
 * @author Thierry Boileau
 */
public class RdfXmlRepresentation extends RdfRepresentation {

    /** List "first". */
    public static Reference LIST_FIRST = new Reference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#first");

    /** List "rest". */
    public static Reference LIST_REST = new Reference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#rest");

    /** Object "nil". */
    public static Reference OBJECT_NIL = new Reference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#nil");

    /** Predicate "implies" . */
    public static Reference PREDICATE_IMPLIES = new Reference(
            "http://www.w3.org/2000/10/swap/log#implies");

    /** Predicate "same as". */
    public static Reference PREDICATE_SAME = new Reference(
            "http://www.w3.org/2002/07/owl#sameAs");

    /** Predicate "is a". */
    public static Reference PREDICATE_TYPE = new Reference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

    /** Rdf schema. */
    public static Reference RDF_SCHEMA = new Reference(
            "http://www.w3.org/2000/01/rdf-schema#");

    /** Rdf syntax. */
    public static Reference RDF_SYNTAX = new Reference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#");

    public static Reference XML_SCHEMA = new Reference(
            "http://www.w3.org/2001/XMLSchema#");

    public static Reference XML_SCHEMA_TYPE_INTEGER = new Reference(
            "http://www.w3.org/2001/XMLSchema#int");

    public static Reference XML_SCHEMA_TYPE_FLOAT = new Reference(
            "http://www.w3.org/2001/XMLSchema#float");

    /**
     * Constructor.
     * 
     * @param linkSet
     *            The given graph of links.
     */
    public RdfXmlRepresentation(Graph linkSet) {
        super(linkSet);
    }

    /**
     * Constructor. Parses the given representation into the given graph.
     * 
     * @param rdfRepresentation
     *            The RDF N3 representation to parse.
     * @param linkSet
     *            The graph to update.
     * @throws IOException
     */
    public RdfXmlRepresentation(Representation rdfRepresentation, Graph linkSet)
            throws IOException {
        super(rdfRepresentation, linkSet);
        new RdfXmlParsingContentHandler(linkSet, rdfRepresentation);
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        if (getGraph() != null) {
            new RdfXmlWritingContentHandler(getGraph(), outputStream);
        }
    }
}
