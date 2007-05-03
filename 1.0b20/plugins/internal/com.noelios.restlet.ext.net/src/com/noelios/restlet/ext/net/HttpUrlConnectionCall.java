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

package com.noelios.restlet.ext.net;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.logging.Level;

import javax.net.ssl.HttpsURLConnection;

import org.restlet.data.Parameter;
import org.restlet.data.ParameterList;
import org.restlet.data.Request;
import org.restlet.data.Status;
import org.restlet.resource.Representation;

import com.noelios.restlet.http.HttpClientCall;

/**
 * HTTP client connector call based on JDK's java.net.HttpUrlConnection class.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class HttpUrlConnectionCall extends HttpClientCall
{
	/** The associated HTTP client. */
	private HttpClientHelper clientHelper;

	/** The wrapped HTTP URL connection. */
	private HttpURLConnection connection;

	/** Indicates if the response headers were added. */
	private boolean responseHeadersAdded;

	/**
	 * Constructor.
	 * @param client The client connector.
	 * @param method The method name.
	 * @param requestUri The request URI.
	 * @param hasEntity Indicates if the call will have an entity to send to the server.
	 * @throws IOException
	 */
	public HttpUrlConnectionCall(HttpClientHelper client, String method,
			String requestUri, boolean hasEntity) throws IOException
	{
		super(method, requestUri);
		this.clientHelper = client;

		if (requestUri.startsWith("http"))
		{
			URL url = new URL(requestUri);
			this.connection = (HttpURLConnection) url.openConnection();
			this.connection.setConnectTimeout(client.getConnectTimeout());
			this.connection.setReadTimeout(client.getReadTimeout());
			this.connection.setAllowUserInteraction(client.isAllowUserInteraction());
			this.connection.setDoOutput(hasEntity);
			this.connection.setInstanceFollowRedirects(client.isFollowRedirects());
			this.connection.setUseCaches(client.isUseCaches());
			this.responseHeadersAdded = false;
			setConfidential(this.connection instanceof HttpsURLConnection);
		}
		else
		{
			throw new IllegalArgumentException(
					"Only HTTP or HTTPS resource URIs are allowed here");
		}
	}

	/**
	 * Returns the connection.
	 * @return The connection.
	 */
	public HttpURLConnection getConnection()
	{
		return this.connection;
	}

	/**
	 * Sends the request to the client. Commits the request line, headers and optional entity and 
	 * send them over the network. 
	 * @param request The high-level request.
	 * @return The result status.
	 */
	public Status sendRequest(Request request) throws IOException
	{
		Status result = null;

		try
		{
			Representation entity = request.getEntity();
			
			if (entity != null)
			{
				// Adjust the streaming mode
				if (entity.getSize() > 0)
				{
					// The size of the entity is known in advance
					getConnection().setFixedLengthStreamingMode((int) entity.getSize());
				}
				else
				{
					// The size of the entity is not known in advance
					if (this.clientHelper.getChunkLength() >= 0)
					{
						// Use chunked encoding
						getConnection().setChunkedStreamingMode(
								this.clientHelper.getChunkLength());
					}
					else
					{
						// Use entity buffering to determine the content length
					}
				}
			}

			// Set the request method
			getConnection().setRequestMethod(getMethod());

			// Set the request headers
			for (Parameter header : getRequestHeaders())
			{
				getConnection().addRequestProperty(header.getName(), header.getValue());
			}

			// Ensure that the connections is active
			getConnection().connect();

			// Send the optional entity
			result = super.sendRequest(request);
		}
		catch (ConnectException ce)
		{
			this.clientHelper.getLogger().log(Level.FINE,
					"An error occured during the connection to the remote HTTP server.", ce);
			result = new Status(Status.CONNECTOR_ERROR_CONNECTION,
					"Unable to connect to the remote server. " + ce.getMessage());
		}
		catch (SocketTimeoutException ste)
		{
			this.clientHelper
					.getLogger()
					.log(
							Level.FINE,
							"An timeout error occured during the communication with the remote HTTP server.",
							ste);
			result = new Status(Status.CONNECTOR_ERROR_COMMUNICATION,
					"Unable to complete the HTTP call due to a communication timeout error. "
							+ ste.getMessage());
		}
		catch (FileNotFoundException fnfe)
		{
			this.clientHelper.getLogger().log(Level.FINE,
					"An unexpected error occured during the sending of the HTTP request.",
					fnfe);
			result = new Status(Status.CONNECTOR_ERROR_INTERNAL,
					"Unable to find a local file for sending. " + fnfe.getMessage());
		}
		catch (IOException ioe)
		{
			this.clientHelper.getLogger().log(Level.FINE,
					"An error occured during the communication with the remote HTTP server.",
					ioe);
			result = new Status(Status.CONNECTOR_ERROR_COMMUNICATION,
					"Unable to complete the HTTP call due to a communication error with the remote server. "
							+ ioe.getMessage());
		}
		catch (Exception e)
		{
			this.clientHelper.getLogger().log(Level.FINE,
					"An unexpected error occured during the sending of the HTTP request.", e);
			result = new Status(Status.CONNECTOR_ERROR_INTERNAL,
					"Unable to send the HTTP request. " + e.getMessage());
		}

		return result;
	}

	/**
	 * Returns the request entity stream if it exists.
	 * @return The request entity stream if it exists.
	 */
	public OutputStream getRequestStream()
	{
		try
		{
			return getConnection().getOutputStream();
		}
		catch (IOException ioe)
		{
			return null;
		}
	}

	/**
	 * Returns the response address.<br/>
	 * Corresponds to the IP address of the responding server.
	 * @return The response address.
	 */
	public String getServerAddress()
	{
		return getConnection().getURL().getHost();
	}

	/**
	 * Returns the modifiable list of response headers.
	 * @return The modifiable list of response headers.
	 */
	public ParameterList getResponseHeaders()
	{
		ParameterList result = super.getResponseHeaders();

		if (!this.responseHeadersAdded)
		{
			// Read the response headers
			int i = 1;
			String headerName = getConnection().getHeaderFieldKey(i);
			String headerValue = getConnection().getHeaderField(i);
			while (headerName != null)
			{
				result.add(headerName, headerValue);
				i++;
				headerName = getConnection().getHeaderFieldKey(i);
				headerValue = getConnection().getHeaderField(i);
			}

			this.responseHeadersAdded = true;
		}

		return result;
	}

	/**
	 * Returns the response status code.
	 * @return The response status code.
	 */
	public int getStatusCode()
	{
		try
		{
			return getConnection().getResponseCode();
		}
		catch (IOException e)
		{
			return -1;
		}
	}

	/**
	 * Returns the response reason phrase.
	 * @return The response reason phrase.
	 */
	public String getReasonPhrase()
	{
		try
		{
			return getConnection().getResponseMessage();
		}
		catch (IOException e)
		{
			return null;
		}
	}

	/**
	 * Returns the response stream if it exists.
	 * @return The response stream if it exists.
	 */
	public InputStream getResponseStream()
	{
		InputStream result = null;

		try
		{
			result = getConnection().getInputStream();
		}
		catch (IOException ioe)
		{
			result = getConnection().getErrorStream();
		}

		if (result == null)
		{
			// Maybe an error stream is available instead
			result = getConnection().getErrorStream();
		}

		return result;
	}
}
