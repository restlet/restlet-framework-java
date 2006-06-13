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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Call;
import org.restlet.component.Component;
import org.restlet.connector.AbstractClient;
import org.restlet.data.CharacterSet;
import org.restlet.data.DefaultStatus;
import org.restlet.data.Encoding;
import org.restlet.data.Encodings;
import org.restlet.data.Language;
import org.restlet.data.Languages;
import org.restlet.data.MediaType;
import org.restlet.data.MediaTypes;
import org.restlet.data.Metadata;
import org.restlet.data.Methods;
import org.restlet.data.ParameterList;
import org.restlet.data.Protocols;
import org.restlet.data.ReferenceList;
import org.restlet.data.Representation;
import org.restlet.data.RepresentationMetadata;
import org.restlet.data.Statuses;

import com.noelios.restlet.data.ContextReference;
import com.noelios.restlet.data.FileReference;
import com.noelios.restlet.data.FileRepresentation;
import com.noelios.restlet.data.ContextReference.AuthorityType;
import com.noelios.restlet.util.ByteUtils;

/**
 * Connector to the contextual resources accessible via the file system, classloaders and similar mechanisms.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ContextClient extends AbstractClient
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger("com.noelios.restlet.impl.ContextClient");

   /** The default context client name. */
   public static final String DEFAULT_NAME = "ContextClient";
   
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
    * @param owner The owner component.
    * @param parameters The initial parameters.
    */
   public ContextClient(Component owner, ParameterList parameters)
   {
   	super(owner, parameters);
      getProtocols().add(Protocols.CONTEXT);
      getProtocols().add(Protocols.FILE);
      this.defaultEncoding = Encodings.IDENTITY;
      this.defaultMediaType = MediaTypes.TEXT_PLAIN;
      this.defaultLanguage = Languages.ENGLISH_US;
      this.metadataMappings = new TreeMap<String, Metadata>();
      this.timeToLive = 600;
      // if(commonExtensions) addCommonExtensions();
   }

   /**
    * Handles a call.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
      String scheme = call.getResourceRef().getScheme();
      
      if(scheme.equalsIgnoreCase("file"))
      {
      	handleFile(call);
      }
      else if(scheme.equalsIgnoreCase("context"))
      {
			ContextReference cr = new ContextReference(call.getResourceRef());
			
      	if(cr.getAuthorityType() == AuthorityType.CLASS)
      	{
      		handleClassLoader(call, getClass().getClassLoader());
      	}
      	else if(cr.getAuthorityType() == AuthorityType.SYSTEM)
      	{
      		handleClassLoader(call, ClassLoader.getSystemClassLoader());
      	}
      	else if(cr.getAuthorityType() == AuthorityType.THREAD)
      	{
      		handleClassLoader(call, Thread.currentThread().getContextClassLoader());
      	}
      	else if(cr.getAuthorityType() == AuthorityType.WEB_APPLICATION)
      	{
      		// TODO
	      }
      }
      else
      {
         throw new IllegalArgumentException("Protocol not supported by the connector. Only FILE and CONTEXT are supported.");
      }
	}

   /**
    * Handles a call for the FILE protocol.
    * @param call The call to handle.
    */
   protected void handleFile(Call call)
   {
      FileReference fr = new FileReference(call.getResourceRef());
      File file = null;
      
      if(fr.getScheme().equalsIgnoreCase("file"))
      {
         file = fr.getFile();
      }
      else
      {
         throw new IllegalArgumentException("Only FILE resource URIs are allowed here");
      }

      if(call.getMethod().equals(Methods.GET) || call.getMethod().equals(Methods.HEAD))
		{
 			if((file != null) && file.exists())
 			{
 				Representation output = null;
 				
 				if(file.isDirectory())
 				{
 					// Return the directory listing
 					File[] files = file.listFiles();
 					ReferenceList rl = new ReferenceList(files.length);
 					rl.setListRef(fr);
 					
 					for(File entry : files)
 					{
 						try
						{
							rl.add(new FileReference(entry));
						}
						catch (IOException ioe)
						{
							logger.log(Level.WARNING, "Unable to create file reference", ioe);
						}
 					}
 					
 					output = rl.getRepresentation();
 				}
 				else
 				{
 					// Return the file content
               output = new FileRepresentation(file, getDefaultMediaType(), getTimeToLive());
               updateMetadata(file.getName(), output.getMetadata());
 				}
 				
 				call.setOutput(output);
 				call.setStatus(Statuses.SUCCESS_OK);
 			}
 			else
 			{
 				call.setStatus(Statuses.CLIENT_ERROR_NOT_FOUND);
 			}
		}
		else if(call.getMethod().equals(Methods.POST))
		{
			call.setStatus(Statuses.CLIENT_ERROR_METHOD_NOT_ALLOWED);
		}
		else if(call.getMethod().equals(Methods.PUT))
		{
			File tmp = null;

			if(file.exists())
			{
				if(file.isDirectory())
				{
					call.setStatus(new DefaultStatus(Statuses.CLIENT_ERROR_FORBIDDEN, "Can't put a new representation of a directory"));
				}
				else
				{
					// Replace the content of the file
					// First, create a temporary file
					try
					{
						tmp = File.createTempFile("restlet-upload", "bin");
						
						if(call.getInput() != null)
						{
							FileOutputStream fos = new FileOutputStream(tmp);
							ByteUtils.write(call.getInput().getStream(), fos);
							fos.close();
						}
					}
					catch (IOException ioe)
					{
						logger.log(Level.WARNING, "Unable to create the temporary file", ioe);
						call.setStatus(new DefaultStatus(Statuses.SERVER_ERROR_INTERNAL, "Unable to create a temporary file"));
					}
					
					// Then delete the existing file
					if(file.delete())
					{
						// Finally move the temporary file to the existing file location
						if(tmp.renameTo(file))
						{
							if(call.getInput() == null)
							{
								call.setStatus(Statuses.SUCCESS_NO_CONTENT);
							}
							else
							{
								call.setStatus(Statuses.SUCCESS_OK);
							}
						}
						else
						{
							logger.log(Level.WARNING, "Unable to move the temporary file to replace the existing file");
							call.setStatus(new DefaultStatus(Statuses.SERVER_ERROR_INTERNAL, "Unable to move the temporary file to replace the existing file"));
						}
					}
					else
					{
						logger.log(Level.WARNING, "Unable to delete the existing file");
						call.setStatus(new DefaultStatus(Statuses.SERVER_ERROR_INTERNAL, "Unable to delete the existing file"));
					}
				}
			}
			else
			{
				// No existing file or directory found
				if(fr.getPath().endsWith("/"))
				{
					// Create a new directory and its necessary parents
					if(file.mkdirs())
					{
						call.setStatus(Statuses.SUCCESS_NO_CONTENT);
					}
					else
					{
						logger.log(Level.WARNING, "Unable to create the new directory");
						call.setStatus(new DefaultStatus(Statuses.SERVER_ERROR_INTERNAL, "Unable to create the new directory"));
					}
				}
				else
				{
					File parent = file.getParentFile(); 
					if((parent != null) && parent.isDirectory())
					{
						if(!parent.exists())
						{
							// Create the parent directories then the new file
							if(!parent.mkdirs())
							{
								logger.log(Level.WARNING, "Unable to create the parent directory");
								call.setStatus(new DefaultStatus(Statuses.SERVER_ERROR_INTERNAL, "Unable to create the parent directory"));
							}
						}
					}
					
					// Create the new file
					try
					{
						if(file.createNewFile())
						{
							if(call.getInput() == null)
							{
								call.setStatus(Statuses.SUCCESS_NO_CONTENT);
							}
							else
							{
								FileOutputStream fos = new FileOutputStream(file);
								ByteUtils.write(call.getInput().getStream(), fos);
								fos.close();
								call.setStatus(Statuses.SUCCESS_OK);
							}
						}
						else
						{
							logger.log(Level.WARNING, "Unable to create the new file");
							call.setStatus(new DefaultStatus(Statuses.SERVER_ERROR_INTERNAL, "Unable to create the new file"));
						}
					}
					catch (FileNotFoundException fnfe)
					{
						logger.log(Level.WARNING, "Unable to create the new file", fnfe);
						call.setStatus(new DefaultStatus(Statuses.SERVER_ERROR_INTERNAL, "Unable to create the new file"));
					}
					catch (IOException ioe)
					{
						logger.log(Level.WARNING, "Unable to create the new file", ioe);
						call.setStatus(new DefaultStatus(Statuses.SERVER_ERROR_INTERNAL, "Unable to create the new file"));
					}
				}
			}
		}
		else if(call.getMethod().equals(Methods.DELETE))
		{
			if(file.delete())
			{
				call.setStatus(Statuses.SUCCESS_NO_CONTENT);
			}
			else
			{
				if(file.isDirectory())
				{
					if(file.listFiles().length == 0)
					{
						call.setStatus(new DefaultStatus(Statuses.SERVER_ERROR_INTERNAL, "Couldn't delete the empty directory"));
					}
					else
					{
						call.setStatus(new DefaultStatus(Statuses.CLIENT_ERROR_FORBIDDEN, "Couldn't delete the non-empty directory"));
					}
				}
				else
				{
					call.setStatus(new DefaultStatus(Statuses.SERVER_ERROR_INTERNAL, "Couldn't delete the file"));
				}
			}
		}
		else
		{
			call.setStatus(Statuses.CLIENT_ERROR_METHOD_NOT_ALLOWED);
		}
   }
   
   /**
    * Handles a call for the Cprotocol.
    * @param call The call to handle.
    */
   protected void handleClassLoader(Call call, ClassLoader classLoader)
   {
   	
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
    * Updates some representation metadata based on a given entry name with extensions. 
    * @param entryName The entry name with extensions.
    * @param metadata The representation metadata to update.
    */
   public void updateMetadata(String entryName, RepresentationMetadata metadata)
   {
      String[] tokens = entryName.split("\\.");
      Metadata current;
      
      // We found a potential variant
      for(int j = 1; j < tokens.length; j++)
      {
      	current = getMetadata(tokens[j]);
         if(current instanceof MediaType) metadata.setMediaType((MediaType)metadata);
         if(current instanceof CharacterSet) metadata.setCharacterSet((CharacterSet)metadata);
         if(current instanceof Encoding) metadata.setEncoding((Encoding)metadata);
         if(current instanceof Language) metadata.setLanguage((Language)metadata);

         int dashIndex = tokens[j].indexOf('-');
         if((metadata == null) && (dashIndex != -1))
         {
            // We found a language extension with a region area specified
            // Try to find a language matching the primary part of the extension
            String primaryPart = tokens[j].substring(0, dashIndex);
            current = getMetadata(primaryPart);
            if(metadata instanceof Language) metadata.setLanguage((Language)metadata);
         }
      }
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
