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

package org.restlet.ext.rdf.internal.xml;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.ext.rdf.Graph;
import org.restlet.ext.rdf.GraphHandler;
import org.restlet.ext.rdf.Link;
import org.restlet.ext.rdf.Literal;
import org.restlet.ext.rdf.internal.RdfConstants;
import org.restlet.ext.xml.XmlWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Handler of RDF content according to the RDF XML syntax.
 * 
 * @author Thierry Boileau
 */
public class RdfXmlWriter extends GraphHandler {

    /** URI of the RDF SYNTAX namespace. */
    private final String RDF_SYNTAX = RdfConstants.RDF_SYNTAX.toString(true,
            true);

    /** XML writer. */
    private XmlWriter writer;

    /**
     * Constructor.
     * 
     * @param writer
     *            The character writer.
     * @throws UnsupportedEncodingException
     */
    public RdfXmlWriter(Writer writer) throws UnsupportedEncodingException {
        super();
        this.writer = new XmlWriter(writer);
    }

    @Override
    public void endGraph() throws IOException {
        try {
            this.writer.endElement(RDF_SYNTAX, "RDF");
            this.writer.endDocument();
        } catch (SAXException e) {
            Context.getCurrentLogger().warning(
                    "Cannot write the end of the graph: " + e.getMessage());
        }
        this.writer.flush();
    }

    /**
     * Returns the namespace of the given reference.
     * 
     * @param reference
     *            the given reference.
     * @return The namespace of the given reference.
     */
    private String getNamespace(Reference reference) {
        String prefix = getPrefix(reference);
        String ref = reference.toString(true, true);
        if (prefix != null) {
            return ref.substring(0, ref.length() - prefix.length());
        }

        return ref;
    }

    /**
     * Returns the prefix of the qualified name representing the given
     * reference.
     * 
     * @param ref
     *            the given reference.
     * @return The prefix of the qualified name representing the given
     *         reference. Context .getCurrentLogger() .warning(
     *         "Cannot write the end of the graph" + e.getMessage());
     */
    private String getPrefix(Reference ref) {
        String result = null;
        if (ref.hasFragment()) {
            result = ref.getFragment();
        } else {
            result = ref.getLastSegment();
        }
        return result;
    }

    @Override
    public void link(Graph source, Reference typeRef, Literal target) {
        Context
                .getCurrentLogger()
                .warning(
                        "Cannot write the representation of a statement due to the fact that the subject is not a Reference.");
    }

    @Override
    public void link(Graph source, Reference typeRef, Reference target) {
        Context
                .getCurrentLogger()
                .warning(
                        "Cannot write the representation of a statement due to the fact that the subject is not a Reference.");
    }

    @Override
    public void link(Reference source, Reference typeRef, Literal target) {
        try {
            writeNode(source, true);

            String typeRefNs = getNamespace(typeRef.getTargetRef());
            String typeRefPrefix = getPrefix(typeRef.getTargetRef());
            if (target.getLanguage() != null || target.getDatatypeRef() != null) {
                AttributesImpl attr = new AttributesImpl();
                if (target.getLanguage() != null) {
                    attr.addAttribute(null, "lang", "xml:lang", "text", target
                            .getLanguage().getName());
                }
                if (target.getDatatypeRef() != null) {
                    attr.addAttribute(RDF_SYNTAX, "datatype", "rdf:datatype",
                            "text", target.getDatatypeRef()
                                    .toString(true, true));
                }
                this.writer.startElement(typeRefNs, typeRefPrefix, null, attr);
            } else {
                this.writer.startElement(typeRefNs, typeRefPrefix);
            }
            this.writer.characters(target.getValue());
            this.writer.endElement(typeRefNs, typeRefPrefix);
            this.writer.endElement(RDF_SYNTAX, "Description");
        } catch (SAXException e) {
            org.restlet.Context.getCurrentLogger().warning(
                    "Cannot write the representation of a statement due to: "
                            + e.getMessage());
        }
    }

    @Override
    public void link(Reference source, Reference typeRef, Reference target) {
        try {
            writeNode(source, true);
            String typeRefNs = getNamespace(typeRef.getTargetRef());
            String typeRefPrefix = getPrefix(typeRef.getTargetRef());
            this.writer.startElement(typeRefNs, typeRefPrefix);
            writeNode(target, false);
            this.writer.endElement(typeRefNs, typeRefPrefix);
            this.writer.endElement(RDF_SYNTAX, "Description");
        } catch (SAXException e) {
            Context.getCurrentLogger().warning(
                    "Cannot write the representation of a statement due to: "
                            + e.getMessage());
        }
    }

    @Override
    public void startGraph() throws IOException {
        this.writer.setPrefix(RDF_SYNTAX, "rdf");
        this.writer.setPrefix(RdfConstants.XML_SCHEMA.toString(true, true),
                "type");
        writer.setDataFormat(true);
        writer.setIndentStep(3);
        try {
            this.writer.startDocument();
            this.writer.startElement(RDF_SYNTAX, "RDF");
        } catch (SAXException e) {
            Context.getCurrentLogger().warning(
                    "Cannot write the start of the graph: " + e.getMessage());
        }
    }

    @Override
    public void startPrefixMapping(String prefix, Reference reference) {
        if (prefix == null) {
            writer.forceNSDecl(getNamespace(reference.getTargetRef()));
        } else {
            writer.forceNSDecl(getNamespace(reference.getTargetRef()), prefix);
        }
    }

    /**
     * Writes a subject or object node.
     * 
     * @param ref
     *            The reference of the subject or object node.
     * @param subject
     *            True if the node is the subject of a predicate
     */
    private void writeNode(Reference reference, boolean subject) {
        AttributesImpl atts = new AttributesImpl();
        if (Link.isBlankRef(reference)) {
            atts.addAttribute(RDF_SYNTAX, "NodeId", "rdf:NodeId", "text",
                    reference.getTargetRef().toString(true, true));
        } else {
            atts.addAttribute(RDF_SYNTAX, "about", "rdf:about", "text",
                    reference.getTargetRef().toString(true, true));
        }
        try {
            if (!subject) {
                this.writer.emptyElement(RDF_SYNTAX, "Description",
                        "rdf:Description", atts);
            } else {
                this.writer.startElement(RDF_SYNTAX, "Description",
                        "rdf:Description", atts);
            }
        } catch (SAXException e) {
            org.restlet.Context.getCurrentLogger().warning(
                    "Cannot write the representation of a statement due to: "
                            + e.getMessage());
        }
    }
}
