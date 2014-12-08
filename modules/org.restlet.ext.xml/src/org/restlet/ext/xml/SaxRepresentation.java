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
import java.io.Writer;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;
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
import org.xml.sax.XMLReader;

/**
 * XML representation for SAX events processing. The purpose is to create a
 * streamable content based on a custom Java object model instead of a neutral
 * DOM tree. This domain object can then be directly modified and efficiently
 * serialized at a later time.<br>
 * <br>
 * Subclasses only need to override the ContentHandler methods required for the
 * reading and also the write(XmlWriter writer) method when serialization is
 * requested. <br>
 * <br>
 * SECURITY WARNING: Using XML parsers configured to not prevent nor limit
 * document type definition (DTD) entity resolution can expose the parser to an
 * XML Entity Expansion injection attack, see
 * https://github.com/restlet/restlet-
 * framework-java/wiki/XEE-injection-security-fix.
 * 
 * @author Jerome Louvel
 */
public class SaxRepresentation extends XmlRepresentation {

    /**
     * True for turning on secure parsing XML representations; default value
     * provided by system property "org.restlet.ext.xml.secureProcessing", true
     * by default.
     */
    public static final boolean XML_SECURE_PROCESSING = (System
            .getProperty("org.restlet.ext.xml.secureProcessing") == null) ? true
            : Boolean.getBoolean("org.restlet.ext.xml.secureProcessing");

    /** Limits potential XML overflow attacks. */
    private boolean secureProcessing;

    /** The SAX source. */
    private volatile SAXSource source;

    /** The source XML representation. */
    private volatile Representation xmlRepresentation;

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
        this.secureProcessing = XML_SECURE_PROCESSING;
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
        this.secureProcessing = XML_SECURE_PROCESSING;
        this.source = new SAXSource(
                SAXSource.sourceToInputSource(new DOMSource(xmlDocument)));
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
        this.secureProcessing = XML_SECURE_PROCESSING;
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
        this.secureProcessing = XML_SECURE_PROCESSING;
        this.source = xmlSource;
    }

    /**
     * Constructor.
     * 
     * @param xmlRepresentation
     *            A source XML representation to parse.
     */
    public SaxRepresentation(Representation xmlRepresentation) {
        super((xmlRepresentation == null) ? null : xmlRepresentation
                .getMediaType());
        this.secureProcessing = XML_SECURE_PROCESSING;
        this.xmlRepresentation = xmlRepresentation;
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
        if (this.source == null && this.xmlRepresentation != null) {
            if (xmlRepresentation instanceof XmlRepresentation) {
                this.source = ((XmlRepresentation) xmlRepresentation)
                        .getSaxSource();
            } else {
                try {
                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    spf.setNamespaceAware(isNamespaceAware());

                    // Keep before the external entity preferences
                    spf.setValidating(isValidatingDtd());

                    javax.xml.validation.Schema xsd = getSchema();

                    if (xsd != null) {
                        spf.setSchema(xsd);
                    }

                    spf.setXIncludeAware(isXIncludeAware());
                    spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,
                            isSecureProcessing());
                    spf.setFeature(
                            "http://xml.org/sax/features/external-general-entities",
                            isExpandingEntityRefs());
                    spf.setFeature(
                            "http://xml.org/sax/features/external-parameter-entities",
                            isExpandingEntityRefs());
                    XMLReader xmlReader = spf.newSAXParser().getXMLReader();
                    this.source = new SAXSource(xmlReader, new InputSource(
                            xmlRepresentation.getReader()));
                } catch (Exception e) {
                    throw new IOException(
                            "Unable to create customized SAX source", e);
                }
            }

            if (xmlRepresentation.getLocationRef() != null) {
                this.source.setSystemId(xmlRepresentation.getLocationRef()
                        .getTargetRef().toString());
            }
        }

        return this.source;
    }

    /**
     * Indicates if it limits potential XML overflow attacks.
     * 
     * @return True if it limits potential XML overflow attacks.
     */
    public boolean isSecureProcessing() {
        return secureProcessing;
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
                TransformerFactory.newInstance().newTransformer()
                        .transform(getSaxSource(), result);
            } catch (TransformerConfigurationException tce) {
                throw new IOException(
                        "Couldn't parse the source representation: "
                                + tce.getMessage(), tce);
            } catch (TransformerException te) {
                throw new IOException(
                        "Couldn't parse the source representation: "
                                + te.getMessage(), te);
            } catch (TransformerFactoryConfigurationError tfce) {
                throw new IOException(
                        "Couldn't parse the source representation: "
                                + tfce.getMessage(), tfce);
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
        if (this.xmlRepresentation != null) {
            this.xmlRepresentation.release();
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

    /**
     * Indicates if it limits potential XML overflow attacks.
     * 
     * @param secureProcessing
     *            True if it limits potential XML overflow attacks.
     */
    public void setSecureProcessing(boolean secureProcessing) {
        this.secureProcessing = secureProcessing;
    }

    @Override
    public void write(Writer writer) throws IOException {
        XmlWriter xmlWriter = new XmlWriter(writer);
        write(xmlWriter);
    }

    /**
     * Writes the representation to a XML writer. The default implementation
     * calls {@link #parse(ContentHandler)} using the {@link XmlWriter}
     * parameter as the content handler. This behavior is intended to be
     * overridden.
     * 
     * @param writer
     *            The XML writer to write to.
     * @throws IOException
     */
    public void write(XmlWriter writer) throws IOException {
        parse(writer);
    }
}
