/*
 * Copyright 2005-2008 Noelios Technologies.
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

package org.restlet.gwt.resource;

import org.restlet.gwt.data.MediaType;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;

/**
 * XML representation based on an XML DOM document. DOM is a standard XML object
 * model defined by the W3C.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class XmlRepresentation extends Representation {
    /** The wrapped DOM document. */
    private volatile Document dom;

    /** The source XML representation. */
    private volatile Representation xmlRepresentation;

    /**
     * Constructor for an empty document.
     * 
     * @param mediaType
     *            The representation's media type.
     */
    public XmlRepresentation(MediaType mediaType) {
        super(mediaType);
        this.dom = XMLParser.createDocument();
    }

    /**
     * Constructor from an existing DOM document.
     * 
     * @param mediaType
     *            The representation's media type.
     * @param xmlDocument
     *            The source DOM document.
     */
    public XmlRepresentation(MediaType mediaType, Document xmlDocument) {
        super(mediaType);
        this.dom = xmlDocument;
    }

    /**
     * Constructor.
     * 
     * @param xmlRepresentation
     *            A source XML representation to parse.
     */
    public XmlRepresentation(Representation xmlRepresentation) {
        super((xmlRepresentation == null) ? null : xmlRepresentation
                .getMediaType());
        this.xmlRepresentation = xmlRepresentation;
    }

    /**
     * Returns the wrapped DOM document. If no document is defined yet, it
     * attempts to parse the XML representation eventually given at construction
     * time. Otherwise, it just creates a new document.
     * 
     * @return The wrapped DOM document.
     */
    public Document getDocument() {
        if (this.dom == null) {
            if (this.xmlRepresentation != null) {
                this.dom = XMLParser.parse(this.xmlRepresentation.getText());
            } else {
                this.dom = XMLParser.createDocument();
            }
        }

        return this.dom;
    }

    @Override
    public String getText() {
        return (getDocument() != null) ? getDocument().toString() : null;
    }

    /**
     * Releases the wrapped DOM document and the source XML representation if
     * they have been defined.
     */
    @Override
    public void release() {
        setDocument(null);

        if (this.xmlRepresentation != null) {
            this.xmlRepresentation.release();
        }

        super.release();
    }

    /**
     * Sets the wrapped DOM document.
     * 
     * @param dom
     *            The wrapped DOM document.
     */
    public void setDocument(Document dom) {
        this.dom = dom;
    }
}
