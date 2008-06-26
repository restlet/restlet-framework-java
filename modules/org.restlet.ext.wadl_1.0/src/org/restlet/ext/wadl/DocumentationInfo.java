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

package org.restlet.ext.wadl;

import static org.restlet.ext.wadl.WadlRepresentation.APP_NAMESPACE;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.data.Language;
import org.restlet.util.XmlWriter;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Document WADL description elements.
 * 
 * @author Jerome Louvel
 */
public class DocumentationInfo {
    /** Obtain a suitable logger. */
    private static Logger logger = Logger.getLogger(DocumentationInfo.class
            .getCanonicalName());

    /** The language of that documentation element. */
    private Language language;

    /** The content of that element as text. */
    private String textContent;

    /** The title of that documentation element. */
    private String title;

    /** The content of that element as XML element. */
    private Element xmlContent;

    /**
     * Returns the language of that documentation element.
     * 
     * @return The language of this documentation element.
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * Returns the language of that documentation element.
     * 
     * @return The content of that element as text.
     */
    public String getTextContent() {
        return textContent;
    }

    /**
     * Returns the title of that documentation element.
     * 
     * @return The title of that documentation element.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the content of that element as XML element.
     * 
     * @return The content of that element as XML element.
     */
    public Element getXmlContent() {
        return xmlContent;
    }

    /**
     * The language of that documentation element.
     * 
     * @param language
     *                The language of that documentation element.
     */
    public void setLanguage(Language language) {
        this.language = language;
    }

    /**
     * Sets the content of that element as text.
     * 
     * @param textContent
     *                The content of that element as text.
     */
    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    /**
     * Sets the title of that documentation element.
     * 
     * @param title
     *                The title of that documentation element.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the content of that element as XML element.
     * 
     * @param xmlContent
     *                The content of that element as XML element.
     */
    public void setXmlContent(Element xmlContent) {
        this.xmlContent = xmlContent;
    }

    /**
     * Writes the current object as an XML element using the given SAX writer.
     * 
     * @param writer
     *                The SAX writer.
     * @throws SAXException
     */
    public void writeElement(XmlWriter writer) throws SAXException {
        AttributesImpl attributes = new AttributesImpl();
        if (getTitle() != null && !getTitle().equals("")) {
            attributes.addAttribute("", "title", null, "xs:string", getTitle());
        }
        if (getLanguage() != null && getLanguage().toString() != null) {
            attributes.addAttribute("", "xml:lang", null, "xs:string",
                    getLanguage().toString());
        }

        if ((getTextContent() == null || getTextContent().equals(""))
                && getXmlContent() == null) {
            writer.emptyElement(APP_NAMESPACE, "doc", null, attributes);
        } else {

            if (getXmlContent() != null) {
                // TODO what do we do?
            } else {
                writer.startElement(APP_NAMESPACE, "doc", null, attributes);
                try {
                    writer.getWriter().write(getTextContent());
                } catch (IOException e) {
                    logger
                            .log(
                                    Level.SEVERE,
                                    "Error when writing the text content of the current \"doc\" tag.",
                                    e);
                }
                writer.endElement(APP_NAMESPACE, "doc");
            }
        }
    }
}
