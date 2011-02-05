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

package org.restlet.ext.xml;

import java.io.IOException;
import java.io.Writer;

import javax.xml.transform.Result;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
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
 * @author Jerome Louvel
 */
public class SaxRepresentation extends XmlRepresentation {

    /** The SAX source. */
    private volatile SAXSource source;

    /**
     * Default constructor. Uses the {@link MediaType#TEXT_XML} media type.
     */
    public SaxRepresentation() {
        this(MediaType.TEXT_XML);
    }

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
     *            A DOM document to parse.
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
     *            The representation's media type.
     * @param xmlSource
     *            A SAX input source to parse.
     */
    public SaxRepresentation(MediaType mediaType, InputSource xmlSource) {
        super(mediaType);
        this.source = new SAXSource(xmlSource);
    }

    /**
     * Constructor.
     * 
     * @param mediaType
     *            The representation's media type.
     * @param xmlSource
     *            A JAXP source to parse.
     */
    public SaxRepresentation(MediaType mediaType, SAXSource xmlSource) {
        super(mediaType);
        this.source = xmlSource;
    }

    /**
     * Constructor.
     * 
     * @param xmlRepresentation
     *            A source XML representation to parse.
     * @throws IOException
     */
    public SaxRepresentation(Representation xmlRepresentation) {
        super((xmlRepresentation == null) ? null : xmlRepresentation
                .getMediaType());

        try {
            if (xmlRepresentation instanceof XmlRepresentation) {
                this.source = ((XmlRepresentation) xmlRepresentation)
                        .getSaxSource();
            } else {
                this.source = new SAXSource(new InputSource(xmlRepresentation
                        .getReader()));
            }

            if (xmlRepresentation.getLocationRef() != null) {
                this.source.setSystemId(xmlRepresentation.getLocationRef()
                        .getTargetRef().toString());
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Override
    public InputSource getInputSource() throws IOException {
        return (getSaxSource() == null) ? null : getSaxSource()
                .getInputSource();
    }

    /**
     * Returns the SAX source that can be parsed by the
     * {@link #parse(ContentHandler)} method or used for an XSLT transformation.
     */
    @Override
    public SAXSource getSaxSource() throws IOException {
        return this.source;
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
     *            A SAX source.
     */
    public void setSaxSource(SAXSource source) {
        this.source = source;
    }

    @Override
    public void write(Writer writer) throws IOException {
        XmlWriter xmlWriter = new XmlWriter(writer);
        write(xmlWriter);
    }

    /**
     * Writes the representation to a XML writer. The default implementation
     * does nothing and is intended to be overridden.
     * 
     * @param writer
     *            The XML writer to write to.
     * @throws IOException
     */
    public void write(XmlWriter writer) throws IOException {
        // Do nothing by default.
    }
}
