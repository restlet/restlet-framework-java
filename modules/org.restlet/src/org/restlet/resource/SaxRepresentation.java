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

package org.restlet.resource;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.restlet.data.MediaType;
import org.restlet.util.XmlWriter;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;

/**
 * XML representation for SAX events processing. The purpose is to create a
 * streamable content based on a custom Java object model instead of a neutral
 * DOM tree. This domain object can then be directly modified and efficiently
 * serialized at a later time.<br>
 * <br>
 * Subclasses only need to override the ContentHandler methods required for the
 * reading and also the write(XmlWriter writer) method when serialization is
 * requested.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class SaxRepresentation extends XmlRepresentation {

    /** The SAX source. */
    private volatile SAXSource source;

    /**
     * Constructor.
     * 
     * @param mediaType
     *                The representation media type.
     */
    public SaxRepresentation(MediaType mediaType) {
        super(mediaType);
    }

    /**
     * Constructor.
     * 
     * @param mediaType
     *                The representation's media type.
     * @param xmlDocument
     *                A DOM document to parse.
     */
    public SaxRepresentation(MediaType mediaType, Document xmlDocument) {
        super(mediaType);
        this.source = new SAXSource(SAXSource
                .sourceToInputSource(new DOMSource(xmlDocument)));
    }

    /**
     * Constructor.
     * 
     * @param mediaType
     *                The representation's media type.
     * @param xmlSource
     *                A SAX input source to parse.
     */
    public SaxRepresentation(MediaType mediaType, InputSource xmlSource) {
        super(mediaType);
        this.source = new SAXSource(xmlSource);
    }

    /**
     * Constructor.
     * 
     * @param mediaType
     *                The representation's media type.
     * @param xmlSource
     *                A JAXP source to parse.
     */
    public SaxRepresentation(MediaType mediaType, SAXSource xmlSource) {
        super(mediaType);
        this.source = xmlSource;
    }

    /**
     * Constructor.
     * 
     * @param xmlRepresentation
     *                A source XML representation to parse.
     * @throws IOException
     */
    public SaxRepresentation(Representation xmlRepresentation) {
        super(xmlRepresentation.getMediaType());

        try {
            if (xmlRepresentation instanceof XmlRepresentation) {
                this.source = ((XmlRepresentation) xmlRepresentation)
                        .getSaxSource();
            } else {
                this.source = new SAXSource(new InputSource(xmlRepresentation
                        .getStream()));
            }

            if (xmlRepresentation.getIdentifier() != null) {
                this.source.setSystemId(xmlRepresentation.getIdentifier()
                        .getTargetRef().toString());
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Override
    public Object evaluate(String expression, QName returnType)
            throws Exception {
        Object result = null;
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(this);

        if (this.source != null) {
            Document document = getDocumentBuilder().parse(
                    SAXSource.sourceToInputSource(this.source));
            result = xpath.evaluate(expression, document, returnType);
        } else {
            throw new Exception(
                    "Unable to obtain a DOM document for the SAX representation. "
                            + "XPath evaluation cancelled.");
        }

        return result;
    }

    /**
     * Returns the SAX source that can be parsed by the
     * {@link #parse(ContentHandler)} method.
     */
    @Override
    public SAXSource getSaxSource() throws IOException {
        return this.source;
    }

    /**
     * Parses the source and sends SAX events to a content handler.
     * 
     * @param contentHandler
     *                The SAX content handler to use for parsing.
     */
    public void parse(ContentHandler contentHandler) throws IOException {
        if (contentHandler != null) {
            try {
                Result result = new SAXResult(contentHandler);
                TransformerFactory.newInstance().newTransformer().transform(
                        this.source, result);
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
     * Releases the namespaces map.
     */
    @Override
    public void release() {
        if (this.source != null) {
            this.source = null;
        }

        super.release();
    }

    /**
     * Sets a SAX source that can be parsed by the
     * {@link #parse(ContentHandler)} method.
     * 
     * @param source
     *                A SAX source.
     */
    public void setSaxSource(SAXSource source) {
        this.source = source;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        write(new XmlWriter(outputStream, "UTF-8"));
    }

    /**
     * Writes the representation to a XML writer. The default implementation
     * does nothing and is intended to be overriden.
     * 
     * @param writer
     *                The XML writer to write to.
     * @throws IOException
     */
    @SuppressWarnings("unused")
    public void write(XmlWriter writer) throws IOException {
        // Do nothing by default.
    }
}
