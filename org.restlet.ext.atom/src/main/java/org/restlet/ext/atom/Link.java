/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.atom;

import static org.restlet.ext.atom.Feed.ATOM_NAMESPACE;

import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.ext.xml.XmlWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Defines a reference from an entry or feed to a Web resource.
 * 
 * @author Jerome Louvel
 */
public class Link {

    /** Contains or links to the content of the entry. */
    private volatile Content content;

    /** The link's IRI. */
    private volatile Reference href;

    /** Language of the resource pointed to by the href attribute. */
    private volatile Language hrefLang;

    /** Advisory length of the linked content in octets. */
    private volatile long length;

    /** The link's relation type. */
    private volatile Relation rel;

    /** Human-readable information about the link. */
    private volatile String title;

    /** Advisory media type. */
    private volatile MediaType type;

    /**
     * Constructor.
     */
    public Link() {
        this.content = null;
        this.href = null;
        this.rel = null;
        this.type = null;
        this.hrefLang = null;
        this.title = null;
        this.length = -1;
    }

    /**
     * Constructor.
     * 
     * @param href
     *            The link's IRI.
     * @param rel
     *            The link's relation type.
     * @param type
     *            Advisory media type.
     */
    public Link(Reference href, Relation rel, MediaType type) {
        super();
        this.href = href;
        this.rel = rel;
        this.type = type;
    }

    /**
     * Returns the content of the entry or links to it.
     * 
     * @return The content of the entry or links to it.
     */
    public Content getContent() {
        return this.content;
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
     * Sets the content of the entry or links to it.
     * 
     * @param content
     *            The content of the entry or links to it.
     */
    public void setContent(Content content) {
        this.content = content;
    }

    /**
     * Sets the link's IRI.
     * 
     * @param href
     *            The link's IRI.
     */
    public void setHref(Reference href) {
        this.href = href;
    }

    /**
     * Sets the language of the resource pointed to by the href attribute.
     * 
     * @param hrefLang
     *            The language of the resource pointed to by the href attribute.
     */
    public void setHrefLang(Language hrefLang) {
        this.hrefLang = hrefLang;
    }

    /**
     * Sets the advisory length of the linked content in octets.
     * 
     * @param length
     *            The advisory length of the linked content in octets.
     */
    public void setLength(long length) {
        this.length = length;
    }

    /**
     * Sets the link's relation type.
     * 
     * @param rel
     *            The link's relation type.
     */
    public void setRel(Relation rel) {
        this.rel = rel;
    }

    /**
     * Sets the human-readable information about the link.
     * 
     * @param title
     *            The human-readable information about the link.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the advisoty media type.
     * 
     * @param type
     *            The advisoty media type.
     */
    public void setType(MediaType type) {
        this.type = type;
    }

    /**
     * Writes the current object as an XML element using the given SAX writer.
     * 
     * @param writer
     *            The SAX writer.
     * @throws SAXException
     */
    public void writeElement(XmlWriter writer) throws SAXException {
        AttributesImpl attributes = new AttributesImpl();

        if ((getHref() != null) && (getHref().toString() != null)) {
            attributes.addAttribute("", "href", null, "atomURI", getHref()
                    .toString());
        }

        if ((getHrefLang() != null) && (getHrefLang().toString() != null)) {
            attributes.addAttribute("", "hreflang", null, "atomLanguageTag",
                    getHrefLang().toString());
        }

        if (getLength() > 0) {
            attributes.addAttribute("", "length", null, "text",
                    Long.toString(getLength()));
        }

        attributes.addAttribute("", "rel", null, "text",
                Relation.toString(getRel()));

        if (getTitle() != null) {
            attributes.addAttribute("", "title", null, "text", getTitle());
        }

        if (getType() != null) {
            attributes.addAttribute("", "type", null, "atomMediaType",
                    getType().toString());
        }

        if (getContent() != null) {
            writer.startElement(ATOM_NAMESPACE, "link", null, attributes);
            getContent().writeElement(writer);
            writer.endElement(ATOM_NAMESPACE, "link");
        } else {
            writer.emptyElement(ATOM_NAMESPACE, "link", null, attributes);
        }

    }

}
