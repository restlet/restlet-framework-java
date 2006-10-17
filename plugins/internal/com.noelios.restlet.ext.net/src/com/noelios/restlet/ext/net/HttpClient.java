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

import java.io.IOException;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.data.Request;

import com.noelios.restlet.impl.http.HttpClientCall;

/**
 * HTTP client connector using the HttpUrlConnectionCall. Here is the list of parameters that are supported:
 * <table>
 * 	<tr>
 * 		<th>Parameter name</th>
 * 		<th>Value type</th>
 * 		<th>Default value</th>
 * 		<th>Description</th>
 * 	</tr>
 * 	<tr>
 * 		<td>chunkLength</td>
 * 		<td>int</td>
 * 		<td>0 (uses HttpURLConnection's default)</td>
 * 		<td>The chunk-length when using chunked encoding streaming mode for response entities. A value of -1 means chunked encoding is disabled for response entities.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>followRedirects</td>
 * 		<td>boolean</td>
 * 		<td>false</td>
 * 		<td>If true, the protocol will automatically follow redirects. If false, the protocol will not automatically follow redirects.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>allowUserInteraction</td>
 * 		<td>boolean</td>
 * 		<td>false</td>
 * 		<td>If true, this URL is being examined in a context in which it makes sense to allow user interactions such as popping up an authentication dialog.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>useCaches</td>
 * 		<td>boolean</td>
 * 		<td>false</td>
 * 		<td>If true, the protocol is allowed to use caching whenever it can.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>connectTimeout</td>
 * 		<td>int</td>
 * 		<td>0</td>
 * 		<td>Sets a specified timeout value, in milliseconds, to be used when opening a communications link to the resource referenced. 0 means infinite timeout.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>readTimeout</td>
 * 		<td>int</td>
 * 		<td>0</td>
 * 		<td>Sets the read timeout to a specified timeout, in milliseconds. A timeout of zero is interpreted as an infinite timeout.</td>
 * 	</tr>
 * </table>
 * @see <a href="http://java.sun.com/j2se/1.5.0/docs/guide/net/index.html">Networking Features</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class HttpClient extends com.noelios.restlet.impl.http.HttpClient
{
	/**
	 * Constructor.
	 * @param context The context.
	 */
	public HttpClient(Context context)
	{
		super(context);
		getProtocols().add(Protocol.HTTP);
		getProtocols().add(Protocol.HTTPS);
	}

	/**
	 * Creates a low-level HTTP client call from a high-level uniform call.
	 * @param request The high-level request.
	 * @return A low-level HTTP client call.
	 */
	public HttpClientCall create(Request request)
	{
		HttpClientCall result = null;
		
		try
		{
			result = new HttpUrlConnectionCall(this, request.getMethod().toString(), 
					request.getResourceRef().toString(), request.isEntityAvailable());
		}
		catch (IOException ioe)
		{
			getLogger().log(Level.WARNING, "Unable to create the HTTP client call", ioe);
		}
		
		return result;
	}

	/**
	 * Returns the chunk-length when using chunked encoding streaming mode for response entities. 
	 * A value of -1 means chunked encoding is disabled for response entities.
	 * @return The chunk-length when using chunked encoding streaming mode for response entities.
	 */
	public int getChunkLength()
	{
		return Integer.parseInt(getContext().getParameters().getFirstValue("chunkLength",
				"0"));
	}

	/**
	 * Indicates if the protocol will automatically follow redirects. 
	 * @return True if the protocol will automatically follow redirects.
	 */
	public boolean isFollowRedirects()
	{
		return Boolean.parseBoolean(getContext().getParameters().getFirstValue(
				"followRedirects", "false"));
	}

	/**
	 * Indicates if this URL is being examined in a context in which it makes sense to allow user interactions 
	 * such as popping up an authentication dialog. 
	 * @return True if it makes sense to allow user interactions.
	 */
	public boolean isAllowUserInteraction()
	{
		return Boolean.parseBoolean(getContext().getParameters().getFirstValue(
				"allowUserInteraction", "false"));
	}

	/**
	 * Indicates if the protocol is allowed to use caching whenever it can.
	 * @return True if the protocol is allowed to use caching whenever it can.
	 */
	public boolean isUseCaches()
	{
		return Boolean.parseBoolean(getContext().getParameters().getFirstValue("useCaches",
				"false"));
	}

	/**
	 * Returns the timeout value, in milliseconds, to be used when opening a communications link to 
	 * the resource referenced. 0 means infinite timeout.
	 * @return The connection timeout value.
	 */
	public int getConnectTimeout()
	{
		return Integer.parseInt(getContext().getParameters().getFirstValue(
				"connectTimeout", "0"));
	}

	/**
	 * Returns the read timeout value. A timeout of zero is interpreted as an infinite timeout.
	 * @return The read timeout value.
	 */
	public int getReadTimeout()
	{
		return Integer.parseInt(getContext().getParameters().getFirstValue("readTimeout",
				"0"));
	}
}
