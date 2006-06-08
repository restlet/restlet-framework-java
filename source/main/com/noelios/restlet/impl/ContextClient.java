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

package com.noelios.restlet.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Call;
import org.restlet.connector.AbstractClient;
import org.restlet.data.Encoding;
import org.restlet.data.Encodings;
import org.restlet.data.Language;
import org.restlet.data.Languages;
import org.restlet.data.MediaType;
import org.restlet.data.MediaTypes;
import org.restlet.data.Metadata;
import org.restlet.data.Protocol;
import org.restlet.data.Protocols;

import com.noelios.restlet.data.ContextReference;
import com.noelios.restlet.data.ContextReference.AuthorityType;

/**
 * Connector to the contextual resources accessible via the file system, classloaders and similar mechanisms.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ContextClient extends AbstractClient
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger("com.noelios.restlet.impl.ContextClient");

   /** Default encoding used when no encoding extension is available. */
   protected Encoding defaultEncoding;

   /** Default media type used when no media type extension is available. */
   protected MediaType defaultMediaType;

   /** Default language used when no language extension is available. */
   protected Language defaultLanguage;

   /** Mappings from extensions to metadata. */
   protected Map<String, Metadata> metadataMappings;
   
   /** Indicates the time to live for a file representation before it expires (in seconds; default to 10 minutes). */
   protected int timeToLive;

   /**
    * Constructor.
    * @param commonExtensions Indicates if the common extensions should be added.
    */
   public ContextClient(Protocol protocol, boolean commonExtensions)
   {
      super(protocol);
      this.defaultEncoding = Encodings.IDENTITY;
      this.defaultMediaType = MediaTypes.TEXT_PLAIN;
      this.defaultLanguage = Languages.ENGLISH_US;
      this.metadataMappings = new TreeMap<String, Metadata>();
      this.timeToLive = 600;
      if(commonExtensions) addCommonExtensions();
   }
   
   /**
    * Returns the supported protocols. 
    * @return The supported protocols.
    */
   public static List<Protocol> getProtocols()
   {
   	return Arrays.asList(new Protocol[]{Protocols.CONTEXT});
   }

   /**
    * Handles a call.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
   	// TODO
   	
   }
   
   /**
    * Returns a new client call.
    * @param method The request method.
    * @param requestUri The requested resource URI.
    * @param hasInput Indicates if the call will have an input to send to the server.
    * @return A new client call.
    */
	public ContextCall createCall(String method, String requestUri, boolean hasInput)
	{
		ContextCall result = null;
		
		try
		{
			if(getProtocol().equals(Protocols.CONTEXT))
			{
				ContextReference cr = new ContextReference(requestUri);
				
		      if(cr.getScheme().equalsIgnoreCase("context"))
		      {
		      	if(cr.getAuthorityType() == AuthorityType.CLASS)
		      	{
		      		result = new ClassLoaderCall(method, requestUri, getClass().getClassLoader());
		      	}
		      	else if(cr.getAuthorityType() == AuthorityType.SYSTEM)
		      	{
		      		result = new ClassLoaderCall(method, requestUri, ClassLoader.getSystemClassLoader());
		      	}
		      	else if(cr.getAuthorityType() == AuthorityType.THREAD)
		      	{
		      		result = new ClassLoaderCall(method, requestUri, Thread.currentThread().getContextClassLoader());
		      	}
		      	else if(cr.getAuthorityType() == AuthorityType.WEB_APPLICATION)
		      	{
		      		result = null; // TODO
		      	}
		      }
		      else
		      {
		         throw new IllegalArgumentException("Only CONTEXT resource URIs are allowed here");
		      }
				
				
			}
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, "Unable to create the call", e);
		}
		
		return result;
	}

   /**
    * Maps an extension to some metadata (media type, language or character set) to an extension.
    * @param extension The extension name.
    * @param metadata The metadata to map.
    */
   public void addExtension(String extension, Metadata metadata)
   {
      this.metadataMappings.put(extension, metadata);
   }

   /**
    * Adds a common list of extensions to a directory Restlet.
    * The list of languages extensions:<br/>
    * <ul>
    *  <li>en: English</li>
    *  <li>es: Spanish</li>
    *  <li>fr: French</li>
    * </ul><br/>
    * The list of media type extensions:<br/>
    * <ul>
    *  <li>css: CSS stylesheet</li>
    *  <li>doc: Microsoft Word document</li>
    *  <li>gif: GIF image</li>
    *  <li>html: HTML document</li>
    *  <li>ico: Windows icon (Favicon)</li>
    *  <li>jpeg, jpg: JPEG image</li>
    *  <li>js: Javascript document</li>
    *  <li>pdf: Adobe PDF document</li>
    *  <li>png: PNG image</li>
    *  <li>ppt: Microsoft Powerpoint document</li>
    *  <li>rdf:  Description Framework document</li>
    *  <li>txt: Plain text</li>
    *  <li>swf: Shockwave Flash object</li>
    *  <li>xhtml: XHTML document</li>
    *  <li>xml: XML document</li>
    *  <li>zip: Zip archive</li>
    * </ul>
    */
   public void addCommonExtensions()
   {
      addExtension("en",   Languages.ENGLISH);
      addExtension("es",   Languages.SPANISH);
      addExtension("fr",   Languages.FRENCH);
      
      addExtension("css",  MediaTypes.TEXT_CSS);
      addExtension("doc",  MediaTypes.APPLICATION_WORD);
      addExtension("gif",  MediaTypes.IMAGE_GIF);
      addExtension("html", MediaTypes.TEXT_HTML);
      addExtension("ico",  MediaTypes.IMAGE_ICON);
      addExtension("jpeg", MediaTypes.IMAGE_JPEG);
      addExtension("jpg",  MediaTypes.IMAGE_JPEG);
      addExtension("js",   MediaTypes.APPLICATION_JAVASCRIPT);
      addExtension("pdf",  MediaTypes.APPLICATION_PDF);
      addExtension("png",  MediaTypes.IMAGE_PNG);
      addExtension("ppt",  MediaTypes.APPLICATION_POWERPOINT);
      addExtension("rdf",  MediaTypes.APPLICATION_RESOURCE_DESCRIPTION_FRAMEWORK);
      addExtension("txt",  MediaTypes.TEXT_PLAIN);
      addExtension("swf",  MediaTypes.APPLICATION_SHOCKWAVE_FLASH);
      addExtension("xhtml",MediaTypes.APPLICATION_XHTML_XML);
      addExtension("xml",  MediaTypes.TEXT_XML);
      addExtension("zip",	MediaTypes.APPLICATION_ZIP);
   }

   /**
    * Returns the metadata mapped to an extension.
    * @param extension The extension name.
    * @return The mapped metadata.
    */
   public Metadata getMetadata(String extension)
   {
      return this.metadataMappings.get(extension);
   }

   /**
    * Set the default encoding ("identity" by default).
    * Used when no encoding extension is available.
    * @param encoding The default encoding.
    */
   public void setDefaultEncoding(Encoding encoding)
   {
      this.defaultEncoding = encoding;
   }

   /**
    * Returns the default encoding.
    * Used when no encoding extension is available.
    * @return The default encoding.
    */
   public Encoding getDefaultEncoding()
   {
      return this.defaultEncoding;
   }

   /**
    * Set the default media type ("text/plain" by default).
    * Used when no media type extension is available.
    * @param mediaType The default media type.
    */
   public void setDefaultMediaType(MediaType mediaType)
   {
      this.defaultMediaType = mediaType;
   }

   /**
    * Returns the default media type.
    * Used when no media type extension is available.
    * @return The default media type.
    */
   public MediaType getDefaultMediaType()
   {
      return this.defaultMediaType;
   }

   /**
    * Set the default language ("en-us" by default).
    * Used when no language extension is available.
    * @param language The default language.
    */
   public void setDefaultLanguage(Language language)
   {
      this.defaultLanguage = language;
   }

   /**
    * Returns the default language.
    * Used when no language extension is available.
    * @return The default language.
    */
   public Language getDefaultLanguage()
   {
      return this.defaultLanguage;
   }

   /**
    * Returns the time to live for a file representation before it expires (in seconds).
    * @return The time to live for a file representation before it expires (in seconds).
    */
   public int getTimeToLive()
   {
      return this.timeToLive;
   }

   /**
    * Sets the time to live for a file representation before it expires (in seconds).
    * @param ttl The time to live for a file representation before it expires (in seconds).
    */
   public void setTimeToLive(int ttl)
   {
      this.timeToLive = ttl;
   }

}
