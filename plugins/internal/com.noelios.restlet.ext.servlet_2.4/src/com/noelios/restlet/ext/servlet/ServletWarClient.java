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

package com.noelios.restlet.ext.servlet;

import java.util.List;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.spi.Helper;

/**
 * Connector acting as a WAR client for a Servlet Application. 
 * It internally uses one of the available connectors registered with the current Restlet implementation.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ServletWarClient extends Client
{
	/** The helper provided by the implementation. */
	private Helper helper;
	
	/**
	 * Constructor.
	 * @param context The context.
	 */
	public ServletWarClient(Context context, javax.servlet.ServletContext servletContext)
	{
		super(context, (List<Protocol>)null);
		getProtocols().add(Protocol.WAR);
		this.helper = new ServletWarClientHelper(this, servletContext);
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
	 * Handles a call.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	public void handle(Request request, Response response)
	{
  		init(request, response);
  		getHelper().handle(request, response);
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
