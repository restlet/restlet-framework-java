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
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
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
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Representation;
import org.restlet.data.Statuses;

import com.noelios.restlet.data.ContextReference;
import com.noelios.restlet.data.FileReference;
import com.noelios.restlet.data.FileRepresentation;
import com.noelios.restlet.data.InputRepresentation;
import com.noelios.restlet.data.ContextReference.AuthorityType;
import com.noelios.restlet.util.ByteUtils;

/**
 * Connector to the contextual resources accessible via the file system, class loaders and similar mechanisms.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ContextClient extends AbstractClient
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger(ContextClient.class.getCanonicalName());

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
   
   /** The location of the Web Application archive file or directory path. */
   protected String webAppPath;
   
   /** Indicates if the Web Application path corresponds to an archive file or a directory path. */
   protected boolean webAppArchive;
   
   /** Cache of all the WAR file entries to improve directory listing time. */
   protected List<String> warEntries;

   /**
    * Constructor. Note that the common list of metadata associations based on extensions is added, see
    * the addCommonExtensions() method.
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
      this.webAppPath = null;
      this.webAppArchive = false;
      this.warEntries = null;

      addCommonExtensions();
   }
   
   /**
    * Handles a call.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
      String scheme = call.getResourceRef().getScheme();

      // Ensure that all ".." and "." are normalized into the path
   	// to preven unauthorized access to user directories.
   	call.getResourceRef().normalize();
      
      if(scheme.equalsIgnoreCase("file"))
      {
      	handleFile(call, call.getResourceRef().getPath());
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
      		handleWebApplication(call);
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
    * @param path The file or directory path.
    */
   protected void handleFile(Call call, String path)
   {
      File file = new File(FileReference.localizePath(path));

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
 					rl.setListRef(call.getResourceRef());
 					
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
               updateMetadata(file.getName(), output);
 				}
 				
 				call.setOutput(output);
 				call.setStatus(Statuses.SUCCESS_OK);
 			}
 			else
 			{
 				call.setStatus(Statuses.CLIENT_ERROR_NOT_FOUND);
 			}
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
				if(path.endsWith("/"))
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
    * Handles a call with a given class loader.
    * @param call The call to handle.
    */
   protected void handleClassLoader(Call call, ClassLoader classLoader)
   {
      if(call.getMethod().equals(Methods.GET) || call.getMethod().equals(Methods.HEAD))
		{
      	URL url = classLoader.getResource(call.getResourceRef().getPath());
      	
      	if(url != null)
      	{
				try
				{
					call.setOutput(new InputRepresentation(url.openStream(), null));
	 				call.setStatus(Statuses.SUCCESS_OK);
				}
				catch (IOException ioe)
				{
					logger.log(Level.WARNING, "Unable to open the representation's input stream", ioe);
					call.setStatus(Statuses.SERVER_ERROR_INTERNAL);
				}
      	}
      	else
      	{
      		call.setStatus(Statuses.CLIENT_ERROR_NOT_FOUND);
      	}
		}
		else
		{
			call.setStatus(Statuses.CLIENT_ERROR_METHOD_NOT_ALLOWED);
		}
   }

   /**
    * Handles a call using the current Web Application.
    * @param call The call to handle.
    */
   protected void handleWebApplication(Call call)
   {
		if(this.webAppArchive)
		{
			try
			{
				String path = call.getResourceRef().getPath();
				JarFile war = new JarFile(getWebAppPath());
				JarEntry entry = war.getJarEntry(path);
				
				if(entry.isDirectory())
				{
					if(warEntries == null)
					{
						// Cache of all the WAR file entries to improve directory listing time.
						warEntries = new ArrayList<String>();
						for(Enumeration<JarEntry> entries = war.entries(); entries.hasMoreElements(); )
						{
							warEntries.add(entries.nextElement().getName());
						}
					}
					
 					// Return the directory listing
 					ReferenceList rl = new ReferenceList();
 					rl.setListRef(call.getResourceRef());
 					
 					for(String warEntry : warEntries)
 					{
 						if(warEntry.startsWith(path))
 						{
							rl.add(new Reference(warEntry));
 						}
 					}
 					
 					call.setOutput(rl.getRepresentation());
    				call.setStatus(Statuses.SUCCESS_OK);
				}
 				else
 				{
 					// Return the file content
               Representation output = new InputRepresentation(war.getInputStream(entry), null);
               updateMetadata(path, output);
    				call.setOutput(output);
    				call.setStatus(Statuses.SUCCESS_OK);
 				}
			}
			catch (IOException e)
			{
				logger.log(Level.WARNING, "Unable to access to the WAR file", e);
				call.setStatus(Statuses.SERVER_ERROR_INTERNAL);
			}
			
		}
		else
		{
			String path = call.getResourceRef().getPath();
			
			if(path.toUpperCase().startsWith("/WEB-INF/"))
			{
				logger.warning("Forbidden access to the WEB-INF directory detected. Path requested: " + path);
				call.setStatus(Statuses.CLIENT_ERROR_NOT_FOUND);
			}
			else if(path.toUpperCase().startsWith("/META-INF/"))
			{
				logger.warning("Forbidden access to the META-INF directory detected. Path requested: " + path);
				call.setStatus(Statuses.CLIENT_ERROR_NOT_FOUND);
			}
			else
			{
				path = getWebAppPath() + path;
				handleFile(call, path);
			}
		}
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
    * Adds a common list of associations from extensions to metadata.
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
    * @param representation The representation to update.
    */
   public void updateMetadata(String entryName, Representation representation)
   {
      String[] tokens = entryName.split("\\.");
      Metadata current;
      
      // We found a potential variant
      for(int j = 1; j < tokens.length; j++)
      {
      	current = getMetadata(tokens[j]);
         if(current instanceof MediaType) representation.setMediaType((MediaType)current);
         if(current instanceof CharacterSet) representation.setCharacterSet((CharacterSet)current);
         if(current instanceof Encoding) representation.setEncoding((Encoding)current);
         if(current instanceof Language) representation.setLanguage((Language)current);

         int dashIndex = tokens[j].indexOf('-');
         if((representation == null) && (dashIndex != -1))
         {
            // We found a language extension with a region area specified
            // Try to find a language matching the primary part of the extension
            String primaryPart = tokens[j].substring(0, dashIndex);
            current = getMetadata(primaryPart);
            if(representation instanceof Language) representation.setLanguage((Language)representation);
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

   /**
    * Returns the Web Application archive file or directory path.
    * @return The Web Application archive file or directory path.
    */
   public String getWebAppPath()
   {
   	return this.webAppPath;
   }

   /**
    * Sets the Web Application archive file or directory path.
    * @param webAppPath The Web Application archive file or directory path.
    */
   public void setWebAppPath(String webAppPath)
   {
   	this.webAppPath = webAppPath;
   	
   	File file = new File(webAppPath);
   	if(file.isDirectory())
   	{
   		this.webAppArchive = false;

   		// Adjust the archive directory path if necessary
   		if(webAppPath.endsWith("/")) this.webAppPath = this.webAppPath.substring(0, this.webAppPath.length() - 1);
   	}
   	else
   	{
   		this.webAppArchive = true;
   	}
   }

}
