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

import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.ServerInfo;
import org.restlet.data.Status;

import com.noelios.restlet.impl.Factory;

/**
 * Response wrapper for server HTTP calls.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class HttpResponse extends Response
{
	/** The low-level HTTP call. */
	private HttpCall httpCall;

	/** Indicates if the server data was parsed and added. */
	private boolean serverAdded;

	/**
	 * Constructor.
	 * @param httpCall The low-level HTTP server call.
	 * @param request The associated high-level request.
	 */
	public HttpResponse(HttpServerCall httpCall, Request request)
	{
		super(request);
		this.serverAdded = false;
		this.httpCall = httpCall;

		// Set the properties
		setStatus(Status.SUCCESS_OK);
	}

	/**
	 * Returns the low-level HTTP call.
	 * @return The low-level HTTP call.
	 */
	public HttpCall getHttpCall()
	{
		return this.httpCall;
	}

	/**
	 * Returns the server specific data.
	 * @return The server specific data.
	 */
	public ServerInfo getServer()
	{
		ServerInfo result = super.getServerInfo();

		if (!this.serverAdded)
		{
			result.setAddress(httpCall.getServerAddress());
			result.setAgent(Factory.VERSION_HEADER);
			result.setName(httpCall.getServerName());
			result.setPort(httpCall.getServerPort());
			this.serverAdded = true;
		}

		return result;
	}
}
