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

package com.noelios.restlet.connector;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Call;
import org.restlet.connector.Server;

/**
 * Base HTTP server connector.
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
 * 		<td>com.noelios.restlet.connector.HttpServerConverter</td>
 * 		<td>The qualified class name of the converter from HTTP server calls to uniform calls.</td>
 * 	</tr>
 * </table>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class HttpServer extends Server
{
	/** Obtain a suitable logger. */
	private static Logger logger = Logger.getLogger(HttpServer.class.getCanonicalName());

	/** The listening address if specified. */
	private String address;

	/** The listening port if specified. */
	private int port;

	/** The converter from HTTP calls to uniform calls. */
	private HttpServerConverter converter;

	/** The qualified class name of the call converter. */
	private String converterName;

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
		this.converterName = null;
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
			if (this.converterName != null)
			{
				try
				{
					// Load the converter class using the given class name
					Class converterClass = Class.forName(converterName);
					this.converter = (HttpServerConverter) converterClass.newInstance();
				}
				catch (ClassNotFoundException e)
				{
					getContext().getLogger().log(
							Level.WARNING,
							"Couldn't find the converter class. Please check that your classpath includes "
									+ converterName, e);
				}
				catch (InstantiationException e)
				{
					getContext()
							.getLogger()
							.log(
									Level.WARNING,
									"Couldn't instantiate the converter class. Please check this class has an empty constructor "
											+ converterName, e);
				}
				catch (IllegalAccessException e)
				{
					getContext()
							.getLogger()
							.log(
									Level.WARNING,
									"Couldn't instantiate the converter class. Please check that you have to proper access rights to "
											+ converterName, e);
				}
			}

			if (this.converter == null)
			{
				getContext().getLogger().log(Level.WARNING,
						"Instantiating the default HTTP call converter");
				this.converter = new HttpServerConverter();
			}
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

	/**
	 * Returns the qualified class name of the call converter.
	 * @return the qualified class name of the call converter.
	 */
	public String getConverterName()
	{
		if (this.converterName == null)
		{
			this.converterName = getContext().getParameters().getFirstValue("converter",
					HttpServerConverter.class.getCanonicalName());
		}

		return this.converterName;
	}

	/**
	 * Sets the qualified class name of the call converter.
	 * @param converterName The qualified class name of the call converter.
	 */
	public void setConverterName(String converterName)
	{
		this.converterName = converterName;
	}

}
