/*
 * Copyright 2005-2006 Noelios Consulting.
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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.restlet.data.MediaType;
import org.restlet.data.Representation;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Representation based on a DOM document.
 * DOM is a standard XML object model defined by the W3C.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class DomRepresentation extends OutputRepresentation 
{
	/** 
	 * The wrapped DOM document. 
	 */
	protected Document dom;
	
	/** 
	 * Indicates if indentation should occur. 
	 */
	protected boolean indent;
	
   /**
    * Constructor.
    * @param mediaType The representation's media type.
    * @param xmlRepresentation A source XML representation to parse.
    */
   public DomRepresentation(MediaType mediaType, Representation xmlRepresentation) throws IOException
   {
      super(mediaType);
      
      try
		{
			this.dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlRepresentation.getStream());
		}
		catch (SAXException se)
		{
			throw new IOException("Couldn't read the XML representation: " + se.getMessage());
		}
		catch (IOException ioe)
		{
			throw new IOException("Couldn't read the XML representation: " + ioe.getMessage());
		}
		catch (ParserConfigurationException pce)
		{
			throw new IOException("Couldn't read the XML representation: " + pce.getMessage());
		}
   }

   /**
    * Constructor from an existing DOM document.
    * @param mediaType The representation's media type.
    * @param xmlDocument The source DOM document.
    */
   public DomRepresentation(MediaType mediaType, Document xmlDocument)
   {
      super(mediaType);
   	this.dom = xmlDocument;
   }

   /**
    * Constructor for an empty document.
    * @param mediaType The representation's media type.
    */
   public DomRepresentation(MediaType mediaType) throws IOException
   {
      super(mediaType);
      
		try
		{
			this.dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		}
		catch (ParserConfigurationException pce)
		{
			throw new IOException("Couldn't create the empty document: " + pce.getMessage());
		}
   }

   /**
    * Returns the wrapped DOM document.
    * @return The wrapped DOM document.
    */
   public Document getDocument()
   {
   	return this.dom;
   }
	
   /**
    * Sets the wrapped DOM document.
    * @param dom The wrapped DOM document.
    */
   public void setDocument(Document dom)
   {
   	this.dom = dom;
   }

   /**
    * Writes the representation to a byte stream.
    * @param outputStream The output stream.
    */
	public void write(OutputStream outputStream) throws IOException
	{
      try
		{
			TransformerFactory.newInstance().newTransformer().transform(new DOMSource(this.dom), new StreamResult(outputStream));
		}
		catch (TransformerConfigurationException tce)
		{
			throw new IOException("Couldn't write the XML representation: " + tce.getMessage());
		}
		catch (TransformerException te)
		{
			throw new IOException("Couldn't write the XML representation: " + te.getMessage());
		}
		catch (TransformerFactoryConfigurationError tfce)
		{
			throw new IOException("Couldn't write the XML representation: " + tfce.getMessage());
		}
	}

	
	
}
