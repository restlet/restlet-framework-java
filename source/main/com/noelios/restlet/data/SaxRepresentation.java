/*
 * Copyright 2005-2006 Jérôme LOUVEL
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.data;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.restlet.data.MediaType;
import org.restlet.data.Representation;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.noelios.restlet.util.XmlWriter;

/**
 * Abstract representation based on SAX events processing.<br/>
 * The purpose is to create a streamable representation based on a custom Java object model instead
 * of a neutral DOM tree. This domain object can then be directly modified and efficiently serialized at a later time.
 * <br/>
 * Subclasses only need to override the ContentHandler methods required for the reading and also the 
 * write(XmlWriter writer) method when serialization is requested.<br/>
 */
public abstract class SaxRepresentation extends OutputRepresentation implements ContentHandler 
{
	/**
	 * The parser content handler (this instance by default).
	 */
	protected ContentHandler contentHandler;
	
	/**
	 * The source to parse.
	 */
	protected Source source;
	
   /**
    * Constructor.
    * @param mediaType The representation's media type.
    * @param xmlRepresentation A source XML representation to parse.
    */
   public SaxRepresentation(MediaType mediaType, Representation xmlRepresentation) throws IOException
   {
      super(mediaType);
      this.contentHandler = this;
      this.source = new StreamSource(xmlRepresentation.getStream());
   }

   /**
    * Constructor.
    * @param mediaType The representation's media type.
    * @param xmlDocument A source DOM representation to parse.
    */
   public SaxRepresentation(MediaType mediaType, Document xmlDocument)
   {
      super(mediaType);
      this.contentHandler = this;
      this.source = new DOMSource(xmlDocument);
   }

   /**
    * Returns a content handler (this instance by default).
    * @return The content handler.
    */
   public ContentHandler getContentHandler()
   {
   	return this.contentHandler;
   }

   /**
    * Sets a content handler (this instance by default).
    * @param contentHandler The content handler.
    */
   public void setContentHandler(ContentHandler contentHandler)
   {
   	this.contentHandler = contentHandler;
   }
   
   /**
    * Parses the source and sends SAX events to the current content handler (this instance by default). 
    */
   public void parse() throws IOException 
   {
      try
		{
      	Result result = new SAXResult(getContentHandler());
			TransformerFactory.newInstance().newTransformer().transform(this.source, result);
		}
		catch (TransformerConfigurationException tce)
		{
			throw new IOException("Couldn't parse the source representation: " + tce.getMessage());
		}
		catch (TransformerException te)
		{
			throw new IOException("Couldn't parse the source representation: " + te.getMessage());
		}
		catch (TransformerFactoryConfigurationError tfce)
		{
			throw new IOException("Couldn't parse the source representation: " + tfce.getMessage());
		}
   }
   
   /**
    * Writes the representation to a byte stream.
    * @param outputStream The output stream.
    */
	public void write(OutputStream outputStream) throws IOException
	{
		write(new XmlWriter(outputStream, "UTF-8"));
	}

	/**
	 * Writes the representation to a XML writer. 
	 * @param writer The XML writer to write to.
	 * @throws IOException
	 */
	public abstract void write(XmlWriter writer) throws IOException;
	
	/**
	 * Receive an object for locating the origin of SAX document events.
	 * @param locator An object that can return the location of any SAX document event.
	 */
	public void setDocumentLocator(Locator locator)
	{
		// To override if necessary
	}

	/**
	 * Receive notification of the beginning of a document.
	 */
	public void startDocument() throws SAXException
	{
		// To override if necessary
	}

	/**
	 * Receive notification of the end of a document.
	 */
	public void endDocument() throws SAXException
	{
		// To override if necessary
	}

	/**
	 * Begin the scope of a prefix-URI Namespace mapping.
	 * @param prefix The Namespace prefix being declared. An empty string is used for the default element namespace, which has no prefix.
	 * @param uri The Namespace URI the prefix is mapped to.
	 */
	public void startPrefixMapping(String prefix, String uri) throws SAXException
	{
		// To override if necessary
	}

	/**
	 * End the scope of a prefix-URI mapping.
	 * @param prefix The prefix that was being mapped. This is the empty string when a default mapping scope ends.
	 */
	public void endPrefixMapping(String prefix) throws SAXException
	{
		// To override if necessary
	}

	/**
	 * Receive notification of the beginning of an element.
	 * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
	 * @param localName The local name (without prefix), or the empty string if Namespace processing is not being performed.
	 * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
	 * @param attrs The attributes attached to the element. If there are no attributes, it shall be an empty Attributes object. The value of this object after startElement returns is undefined.
	 */
	public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException
	{
		// To override if necessary
	}

	/**
	 * Receive notification of the end of an element.
	 * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
	 * @param localName The local name (without prefix), or the empty string if Namespace processing is not being performed.
	 * @param qName The qualified XML name (with prefix), or the empty string if qualified names are not available.
	 */
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		// To override if necessary
	}

	/**
	 * Receive notification of character data.
	 * @param ch The characters from the XML document.
	 * @param start The start position in the array.
	 * @param length The number of characters to read from the array.
	 */
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		// To override if necessary
	}

	/**
	 * Receive notification of ignorable whitespace in element content. 
	 * @param ch The characters from the XML document.
	 * @param start The start position in the array.
	 * @param length The number of characters to read from the array.
	 */
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
	{
		// To override if necessary
	}

	/**
	 * Receive notification of a processing instruction.
	 * @param target The processing instruction target.
	 * @param data The processing instruction data, or null if none was supplied. The data does not include any whitespace separating it from the target.
	 */
	public void processingInstruction(String target, String data) throws SAXException
	{
		// To override if necessary
	}

	/**
	 * Receive notification of a skipped entity.
	 * @param name The name of the skipped entity. If it is a parameter entity, the name will begin with '%', and if it is the external DTD subset, it will be the string "[dtd]"
	 */
	public void skippedEntity(String name) throws SAXException
	{
		// To override if necessary
	}
	
}
