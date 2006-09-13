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
import org.restlet.connector.Client;

/**
 * Base HTTP client connector.
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
 * 		<td>com.noelios.restlet.connector.HttpClientConverter</td>
 * 		<td>The qualified class name of the converter from uniform calls to HTTP client calls.</td>
 * 	</tr>
 * </table>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class HttpClient extends Client
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger(HttpClient.class.getCanonicalName());

   /** The converter from uniform calls to HTTP calls. */
   private HttpClientConverter converter;

	/** The qualified class name of the call converter. */
	private String converterName;

   /**
    * Constructor.
    */
   public HttpClient()
   {
      this.converter = null;
		this.converterName = null;
   }

   /**
    * Creates a low-level HTTP client call from a high-level uniform call.
    * @param call The high-level uniform call.
    * @return A low-level HTTP client call.
    */
   public abstract HttpClientCall create(Call call);

   /**
    * Handles a call.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
      try
      {
         HttpClientCall httpCall = getConverter().toSpecific(this, call);
         getConverter().commit(httpCall, call);
      }
      catch (Exception e)
      {
         logger.log(Level.WARNING, "Error while handling an HTTP client call: ", e.getMessage());
         logger.log(Level.INFO, "Error while handling an HTTP client call", e);
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
			if (getConverterName() != null)
			{
				try
				{
					// Load the converter class using the given class name
					Class converterClass = Class.forName(getConverterName());
					this.converter = (HttpClientConverter) converterClass.newInstance();
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
				this.converter = new HttpClientConverter();
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

	/**
	 * Returns the qualified class name of the call converter.
	 * @return the qualified class name of the call converter.
	 */
	public String getConverterName()
	{
		if (this.converterName == null)
		{
			this.converterName = getContext().getParameters().getFirstValue("converter",
					HttpClientConverter.class.getCanonicalName());
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
