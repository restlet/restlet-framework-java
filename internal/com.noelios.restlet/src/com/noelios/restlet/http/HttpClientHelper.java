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

package com.noelios.restlet.http;

import java.util.logging.Level;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

import com.noelios.restlet.ClientHelper;

/**
 * Base HTTP client connector. Here is the list of parameters that are supported:
 * <table>
 * 	<tr>
 * 		<th>Parameter name</th>
 * 		<th>Value type</th>
 * 		<th>Default value</th>
 * 		<th>Description</th>
 * 	</tr>
 * 	<tr>
 * 		<td>converter</td>
 * 		<td>String</td>
 * 		<td>com.noelios.restlet.http.HttpClientConverter</td>
 * 		<td>Class name of the converter of low-level HTTP calls into high level requests and responses.</td>
 * 	</tr>
 *	</table>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class HttpClientHelper extends ClientHelper
{
	/** The converter from uniform calls to HTTP calls. */
	private HttpClientConverter converter;

	/**
	 * Constructor.
	 * @param client The client to help.
	 */
	public HttpClientHelper(Client client)
	{
		super(client);
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
			HttpClientCall httpCall = getConverter().toSpecific(this, request, response);
			getConverter().commit(httpCall, request, response);
		}
		catch (Exception e)
		{
			getLogger().log(Level.WARNING, "Error while handling an HTTP client call: ",
					e.getMessage());
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
			try
			{
				String converterClass = getParameters().getFirstValue("converter",
						"com.noelios.restlet.http.HttpClientConverter");
				this.converter = (HttpClientConverter) Class.forName(converterClass)
						.getConstructor(Context.class).newInstance(getContext());
			}
			catch (Exception e)
			{
				getLogger().log(Level.SEVERE, "Unable to create the HTTP server converter");
			}
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
