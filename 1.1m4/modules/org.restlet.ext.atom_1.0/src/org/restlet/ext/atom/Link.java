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

import static org.restlet.ext.atom.Feed.ATOM_NAMESPACE;

import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.util.XmlWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Defines a reference from an entry or feed to a Web resource.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Link {

    /** Contains the link's IRI. */
    private volatile Reference href;

    /** Language of the resource pointed to by the href attribute. */
    private volatile Language hrefLang;

    /** Advisory length of the linked content in octets. */
    private volatile long length;

    /** Indicates the link's relation type */
    private volatile Relation rel;

    /** Human-readable information about the link. */
    private volatile String title;

    /** Advisory media type. */
    private volatile MediaType type;

    /**
     * Constructor.
     */
    public Link() {
        this.href = null;
        this.rel = null;
        this.type = null;
        this.hrefLang = null;
        this.title = null;
        this.length = -1;
    }

    /**
     * Returns the link's IRI.
     * 
     * @return The link's IRI.
     */
    public Reference getHref() {
        return this.href;
    }

    /**
     * Returns the language of the resource pointed to by the href attribute.
     * 
     * @return The language of the resource pointed to by the href attribute.
     */
    public Language getHrefLang() {
        return this.hrefLang;
    }

    /**
     * Returns the advisory length of the linked content in octets.
     * 
     * @return The advisory length of the linked content in octets.
     */
    public long getLength() {
        return this.length;
    }

    /**
     * Returns the link's relation type.
     * 
     * @return The link's relation type.
     */
    public Relation getRel() {
        return this.rel;
    }

    /**
     * Returns the human-readable information about the link.
     * 
     * @return The human-readable information about the link.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Returns the advisoty media type.
     * 
     * @return The advisoty media type.
     */
    public MediaType getType() {
        return this.type;
    }

    /**
     * Sets the link's IRI.
     * 
     * @param href
     *                The link's IRI.
     */
    public void setHref(Reference href) {
        this.href = href;
    }

    /**
     * Sets the language of the resource pointed to by the href attribute.
     * 
     * @param hrefLang
     *                The language of the resource pointed to by the href
     *                attribute.
     */
    public void setHrefLang(Language hrefLang) {
        this.hrefLang = hrefLang;
    }

    /**
     * Sets the advisory length of the linked content in octets.
     * 
     * @param length
     *                The advisory length of the linked content in octets.
     */
    public void setLength(long length) {
        this.length = length;
    }

    /**
     * Sets the link's relation type.
     * 
     * @param rel
     *                The link's relation type.
     */
    public void setRel(Relation rel) {
        this.rel = rel;
    }

    /**
     * Sets the human-readable information about the link.
     * 
     * @param title
     *                The human-readable information about the link.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the advisoty media type.
     * 
     * @param type
     *                The advisoty media type.
     */
    public void setType(MediaType type) {
        this.type = type;
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

        if (getHref() != null && getHref().toString() != null) {
            attributes.addAttribute("", "href", null, "atomURI", getHref()
                    .toString());
        }

        if (getHrefLang() != null && getHrefLang().toString() != null) {
            attributes.addAttribute("", "hreflang", null, "atomLanguageTag",
                    getHrefLang().toString());
        }

        if (getLength() > 0) {
            attributes.addAttribute("", "length", null, "text", Long
                    .toString(getLength()));
        }

        attributes.addAttribute("", "rel", null, "text", Relation
                .toString(getRel()));

        if (getTitle() != null) {
            attributes.addAttribute("", "title", null, "text", getTitle());
        }

        if (getType() != null) {
            attributes.addAttribute("", "type", null, "atomMediaType",
                    getType().toString());
        }

        writer.emptyElement(ATOM_NAMESPACE, "link", null, attributes);
    }

}
