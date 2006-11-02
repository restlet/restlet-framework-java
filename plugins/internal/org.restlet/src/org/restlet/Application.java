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

import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.service.ConnectorService;
import org.restlet.service.DecoderService;
import org.restlet.service.LocalService;
import org.restlet.service.LogService;
import org.restlet.service.StatusService;
import org.restlet.spi.Factory;
import org.restlet.spi.Helper;

/**
 * Restlet deployable into Containers. Applications are guaranteed to receive calls with their base reference
 * set relatively to the VirtualHost that served them. This class is both a descriptor able to create the 
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
	
	/** The decoder service. */
	private DecoderService decoderService;
	
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
		this.decoderService = null;
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
		if(this.connectorService == null) this.connectorService = new ConnectorService(true);
		return this.connectorService;
	}

	/** 
	 * Returns the decoder service. This service is enabled by default.
	 * @return The decoderservice.
	 */
	public DecoderService getDecoderService()
	{
		if(this.decoderService == null) this.decoderService = new DecoderService(true);
		return this.decoderService;
	}

	/** 
	 * Returns the local service. This service is enabled by default.
	 * @return The local service.
	 */
	public LocalService getLocalService()
	{
		if(this.localService == null) this.localService = new LocalService(true);
		return this.localService;
	}

	/** 
	 * Returns the log service. This service is enabled by default.
	 * @return The log service.
	 */
	public LogService getLogService()
	{
		if(this.logService == null) 
		{
			this.logService = new LogService(true);
			this.logService.setAccessLoggerName(getClass().getCanonicalName() + " (" + hashCode() + ")");
		}
		
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
		if(this.statusService == null) this.statusService = new StatusService(true);
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
	 * Sets the connector service. 
	 * @param connectorService The connector service.
	 */
	public void setConnectorService(ConnectorService connectorService)
	{
		this.connectorService = connectorService;
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
	 * Sets the local service. 
	 * @param localService The local service.
	 */
	public void setLocalService(LocalService localService)
	{
		this.localService = localService;
	}

	/** 
	 * Sets the log service. 
	 * @param logService The log service.
	 */
	public void setLogService(LogService logService)
	{
		this.logService = logService;
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
	 * Sets the status service. 
	 * @param statusService The status service.
	 */
	public void setStatusService(StatusService statusService)
	{
		this.statusService = statusService;
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
