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
package com.noelios.restlet.ext.asyncweb;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.restlet.data.Parameter;
import org.restlet.data.ParameterList;
import org.restlet.data.Reference;

import org.safehaus.asyncweb.http.HttpRequest;
import org.safehaus.asyncweb.http.HttpResponse;
import org.safehaus.asyncweb.http.ResponseStatus;
import org.safehaus.asyncweb.http.internal.HttpHeaders;
import org.safehaus.asyncweb.http.internal.Request;
import org.safehaus.asyncweb.http.internal.Response;

import com.noelios.restlet.connector.AbstractHttpServerCall;

/**
 * HttpServerCall implementation used by the AsyncServer.
 * @author Lars Heuer (heuer[at]semagia.com) <a href="http://www.semagia.com/">Semagia</a>
 */
public class AsyncWebServerCall extends AbstractHttpServerCall
{
	/**
	 * AsyncWeb request.
	 */
	protected Request request;

	/**
	 * AsyncWeb response.
	 */
	protected Response response;

	/**
	 * Constructor.
	 *
	 * @param request The AsyncWebRequest.
	 * @param response The AsyncWebResponse.
	 * @param confidential Indicates if the server is acting in HTTPS mode.
	 * @param address IP address of the server.
	 */
	public AsyncWebServerCall(HttpRequest request, HttpResponse response,
			boolean confidential, String address)
	{
		super();
		this.request = (Request) request;
		this.response = (Response) response;
		super.confidential = confidential;
		super.responseAddress = address;
	}

	/* (non-Javadoc)
	 * @see com.noelios.restlet.impl.ConnectorCallImpl#getRequestAddress()
	 */
	@Override
	public String getRequestAddress()
	{
		return request.getRemoteAddress();
	}

	/* (non-Javadoc)
	 * @see com.noelios.restlet.impl.ConnectorCallImpl#getRequestUri()
	 */
	@Override
	public String getRequestUri()
	{
		return Reference.toUri(isConfidential() ? "https" : "http", request.getHeader("host"), null,
				request.getRequestURI(), null, null);
	}

	/* (non-Javadoc)
	 * @see com.noelios.restlet.impl.ConnectorCallImpl#getRequestMethod()
	 */
	@Override
	public String getRequestMethod()
	{
		return request.getMethod().getName();
	}

	/* (non-Javadoc)
	 * @see com.noelios.restlet.impl.ConnectorCallImpl#getResponseStatusCode()
	 */
	@Override
	public int getResponseStatusCode()
	{
		return response.getStatus().getCode();
	}

	/* (non-Javadoc)
	 * @see com.noelios.restlet.impl.ConnectorCallImpl#getResponseReasonPhrase()
	 */
	@Override
	public String getResponseReasonPhrase()
	{
		return response.getStatusReasonPhrase();
	}

	/* (non-Javadoc)
	 * @see org.restlet.connector.ServerCall#setResponseStatus(int, java.lang.String)
	 */
	public void setResponseStatus(int code, String reason)
	{
		response.setStatus(ResponseStatus.forId(code), reason);
	}
	
	/* (non-Javadoc)
	 * @see com.noelios.restlet.impl.ConnectorCallImpl#getRequestHeaders()
	 */
	@Override
	public ParameterList getRequestHeaders()
	{
		if (super.requestHeaders == null) 
		{
			HttpHeaders headers = request.getHeaders();
			int headerCount = headers.getSize();
			super.requestHeaders = new ParameterList(headerCount);
			for (int i=0; i < headerCount; i++) 
			{
				super.requestHeaders.add(headers.getHeaderName(i).getValue(),
												 headers.getHeaderValue(i).getValue());
			}
		}
		return super.requestHeaders;
	}
	
	/* (non-Javadoc)
	 * @see com.noelios.restlet.impl.ConnectorCallImpl#getResponseHeaders()
	 */
	@Override
	public ParameterList getResponseHeaders()
	{
		if (super.responseHeaders == null) 
		{
			HttpHeaders headers = response.getHeaders();
			int headerCount = headers.getSize();
			super.responseHeaders = new ParameterList(headerCount);
			for (int i=0; i < headerCount; i++) 
			{
				super.responseHeaders.add(headers.getHeaderName(i).getValue(),
												 headers.getHeaderValue(i).getValue());
			} 
		}
		return super.responseHeaders;
	}

	/* (non-Javadoc)
	 * @see org.restlet.connector.ServerCall#sendResponseHeaders()
	 */
	public void sendResponseHeaders()
	{
		// Ensure that headers are empty
		response.getHeaders().dispose();
		for (Parameter header : super.getResponseHeaders())
		{
			response.addHeader(header.getName(), header.getValue());
		}
	}

	/* (non-Javadoc)
	 * @see org.restlet.connector.ServerCall#getRequestChannel()
	 */
	public ReadableByteChannel getRequestChannel()
	{
		// Unsupported.
		return null;
	}

	/* (non-Javadoc)
	 * @see org.restlet.connector.ServerCall#getRequestStream()
	 */
	public InputStream getRequestStream()
	{
		return request.getInputStream();
	}

	/* (non-Javadoc)
	 * @see org.restlet.connector.ServerCall#getResponseChannel()
	 */
	public WritableByteChannel getResponseChannel()
	{
		// Unsupported.
		return null;
	}

	/* (non-Javadoc)
	 * @see org.restlet.connector.ServerCall#getResponseStream()
	 */
	public OutputStream getResponseStream()
	{
		return response.getOutputStream();
	}

}
