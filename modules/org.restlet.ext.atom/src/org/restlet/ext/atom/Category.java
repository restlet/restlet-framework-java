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
 */
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
