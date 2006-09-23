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

package com.noelios.restlet.impl.connector;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Call;

/**
 * Base HTTP server connector.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class HttpServer extends ServerImpl
{
	/** Obtain a suitable logger. */
	private static Logger logger = Logger.getLogger(HttpServer.class.getCanonicalName());

	/** The listening address if specified. */
	private String address;

	/** The listening port if specified. */
	private int port;

	/** The converter from HTTP calls to uniform calls. */
	private HttpServerConverter converter;

	/**
	 * Constructor.
	 */
	public HttpServer()
	{
		this(null, 0);
	}

	/**
	 * Constructor.
	 * @param address The optional listening IP address (local host used if null).
	 * @param port The listening port.
	 */
	public HttpServer(String address, int port)
	{
		this.address = address;
		this.port = port;
		this.converter = null;
	}

	/**
	 * Returns the optional listening IP address (local host used if null).
	 * @return The optional listening IP address (local host used if null).
	 */
	public String getAddress()
	{
		return this.address;
	}

	/**
	 * Sets the optional listening IP address (local host used if null).
	 * @param address The optional listening IP address (local host used if null).
	 */
	protected void setAddress(String address)
	{
		this.address = address;
	}

	/**
	 * Returns the listening port if specified.
	 * @return The listening port if specified.
	 */
	public int getPort()
	{
		return this.port;
	}

	/**
	 * Sets the listening port if specified.
	 * @param port The listening port if specified.
	 */
	protected void setPort(int port)
	{
		this.port = port;
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
			Call call = getConverter().toUniform(httpCall, getContext());
			handle(call);
			getConverter().commit(httpCall, call);
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, "Error while handling an HTTP server call: ", e
					.getMessage());
			logger.log(Level.INFO, "Error while handling an HTTP server call", e);
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
			this.converter = new HttpServerConverter();
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
