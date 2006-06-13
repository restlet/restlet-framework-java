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

import org.restlet.AbstractRestlet;
import org.restlet.Call;
import org.restlet.component.Component;

import com.noelios.restlet.data.FileReference;

/**
 * Restlet supported by a directory of files. An automatic content negotiation mechanism 
 * (similar to Apache HTTP server) is used for files serving.
 * @see com.noelios.restlet.impl.FileClient
 * @see com.noelios.restlet.impl.ContextResource
 * @see <a href="http://www.restlet.org/tutorial#part06">Tutorial: Serving static files</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class DirectoryRestlet extends AbstractRestlet
{
   /** The file client connector. */
   protected String fileClientName;

   /** If no file name is specified, use the (optional) index name. */
   protected String indexName;

   /** Indicates if the sub-directories are deeply accessible. */
   protected boolean deeply;

   /** The directory's root path. */
   protected String rootPath;
   
   /**
    * Constructor.
    * @param owner The owner component.
    * @param fileClientName The file client connector name.
    * @param rootPath The directory's root path.
    * @param deeply Indicates if the sub-directories are deeply accessible.
    * @param indexName If no file name is specified, use the (optional) index name.
    */
   public DirectoryRestlet(Component owner, String fileClientName, String rootPath, boolean deeply, String indexName)
   {
      super(owner);
      this.fileClientName = fileClientName;
      this.indexName = indexName;
      this.rootPath = FileReference.localizePath(rootPath);
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
    * Handles a GET call.
    * @param call The call to handle.
    */
   public void handleGet(Call call)
   {
		getOwner().callClient(this.fileClientName, call);
   }

}
