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

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Base HTTP client connector.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class HttpClient extends Client
{
	/** The converter from uniform calls to HTTP calls. */
	private HttpClientConverter converter;

	/**
	 * Constructor.
	 * @param context The context.
	 */
	public HttpClient(Context context)
	{
		super(context);
		this.converter = null;
	}

	/**
	 * Creates a low-level HTTP client call from a high-level request.
	 * @param request The high-level request.
	 * @return A low-level HTTP client call.
	 */
	public abstract HttpClientCall create(Request request);

	/**
	 * Handles a call.
    * @param request The request to handle.
    * @param response The response to update.
	 */
	public void handle(Request request, Response response)
	{
		try
		{
			if (!isStarted()) start();
			HttpClientCall httpCall = getConverter().toSpecific(this, request, response);
			getConverter().commit(httpCall, request, response);
		}
		catch (Exception e)
		{
			getLogger().log(Level.WARNING, "Error while handling an HTTP client call: ", e
					.getMessage());
			getLogger().log(Level.INFO, "Error while handling an HTTP client call", e);
		}
	}

	/**
	 * Returns the converter from uniform calls to HTTP calls.
	 * @return the converter from uniform calls to HTTP calls.
	 */
	public HttpClientConverter getConverter()
	{
		if (this.converter == null)
		{
			this.converter = new HttpClientConverter();
		}

		return this.converter;
	}

	/**
	 * Sets the converter from uniform calls to HTTP calls.
	 * @param converter The converter to set.
	 */
	public void setConverter(HttpClientConverter converter)
	{
		this.converter = converter;
	}
}
