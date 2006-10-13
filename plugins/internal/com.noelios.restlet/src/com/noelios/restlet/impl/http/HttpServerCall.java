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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Logger;

import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Representation;

import com.noelios.restlet.data.InputRepresentation;
import com.noelios.restlet.data.ReadableRepresentation;

/**
 * Abstract HTTP server connector call.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class HttpServerCall extends HttpCall
{
	/** Obtain a suitable logger. */
	private static Logger logger = Logger.getLogger(HttpRequest.class
			.getCanonicalName());

	/** Indicates if the "host" header was already parsed. */
	private boolean hostParsed;

	/**
	 * Constructor.
	 */
	public HttpServerCall()
	{
		this.hostParsed = false;
	}

	/**
	 * Returns the request entity channel if it exists.
	 * @return The request entity channel if it exists.
	 */
	public abstract ReadableByteChannel getRequestChannel();

	/**
	 * Returns the request entity stream if it exists.
	 * @return The request entity stream if it exists.
	 */
	public abstract InputStream getRequestStream();

	/**
	 * Returns the response channel if it exists.
	 * @return The response channel if it exists.
	 */
	public abstract WritableByteChannel getResponseChannel();

	/**
	 * Returns the response stream if it exists.
	 * @return The response stream if it exists.
	 */
	public abstract OutputStream getResponseStream();

	/**
	 * Returns the request input representation if available.
	 * @return The request input representation if available.
	 */
	public Representation getRequestInput()
	{
		Representation result = null;
		InputStream requestStream = getRequestStream();
		ReadableByteChannel requestChannel = getRequestChannel();

		if ((requestStream != null || requestChannel != null))
		{
			// Extract the header values
			Encoding contentEncoding = null;
			Language contentLanguage = null;
			MediaType contentType = null;
			long contentLength = -1L;

			for (Parameter header : getRequestHeaders())
			{
				if (header.getName().equalsIgnoreCase(HttpConstants.HEADER_CONTENT_ENCODING))
				{
					contentEncoding = new Encoding(header.getValue());
				}
				else if (header.getName().equalsIgnoreCase(
						HttpConstants.HEADER_CONTENT_LANGUAGE))
				{
					contentLanguage = new Language(header.getValue());
				}
				else if (header.getName().equalsIgnoreCase(HttpConstants.HEADER_CONTENT_TYPE))
				{
					contentType = new MediaType(header.getValue());
				}
				else if (header.getName().equalsIgnoreCase(
						HttpConstants.HEADER_CONTENT_LENGTH))
				{
					contentLength = Long.parseLong(header.getValue());
				}
			}

			if (requestStream != null)
			{
				result = new InputRepresentation(requestStream, contentType, contentLength);
			}
			else if (requestChannel != null)
			{
				result = new ReadableRepresentation(requestChannel, contentType,
						contentLength);
			}

			result.setEncoding(contentEncoding);
			result.setLanguage(contentLanguage);
		}

		return result;
	}

	/** 
	 * Returns the server name.
	 * @return The server name.
	 */
	public String getServerName()
	{
		if (!hostParsed) parseHost();
		return super.getServerName();
	}

	/** 
	 * Returns the server port.
	 * @return The server port.
	 */
	public Integer getServerPort()
	{
		if (!hostParsed) parseHost();
		return super.getServerPort();
	}

	/**
	 * Parses the "host" header to set the server host and port properties.
	 */
	private void parseHost()
	{
		String host = getRequestHeaders().getFirstValue(HttpConstants.HEADER_HOST);

		if (host != null)
		{
			int colonIndex = host.indexOf(':');

			if (colonIndex != -1)
			{
				super.setServerName(host.substring(0, colonIndex));
				super.setServerPort(Integer.valueOf(host.substring(colonIndex + 1)));
			}
			else
			{
				super.setServerName(host);

				if (isConfidential())
				{
					super.setServerPort(Protocol.HTTPS.getDefaultPort());
				}
				else
				{
					super.setServerPort(Protocol.HTTP.getDefaultPort());
				}
			}
		}
		else
		{
			logger.warning("Couldn't find the mandatory \"Host\" HTTP header.");
		}

		this.hostParsed = true;
	}

	/**
	 * Sends the response back to the client. Commits the status, headers and optional output and 
	 * send them over the network. The default implementation only writes the output representation 
	 * on the reponse stream or channel. Subclasses will probably also copy the response headers and 
	 * status.
	 * @param output The optional output representation to send.
	 */
	public void sendResponse(Representation output) throws IOException
	{
		if ((output != null) && !getMethod().equals(Method.HEAD.getName()))
		{
			// Send the output to the client
			if (getResponseStream() != null)
			{
				output.write(getResponseStream());
			}
			else if (getResponseChannel() != null)
			{
				output.write(getResponseChannel());
			}
		}

		if (getResponseStream() != null)
		{
			getResponseStream().flush();
		}
	}

}
