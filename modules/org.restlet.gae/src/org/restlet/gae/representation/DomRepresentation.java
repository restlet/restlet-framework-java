/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.gae.representation;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.restlet.gae.data.MediaType;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * XML representation based on a DOM document. DOM is a standard XML object
 * model defined by the W3C.
 * 
 * @author Jerome Louvel
 */
public class DomRepresentation extends XmlRepresentation {
    /** The wrapped DOM document. */
    private volatile Document dom;

    /** Indicates if the XML serialization should be indented. */
    private volatile boolean indent;

    /** The source XML representation. */
    private volatile Representation xmlRepresentation;

    /**
     * Constructor for an empty document.
     * 
     * @param mediaType
     *            The representation's media type.
     */
    public DomRepresentation(MediaType mediaType) throws IOException {
        super(mediaType);
        this.dom = getDocumentBuilder().newDocument();
    }

    /**
     * Constructor from an existing DOM document.
     * 
     * @param mediaType
     *            The representation's media type.
     * @param xmlDocument
     *            The source DOM document.
     */
    public DomRepresentation(MediaType mediaType, Document xmlDocument) {
        super(mediaType);
        this.dom = xmlDocument;
    }

    /**
     * Constructor.
     * 
     * @param xmlRepresentation
     *            A source XML representation to parse.
     */
    public DomRepresentation(Representation xmlRepresentation) {
        super((xmlRepresentation == null) ? null : xmlRepresentation
                .getMediaType());
        this.xmlRepresentation = xmlRepresentation;
    }

    /**
     * Creates a new JAXP Transformer object that will be used to serialize this
     * DOM. This method may be overridden in order to set custom properties on
     * the Transformer.
     * 
     * @return The transformer to be used for serialization.
     */
    protected Transformer createTransformer() throws IOException {
        try {
            final Transformer transformer = TransformerFactory.newInstance()
                    .newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");

            if (getDocument().getDoctype() != null) {
                transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
                        getDocument().getDoctype().getSystemId());
                transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,
                        getDocument().getDoctype().getPublicId());
                transformer.setOutputProperty(OutputKeys.INDENT, Boolean
                        .toString(isIndent()));
            }

            return transformer;
        } catch (TransformerConfigurationException tce) {
            throw new IOException("Couldn't write the XML representation: "
                    + tce.getMessage());
        }
    }

    @Override
    public Object evaluate(String expression, QName returnType)
            throws Exception {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(this);
        return xpath.evaluate(expression, getDocument(), returnType);
    }

    /**
     * Returns the wrapped DOM document. If no document is defined yet, it
     * attempts to parse the XML representation eventually given at construction
     * time. Otherwise, it just creates a new document.
     * 
     * @return The wrapped DOM document.
     */
    public Document getDocument() throws IOException {
        if (this.dom == null) {
            if (this.xmlRepresentation != null) {
                try {
                    this.dom = getDocumentBuilder().parse(
                            this.xmlRepresentation.getStream());
                } catch (SAXException se) {
                    throw new IOException(
                            "Couldn't read the XML representation. "
                                    + se.getMessage());
                }
            } else {
                this.dom = getDocumentBuilder().newDocument();
            }
        }

        return this.dom;
    }

    /**
     * Returns a DOM source.
     * 
     * @return A DOM source.
     */
    @Override
    public DOMSource getDomSource() throws IOException {
        return new DOMSource(getDocument());
    }

    /**
     * Indicates if the XML serialization should be indented. False by default.
     * 
     * @return True if the XML serialization should be indented.
     */
    public boolean isIndent() {
        return indent;
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

    /**
     * Indicates if the XML serialization should be indented.
     * 
     * @param indent
     *            True if the XML serialization should be indented.
     */
    public void setIndent(boolean indent) {
        this.indent = indent;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        try {
            if (getDocument() != null) {
                final Transformer transformer = createTransformer();
                transformer.transform(new DOMSource(getDocument()),
                        new StreamResult(outputStream));
            }
        } catch (TransformerConfigurationException tce) {
            throw new IOException("Couldn't write the XML representation: "
                    + tce.getMessage());
        } catch (TransformerException te) {
            throw new IOException("Couldn't write the XML representation: "
                    + te.getMessage());
        } catch (TransformerFactoryConfigurationError tfce) {
            throw new IOException("Couldn't write the XML representation: "
                    + tfce.getMessage());
        }
    }
}
