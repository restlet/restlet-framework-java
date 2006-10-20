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

import org.restlet.data.Representation;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.spi.Factory;
import org.restlet.spi.Helper;
import org.restlet.util.ConnectorService;
import org.restlet.util.LocalService;
import org.restlet.util.LogService;
import org.restlet.util.StatusService;

/**
 * Restlet deployable into containers. Applications are guaranteed to receive calls with their base reference
 * set relatively to the virtual host that served them. This class is both a descriptor able to create the 
 * root Restlet and the actual Restlet that can be attached to one or more VirtualHost instances.   
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class Application extends Restlet
{
	/** The display name. */
	private String name;

	/** The description. */
	private String description;

	/** The author(s). */
	private String author;

	/** The owner(s). */
	private String owner;
	
	/** The root Restlet. */
	private Restlet root;
	
	/** The connector service. */
	private ConnectorService connectorService;
	
	/** The local service. */
	private LocalService localService;
	
	/** The log service. */
	private LogService logService;
	
	/** The status service. */
	private StatusService statusService;
	
	/** The helper provided by the implementation. */
	private Helper helper;
	
	/**
	 * Constructor.
	 */
	public Application()
	{
		this((Context)null);
	}

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
	 * @param parentContext The parent context. Typically the container's context.
	 */
	public Application(Context parentContext)
	{
		super(null);
		this.helper = Factory.getInstance().createHelper(this, parentContext);
		setContext(this.helper.createContext());
		this.name = null;
		this.description = null;
		this.author = null;
		this.owner = null;
		this.root = null;
		this.connectorService = null;
		this.localService = null;
		this.logService = null;
		this.statusService = null;
	}

	/**
	 * Creates a root Restlet that will receive all incoming calls. In general, instances of Router, Filter 
	 * or Handler classes will be used as initial application Restlet. The default implementation
	 * returns null by default. This method is intended to be overriden by subclasses.  
	 * @return The root Restlet.
	 */
	public abstract Restlet createRoot();

	/**
	 * Returns the author(s).
	 * @return The author(s).
	 */
	public String getAuthor()
	{
		return this.author;
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
	 * Returns the helper provided by the implementation.
	 * @return The helper provided by the implementation.
	 */
	private Helper getHelper()
	{
		return this.helper;
	}

	/** 
	 * Returns the connector service. This service is enabled by default.
	 * @return The connector service.
	 */
	public ConnectorService getConnectorService()
	{
		if(this.connectorService == null) this.connectorService = new ConnectorService(this, true);
		return this.connectorService;
	}

	/** 
	 * Returns the local service. This service is enabled by default.
	 * @return The local service.
	 */
	public LocalService getLocalService()
	{
		if(this.localService == null) this.localService = new LocalService(this, true);
		return this.localService;
	}

	/** 
	 * Returns the log service. This service is enabled by default.
	 * @return The log service.
	 */
	public LogService getLogService()
	{
		if(this.logService == null) this.logService = new LogService(this, true);
		return this.logService;
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
	 * Returns the root Restlet. Invokes the createRoot() method if no Restlet exists.
	 * @return The root Restlet.
	 */
	public Restlet getRoot()
	{
		if(this.root == null)
		{
			this.root = createRoot();
		}
		
		return this.root;
	}

	/** 
	 * Returns the status service. This service is enabled by default.
	 * @return The status service.
	 */
	public StatusService getStatusService()
	{
		if(this.statusService == null) this.statusService = new StatusService(this, true);
		return this.statusService;
	}
   
   /**
	 * Handles a call.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	public void handle(Request request, Response response)
	{
  		init(request, response);
  		getHelper().handle(request, response);
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
	
	/** Start callback. */
	public void start() throws Exception
	{
		super.start();
		getHelper().start();
	}

	/** Stop callback. */
	public void stop() throws Exception
	{
		getHelper().stop();
		super.stop();
	}

}
