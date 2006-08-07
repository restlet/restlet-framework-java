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
import org.restlet.Factory;
import org.restlet.Restlet;
import org.restlet.component.Component;
import org.restlet.connector.Client;
import org.restlet.data.MediaTypes;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Representation;

import com.noelios.restlet.data.DirectoryResource;
import com.noelios.restlet.data.StringRepresentation;

/**
 * Call handler supported by a directory of resource (from the file system, the web application context or 
 * class loaders). An automatic content negotiation mechanism (similar to the one in Apache HTTP server) is 
 * used to select the best representation of a resource based on the available variants and on the client 
 * capabilities and preferences.
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

   /** Indicates if the subdirectories are deeply accessible (true by default). */
   protected boolean deeplyAccessible;

   /** The absolute root URI, including the "file://" or "context://" scheme. */
   protected String rootUri;
   
   /** Indicates if modifications to context resources are allowed (false by default). */
   protected boolean modifiable;
   
   /** Indicates if the display of directory listings is allowed when no index file is found. */
   protected boolean listingAllowed;

	/** Indicates if content negotation should be enabled (false by default). */
	protected boolean negotiationEnabled;
  
   /**
    * Constructor.
    * @param owner The owner component.
    * @param rootUri The absolute root Uri, including the "file://" or "context://" scheme.
    * @param indexName If no file name is specified, use the (optional) index name.
    */
   public DirectoryHandler(Component owner, String rootUri, String indexName)
   {
      super(owner);
      this.contextClient = getOwner().getClients().get(Factory.CONTEXT_CLIENT_NAME);
      this.indexName = indexName;
      
      if(rootUri.endsWith("/"))
      {
      	this.rootUri = rootUri;
      }
      else
      {
      	// We don't take the risk of exposing directory "file:///C:/AA" 
      	// if only "file:///C:/A" was intended
      	this.rootUri = rootUri + "/";
      }
      
      this.deeplyAccessible = true;
      this.modifiable = false;
      this.listingAllowed = false;
      this.negotiationEnabled = false;
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
			return new DirectoryResource(this, call);
		}
		catch (IOException ioe)
		{
			logger.log(Level.WARNING, "Unable to find the directory's resource", ioe);
			return null;
		}
	}
	
	/**
	 * Indicates if the display of directory listings is allowed when no index file is found.
	 * @return True if the display of directory listings is allowed when no index file is found.
	 */
	public boolean isListingAllowed()
	{
		return this.listingAllowed;
	}
	
	/**
	 * Indicates if the display of directory listings is allowed when no index file is found.
	 * @param listingAllowed True if the display of directory listings is allowed when no index file is found.
	 */
	public void setListingAllowed(boolean listingAllowed)
	{
		this.listingAllowed = listingAllowed;
	}

	/** 
	 * Indicates if content negotation should be enabled.
	 * @return True if content negotation should be enabled.
	 */
	public boolean isNegotiationEnabled()
	{
		return this.negotiationEnabled;
	}

	/** 
	 * Indicates if content negotation should be enabled.
	 * @param negotiationEnabled True if content negotation should be enabled.
	 */
	public void setNegotiationEnabled(boolean negotiationEnabled)
	{
		this.negotiationEnabled = negotiationEnabled;
	}
	
   /** 
    * Indicates if modifications to context resources are allowed.
    * @return True if modifications to context resources are allowed.
    */
   public boolean isModifiable()
   {
   	return this.modifiable;
   }

   /** 
    * Indicates if modifications to context resources are allowed.
    * @param modifiable True if modifications to context resources are allowed.
    */
   public void setModifiable(boolean modifiable)
   {
   	this.modifiable = modifiable;
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
    * Indicates if the subdirectories are deeply accessible (true by default).
    * @return True if the subdirectories are deeply accessible.
    */
   public boolean isDeeplyAccessible()
   {
      return deeplyAccessible;
   }

   /**
    * Indicates if the subdirectories are deeply accessible (true by default).
    * @param deeplyAccessible True if the subdirectories are deeply accessible.
    */
   public void setDeeplyAccessible(boolean deeplyAccessible)
   {
      this.deeplyAccessible = deeplyAccessible;
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
   	
   	sb.append("<h1>Listing of directory \"" + directoryContent.getListRef().getPath() + "\"</h1>\n");

   	Reference parentRef = directoryContent.getListRef().getParentRef();
   	
   	if(!parentRef.equals(directoryContent.getListRef()))
   	{
   		sb.append("<a href=\"" + parentRef + "\">..</a><br/>\n");
   	}
   	
   	for(Reference ref : directoryContent)
   	{
   		sb.append("<a href=\"" + ref.toString() + "\">" + ref.getRelativeRef(directoryContent.getListRef()) + "</a><br/>\n");
   	}
   	sb.append("</body></html>\n");

   	// Create the variants list
   	List<Representation> result = new ArrayList<Representation>();
   	result.add(new StringRepresentation(sb.toString(), MediaTypes.TEXT_HTML));
   	return result;
   }

}
