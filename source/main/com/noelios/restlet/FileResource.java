/*
 * Copyright 2005 Jérôme LOUVEL
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.restlet.Resource;
import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Method;
import org.restlet.data.Methods;
import org.restlet.data.Representation;
import org.restlet.data.RepresentationMetadata;

import com.noelios.restlet.data.FileRepresentation;

/**
 * Resource representing a file stored on the local file system. A content negotiation mechanism (similar to
 * Apache HTTP server) is available. It is based on file extensions to specify variants (languages, media
 * types or character sets).
 * @see <a href="http://httpd.apache.org/docs/2.0/content-negotiation.html">Apache mod_negotiation module</a>
 */
public class FileResource implements Resource
{
   /**
    * The parent directory restlet.
    */
   private DirectoryRestlet directoryRestlet;

   /**
    * The absolute base path of the file. For example, "foo.en" will match "foo.en.html" and "foo.en-GB.html".
    */
   private String basePath;

   /**
    * The local base name of the file. For example, "foo.en" and "foo.en-GB.html" return "foo".
    */
   private String baseName;

   /**
    * Constructor.
    * @param directoryRestlet The parent directory restlet.
    * @param basePath The base path of the file.
    */
   public FileResource(DirectoryRestlet directoryRestlet, String basePath)
   {
      // Update the member variables
      this.directoryRestlet = directoryRestlet;

      // Compute the absolute file path
      StringBuilder filePath = new StringBuilder(directoryRestlet.getRootPath());
      int lastIndex = -1;
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

      // Try to detect the presence of the file
      this.basePath = filePath.toString().toLowerCase();
      if(new File(this.basePath).isDirectory())
      {
         // Append the index name
         String indexName = getDirectoryRestlet().getIndexName();
         if(indexName != null)
         {
            this.basePath = this.basePath + indexName;
            this.baseName = indexName;
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
   }

   /**
    * Returns the list of allowed methods.
    * @return The list of allowed methods.
    */
   public List<Method> getMethods()
   {
      List<Method> result = new ArrayList<Method>();
      result.add(Methods.GET);
      return result;
   }

   /**
    * Returns the representation variants.
    * @return The representation variants.
    */
   public List<RepresentationMetadata> getVariantsMetadata()
   {
      List<RepresentationMetadata> result = null;

      // List all the file in the immediate parent directory
      File baseDirectory = new File(getBasePath()).getParentFile();
      if(baseDirectory != null)
      {
         File[] files = baseDirectory.listFiles();
         File currentFile = null;
         Metadata metadata = null;
         MediaType mediaType = null;
         CharacterSet characterSet = null;
         Language language = null;

         for(int i = 0; i < files.length; i++)
         {
            currentFile = files[i];

            // Check if the current file is a valid variant
            if(currentFile.getAbsolutePath().toLowerCase().startsWith(getBasePath()))
            {
               String[] tokens = currentFile.getName().split("\\.");
               if(tokens[0].equalsIgnoreCase(getBaseName()))
               {
                  // We found a potential variant
                  for(int j = 1; j < tokens.length; j++)
                  {
                     metadata = getDirectoryRestlet().getMetadata(tokens[j]);
                     if(metadata instanceof MediaType) mediaType = (MediaType)metadata;
                     if(metadata instanceof CharacterSet) characterSet = (CharacterSet)metadata;
                     if(metadata instanceof Language) language = (Language)metadata;

                     int dashIndex = tokens[j].indexOf('-');
                     if((metadata == null) && (dashIndex != -1))
                     {
                        // We found a language extension with a region area
                        // specified
                        // Try to find a language matching the primary part of
                        // the extension
                        String primaryPart = tokens[j].substring(0, dashIndex);
                        metadata = getDirectoryRestlet().getMetadata(primaryPart);
                        if(metadata instanceof Language) language = (Language)metadata;
                     }
                  }

                  if(mediaType != null)
                  {
                     if(result == null) result = new ArrayList<RepresentationMetadata>();
                     FileRepresentation fr = new FileRepresentation(currentFile.getAbsolutePath(), mediaType);
                     fr.setCharacterSet(characterSet);
                     fr.setLanguage(language);
                     result.add(fr);
                  }
                  else
                  {
                     // Ignore file without matching media type
                  }
               }
            }
         }
      }

      return result;
   }

   /**
    * Returns the representation matching the given metadata.
    * @param metadata The metadata to match.
    * @return The matching representation.
    */
   public Representation getRepresentation(RepresentationMetadata metadata)
   {
      if(metadata instanceof Representation) return (Representation)metadata;
      else return null;
   }

   /**
    * Returns the parent directory restlet.
    * @return The parent directory restlet.
    */
   public DirectoryRestlet getDirectoryRestlet()
   {
      return directoryRestlet;
   }

   /**
    * Sets the parent directory restlet.
    * @param directoryRestlet The parent directory restlet.
    */
   public void setDirectoryRestlet(DirectoryRestlet directoryRestlet)
   {
      this.directoryRestlet = directoryRestlet;
   }

   /**
    * Returns the absolute path of the file. For example, "foo.en" will match "foo.en.html" and
    * "foo.en-GB.html".
    * @return The base path of the file.
    */
   public String getBasePath()
   {
      return basePath;
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
      return baseName;
   }

}
