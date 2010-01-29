/**
 * Copyright 2005-2010 Noelios Technologies.
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

package org.restlet.ext.xml;

import java.io.IOException;
import java.io.OutputStream;

import org.restlet.data.MediaType;
import org.restlet.engine.Edition;
import org.restlet.representation.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.xml.sax.InputSource;
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
    private volatile boolean indenting;

    /** The source XML representation. */
    private volatile Representation xmlRepresentation;

    /**
     * Default constructor. Uses the {@link MediaType#TEXT_XML} media type.
     */
    public DomRepresentation() throws IOException {
        this(MediaType.TEXT_XML);
    }

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

    // [ifndef android] method
    /**
     * Creates a new JAXP Transformer object that will be used to serialize this
     * DOM. This method may be overridden in order to set custom properties on
     * the Transformer.
     * 
     * @return The transformer to be used for serialization.
     */
    protected javax.xml.transform.Transformer createTransformer()
            throws IOException {
        try {
            final javax.xml.transform.Transformer transformer = javax.xml.transform.TransformerFactory
                    .newInstance().newTransformer();
            transformer.setOutputProperty(
                    javax.xml.transform.OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(
                    javax.xml.transform.OutputKeys.INDENT,
                    isIndenting() ? "yes" : "no");

            DocumentType docType = getDocument().getDoctype();
            if (docType != null) {
                if (docType.getSystemId() != null) {
                    transformer.setOutputProperty(
                            javax.xml.transform.OutputKeys.DOCTYPE_SYSTEM,
                            getDocument().getDoctype().getSystemId());
                }

                if (docType.getPublicId() != null) {
                    transformer.setOutputProperty(
                            javax.xml.transform.OutputKeys.DOCTYPE_PUBLIC,
                            getDocument().getDoctype().getPublicId());
                }
            }

            return transformer;
        } catch (javax.xml.transform.TransformerConfigurationException tce) {
            throw new IOException("Couldn't write the XML representation: "
                    + tce.getMessage());
        }
    }

    // [ifndef android] method
    @Override
    public Object evaluate(String expression,
            javax.xml.namespace.QName returnType) throws Exception {
        final javax.xml.xpath.XPath xpath = javax.xml.xpath.XPathFactory
                .newInstance().newXPath();
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
                    this.dom = getDocumentBuilder()
                            .parse(
                                    new InputSource(this.xmlRepresentation
                                            .getReader()));
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

    // [ifndef android] method
    /**
     * Returns a DOM source.
     * 
     * @return A DOM source.
     */
    @Override
    public javax.xml.transform.dom.DOMSource getDomSource() throws IOException {
        return new javax.xml.transform.dom.DOMSource(getDocument());
    }

    /**
     * Indicates if the XML serialization should be indented. False by default.
     * 
     * @return True if the XML serialization should be indented.
     * @deprecated Use {@link #isIndenting()} instead.
     */
    @Deprecated
    public boolean isIndent() {
        return indenting;
    }

    /**
     * Indicates if the XML serialization should be indented. False by default.
     * 
     * @return True if the XML serialization should be indented.
     */
    public boolean isIndenting() {
        return isIndent();
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
     * @param indenting
     *            True if the XML serialization should be indented.
     * @deprecated Use {@link #setIndenting(boolean)} instead.
     */
    @Deprecated
    public void setIndent(boolean indenting) {
        this.indenting = indenting;
    }

    /**
     * Indicates if the XML serialization should be indented.
     * 
     * @param indenting
     *            True if the XML serialization should be indented.
     */
    public void setIndenting(boolean indenting) {
        setIndent(indenting);
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        if (Edition.CURRENT == Edition.ANDROID) {
            throw new UnsupportedOperationException(
                    "Instances of DomRepresentation cannot be written at this time.");
        }
        // [ifndef android]
        try {
            if (getDocument() != null) {
                final javax.xml.transform.Transformer transformer = createTransformer();
                transformer.transform(new javax.xml.transform.dom.DOMSource(
                        getDocument()),
                        new javax.xml.transform.stream.StreamResult(
                                outputStream));
            }
        } catch (javax.xml.transform.TransformerConfigurationException tce) {
            throw new IOException("Couldn't write the XML representation: "
                    + tce.getMessage());
        } catch (javax.xml.transform.TransformerException te) {
            throw new IOException("Couldn't write the XML representation: "
                    + te.getMessage());
        } catch (javax.xml.transform.TransformerFactoryConfigurationError tfce) {
            throw new IOException("Couldn't write the XML representation: "
                    + tfce.getMessage());
        }
        // [enddef]
    }
}
