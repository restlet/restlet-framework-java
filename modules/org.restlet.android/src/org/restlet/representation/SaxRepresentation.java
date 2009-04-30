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

package org.restlet.representation;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.restlet.data.MediaType;
import org.restlet.util.XmlWriter;
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

			if (xmlRepresentation.getIdentifier() != null) {
				this.source.setSystemId(xmlRepresentation.getIdentifier()
						.getTargetRef().toString());
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
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

	/**
	 * Sets a SAX source that can be parsed by the
	 * {@link #parse(ContentHandler)} method.
	 * 
	 * @param source
	 *            A SAX source.
	 */
	public void setSaxSource(InputSource source) {
		this.source = source;
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
}
