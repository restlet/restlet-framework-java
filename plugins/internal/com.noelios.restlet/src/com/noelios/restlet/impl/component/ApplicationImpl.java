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

package com.noelios.restlet.impl.component;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.UniformInterface;
import org.restlet.component.Application;
import org.restlet.component.Container;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Protocol;
import org.restlet.data.Status;

/**
 * Application implementation.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ApplicationImpl extends Application
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger(ApplicationImpl.class.getCanonicalName());

   /** The display name. */
	private String name;
	
	/** The description. */
	private String description;
	
	/** The author(s). */
	private String author;
	
	/** The owner(s). */
	private String owner;
	
	/** The default encoding for local representations. */
	private Encoding defaultEncoding;
	
	/** The default language for local representations. */
	private Language defaultLanguage;

	/** The default media type for local representations. */
	private MediaType defaultMediaType;
	
	/** The mappings from extension names to metadata. */
	private Map<String, Metadata> metadataMappings;

	/** The list of index names (ex: index.html). */
	private List<String> indexNames;
	
	/** The list of client protocols used. */
	private List<Protocol> clientProtocols;
	
	/** The list of server protocols accepted. */
	private List<Protocol> serverProtocols;
	
	/** The root handler. */
	private UniformInterface root;

   /** The context. */
	private Context context;

	/** Indicates if the instance was started. */
   private boolean started;
	
	/**
    * Constructor.
    * @param container The parent container.
    */
   public ApplicationImpl(Container container)
   {
   	super(container);
		this.name = null;
		this.description = null;
		this.author = null;
		this.owner = null;
		this.defaultEncoding = null;
		this.defaultLanguage = null;
		this.defaultMediaType = null;
		this.metadataMappings = new TreeMap<String, Metadata>();
      this.started = false;
   }

   /**
    * Returns the context.
    * @return The context.
    */
   public Context getContext()
   {
      return this.context;
   }

   /**
    * Sets the context.
    * @param context The context.
    */
   public void setContext(Context context)
   {
      this.context = context;
   }

   /** Start hook. */
   public void start() throws Exception
   {
   	this.started = true;
   }

   /** Stop hook. */
   public void stop() throws Exception
   {
      this.started = false;
   }

   /**
    * Indicates if the Restlet is started.
    * @return True if the Restlet is started.
    */
   public boolean isStarted()
   {
      return this.started;
   }

   /**
    * Indicates if the Restlet is stopped.
    * @return True if the Restlet is stopped.
    */
   public boolean isStopped()
   {
      return !this.started;
   }

   /**
    * Handles a direct call.
    * @param request The request to handle.
    * @param response The response to update.
    */
   public void handle(Request request, Response response)
   {
      if(getRoot() != null)
      {
   		getRoot().handle(request, response);
      }
      else
      {
         response.setStatus(Status.SERVER_ERROR_INTERNAL);
         logger.log(Level.SEVERE, "No root handler defined.");
      }
   }
   
	/**
	 * Returns the author(s).
	 * @return The author(s).
	 */
	public String getAuthor()
	{
		return this.author;
	}
	
	/**
	 * Returns the list of client protocols used. 
	 * @return The list of client protocols used.
	 */
	public List<Protocol> getClientProtocols()
	{
		return this.clientProtocols;
	}

	/**
	 * Returns the default encoding for local representations.
	 * @return The default encoding for local representations.
	 */
	public Encoding getDefaultEncoding()
	{
		return this.defaultEncoding;
	}

	/**
	 * Returns the default language for local representations.
	 * @return The default language for local representations.
	 */
	public Language getDefaultLanguage()
	{
		return this.defaultLanguage;
	}

	/**
	 * Returns the default media type for local representations.
	 * @return The default media type for local representations.
	 */
	public MediaType getDefaultMediaType()
	{
		return this.defaultMediaType;
	}

	/**
	 * Returns the description.
	 * @return The description
	 */
	public String getDescription()
	{
		return this.description;
	}

	/**
	 * Returns the list of index names (ex: index.html).
	 * @return The list of index names (ex: index.html).
	 */
	public List<String> getIndexNames()
	{
		return this.indexNames;
	}

	/**
	 * Returns the mappings from extension names to metadata.
	 * @return The mappings from extension names to metadata.
	 */
	public Map<String, Metadata> getMetadataMappings()
	{
		return this.metadataMappings;
	}

	/**
	 * Returns the display name.
	 * @return The display name.
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * Returns the owner(s).
	 * @return The owner(s).
	 */
	public String getOwner()
	{
		return this.owner;
	}

	/**
	 * Returns the root handler.
	 * @return The root handler.
	 */
	public UniformInterface getRoot()
	{
		return this.root;
	}

	/**
	 * Returns the list of server protocols accepted. 
	 * @return The list of server protocols accepted.
	 */
	public List<Protocol> getServerProtocols()
	{
		return this.serverProtocols;
	}

	/**
	 * Indicates if a root handler is set. 
	 * @return True if a root handler is set. 
	 */
	public boolean hasRoot()
	{
		return getRoot() != null;
	}

	/**
	 * Sets the author(s).
	 * @param author The author(s).
	 */
	public void setAuthor(String author)
	{
		this.author = author;
	}

	/**
	 * Sets the default encoding for local representations.
	 * @param defaultEncoding The default encoding for local representations.
	 */
	public void setDefaultEncoding(Encoding defaultEncoding)
	{
		this.defaultEncoding = defaultEncoding;
	}

	/**
	 * Sets the default language for local representations.
	 * @param defaultLanguage The default language for local representations.
	 */
	public void setDefaultLanguage(Language defaultLanguage)
	{
		this.defaultLanguage = defaultLanguage;
	}

	/**
	 * Sets the default media type for local representations.
	 * @param defaultMediaType The default media type for local representations.
	 */
	public void setDefaultMediaType(MediaType defaultMediaType)
	{
		this.defaultMediaType = defaultMediaType;
	}

	/**
	 * Sets the description.
	 * @param description The description.
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * Sets the display name.
	 * @param name The display name.
	 */
	public void setName(String name)
	{
		this.name = name;
	}

   /**
	 * Sets the owner(s).
	 * @param owner The owner(s).
	 */
	public void setOwner(String owner)
	{
		this.owner = owner;
	}

	/**
	 * Sets the root handler that will receive all incoming calls. In general, instances of Restlet, Router, 
	 * Filter or Finder classes will be used as root handlers.
	 * @param root The root handler to use.
	 */
	public void setRoot(UniformInterface root)
	{
		this.root = root;
	}
}
