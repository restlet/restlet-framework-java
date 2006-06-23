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
import java.util.List;
import java.util.logging.Logger;

import org.restlet.Call;
import org.restlet.data.AbstractResource;
import org.restlet.data.Representation;
import org.restlet.data.Statuses;

import com.noelios.restlet.DirectoryHandler;

/**
 * Resource supported by a set of context representations (from file system, class loaders and webapp context). 
 * A content negotiation mechanism (similar to Apache HTTP server) is available. It is based on path extensions 
 * to detect variants (languages, media types or character sets).
 * @see <a href="http://httpd.apache.org/docs/2.0/content-negotiation.html">Apache mod_negotiation module</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class DirectoryResource extends AbstractResource
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger(DirectoryResource.class.getCanonicalName());

   /** The parent directory handler. */
   protected DirectoryHandler directory;

   /**
    * The absolute base path in the context. For example, "foo.en" will match "foo.en.html" and "foo.en-GB.html".
    */
   protected String basePath;

   /**
    * The local base name of the file. For example, "foo.en" and "foo.en-GB.html" return "foo".
    */
   protected String baseName;

   /**
    * Constructor.
    * @param contextClient The context client connector.
    * @param basePath The base path of the file.
    * @param indexName The optional index name.
    */
   public DirectoryResource(DirectoryHandler directory, String basePath)
   {
      // Update the member variables
      this.directory = directory;

      logger.info("Context base path: " + basePath);

      // Compute the absolute context path
      StringBuilder filePath = new StringBuilder();
      int lastIndex = -1;
      
      if(!basePath.equals("."))
      {
         char nextChar;
         for(int i = 0; i < basePath.length(); i++)
         {
            nextChar = basePath.charAt(i);
            if(nextChar == '/')
            {
               // Remember the position of the last slash
               lastIndex = i;
   
               // Convert the URI separator to the system dependent path separator
               filePath.append(File.separatorChar);
            }
            else
            {
               filePath.append(nextChar);
            }
         }
      }
      
      // Try to detect the presence of the file
      this.basePath = filePath.toString();
      if(new File(this.basePath).isDirectory())
      {
         // Append the index name
         if(getDirectory().getIndexName() != null)
         {
            this.basePath = this.basePath + getDirectory().getIndexName();
            this.baseName = getDirectory().getIndexName();
         }
      }
      else
      {
         if(lastIndex == -1)
         {
            this.baseName = basePath;
         }
         else
         {
            this.baseName = basePath.substring(lastIndex + 1);
         }
      }

      // Remove the extensions from the base name
      int dotIndex = this.baseName.indexOf('.');
      if(dotIndex != -1) this.baseName = this.baseName.substring(0, dotIndex);

      // Log results
      logger.info("Converted base path: " + this.basePath);
      logger.info("Converted base name: " + this.baseName);
   }
   
   /**
    * Returns the parent directory handler.
    * @return The parent directory handler.
    */
   public DirectoryHandler getDirectory()
   {
   	return this.directory;
   }

   /**
    * Returns the absolute path of the file. For example, "foo.en" will match "foo.en.html" and
    * "foo.en-GB.html".
    * @return The base path of the file.
    */
   public String getBasePath()
   {
      return this.basePath;
   }

   /**
    * Sets the absolute path of the file.
    * @param absolutePath The absolute path of the file.
    */
   public void setBasePath(String absolutePath)
   {
      this.basePath = absolutePath;
   }

   /**
    * Returns the local base name of the file. For example, "foo.en" and "foo.en-GB.html" return "foo".
    * @return The local name of the file.
    */
   public String getBaseName()
   {
      return this.baseName;
   }

   /**
    * Handles a GET call.
    * @param call The call to handle.
    */
   protected void handleGet(Call call)
   {
   	// We always allow the transfer of the GET calls
		
   }
   
   /**
    * Returns the representation variants.
    * @return The representation variants.
    */
   public List<Representation> getVariants()
   {
      logger.info("Getting variants for : " + getBasePath());
      List<Representation> result = null;

      // List all the file in the immediate parent directory
      File baseDirectory = new File(getBasePath()).getParentFile();
      if(baseDirectory != null)
      {
         File[] files = baseDirectory.listFiles();
         File currentFile = null;
//         Metadata metadata = null;
//         MediaType mediaType = null;
//         CharacterSet characterSet = null;
//         Encoding encoding = null;
//         Language language = null;

         for(int i = 0; (files != null) && (i < files.length); i++)
         {
            currentFile = files[i];

            // Check if the current file is a valid variant
            if(currentFile.getAbsolutePath().startsWith(getBasePath()))
            {
//               String[] tokens = currentFile.getName().split("\\.");
//               if(tokens[0].equals(getBaseName()))
//               {
//                  // We found a potential variant
//                  for(int j = 1; j < tokens.length; j++)
//                  {
//                     metadata = getContextClient().getMetadata(tokens[j]);
//                     if(metadata instanceof MediaType) mediaType = (MediaType)metadata;
//                     if(metadata instanceof CharacterSet) characterSet = (CharacterSet)metadata;
//                     if(metadata instanceof Encoding) encoding = (Encoding)metadata;
//                     if(metadata instanceof Language) language = (Language)metadata;
//
//                     int dashIndex = tokens[j].indexOf('-');
//                     if((metadata == null) && (dashIndex != -1))
//                     {
//                        // We found a language extension with a region area
//                        // specified
//                        // Try to find a language matching the primary part of
//                        // the extension
//                        String primaryPart = tokens[j].substring(0, dashIndex);
//                        metadata = getContextClient().getMetadata(primaryPart);
//                        if(metadata instanceof Language) language = (Language)metadata;
//                     }
//                  }
//
//                  // Add the new variant to the result list
//                  if(result == null) result = new ArrayList<RepresentationMetadata>();
//                  if(encoding == null) encoding = getContextClient().getDefaultEncoding();
//                  if(mediaType == null) mediaType = getContextClient().getDefaultMediaType();
//                  if(language == null) language = getContextClient().getDefaultLanguage();
//                  FileRepresentation fr = new FileRepresentation(currentFile.getAbsolutePath(), mediaType,
//                        getContextClient().getTimeToLive());
//                  fr.setCharacterSet(characterSet);
//                  fr.setEncoding(encoding);
//                  fr.setLanguage(language);
//                  result.add(fr);
//               }
            }
         }
      }

      return result;
   }

   /**
    * Handles a DELETE call.
    * @param call The call to handle.
    */
   protected void handleDelete(Call call)
   {
   	// We allow the transfer of the DELETE calls only if the readOnly flag is not set
		if(getDirectory().isReadOnly())
		{
   		call.setStatus(Statuses.CLIENT_ERROR_FORBIDDEN);
		}
		else
		{
			// TODO
		}
   }

   /**
    * Handles a PUT call.
    * @param call The call to handle.
    */
   protected void handlePut(Call call)
   {
   	// We allow the transfer of the PUT calls only if the readOnly flag is not set
		if(getDirectory().isReadOnly())
		{
   		call.setStatus(Statuses.CLIENT_ERROR_FORBIDDEN);
		}
		else
		{
			// TODO
		}
   }
   
   /**
    * Default implementation for all the handle*() methods that simply calls the nextHandle() method. 
    * @param call The call to handle.
    */
   protected void defaultHandle(Call call)
   {
   	call.setStatus(Statuses.CLIENT_ERROR_METHOD_NOT_ALLOWED);
   }
   
}
