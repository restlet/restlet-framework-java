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
import org.xml.sax.ContentHandler;

import com.noelios.restlet.util.XmlWriter;

/**
 * Abstract representation based on SAX events processing.<br/>
 * The purpose is to create a streamable representation based on a custom Java object model instead
 * of a neutral DOM tree. This domain object can then be directly modified and efficiently serialized at a later time.
 * <br/>
 * Subclasses only need to override the ContentHandler methods required for the reading and also the 
 * write(XmlWriter writer) method when serialization is requested.<br/>
 */
public abstract class SaxRepresentation extends OutputRepresentation 
{
	/**
	 * The source to parse.
	 */
	protected Source source;
	
   /**
    * Constructor.
    * @param mediaType The representation's media type.
    */
   public SaxRepresentation(MediaType mediaType)
   {
      super(mediaType);
      this.source = null;
   }
	
   /**
    * Constructor.
    * @param xmlRepresentation A source XML representation to parse.
    */
   public SaxRepresentation(Representation xmlRepresentation) throws IOException
   {
      super(xmlRepresentation.getMetadata().getMediaType());
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
      this.source = new DOMSource(xmlDocument);
   }
   
   /**
    * Parses the source and sends SAX events to a content reader.
    * @param contentReader The SAX content reader to use for parsing. 
    */
   public void parse(ContentHandler contentReader) throws IOException 
   {
   	if(contentReader != null)
   	{
	      try
			{
	      	Result result = new SAXResult(contentReader);
				TransformerFactory.newInstance().newTransformer().transform(this.source, result);
			}
			catch (TransformerConfigurationException tce)
			{
				throw new IOException("Couldn't parse the source representation: " + tce.getMessage());
			}
			catch (TransformerException te)
			{
				te.printStackTrace();
				throw new IOException("Couldn't parse the source representation: " + te.getMessage());
			}
			catch (TransformerFactoryConfigurationError tfce)
			{
				throw new IOException("Couldn't parse the source representation: " + tfce.getMessage());
			}
   	}
   	else
   	{
			throw new IOException("Couldn't parse the source representation: no content handler defined.");
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
	
}
