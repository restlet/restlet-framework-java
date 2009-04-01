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

package org.restlet.ext.rdf.internal.xml;

import java.io.IOException;

import org.restlet.data.Reference;
import org.restlet.ext.rdf.Graph;
import org.restlet.ext.rdf.GraphHandler;
import org.restlet.ext.rdf.Link;
import org.restlet.ext.rdf.LinkReference;
import org.restlet.ext.rdf.Literal;
import org.restlet.ext.rdf.internal.RdfConstants;
import org.restlet.util.XmlWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Handler of RDF content according to the RDF XML syntax.
 * 
 * @author Thierry Boileau
 */
public class RdfXmlWritingContentHandler extends GraphHandler {

	/** . */
	private final String RDF_SYNTAX = RdfConstants.RDF_SYNTAX.toString(true,
			true);

	/** XML writer. */
	private XmlWriter writer;

	/**
	 * Constructor.
	 * 
	 * @param linkSet
	 *            The set of links to write.
	 * @param writer
	 *            The XML writer.
	 * @throws IOException
	 * @throws SAXException
	 */
	public RdfXmlWritingContentHandler(Graph linkset, XmlWriter writer)
			throws IOException, SAXException {
		super();
		this.writer = writer;
		this.writer.setPrefix(RDF_SYNTAX, "rdf");
		this.writer.setPrefix(RdfConstants.XML_SCHEMA.toString(true, true),
				"type");
		// Discover the list of known namespaces
		discoverNamespaces(linkset, writer);

		writer.setDataFormat(true);
		writer.setIndentStep(3);
		this.writer.startDocument();
		this.writer.startElement(RDF_SYNTAX, "RDF");

		this.write(linkset);
		this.writer.endElement(RDF_SYNTAX, "RDF");
		this.writer.endDocument();
		this.writer.flush();
	}

	/**
	 * Updates the list of known namespaces of the XML writer for the given
	 * graph of links.
	 * 
	 * @param linkset
	 *            The given graph of links.
	 * @param xmlWriter
	 *            the XML writer.
	 */
	private void discoverNamespaces(Graph linkset, XmlWriter xmlWriter) {
		for (Link link : linkset) {
			discoverNamespaces(link, xmlWriter);
		}
	}

	/**
	 * Updates the list of known namespaces of the XML writer for the given
	 * link.
	 * 
	 * @param link
	 *            The given link.
	 * @param xmlWriter
	 *            the XML writer.
	 */
	private void discoverNamespaces(Link link, XmlWriter xmlWriter) {
		// The subject of the link is not discovered, it is generated as the
		// value of an "about" attribute.
		if (link.hasLinkSource()) {
			discoverNamespaces(link.getSourceAsLink(), xmlWriter);
		} else if (link.hasGraphSource()) {
			discoverNamespaces(link.getSourceAsGraph(), xmlWriter);
		}
		discoverNamespaces(link.getTypeRef(), xmlWriter);
		if (link.hasLinkTarget()) {
			discoverNamespaces(link.getTargetAsLink(), xmlWriter);
		} else if (link.hasGraphSource()) {
			discoverNamespaces(link.getSourceAsGraph(), xmlWriter);
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
	private void discoverNamespaces(Reference reference, XmlWriter xmlWriter) {
		if (!LinkReference.isBlank(reference)) {
			xmlWriter.forceNSDecl(getNamespace(reference));
		}
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
		} else {
			return ref;
		}

	}

	/**
	 * Returns the prefix of the qualified name representing the given
	 * reference.
	 * 
	 * @param ref
	 *            the given reference.
	 * @return The prefix of the qualified name representing the given
	 *         reference.
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
		// TODO Auto-generated method stub

	}

	@Override
	public void link(Graph source, Reference typeRef, Reference target) {
		// TODO Auto-generated method stub

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
			e.printStackTrace();
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
			e.printStackTrace();
		}
	}

	/**
	 * Write the representation of the given graph of links.
	 * 
	 * @param linkset
	 *            the given graph of links.
	 * @throws IOException
	 * @throws IOException
	 */
	private void write(Graph linkset) throws IOException {
		for (Link link : linkset) {
			if (link.hasReferenceSource()) {
				if (link.hasReferenceTarget()) {
					link(link.getSourceAsReference(), link.getTypeRef(), link
							.getTargetAsReference());
				} else if (link.hasLiteralTarget()) {
					link(link.getSourceAsReference(), link.getTypeRef(), link
							.getTargetAsLiteral());
				} else if (link.hasLiteralTarget()) {
					// TODO Hande source as link.
				} else {
					// Error?
				}
			} else if (link.hasGraphSource()) {
				if (link.hasReferenceTarget()) {
					link(link.getSourceAsGraph(), link.getTypeRef(), link
							.getTargetAsReference());
				} else if (link.hasLiteralTarget()) {
					link(link.getSourceAsGraph(), link.getTypeRef(), link
							.getTargetAsLiteral());
				} else if (link.hasLiteralTarget()) {
					// TODO Handle source as link.
				} else {
					// Error?
				}
			}
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
		if (LinkReference.isBlank(reference)) {
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
			e.printStackTrace();
		}
	}
}
