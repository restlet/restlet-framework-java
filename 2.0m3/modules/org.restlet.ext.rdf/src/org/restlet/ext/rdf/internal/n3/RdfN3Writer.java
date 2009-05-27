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

package org.restlet.ext.rdf.internal.n3;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Map.Entry;

import org.restlet.data.Reference;
import org.restlet.ext.rdf.Graph;
import org.restlet.ext.rdf.GraphHandler;
import org.restlet.ext.rdf.Link;
import org.restlet.ext.rdf.LinkReference;
import org.restlet.ext.rdf.Literal;
import org.restlet.ext.rdf.internal.RdfConstants;
import org.restlet.ext.rdf.internal.turtle.Context;

/**
 * Handler of RDF content according to the N3 notation.
 * 
 * @author Thierry Boileau
 */
public class RdfN3Writer extends GraphHandler {

    /** Buffered writer. */
    private BufferedWriter bw;

    /** The current context object. */
    private Context context;

    /** The preceding predicate used for factorization matter. */
    private Reference precPredicate;

    /** The preceding source used for factorization matter. */
    private Reference precSource;

    /** Indicates if the end of the statement is to be written. */
    private boolean writeExtraDot;

    /**
     * Constructor.
     * 
     * @param outputStream
     *            The output stream to write to.
     * @throws IOException
     */
    public RdfN3Writer(OutputStream outputStream) throws IOException {
        super();
        this.bw = new BufferedWriter(new OutputStreamWriter(outputStream));
        this.context = new Context();

        Map<String, String> prefixes = context.getPrefixes();
        prefixes.put(RdfConstants.RDF_SCHEMA.toString(), "rdf");
        prefixes.put(RdfConstants.RDF_SYNTAX.toString(), "rdfs");
        prefixes.put("http://www.w3.org/2000/10/swap/grammar/bnf#", "cfg");
        prefixes.put("http://www.w3.org/2000/10/swap/grammar/n3#", "n3");
        prefixes.put("http://www.w3.org/2000/10/swap/list#", "list");
        prefixes.put("http://www.w3.org/2000/10/swap/pim/doc#", "doc");
        prefixes.put("http://www.w3.org/2002/07/owl#", "owl");
        prefixes.put("http://www.w3.org/2000/10/swap/log#", "log");
        prefixes.put("http://purl.org/dc/elements/1.1/", "dc");
        prefixes.put("http://www.w3.org/2001/XMLSchema#", "type");

        for (Entry<String, String> entry : prefixes.entrySet()) {
            this.bw.append("@prefix ").append(entry.getValue()).append(": ")
                    .append(entry.getKey()).append(".\n");
        }
        this.bw.append("@keywords a, is, of, has.\n");
    }

    @Override
    public void link(Graph source, Reference typeRef, Literal target) {
        try {
            this.bw.write("{");
            write(source);
            this.bw.write("} ");
            write(typeRef, this.context.getPrefixes());
            this.bw.write(" ");
            write(target);

            this.precSource = null;
            this.precPredicate = typeRef;
        } catch (IOException e) {
            org.restlet.Context.getCurrentLogger().warning(
                    "Cannot write the representation of a statement due to "
                            + e.getMessage());
        }
    }

    @Override
    public void link(Graph source, Reference typeRef, Reference target) {
        try {
            this.bw.write("{");
            write(source);
            this.bw.write("} ");
            write(typeRef, this.context.getPrefixes());
            this.bw.write(" ");
            write(target, this.context.getPrefixes());

            this.precSource = null;
            this.precPredicate = typeRef;
        } catch (IOException e) {
            org.restlet.Context.getCurrentLogger().warning(
                    "Cannot write the representation of a statement due to "
                            + e.getMessage());
        }
    }

    @Override
    public void link(Reference source, Reference typeRef, Literal target) {
        try {
            if (source.equals(this.precSource)) {
                if (typeRef.equals(this.precPredicate)) {
                    this.bw.write(", ");
                } else {
                    this.bw.write("; ");
                    write(typeRef, this.context.getPrefixes());
                    this.bw.write(" ");
                }
            } else {
                this.writeExtraDot = true;
                this.bw.write(".\n");
                write(source, this.context.getPrefixes());
                this.bw.write(" ");
                write(typeRef, this.context.getPrefixes());
                this.bw.write(" ");
            }
            write(target);

            this.precSource = source;
            this.precPredicate = typeRef;
        } catch (IOException e) {
            org.restlet.Context.getCurrentLogger().warning(
                    "Cannot write the representation of a statement due to "
                            + e.getMessage());
        }
    }

    @Override
    public void link(Reference source, Reference typeRef, Reference target) {
        try {
            if (source.equals(this.precSource)) {
                this.writeExtraDot = false;
                if (typeRef.equals(this.precPredicate)) {
                    this.bw.write(", ");
                } else {
                    this.bw.write("; ");
                    write(typeRef, this.context.getPrefixes());
                    this.bw.write(" ");
                }
            } else {
                this.writeExtraDot = true;
                this.bw.write(".\n");
                write(source, this.context.getPrefixes());
                this.bw.write(" ");
                write(typeRef, this.context.getPrefixes());
                this.bw.write(" ");
            }
            write(target, this.context.getPrefixes());

            this.precSource = source;
            this.precPredicate = typeRef;
        } catch (IOException e) {
            org.restlet.Context.getCurrentLogger().warning(
                    "Cannot write the representation of a statement due to "
                            + e.getMessage());
        }
    }

    @Override
    public void endGraph() throws IOException {
        if (writeExtraDot) {
            this.bw.write(".\n");
        }
        this.bw.flush();
    }

    /**
     * Write the representation of the given graph of links.
     * 
     * @param linkset
     *            the given graph of links.
     * @throws IOException
     */
    private void write(Graph linkset) throws IOException {
        for (Link link : linkset) {
            if (link.hasReferenceSource()) {
                if (!link.getSourceAsReference().equals(this.precSource)) {
                    this.bw.write(".\n");
                    this.writeExtraDot = true;
                }
                if (link.hasReferenceTarget()) {
                    link(link.getSourceAsReference(), link.getTypeRef(), link
                            .getTargetAsReference());
                } else if (link.hasLiteralTarget()) {
                    link(link.getSourceAsReference(), link.getTypeRef(), link
                            .getTargetAsLiteral());
                } else if (link.hasLinkTarget()) {
                    // TODO Hande source as link.
                } else {
                    org.restlet.Context
                            .getCurrentLogger()
                            .warning(
                                    "Cannot write the representation of a statement due to the fact that the object is neither a Reference nor a literal.");
                }
            } else if (link.hasGraphSource()) {
                this.writeExtraDot = false;
                if (link.hasReferenceTarget()) {
                    link(link.getSourceAsGraph(), link.getTypeRef(), link
                            .getTargetAsReference());
                } else if (link.hasLiteralTarget()) {
                    link(link.getSourceAsGraph(), link.getTypeRef(), link
                            .getTargetAsLiteral());
                } else if (link.hasLinkTarget()) {
                    // TODO Handle source as link.
                } else {
                    org.restlet.Context
                            .getCurrentLogger()
                            .warning(
                                    "Cannot write the representation of a statement due to the fact that the object is neither a Reference nor a literal.");
                }
                this.bw.write(".\n");
            }
            this.precSource = link.getSourceAsReference();
            this.precPredicate = link.getTypeRef();
        }
    }

    /**
     * Writes the representation of a literal.
     * 
     * @param literal
     *            The literal to write.
     * @throws IOException
     */
    private void write(Literal literal) throws IOException {
        // Write it as a string
        this.bw.write("\"");
        if (literal.getValue().contains("\n")) {
            this.bw.write("\"");
            this.bw.write("\"");
            this.bw.write(literal.getValue());
            this.bw.write("\"");
            this.bw.write("\"");
        } else {
            this.bw.write(literal.getValue());
        }

        this.bw.write("\"");
        if (literal.getDatatypeRef() != null) {
            this.bw.write("^^");
            write(literal.getDatatypeRef(), context.getPrefixes());
        }
        if (literal.getLanguage() != null) {
            this.bw.write("@");
            this.bw.write(literal.getLanguage().toString());
        }
    }

    /**
     * Writes the representation of a given reference.
     * 
     * @param reference
     *            The reference to write.
     * @param prefixes
     *            The map of known namespaces.
     * @throws IOException
     */
    private void write(Reference reference, Map<String, String> prefixes)
            throws IOException {
        String uri = reference.toString();
        if (LinkReference.isBlank(reference)) {
            this.bw.write(uri);
        } else {
            boolean found = false;
            for (Entry<String, String> entry : prefixes.entrySet()) {
                if (uri.startsWith(entry.getKey())) {
                    found = true;
                    this.bw.append(entry.getValue());
                    this.bw.append(":");
                    this.bw.append(uri.substring(entry.getKey().length()));
                    break;
                }
            }
            if (!found) {
                this.bw.append("<");
                this.bw.append(uri);
                this.bw.append(">");
            }
        }
    }

}
