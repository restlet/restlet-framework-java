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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.restlet.data.Reference;
import org.restlet.ext.rdf.internal.RdfN3ContentHandler;
import org.restlet.representation.Representation;

/**
 * Representation for RDF/n3 documents. It knows how to serialize and
 * deserialize a {@link Graph}.
 * 
 * @author Thierry Boileau
 */
public class RdfN3Representation extends RdfRepresentation {

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

    /** Predicate "is as". */
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

    public RdfN3Representation(Graph linkSet) {
        super(linkSet);
    }

    public RdfN3Representation(Representation rdfRepresentation, Graph linkSet)
            throws IOException {
        super(rdfRepresentation, linkSet);
        new RdfN3ContentHandler(linkSet, rdfRepresentation);
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        if (getGraph() != null) {
            Map<String, String> prefixes = new HashMap<String, String>();
            prefixes.put(RDF_SCHEMA.toString(), "rdf");
            prefixes.put(RDF_SYNTAX.toString(), "rdfs");
            prefixes.put("http://www.w3.org/2000/10/swap/grammar/bnf#", "cfg");
            prefixes.put("http://www.w3.org/2000/10/swap/grammar/n3#", "n3");
            prefixes.put("http://www.w3.org/2000/10/swap/list#", "list");
            prefixes.put("http://www.w3.org/2000/10/swap/pim/doc#", "doc");
            prefixes.put("http://www.w3.org/2002/07/owl#", "owl");
            prefixes.put("http://www.w3.org/2000/10/swap/log#", "log");
            prefixes.put("http://purl.org/dc/elements/1.1/", "dc");
            prefixes.put("http://www.w3.org/2001/XMLSchema#", "type");

            StringBuilder builder = new StringBuilder();
            for (Entry<String, String> entry : prefixes.entrySet()) {
                builder.append("@prefix ").append(entry.getValue()).append(":")
                        .append(entry.getKey()).append(".\n");
            }
            builder.append("@keywords a, is, of, has.\n");

            outputStream.write(builder.toString().getBytes());
            write(outputStream, getGraph(), prefixes);
        }
    }

    /**
     * Writes the representation of a given graph to a byte stream.
     * 
     * @param outputStream
     *            The output stream.
     * @param graph
     *            The graph to write.
     * @param prefixes
     *            The map of known namespaces.
     * @throws IOException
     */
    private void write(OutputStream outputStream, Graph graph,
            Map<String, String> prefixes) throws IOException {
        for (Link link : getGraph()) {
            System.out.print("*** Link ");
            System.out.print(link.getSource().toString());
            System.out.print(" ");
            System.out.print(link.getTypeRef().toString());
            System.out.print(" ");
            System.out.println(link.getTarget().toString());
            if (link.hasReferenceSource()) {
                write(outputStream, link.getSourceAsReference(), prefixes);
            } else if (link.hasLinkSource()) {
                // TODO Hande source as link.
            } else if (link.hasGraphSource()) {
                outputStream.write("{".getBytes());
                write(outputStream, link.getSourceAsGraph(),
                        new HashMap<String, String>(prefixes));
                outputStream.write("}".getBytes());
            } else {
                // TODO Must be an error
            }
            outputStream.write(" ".getBytes());
            write(outputStream, link.getTypeRef(), prefixes);
            outputStream.write(" ".getBytes());
            if (link.hasReferenceTarget()) {
                write(outputStream, link.getTargetAsReference(), prefixes);
            } else if (link.hasLiteralTarget()) {
                Literal target = link.getTargetAsLiteral();
                // Write it as a string
                outputStream.write("\"".getBytes());
                if (target.getValue().contains("\n")) {
                    outputStream.write("\"".getBytes());
                    outputStream.write("\"".getBytes());
                    outputStream.write(target.getValue().getBytes());
                    outputStream.write("\"".getBytes());
                    outputStream.write("\"".getBytes());
                } else {
                    outputStream.write(target.getValue().getBytes());
                }

                outputStream.write("\"".getBytes());
                if (target.getDatatypeRef() != null) {
                    outputStream.write("^^".getBytes());
                    write(outputStream, target.getDatatypeRef(), prefixes);
                }
                if (target.getLanguage() != null) {
                    outputStream.write("@".getBytes());
                    outputStream.write(target.getLanguage().toString()
                            .getBytes());
                }
            } else if (link.hasLinkTarget()) {
                // TODO Hande target as link.
            } else if (link.hasGraphTarget()) {
                outputStream.write("{".getBytes());
                write(outputStream, link.getTargetAsGraph(),
                        new HashMap<String, String>(prefixes));
                outputStream.write("}".getBytes());
            } else {
                // TODO Must be an error
            }
            outputStream.write(".\n".getBytes());
        }
    }

    /**
     * Writes the representation of a given reference to a byte stream.
     * 
     * @param outputStream
     *            The output stream.
     * @param reference
     *            The reference to write.
     * @param prefixes
     *            The map of known namespaces.
     * @throws IOException
     */
    private void write(OutputStream outputStream, Reference reference,
            Map<String, String> prefixes) throws IOException {
        String uri = reference.toString();
        if (LinkReference.isBlank(reference)) {
            outputStream.write(uri.getBytes());
        } else {
            boolean found = false;
            for (Entry<String, String> entry : prefixes.entrySet()) {
                if (uri.startsWith(entry.getKey())) {
                    found = true;
                    StringBuilder builder = new StringBuilder(entry.getValue());
                    builder.append(":");
                    builder.append(uri.substring(entry.getKey().length()));
                    outputStream.write(builder.toString().getBytes());
                    break;
                }
            }
            if (!found) {
                outputStream.write("<".getBytes());
                outputStream.write(uri.getBytes());
                outputStream.write(">".getBytes());
            }
        }
    }
}
