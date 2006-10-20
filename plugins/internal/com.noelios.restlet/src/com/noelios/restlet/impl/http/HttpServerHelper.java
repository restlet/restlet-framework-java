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

package com.noelios.restlet.impl.http;

import java.util.logging.Level;

import org.restlet.Server;
import org.restlet.data.Request;
import org.restlet.data.Response;

import com.noelios.restlet.impl.ServerHelper;

/**
 * Base HTTP server connector.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class HttpServerHelper extends ServerHelper
{
	/** The converter from HTTP calls to uniform calls. */
	private HttpServerConverter converter;

	/**
	 * Constructor.
	 * @param server The server to help.
	 */
	public HttpServerHelper(Server server)
	{
		super(server);
		this.converter = null;
	}

	/**
	 * Handles the connector call.<br/>
	 * The default behavior is to create an REST call and delegate it to the attached Restlet.
	 * @param httpCall The HTTP server call.
	 */
	public void handle(HttpServerCall httpCall)
	{
		try
		{
			Request request = getConverter().toUniform(httpCall);
			Response response = new HttpResponse(httpCall, request);
			handle(request, response);
			getConverter().commit(httpCall, response);
		}
		catch (Exception e)
		{
			getLogger().log(Level.WARNING, "Error while handling an HTTP server call: ",
					e.getMessage());
			getLogger().log(Level.INFO, "Error while handling an HTTP server call", e);
		}
	}

	/**
	 * Returns the converter from HTTP calls to uniform calls.
	 * @return the converter from HTTP calls to uniform calls.
	 */
	public HttpServerConverter getConverter()
	{
		if (this.converter == null)
		{
			this.converter = new HttpServerConverter(getContext());
		}

		return this.converter;
	}

	/**
	 * Sets the converter from HTTP calls to uniform calls.
	 * @param converter The converter to set.
	 */
	public void setConverter(HttpServerConverter converter)
	{
		this.converter = converter;
	}
}
