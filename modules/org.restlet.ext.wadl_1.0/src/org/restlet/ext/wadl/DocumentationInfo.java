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

    private Language language;

    private String textContent;

    private String title;

    private Element xmlContent;

    public Language getLanguage() {
        return language;
    }

    public String getTextContent() {
        return textContent;
    }

    public String getTitle() {
        return title;
    }

    public Element getXmlContent() {
        return xmlContent;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public void setTitle(String title) {
        this.title = title;
    }

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
            writer.emptyElement("", "doc", null, attributes);
        } else {

            if (getXmlContent() != null) {
                // TODO what do we do?
            } else {
                writer.dataElement("", "doc", null, attributes,
                        getTextContent());
            }
        }
    }

}
