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

package com.noelios.restlet.ext.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.httpclient.ConnectMethod;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.TraceMethod;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.ParameterList;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Status;
import org.restlet.resource.Representation;

import com.noelios.restlet.http.HttpClientCall;

/**
 * HTTP client connector call based on JDK's java.net.HttpUrlConnection class.
 * @author Jerome Louvel (contact@noelios.com)
 */
public class HttpMethodCall extends HttpClientCall
{
	/** The associated HTTP client. */
	private HttpClientHelper clientHelper;

	/** The wrapped HTTP method. */
	private HttpMethod httpMethod;

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
	public HttpMethodCall(HttpClientHelper client, final String method, String requestUri,
			boolean hasEntity) throws IOException
	{
		super(method, requestUri);
		this.clientHelper = client;

		if (requestUri.startsWith("http"))
		{
			if (method.equalsIgnoreCase(Method.GET.getName()))
			{
				this.httpMethod = new GetMethod(requestUri);
			}
			else if (method.equalsIgnoreCase(Method.POST.getName()))
			{
				this.httpMethod = new PostMethod(requestUri);
			}
			else if (method.equalsIgnoreCase(Method.PUT.getName()))
			{
				this.httpMethod = new PutMethod(requestUri);
			}
			else if (method.equalsIgnoreCase(Method.HEAD.getName()))
			{
				this.httpMethod = new HeadMethod(requestUri);
			}
			else if (method.equalsIgnoreCase(Method.DELETE.getName()))
			{
				this.httpMethod = new DeleteMethod(requestUri);
			}
			else if (method.equalsIgnoreCase(Method.CONNECT.getName()))
			{
				HostConfiguration host = new HostConfiguration();
				host.setHost(new URI(requestUri, false));
				this.httpMethod = new ConnectMethod(host);
			}
			else if (method.equalsIgnoreCase(Method.OPTIONS.getName()))
			{
				this.httpMethod = new OptionsMethod(requestUri);
			}
			else if (method.equalsIgnoreCase(Method.TRACE.getName()))
			{
				this.httpMethod = new TraceMethod(requestUri);
			}
			else
			{
				this.httpMethod = new EntityEnclosingMethod(requestUri)
				{
					public String getName()
					{
						return method;
					}
				};
			}

			this.httpMethod.setFollowRedirects(this.clientHelper.isFollowRedirects());
			this.httpMethod.setDoAuthentication(false);

			this.responseHeadersAdded = false;
			setConfidential(this.httpMethod.getURI().getScheme().equalsIgnoreCase(
					Protocol.HTTPS.getSchemeName()));
		}
		else
		{
			throw new IllegalArgumentException(
					"Only HTTP or HTTPS resource URIs are allowed here");
		}
	}

	/**
	 * Returns the HTTP method.
	 * @return The HTTP method.
	 */
	public HttpMethod getHttpMethod()
	{
		return this.httpMethod;
	}

	/**
	 * Sends the request to the client. Commits the request line, headers and optional entity and 
	 * send them over the network. 
	 * @param request The high-level request.
	 * @return The result status.
	 */
	@Override
	public Status sendRequest(Request request) throws IOException
	{
		Status result = null;
		final Representation entity = request.getEntity();

		// Set the request headers
		for (Parameter header : getRequestHeaders())
		{
			getHttpMethod().setRequestHeader(header.getName(), header.getValue());
		}

		// For those method that accept enclosing entites, provide it
		if ((entity != null) && (getHttpMethod() instanceof EntityEnclosingMethod))
		{
			EntityEnclosingMethod eem = (EntityEnclosingMethod) getHttpMethod();
			eem.setRequestEntity(new RequestEntity()
			{
				public long getContentLength()
				{
					return entity.getSize();
				}

				public String getContentType()
				{
					return entity.getMediaType().toString();
				}

				public boolean isRepeatable()
				{
					return !entity.isTransient();
				}

				public void writeRequest(OutputStream os) throws IOException
				{
					entity.write(os);
				}
			});
		}

		// Ensure that the connections is active
		this.clientHelper.getHttpClient().executeMethod(getHttpMethod());

		// Now we can access the status code, this MUST happen after closing any open request stream.
		result = new Status(getStatusCode(), null, getReasonPhrase(), null);

		return result;
	}

	/**
	 * Returns the response address.<br/>
	 * Corresponds to the IP address of the responding server.
	 * @return The response address.
	 */
	public String getServerAddress()
	{
		try
		{
			return getHttpMethod().getURI().getHost();
		}
		catch (URIException e)
		{
			return null;
		}
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
			for (Header header : getHttpMethod().getResponseHeaders())
			{
				result.add(header.getName(), header.getValue());
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
		return getHttpMethod().getStatusCode();
	}

	/**
	 * Returns the response reason phrase.
	 * @return The response reason phrase.
	 */
	public String getReasonPhrase()
	{
		return getHttpMethod().getStatusText();
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
			result = getHttpMethod().getResponseBodyAsStream();
		}
		catch (IOException ioe)
		{
			result = null;
		}

		return result;
	}
}
