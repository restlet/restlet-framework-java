/*
 * Copyright © 2005 Jérôme LOUVEL.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.noelios.restlet;

import java.util.Map;
import java.util.TreeMap;

import org.restlet.AbstractRestlet;
import org.restlet.RestletCall;
import org.restlet.RestletException;
import org.restlet.component.RestletContainer;
import org.restlet.data.Metadata;

/**
 * Restlet supported by a directory of files.
 * A content negotiation mechanism (similar to Apache HTTP server) is available for files.
 * @see com.noelios.restlet.FileResource
 */
public class DirectoryRestlet extends AbstractRestlet
{
   /** Indicates if the sub-directories are deeply accessible. */
   private boolean deeply;

   /** If no file name is specified, use the (optional) index name. */
   private String indexName;

   /** The directory's root path. */
   private String rootPath;

   /** Mappings from extensions to metadata. */
   private Map<String, Metadata> metadataMappings;

   /**
    * Constructor.
    * @param container 	         The parent container.
    * @param rootPath            The directory's root path.
    * @param deeply              Indicates if the sub-directories are deeply accessible.
    * @param indexName           If no file name is specified, use the (optional) index name.
    */
   public DirectoryRestlet(RestletContainer container, String rootPath, boolean deeply, String indexName)
   {
      super(container);
      this.rootPath = rootPath;
      this.deeply = deeply;
      this.indexName = (indexName == null) ? null : indexName.toLowerCase();
      this.metadataMappings = new TreeMap<String, Metadata>();
   }

   /**
    * Returns the directory's root path.
    * @return The directory's root path.
    */
   public String getRootPath()
   {
      return rootPath;
   }

   public boolean getDeeply()
   {
      return deeply;
   }

   public void setDeeply(boolean deeply)
   {
      this.deeply = deeply;
   }

   public String getIndexName()
   {
      return indexName;
   }

   public void setIndexName(String indexName)
   {
      this.indexName = indexName;
   }

   /**
    * Maps an extension to some metadata (media type, language or character set) to an extension.
    * @param extension  The extension name.
    * @param metadata   The metadata to map.
    */
   public void addExtension(String extension, Metadata metadata)
   {
      this.metadataMappings.put(extension, metadata);
   }

   /**
    * Returns the metadata mapped to an extension.
    * @param extension  The extension name.
    * @return           The mapped metadata.
    */
   public Metadata getMetadata(String extension)
   {
      return this.metadataMappings.get(extension);
   }

   /**
    * Handles a REST call.
    * @param call The call to handle.
    */
   public void handle(RestletCall call) throws RestletException
   {
      call.setBestOutput(new FileResource(this, call.getPath(0, false).toLowerCase()));
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Directory Restlet";
   }

}
