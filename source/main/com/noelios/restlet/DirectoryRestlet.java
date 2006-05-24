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

package com.noelios.restlet;

import java.util.Map;
import java.util.TreeMap;

import org.restlet.AbstractRestlet;
import org.restlet.Call;
import org.restlet.component.Component;
import org.restlet.data.Encoding;
import org.restlet.data.Encodings;
import org.restlet.data.Language;
import org.restlet.data.Languages;
import org.restlet.data.MediaType;
import org.restlet.data.MediaTypes;
import org.restlet.data.Metadata;

import com.noelios.restlet.util.StringUtils;

/**
 * Restlet supported by a directory of files. An automatic content negotiation mechanism 
 * (similar to Apache HTTP server) is used for files serving.
 * @see com.noelios.restlet.FileResource
 * @see <a href="http://www.restlet.org/tutorial#part06">Tutorial: Serving static files</a>
 */
public class DirectoryRestlet extends AbstractRestlet
{
   /** Indicates if the sub-directories are deeply accessible. */
   protected boolean deeply;

   /** Default encoding used when no encoding extension is available. */
   protected Encoding defaultEncoding;

   /** Default media type used when no media type extension is available. */
   protected MediaType defaultMediaType;

   /** Default language used when no language extension is available. */
   protected Language defaultLanguage;

   /** If no file name is specified, use the (optional) index name. */
   protected String indexName;

   /** The directory's root path. */
   protected String rootPath;

   /** Mappings from extensions to metadata. */
   protected Map<String, Metadata> metadataMappings;
   
   /** Indicates the time to live for a file representation before it expires (in seconds; default to 10 minutes). */
   protected int timeToLive;

   /**
    * Constructor.
    * @param parent The parent component.
    * @param rootPath The directory's root path.
    * @param deeply Indicates if the sub-directories are deeply accessible.
    * @param indexName If no file name is specified, use the (optional) index name.
    * @param commonExtensions Indicates if the common extensions should be added.
    */
   public DirectoryRestlet(Component parent, String rootPath, boolean deeply, String indexName, boolean commonExtensions)
   {
      super(parent);
      this.rootPath = StringUtils.normalizePath(rootPath);
      this.deeply = deeply;
      this.defaultEncoding = Encodings.IDENTITY;
      this.defaultMediaType = MediaTypes.TEXT_PLAIN;
      this.defaultLanguage = Languages.ENGLISH_US;
      this.indexName = indexName;
      this.metadataMappings = new TreeMap<String, Metadata>();
      this.timeToLive = 600;
      if(commonExtensions) addCommonExtensions();      	
   }

   /**
    * Returns the directory's root path.
    * @return The directory's root path.
    */
   public String getRootPath()
   {
      return rootPath;
   }

   /**
    * Indicates if the subdirectories should be deeply exposed.
    * @return True if the subdirectories should be deeply exposed.
    */
   public boolean getDeeply()
   {
      return deeply;
   }

   /**
    * Indicates if the subdirectories should be deeply exposed.
    * @param deeply True if the subdirectories should be deeply exposed.
    */
   public void setDeeply(boolean deeply)
   {
      this.deeply = deeply;
   }

   /**
    * Returns the index name.
    * @return The index name.
    */
   public String getIndexName()
   {
      return indexName;
   }

   /**
    * Sets the index name.
    * @param indexName The index name.
    */
   public void setIndexName(String indexName)
   {
      this.indexName = indexName;
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
   
   /**
    * Handles a GET call.
    * @param call The call to handle.
    */
   public void handleGet(Call call)
   {
      call.setBestOutput(new FileResource(this, call.getResourcePath()), getDefaultLanguage());
   }

}
