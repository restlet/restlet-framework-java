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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.AbstractHandler;
import org.restlet.Call;
import org.restlet.Restlet;
import org.restlet.component.Component;
import org.restlet.connector.Client;
import org.restlet.data.MediaTypes;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Representation;

import com.noelios.restlet.data.StringRepresentation;
import com.noelios.restlet.impl.ContextClient;
import com.noelios.restlet.impl.DirectoryResource;

/**
 * Handler supported by a directory of resources. An automatic content negotiation mechanism 
 * (similar to Apache HTTP server) is used to serve the best representations.
 * @see <a href="http://www.restlet.org/tutorial#part06">Tutorial: Serving context resources</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class DirectoryHandler extends AbstractHandler
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger(DirectoryResource.class.getCanonicalName());

   /** The context client. */
   protected Client contextClient;

   /** If no file name is specified, use the (optional) index name. */
   protected String indexName;

   /** Indicates if the sub-directories are deeply accessible. */
   protected boolean deeply;

   /** The absolute root URI, including the "file://" or "context://" scheme. */
   protected String rootUri;
   
   /** Indicates if modifications to context resources are allowed. */
   protected boolean readOnly;
      
   /**
    * Constructor.
    * @param owner The owner component.
    * @param rootUri The absolute root Uri, including the "file://" or "context://" scheme.
    * @param deeply Indicates if the sub-directories are deeply accessible.
    * @param indexName If no file name is specified, use the (optional) index name.
    */
   public DirectoryHandler(Component owner, String rootUri, boolean deeply, String indexName)
   {
      super(owner);
      this.contextClient = getOwner().getClients().get(ContextClient.DEFAULT_NAME);
      this.indexName = indexName;
      this.rootUri = rootUri;
      this.deeply = deeply;
      this.readOnly = true;
   }
   /**
	 * Finds the next Restlet if available.
	 * @param call The current call.
	 * @return The next Restlet if available or null.
	 */
	public Restlet findNext(Call call)
	{
   	try
		{
			return new DirectoryResource(this, call.getResourcePath());
		}
		catch (IOException ioe)
		{
			logger.log(Level.WARNING, "Unable to find the directory's resource", ioe);
			return null;
		}
	}
	
   /** 
    * Indicates if modifications to context resources are allowed.
    * @return False if modifications to context resources are allowed.
    */
   public boolean isReadOnly()
   {
   	return this.readOnly;
   }

   /** 
    * Indicates if modifications to context resources are allowed.
    * @param readOnly False if modifications to context resources are allowed.
    */
   public void setReadOnly(boolean readOnly)
   {
   	this.readOnly = readOnly;
   }
   
   /**
    * Returns the context client.
    * @return The context client.
    */
   public Client getContextClient()
   {
   	return this.contextClient;
   }

   /**
    * Sets the context client.
    * @param contextClient The context client.
    */
   public void setContextClient(Client contextClient)
   {
   	this.contextClient = contextClient;
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
    * Returns the root URI.
    * @return The root URI.
    */
   public String getRootUri()
   {
      return rootUri;
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
    * Returns the variant representations of a directory. This method can be subclassed in order to provide
    * alternative representations.
    * @param directoryContent The list of references contained in the directory.
    * @return The variant representations of a directory.
    */
   public List<Representation> getDirectoryVariants(ReferenceList directoryContent)
   {
   	// Create a simple HTML list
   	StringBuilder sb = new StringBuilder();
   	sb.append("<html><body>\n");
   	for(Reference ref : directoryContent)
   	{
   		sb.append("<a href=\"" + ref.toString() + "\">" + ref.toString() + "</a>\n");
   	}
   	sb.append("</body></html>\n");

   	// Create the variants list
   	List<Representation> result = new ArrayList<Representation>();
   	result.add(new StringRepresentation(sb.toString(), MediaTypes.TEXT_HTML));
   	return result;
   }

}
