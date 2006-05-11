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

package com.noelios.restlet.ext.simple;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

import org.restlet.data.Parameter;

import simple.http.Request;
import simple.http.Response;

import com.noelios.restlet.impl.HttpServerCallImpl;

/**
 * Call that is used by the Simple HTTP server.
 * @author Lars Heuer (heuer[at]semagia.com) <a href="http://semagia.com/">Semagia</a>
 */
final class SimpleCall extends HttpServerCallImpl
{
	/**
	 * Simple Request.
	 */
	protected Request request;

	/**
	 * Simple Response.
	 */
	protected Response response;

	/**
	 * Constructs this class with the specified {@link simple.http.Request}
	 * and {@link simple.http.Response}.
	 * @param request Request to wrap.
	 * @param response Response to wrap.
	 * @param confidential Inidicates if this call is acting in HTTP or HTTPS mode.
	 */
	SimpleCall(Request request, Response response, boolean confidential)
	{
		super();
		this.request = request;
		this.response = response;
		super.confidential = confidential;
	}

	/**
	 * 
	 */
	public String getRequestUri()
	{
		StringBuffer buffer = new StringBuffer(super.confidential ? "https://"
				: "http://");
		buffer.append(request.getValue("host"));
		buffer.append(request.getURI());
		return buffer.toString();
	}

	/**
	 */
	public String getRequestHeaderValue(String name)
	{
		return request.getValue(name);
	}

	/**
	 */
	public String getRequestMethod()
	{
		return request.getMethod();
	}

	/**
	 */
	public String getResponseAddress()
	{
		return response.getInetAddress().getHostAddress();
	}

	/**
	 */
	public int getResponseStatusCode()
	{
		return response.getCode();
	}

	/**
	 */
	public String getRequestAddress()
	{
		return request.getInetAddress().getHostAddress();
	}

	/**
	 */
	public List<Parameter> getRequestHeaders()
	{
		if (super.requestHeaders == null)
		{
			super.requestHeaders = new ArrayList<Parameter>();
			int headerCount = request.headerCount();
			for (int i = 0; i < headerCount; i++)
			{
				super.requestHeaders.add(new Parameter(request.getName(i), request
						.getValue(i)));
			}
		}
		return super.requestHeaders;
	}

	/**
	 */
	public void sendResponseHeaders()
	{
		response.clear();
		for (Parameter param : getResponseHeaders())
		{
			response.add(param.getName(), param.getValue());
		}
	}

	/**
	 */
	public ReadableByteChannel getRequestChannel()
	{
		// Unsupported.
		return null;
	}

	/**
	 */
	public InputStream getRequestStream()
	{
		try
		{
			return request.getInputStream();
		}
		catch (IOException ex)
		{
			return null;
		}
	}

	/**
	 */
	public void setResponseStatus(int code, String reason)
	{
		response.setCode(code);
		response.setText(reason);
	}

	/**
	 */
	public WritableByteChannel getResponseChannel()
	{
		// Unsupported.
		return null;
	}

	/**
	 */
	public OutputStream getResponseStream()
	{
		try
		{
			return response.getOutputStream();
		}
		catch (IOException ex)
		{
			return null;
		}
	}
}
