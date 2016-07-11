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
import java.io.OutputStream;
import java.io.Writer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
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
 * requested.
 * 
 * @author Jerome Louvel
 */
public class SaxRepresentation extends XmlRepresentation {

    /** The SAX source. */
    private volatile InputSource source;

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
     * @param xmlSource
     *            A SAX input source to parse.
     */
    public SaxRepresentation(MediaType mediaType, InputSource xmlSource) {
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
                this.source = new InputSource(xmlRepresentation.getStream());
            } else {
                this.source = new InputSource(xmlRepresentation.getStream());
            }

            if (xmlRepresentation.getCharacterSet()!= null) {
                this.source.setEncoding(xmlRepresentation.getCharacterSet().getName());
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
        return source;
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
                SAXParserFactory spf = SAXParserFactory.newInstance();
                SAXParser sp = spf.newSAXParser();

                XMLReader xr = sp.getXMLReader();
                xr.setContentHandler(contentHandler);
                xr.parse(this.source);
            } catch (Exception tce) {
                throw new IOException(
                        "Couldn't parse the source representation: "
                                + tce.getMessage());
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

    @Override
    public void write(OutputStream outputStream) throws IOException {
        XmlWriter xmlWriter = new XmlWriter(outputStream,
                (getCharacterSet() == null) ? "UTF-8" : getCharacterSet()
                        .toString());
        write(xmlWriter);
        xmlWriter.flush();
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
    
    @Override
    public void write(Writer writer) throws IOException {
    XmlWriter xmlWriter = new XmlWriter(writer);
    write(xmlWriter);
    }
    
}
