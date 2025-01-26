/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.atom;

import static org.restlet.ext.atom.Feed.ATOM_NAMESPACE;

import org.restlet.data.Reference;
import org.restlet.ext.xml.XmlWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Conveys information about a category associated with an entry or feed.
 * 
 * @author Jerome Louvel
 * @deprecated Will be removed in next major release.
 */
@Deprecated
public class Category {
    /** The human-readable label for display in end-user applications. */
    private volatile String label;

    /** The IRI that identifies a categorization scheme. */
    private volatile Reference scheme;

    /** The identifier term. */
    private volatile String term;

    /**
     * Constructor.
     */
    public Category() {
        this(null, null, null);
    }

    /**
     * Constructor.
     * 
     * @param label
     *            The identifier term.
     * @param scheme
     *            The IRI that identifies a categorization scheme.
     * @param term
     *            The human-readable label for display in end-user applications.
     */
    public Category(String label, Reference scheme, String term) {
        this.label = label;
        this.scheme = scheme;
        this.term = term;
    }

    /**
     * Returns the label.
     * 
     * @return The label.
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Returns the scheme.
     * 
     * @return The scheme.
     */
    public Reference getScheme() {
        return this.scheme;
    }

    /**
     * Returns the term.
     * 
     * @return The term.
     */
    public String getTerm() {
        return this.term;
    }

    /**
     * Sets the label.
     * 
     * @param label
     *            The label.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Sets the scheme.
     * 
     * @param scheme
     *            The scheme.
     */
    public void setScheme(Reference scheme) {
        this.scheme = scheme;
    }

    /**
     * Sets the term.
     * 
     * @param term
     *            The term.
     */
    public void setTerm(String term) {
        this.term = term;
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
        if (getLabel() != null) {
            attributes.addAttribute("", "label", null, "text", getLabel());
        }

        if ((getScheme() != null) && (getScheme().toString() != null)) {
            attributes.addAttribute("", "scheme", null, "atomURI", getScheme()
                    .toString());
        }

        if (getTerm() != null) {
            attributes.addAttribute("", "term", null, "text", getTerm());
        }

        writer.emptyElement(ATOM_NAMESPACE, "category", null, attributes);
    }

}
