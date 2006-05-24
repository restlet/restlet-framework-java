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
import org.restlet.data.Reference;

import simple.http.Request;
import simple.http.Response;

import com.noelios.restlet.impl.HttpServerCallImpl;

/**
 * Call that is used by the Simple HTTP server.
 * @author Lars Heuer (heuer[at]semagia.com) <a href="http://semagia.com/">Semagia</a>
 */
public class SimpleCall extends HttpServerCallImpl
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
	 * The listening port used.
	 */
	protected int hostPort;

	/**
	 * Constructs this class with the specified {@link simple.http.Request}
	 * and {@link simple.http.Response}.
	 * @param request Request to wrap.
	 * @param response Response to wrap.
	 * @param confidential Inidicates if this call is acting in HTTP or HTTPS mode.
	 * @param hostPort The listening port used.
	 */
	SimpleCall(Request request, Response response, boolean confidential, int hostPort)
	{
		super();
		this.request = request;
		this.response = response;
		this.confidential = confidential;
		this.hostPort = hostPort;
	}

   /**
    * Returns the full request URI. 
    * @return The full request URI.
    */
	public String getRequestUri()
	{
		return Reference.toUri(isConfidential() ? "https" : "http", request.getValue("host"), hostPort, 
				request.getURI(), null, null);
	}

   /**
    * Returns the value for a request header name.<br/>
    * If multiple headers with the same name are found, all values are returned separated by commas.
    * @param headerName The header name.
    * @return The value for a request header name.
    */
	public String getRequestHeaderValue(String headerName)
	{
		return request.getValue(headerName);
	}

   /**
    * Returns the request method. 
    * @return The request method.
    */
	public String getRequestMethod()
	{
		return request.getMethod();
	}

   /**
    * Returns the request address.<br/>
    * Corresponds to the IP address of the requesting client.
    * @return The request address.
    */
	public String getResponseAddress()
	{
		return response.getInetAddress().getHostAddress();
	}

   /**
    * Returns the response status code.
    * @return The response status code.
    */
	public int getResponseStatusCode()
	{
		return response.getCode();
	}

   /**
    * Returns the request address.<br/>
    * Corresponds to the IP address of the requesting client.
    * @return The request address.
    */
	public String getRequestAddress()
	{
		return request.getInetAddress().getHostAddress();
	}

   /**
    * Returns the list of request headers.
    * @return The list of request headers.
    */
	public List<Parameter> getRequestHeaders()
	{
		if (super.requestHeaders == null)
		{
			int headerCount = request.headerCount();
			super.requestHeaders = new ArrayList<Parameter>(headerCount);
			for (int i = 0; i < headerCount; i++)
			{
				super.requestHeaders.add(new Parameter(request.getName(i), request
						.getValue(i)));
			}
		}
		return super.requestHeaders;
	}

   /**
    * Sends the response headers.<br/>
    * Must be called before sending the response output.
    */
	public void sendResponseHeaders()
	{
		response.clear();
		for(Parameter header : getResponseHeaders())
		{
			response.add(header.getName(), header.getValue());
		}
	}

   /**
    * Returns the request entity channel if it exists.
    * @return The request entity channel if it exists.
    */
	public ReadableByteChannel getRequestChannel()
	{
		// Unsupported.
		return null;
	}

   /**
    * Returns the request entity stream if it exists.
    * @return The request entity stream if it exists.
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
    * Sets the response status code.
    * @param code The response status code.
    * @param reason The response reason phrase.
    */
	public void setResponseStatus(int code, String reason)
	{
		response.setCode(code);
		response.setText(reason);
	}

   /**
    * Returns the response channel if it exists.
    * @return The response channel if it exists.
    */
	public WritableByteChannel getResponseChannel()
	{
		// Unsupported.
		return null;
	}

   /**
    * Returns the response stream if it exists.
    * @return The response stream if it exists.
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
