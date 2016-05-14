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

package org.restlet.ext.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import org.restlet.data.CharacterSet;
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
    private volatile Document document;

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
        this.document = getDocumentBuilder().newDocument();
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
        this.document = xmlDocument;
    }

    /**
     * Constructor.
     * 
     * @param xmlRepresentation
     *            A source XML representation to parse.
     */
    public DomRepresentation(Representation xmlRepresentation) {
        super((xmlRepresentation == null) ? null : xmlRepresentation.getMediaType());
        this.setAvailable((xmlRepresentation == null) ? false : xmlRepresentation.isAvailable());
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
            javax.xml.transform.Transformer transformer = javax.xml.transform.TransformerFactory
                    .newInstance().newTransformer();
            transformer.setOutputProperty(
                    javax.xml.transform.OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(
                    javax.xml.transform.OutputKeys.INDENT,
                    isIndenting() ? "yes" : "no");

            if (getCharacterSet() != null) {
                transformer.setOutputProperty(
                        javax.xml.transform.OutputKeys.ENCODING,
                        getCharacterSet().getName());
            } else {
                transformer.setOutputProperty(
                        javax.xml.transform.OutputKeys.ENCODING,
                        CharacterSet.ISO_8859_1.getName());
            }

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

    /**
     * Returns the wrapped DOM document. If no document is defined yet, it
     * attempts to parse the XML representation eventually given at construction
     * time. Otherwise, it just creates a new document.
     * 
     * @return The wrapped DOM document.
     */
    @Override
    public Document getDocument() throws IOException {
        if (this.document == null) {
            if (this.xmlRepresentation != null) {
                try {
                    this.document = getDocumentBuilder().parse(getInputSource());
                } catch (SAXException se) {
                    throw new IOException("Couldn't read the XML representation. " + se.getMessage());
                }
            } else {
                this.document = getDocumentBuilder().newDocument();
            }
        }

        return this.document;
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

    @Override
    public InputSource getInputSource() throws IOException {
        if (this.xmlRepresentation != null && this.xmlRepresentation.isAvailable()) {
            return new InputSource(this.xmlRepresentation.getStream());
        }
        return new InputSource((InputStream) null);
    }

    /**
     * Indicates if the XML serialization should be indented. False by default.
     * 
     * @return True if the XML serialization should be indented.
     */
    public boolean isIndenting() {
        return indenting;
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
        this.document = dom;
    }

    /**
     * Indicates if the XML serialization should be indented.
     * 
     * @param indenting
     *            True if the XML serialization should be indented.
     */
    public void setIndenting(boolean indenting) {
        this.indenting = indenting;
    }

    @Override
    public void write(Writer writer) throws IOException {
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
                        new javax.xml.transform.stream.StreamResult(writer));
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
