/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.ext.atom;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.restlet.data.MediaType;
import org.restlet.util.XmlWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * A Text construct contains human-readable text, usually in small quantities.
 * The content of Text constructs is Language-Sensitive.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Text {

    /**
     * Writes the current object as an XML element using the given SAX writer.
     * 
     * @param writer
     *                The SAX writer.
     * @param namespace
     *                The element namespace URI.
     * @param localName
     *                The local name of the element.
     * @throws SAXException
     */
    public static void writeElement(XmlWriter writer, Date date,
            String namespace, String localName) throws SAXException {
        writer.startElement(namespace, localName);

        if (date != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd'T'hh:mm:ssZ");
            writer.characters(dateFormat.format(date));
        }

        writer.endElement(namespace, localName);
    }

    /**
     * Writes the current object as an XML element using the given SAX writer.
     * 
     * @param writer
     *                The SAX writer.
     * @param namespace
     *                The element namespace URI.
     * @param localName
     *                The local name of the element.
     * @throws SAXException
     */
    public void writeElement(XmlWriter writer, String namespace,
            String localName) throws SAXException {
        AttributesImpl attributes = new AttributesImpl();
        String type = null;

        if (getType() != null && getType().getSubType() != null) {
            if (getType().getSubType().contains("xhtml")) {
                type = "xhtml";
            } else if (getType().getSubType().contains("html")) {
                type = "html";
            }
        }

        if (type == null) {
            type = "text";
        }

        attributes.addAttribute("", "type", null, "text", type);

        if (getContent() != null) {
            writer.dataElement(namespace, localName, null, attributes,
                    getContent());
        } else {
            writer.emptyElement(namespace, localName, null, attributes);
        }
    }

    /**
     * The content.
     */
    private volatile String content;

    /**
     * The content type.
     */
    private volatile MediaType type;

    /**
     * Constructor.
     * 
     * @param type
     *                The content type.
     */
    public Text(MediaType type) {
        this(type, null);
    }

    /**
     * Constructor.
     * 
     * @param type
     *                The content type.
     * @param content
     *                The content.
     */
    public Text(MediaType type, String content) {
        this.type = type;
        this.content = content;
    }

    /**
     * Returns the content.
     * 
     * @return The content.
     */
    public String getContent() {
        return this.content;
    }

    /**
     * Returns the content type.
     * 
     * @return The content type.
     */
    public MediaType getType() {
        return this.type;
    }

    /**
     * Sets the content.
     * 
     * @param content
     *                The content.
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Sets the content type.
     * 
     * @param type
     *                The content type.
     */
    public void setType(MediaType type) {
        this.type = type;
    }

}
