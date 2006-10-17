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

package org.restlet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Protocol;
import org.restlet.data.Representation;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.spi.Factory;

/**
 * Handler deployable into containers. Applications are guaranteed to receive calls with their base reference
 * set relatively to the virtual host that served it. This class is both a descriptor able to create the root
 * handler and the actual handler that can be attached to one or more VirtualHost instances.   
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class Application extends Handler
{
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
	private Handler root;
	
	/**
	 * The holding filter.
	 */
	private Holder holder;

	/**
	 * Constructor.
	 * @param container The container.
	 */
	public Application(Container container)
	{
		this(container.getContext());
	}
	
	/**
	 * Constructor.
	 * @param context The context.
	 */
	public Application(Context context)
	{
		super(context);
		this.name = null;
		this.description = null;
		this.author = null;
		this.owner = null;
		this.defaultEncoding = null;
		this.defaultLanguage = null;
		this.defaultMediaType = null;
		this.metadataMappings = new TreeMap<String, Metadata>();
		this.indexNames = new ArrayList<String>();
		this.clientProtocols = null;
		this.serverProtocols = null;
		this.root = null;
		this.holder = null;
	}
	
	/**
	 * Creates a root handler that will receive all incoming calls. In general, instances of Router, Filter, 
	 * Restlet or Finder classes will be used as initial application handler. The default implementation
	 * returns null by default. This method is intended to be overriden by subclasses.  
	 * @return The root handler.
	 */
	public abstract Handler createRoot();

	/**
	 * Returns the root handler. Invokes the createRoot() method if no handler exists.
	 * @return The root handler.
	 */
	public Handler getRoot()
	{
		if(this.root == null)
		{
			this.root = createRoot();
		}
		
		return this.root;
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
	 * Returns a representation for the given status.<br/> In order to customize the 
	 * default representation, this method can be overriden. It returns null by default.
	 * @param status The status to represent.
	 * @param request The request handled.
	 * @param response The response updated.
	 * @return The representation of the given status.
	 */
	public Representation getRepresentation(Status status, Request request, Response response)
	{
		return null;
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
	 * Returns the list of server protocols accepted. 
	 * @return The list of server protocols accepted.
	 */
	public List<Protocol> getServerProtocols()
	{
		return this.serverProtocols;
	}

	/**
	 * Handles a call.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	public void handle(Request request, Response response)
	{
  		init(request, response);
  		if(getRoot() != null) getRoot().handle(request, response);
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
	 * Returns the holding filter.
	 * @return The holding filter
	 */
	public Holder getHolder()
	{
		if(this.holder == null) this.holder = Factory.getInstance().createHolder(getContext(), getRoot());
		return this.holder;
	}
	
}
