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

import org.restlet.data.Reference;
import org.restlet.util.XmlWriter;
import org.xml.sax.SAXException;

/**
 * Conveys information about a category associated with an entry or feed.
 * 
 * @author Jerome Louvel (contact@noelios.com)
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
     *                The identifier term.
     * @param scheme
     *                The IRI that identifies a categorization scheme.
     * @param term
     *                The human-readable label for display in end-user
     *                applications.
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
     *                The label.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Sets the scheme.
     * 
     * @param scheme
     *                The scheme.
     */
    public void setScheme(Reference scheme) {
        this.scheme = scheme;
    }

    /**
     * Sets the term.
     * 
     * @param term
     *                The term.
     */
    public void setTerm(String term) {
        this.term = term;
    }

    /**
     * Writes the current object as an XML element using the given SAX writer.
     * 
     * @param writer
     *                The SAX writer.
     * @param namespace
     *                The element namespace URI.
     * @throws SAXException
     */
    public void writeElement(XmlWriter writer, String namespace)
            throws SAXException {
        writer.startElement(namespace, "category");

        if (getLabel() != null) {
            writer.dataElement(namespace, "label", getLabel());
        }

        if (getScheme() != null && getScheme().toString() != null) {
            writer.dataElement(namespace, "scheme", getScheme().toString());
        }

        if (getTerm() != null) {
            writer.dataElement(namespace, "term", getTerm());
        }

        writer.endElement(namespace, "category");
    }

}
