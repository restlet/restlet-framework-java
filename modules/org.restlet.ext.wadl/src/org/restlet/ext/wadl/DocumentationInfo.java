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

package org.restlet.ext.wadl;

import static org.restlet.ext.wadl.WadlRepresentation.APP_NAMESPACE;

import java.io.IOException;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.ext.xml.XmlWriter;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Document WADL description elements.
 * 
 * @author Jerome Louvel
 */
public class DocumentationInfo {

    /** The language of that documentation element. */
    private Language language;

    /** The mixed content of that element. */
    private Node mixedContent;

    /** The title of that documentation element. */
    private String title;

    /**
     * Constructor.
     */
    public DocumentationInfo() {
        super();
    }

    /**
     * Constructor with mixed content.
     * 
     * @param mixedContent
     *            The mixed content.
     */
    public DocumentationInfo(Node mixedContent) {
        super();
        this.mixedContent = mixedContent;
    }

    /**
     * Constructor with text content.
     * 
     * @param textContent
     *            The text content.
     */
    public DocumentationInfo(String textContent) {
        super();
        setTextContent(textContent);
    }

    /**
     * Returns the language of that documentation element.
     * 
     * @return The language of this documentation element.
     */
    public Language getLanguage() {
        return this.language;
    }

    /**
     * Returns the mixed content of that element.
     * 
     * @return The mixed content of that element.
     */
    public Node getMixedContent() {
        return this.mixedContent;
    }

    /**
     * Returns the language of that documentation element.
     * 
     * @return The content of that element as text.
     */
    public String getTextContent() {
        return this.mixedContent.getTextContent();
    }

    /**
     * Returns the title of that documentation element.
     * 
     * @return The title of that documentation element.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * The language of that documentation element.
     * 
     * @param language
     *            The language of that documentation element.
     */
    public void setLanguage(Language language) {
        this.language = language;
    }

    /**
     * Sets the mixed content of that element.
     * 
     * @param mixedContent
     *            The mixed content of that element.
     */
    public void setMixedContent(Node mixedContent) {
        this.mixedContent = mixedContent;
    }

    /**
     * Sets the content of that element as text.
     * 
     * @param textContent
     *            The content of that element as text.
     */
    public void setTextContent(String textContent) {
        try {
            Document doc = new DomRepresentation(MediaType.TEXT_XML)
                    .getDocument();
            this.mixedContent = doc.createTextNode(textContent);
        } catch (IOException e) {
        }
    }

    /**
     * Sets the title of that documentation element.
     * 
     * @param title
     *            The title of that documentation element.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Writes the current object as an XML element using the given SAX writer.
     * 
     * @param writer
     *            The SAX writer.
     * @throws SAXException
     */
    public void writeElement(XmlWriter writer) throws SAXException {
        final AttributesImpl attributes = new AttributesImpl();
        if ((getTitle() != null) && !getTitle().equals("")) {
            attributes.addAttribute("", "title", null, "xs:string", getTitle());
        }
        if ((getLanguage() != null) && (getLanguage().toString() != null)) {
            attributes.addAttribute("", "xml:lang", null, "xs:string",
                    getLanguage().toString());
        }

        if (getMixedContent() == null) {
            writer.emptyElement(APP_NAMESPACE, "doc", null, attributes);
        } else {
            writer.startElement(APP_NAMESPACE, "doc", null, attributes);
            try {
                // Used to restore the SAX writer's dataFormat
                boolean isDataFormat = writer.isDataFormat();
                writer.setDataFormat(false);

                writeElement(writer, getMixedContent());

                // Restore the SAX writer's dataFormat
                writer.setDataFormat(isDataFormat);
            } catch (IOException e) {
                Context
                        .getCurrentLogger()
                        .log(
                                Level.SEVERE,
                                "Error when writing the text content of the current \"doc\" tag.",
                                e);
            }
            writer.endElement(APP_NAMESPACE, "doc");
        }
    }

    /**
     * Writes the given node using the given SAX writer. It detects the type of
     * node (CDATASection, Entity, Comment, Text, DocumentFragment, Node).
     * 
     * @param writer
     *            The SAX writer
     * @param node
     *            the given Node to write.
     * @throws IOException
     * @throws SAXException
     */
    private void writeElement(XmlWriter writer, Node node) throws IOException,
            SAXException {
        if (node instanceof CDATASection) {
            CDATASection section = (CDATASection) node;
            writer.getWriter().write("<![CDATA[");
            writer.getWriter().write(section.getData());
            writer.getWriter().write("]]>");
        } else if (node instanceof Text) {
            Text text = (Text) node;
            writer.getWriter().write(text.getNodeValue());
        } else if (node instanceof EntityReference) {
            EntityReference entity = (EntityReference) node;
            writer.getWriter().write("&");
            writer.getWriter().write(entity.getNodeName());
            writer.getWriter().write(";");
        } else if (node instanceof Comment) {
            Comment comment = (Comment) node;
            writer.getWriter().write("<!-- ");
            writer.getWriter().write(comment.getData());
            writer.getWriter().write(" -->");
        } else if (node instanceof DocumentFragment) {
            DocumentFragment documentFragment = (DocumentFragment) node;
            // Walk along the tree of nodes.
            for (int i = 0; i < documentFragment.getChildNodes().getLength(); i++) {
                writeElement(writer, documentFragment.getChildNodes().item(i));
            }
        } else {
            // Check that the node contains attributes, and convert it into the
            // SAX model.
            AttributesImpl attributes = null;
            if (node.hasAttributes()) {
                attributes = new AttributesImpl();
                for (int i = 0; i < node.getAttributes().getLength(); i++) {
                    Node attribute = node.getAttributes().item(i);
                    // NB : the type of the attribute is set to null.
                    attributes.addAttribute(attribute.getNamespaceURI(),
                            attribute.getLocalName(), "", null, attribute
                                    .getNodeValue());
                }
            }

            if (node.getChildNodes() != null
                    && node.getChildNodes().getLength() > 0) {
                // This node contains children nodes.
                if (attributes == null) {
                    writer.startElement(node.getNamespaceURI(), node
                            .getLocalName());
                } else {
                    writer.startElement(node.getNamespaceURI(), node
                            .getLocalName(), node.getPrefix(), attributes);
                }
                // Add the children nodes.
                for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                    writeElement(writer, node.getChildNodes().item(i));
                }
                writer.endElement(node.getNamespaceURI(), node.getLocalName());
            } else {
                // This node is empty.
                if (attributes == null) {
                    writer.emptyElement(node.getNamespaceURI(), node
                            .getLocalName());
                } else {
                    writer.emptyElement(node.getNamespaceURI(), node
                            .getLocalName(), node.getPrefix(), attributes);
                }
            }
        }
    }
}
