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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CookieSetting;
import org.restlet.data.Encoding;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.ParameterList;
import org.restlet.data.Representation;
import org.restlet.data.Status;

import com.noelios.restlet.impl.util.CookieUtils;
import com.noelios.restlet.impl.util.SecurityUtils;

/**
 * Converter of low-level HTTP server calls into high-level uniform calls.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class HttpServerConverter
{
	/** Obtain a suitable logger. */
	private static Logger logger = Logger.getLogger(HttpServerConverter.class
			.getCanonicalName());

	/**
	 * Converts a low-level HTTP call into a high-level uniform call.
	 * @param httpCall The low-level HTTP call.
	 * @param context The context of the server connector.
	 * @return A new high-level uniform call.
	 */
	public Request toUniform(HttpServerCall httpCall, Context context)
	{
		Request result = new HttpRequest(context, httpCall);
		result.getAttributes().put("org.restlet.http.headers", httpCall.getRequestHeaders());
		return result;
	}
	
	/**
	 * Commits the changes to a handled uniform call back into the original HTTP call. The default 
	 * implementation first invokes the "addResponseHeaders" the asks the "htppCall" to send the 
	 * response back to the client.  
	 * @param httpCall The original HTTP call.
	 * @param response The high-level response.
	 */
	public void commit(HttpServerCall httpCall, Response response)
	{
		try
		{
			// Add the response headers
			addResponseHeaders(httpCall, response);
			
			// Send the response to the client
			httpCall.sendResponse(response.getOutput());
		}
		catch (Exception e)
		{
			logger.log(Level.INFO, "Exception intercepted", e);
			httpCall.setStatusCode(500);
			httpCall.setReasonPhrase("An unexpected exception occured");
		}
	}
	
	/**
	 * Adds the response headers for the handled uniform call.  
	 * @param httpCall The original HTTP call.
	 * @param response The response returned.
	 */
	protected void addResponseHeaders(HttpServerCall httpCall, Response response)
	{
		try
		{
			// Add all the necessary response headers
			ParameterList responseHeaders = httpCall.getResponseHeaders();

			if(response.getStatus().equals(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED) ||
					response.getRequest().getMethod().equals(Method.PUT))
			{
				// Format the "Allow" header
				StringBuilder sb = new StringBuilder();
				boolean first = true;
				for(Method method : response.getAllowedMethods())
				{
					if(first) 
					{
						first = false;
					}
					else 
					{
						sb.append(", ");
					}
					
					sb.append(method.getName());
				}

				responseHeaders.add(HttpConstants.HEADER_ALLOW, sb.toString());
			}
			
			// Add the cookie settings
			List<CookieSetting> cookies = response.getCookieSettings();
			for (int i = 0; i < cookies.size(); i++)
			{
				responseHeaders.add(HttpConstants.HEADER_SET_COOKIE, CookieUtils
						.format(cookies.get(i)));
			}

			// Set the redirection URI
			if (response.getRedirectRef() != null)
			{
				responseHeaders.add(HttpConstants.HEADER_LOCATION, response
						.getRedirectRef().toString());
			}

			// Set the security data
			if (response.getChallengeRequest() != null)
			{
				responseHeaders.add(HttpConstants.HEADER_WWW_AUTHENTICATE, SecurityUtils
						.format(response.getChallengeRequest()));
			}

			// Set the server name again
			httpCall.getResponseHeaders().add(HttpConstants.HEADER_SERVER,
					response.getServer().getAgent());

			// Set the status code in the response
			if (response.getStatus() != null)
			{
				httpCall.setStatusCode(response.getStatus().getCode());
				httpCall.setReasonPhrase(response.getStatus().getDescription());
			}

			// If an output was set during the call, copy it to the output stream;
			if (response.getOutput() != null)
			{
				Representation output = response.getOutput();

				if (output.getExpirationDate() != null)
				{
					responseHeaders.add(HttpConstants.HEADER_EXPIRES, httpCall.formatDate(output
							.getExpirationDate(), false));
				}

				if ((output.getEncoding() != null)
						&& (!output.getEncoding().equals(Encoding.IDENTITY)))
				{
					responseHeaders.add(HttpConstants.HEADER_CONTENT_ENCODING, output
							.getEncoding().getName());
				}

				if (output.getLanguage() != null)
				{
					responseHeaders.add(HttpConstants.HEADER_CONTENT_LANGUAGE, output
							.getLanguage().getName());
				}

				if (output.getMediaType() != null)
				{
					StringBuilder contentType = new StringBuilder(output.getMediaType()
							.getName());

					if (output.getCharacterSet() != null)
					{
						// Specify the character set parameter
						contentType.append("; charset=").append(
								output.getCharacterSet().getName());
					}

					responseHeaders.add(HttpConstants.HEADER_CONTENT_TYPE, contentType
							.toString());
				}

				if (output.getModificationDate() != null)
				{
					responseHeaders.add(HttpConstants.HEADER_LAST_MODIFIED, httpCall.formatDate(output
							.getModificationDate(), false));
				}

				if (output.getTag() != null)
				{
					responseHeaders.add(HttpConstants.HEADER_ETAG, output.getTag().getName());
				}

				if (response.getOutput().getSize() != Representation.UNKNOWN_SIZE)
				{
					responseHeaders.add(HttpConstants.HEADER_CONTENT_LENGTH, Long
							.toString(response.getOutput().getSize()));
				}

				if (response.getOutput().getIdentifier() != null)
				{
					responseHeaders.add(HttpConstants.HEADER_CONTENT_LOCATION, response
							.getOutput().getIdentifier().toString());
				}
			}
			
			// Add user-defined extension headers
			ParameterList additionalHeaders = (ParameterList)response.getAttributes().get("org.restlet.http.headers");
			if(additionalHeaders != null)
			{
				for(Parameter param : additionalHeaders)
				{
					if( 	param.getName().equalsIgnoreCase(HttpConstants.HEADER_ACCEPT) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_ACCEPT_CHARSET) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_ACCEPT_ENCODING) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_ACCEPT_LANGUAGE) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_ACCEPT_RANGES) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_AGE) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_ALLOW) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_AUTHORIZATION) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_CACHE_CONTROL) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_CONNECTION) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_CONTENT_ENCODING) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_CONTENT_LANGUAGE) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_CONTENT_LENGTH) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_CONTENT_LOCATION) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_CONTENT_MD5) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_CONTENT_RANGE) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_CONTENT_TYPE) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_COOKIE) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_DATE) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_ETAG) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_EXPECT) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_EXPIRES) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_FROM) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_HOST) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_IF_MATCH) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_IF_MODIFIED_SINCE) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_IF_NONE_MATCH) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_IF_RANGE) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_IF_UNMODIFIED_SINCE) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_LAST_MODIFIED) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_LOCATION) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_MAX_FORWARDS) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_PRAGMA) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_PROXY_AUTHENTICATE) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_PROXY_AUTHORIZATION) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_RANGE) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_REFERRER) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_RETRY_AFTER) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_SERVER) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_SET_COOKIE) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_SET_COOKIE2) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_TRAILER) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_TRANSFER_ENCODING) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_TRANSFER_EXTENSION) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_UPGRADE) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_USER_AGENT) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_VARY) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_VIA) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_WARNING) ||
							param.getName().equalsIgnoreCase(HttpConstants.HEADER_WWW_AUTHENTICATE)
							)
					{
						// Standard headers can't be overriden
						logger.warning("Addition of the standard header \"" + param.getName() + "\" is not allowed.");
					}
					else
					{
						responseHeaders.add(param);
					}
				}
			}
		}
		catch (Exception e)
		{
			logger.log(Level.INFO, "Exception intercepted while adding the response headers", e);
			httpCall.setStatusCode(Status.SERVER_ERROR_INTERNAL.getCode());
			httpCall.setReasonPhrase(Status.SERVER_ERROR_INTERNAL.getDescription());
		}
	}

}
