/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.resource;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.restlet.data.MediaType;
import org.restlet.util.XmlWriter;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;

/**
 * XML representation for SAX events processing. The purpose is to create a
 * streamable content based on a custom Java object model instead of a neutral
 * DOM tree. This domain object can then be directly modified and efficiently
 * serialized at a later time.<br/> Subclasses only need to override the
 * ContentHandler methods required for the reading and also the write(XmlWriter
 * writer) method when serialization is requested.<br/>
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class SaxRepresentation extends XmlRepresentation {
    /** The wrapped DOM document to parse. */
    private Document xmlDocument;

    /** The wrapped XML representation. */
    private Representation xmlRepresentation;

    /**
     * Constructor.
     * 
     * @param mediaType
     *            The representation media type.
     */
    public SaxRepresentation(MediaType mediaType) {
        super(mediaType);
    }

    /**
     * Constructor.
     * 
     * @param mediaType
     *            The representation's media type.
     * @param xmlDocument
     *            A source DOM representation to parse.
     */
    public SaxRepresentation(MediaType mediaType, Document xmlDocument) {
        super(mediaType);
        this.xmlDocument = xmlDocument;
    }

    /**
     * Constructor.
     * 
     * @param xmlRepresentation
     *            A source XML representation to parse.
     */
    public SaxRepresentation(Representation xmlRepresentation)
            throws IOException {
        super(xmlRepresentation.getMediaType());
        this.xmlRepresentation = xmlRepresentation;
    }

    /**
     * Parses the source and sends SAX events to a content handler.
     * 
     * @param contentHandler
     *            The SAX content handler to use for parsing.
     */
    public void parse(ContentHandler contentHandler) throws IOException {
        if (contentHandler != null) {
            try {
                Source source = null;

                if (this.xmlDocument != null) {
                    source = new DOMSource(xmlDocument);
                } else {
                    source = new StreamSource(xmlRepresentation.getStream());
                }

                if (xmlRepresentation.getIdentifier() != null) {
                    source.setSystemId(xmlRepresentation.getIdentifier()
                            .getTargetRef().toString());
                }

                Result result = new SAXResult(contentHandler);
                TransformerFactory.newInstance().newTransformer().transform(
                        source, result);
            } catch (TransformerConfigurationException tce) {
                throw new IOException(
                        "Couldn't parse the source representation: "
                                + tce.getMessage());
            } catch (TransformerException te) {
                te.printStackTrace();
                throw new IOException(
                        "Couldn't parse the source representation: "
                                + te.getMessage());
            } catch (TransformerFactoryConfigurationError tfce) {
                throw new IOException(
                        "Couldn't parse the source representation: "
                                + tfce.getMessage());
            }
        } else {
            throw new IOException(
                    "Couldn't parse the source representation: no content restlet defined.");
        }
    }

    /**
     * Writes the representation to a byte stream.
     * 
     * @param outputStream
     *            The output stream.
     */
	@Override
    public void write(OutputStream outputStream) throws IOException {
        write(new XmlWriter(outputStream, "UTF-8"));
    }

    /**
     * Writes the representation to a XML writer. The default implementation
     * does nothing and is intended to be overriden.
     * 
     * @param writer
     *            The XML writer to write to.
     * @throws IOException
     */
    public void write(XmlWriter writer) throws IOException {
        // Do nothing by default.
    }

    /**
     * Returns a document builder properly configured.
     * 
     * @return A document builder properly configured.
     */
    private DocumentBuilder getDocumentBuilder() throws IOException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(isNamespaceAware());
            dbf.setValidating(false);
            return dbf.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
            throw new IOException("Couldn't create the empty document: "
                    + pce.getMessage());
        }
    }

    @Override
    public Object evaluate(String expression, QName returnType)
            throws Exception {
        Object result = null;
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(this);

        if (this.xmlDocument == null) {
            this.xmlDocument = getDocumentBuilder().parse(
                    this.xmlRepresentation.getStream());
        }

        if (this.xmlDocument != null) {
            result = xpath.evaluate(expression, this.xmlDocument, returnType);
        } else {
            throw new Exception(
                    "Unable to obtain a DOM document for the SAX representation. "
                            + "XPath evaluation cancelled.");
        }

        return result;
    }

}
