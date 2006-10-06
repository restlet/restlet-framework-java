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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ClientData;
import org.restlet.data.ConditionData;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.ParameterList;
import org.restlet.data.Status;

import com.noelios.restlet.impl.Factory;
import com.noelios.restlet.impl.util.CookieReader;
import com.noelios.restlet.impl.util.CookieUtils;
import com.noelios.restlet.impl.util.DateUtils;
import com.noelios.restlet.impl.util.PreferenceUtils;
import com.noelios.restlet.impl.util.SecurityUtils;

/**
 * Converter of high-level uniform calls into low-level HTTP client calls.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class HttpClientConverter
{
	/** Obtain a suitable logger. */
	private static Logger logger = Logger.getLogger(HttpClientConverter.class
			.getCanonicalName());

	/**
	 * Converts a low-level HTTP call into a high-level uniform call.
	 * @param client The HTTP client that will handle the call.
	 * @param request The high-level request.
	 * @param response The high-level response.
	 * @return A new high-level uniform call.
	 */
	public HttpClientCall toSpecific(HttpClient client, Request request, Response response)
	{
		// Create the low-level HTTP client call
		HttpClientCall result = client.create(request);
		
		// Add the request headers
		addRequestHeaders(result, request, response);

		return result;
	}

	/**
	 * Commits the changes to a handled HTTP client call back into the original uniform call. The default 
	 * implementation first invokes the "addResponseHeaders" the asks the "htppCall" to send the 
	 * response back to the client.  
	 * @param httpCall The original HTTP call.
	 * @param request The high-level request.
	 * @param response The high-level response.
	 */
	public void commit(HttpClientCall httpCall, Request request, Response response)
	{
		try
		{
         // Send the request to the client
			response.setStatus(httpCall.sendRequest(request.isInputAvailable() ? request.getInput() : null));

         // Get the server address
			response.getServer().setAddress(httpCall.getServerAddress());

			// Read the response headers
			readResponseHeaders(httpCall, response);

			// Set the output representation
			response.setOutput(httpCall.getResponseOutput());
		}
		catch (Exception e)
		{
			logger.log(Level.INFO, "Exception intercepted", e);
		}
	}

	/**
	 * Adds the request headers of a uniform call to a HTTP client call.  
	 * @param httpCall The HTTP client call.
	 * @param request The high-level request.
	 * @param response The high-level response.
	 */
	protected void addRequestHeaders(HttpClientCall httpCall, Request request, Response response)
	{
		ParameterList requestHeaders = httpCall.getRequestHeaders();

		// Manually add the host name and port when it is potentially different
		// from the one specified in the target resource reference.
		if(response.getServer().getName() != null)
		{
			String host;
			
			if(response.getServer().getPort() != null)
			{
				host = response.getServer().getName() + ':' + response.getServer().getPort();
			}
			else
			{
				host = response.getServer().getName();
			}

			requestHeaders.add(HttpConstants.HEADER_HOST, host);
		}
		
		// Add the user agent header
		if (request.getClient().getAgent() != null)
		{
			requestHeaders.add(HttpConstants.HEADER_USER_AGENT,
					request.getClient().getAgent());
		}
		else
		{
			requestHeaders.add(HttpConstants.HEADER_USER_AGENT,
					Factory.VERSION_HEADER);
		}

		// Add the conditions
		ConditionData condition = request.getCondition();
		if (condition.getMatch() != null)
		{
			StringBuilder value = new StringBuilder();

			for (int i = 0; i < condition.getMatch().size(); i++)
			{
				if (i > 0) value.append(", ");
				value.append(condition.getMatch().get(i).getName());
			}

			httpCall.getRequestHeaders()
					.add(HttpConstants.HEADER_IF_MATCH, value.toString());
		}

		if (condition.getModifiedSince() != null)
		{
			String imsDate = DateUtils.format(condition.getModifiedSince(),
					DateUtils.FORMAT_RFC_1123[0]);
			requestHeaders.add(HttpConstants.HEADER_IF_MODIFIED_SINCE, imsDate);
		}

		if (condition.getNoneMatch() != null)
		{
			StringBuilder value = new StringBuilder();

			for (int i = 0; i < condition.getNoneMatch().size(); i++)
			{
				if (i > 0) value.append(", ");
				value.append(condition.getNoneMatch().get(i).getName());
			}

			requestHeaders.add(HttpConstants.HEADER_IF_NONE_MATCH,
					value.toString());
		}

		if (condition.getUnmodifiedSince() != null)
		{
			String iusDate = DateUtils.format(condition.getUnmodifiedSince(),
					DateUtils.FORMAT_RFC_1123[0]);
			requestHeaders.add(HttpConstants.HEADER_IF_UNMODIFIED_SINCE,
					iusDate);
		}

		// Add the cookies
		if (request.getCookies().size() > 0)
		{
			String cookies = CookieUtils.format(request.getCookies());
			requestHeaders.add(HttpConstants.HEADER_COOKIE, cookies);
		}

		// Add the referrer header
		if (request.getReferrerRef() != null)
		{
			requestHeaders.add(HttpConstants.HEADER_REFERRER,
					request.getReferrerRef().toString());
		}

		// Add the preferences
		ClientData client = request.getClient();
		if (client.getAcceptedMediaTypes().size() > 0)
		{
			try
			{
				requestHeaders.add(HttpConstants.HEADER_ACCEPT,
						PreferenceUtils.format(client.getAcceptedMediaTypes()));
			}
			catch (IOException ioe)
			{
				logger.log(Level.WARNING, "Unable to format the HTTP Accept header", ioe);
			}
		}
		else
		{
			requestHeaders.add(HttpConstants.HEADER_ACCEPT,
					MediaType.ALL.getName());
		}

		if (client.getAcceptedCharacterSets().size() > 0)
		{
			try
			{
				requestHeaders.add(HttpConstants.HEADER_ACCEPT_CHARSET,
						PreferenceUtils.format(client.getAcceptedCharacterSets()));
			}
			catch (IOException ioe)
			{
				logger.log(Level.WARNING, "Unable to format the HTTP Accept header", ioe);
			}
		}

		if (client.getAcceptedEncodings().size() > 0)
		{
			try
			{
				requestHeaders.add(HttpConstants.HEADER_ACCEPT_ENCODING,
						PreferenceUtils.format(client.getAcceptedEncodings()));
			}
			catch (IOException ioe)
			{
				logger.log(Level.WARNING, "Unable to format the HTTP Accept header", ioe);
			}
		}

		if (client.getAcceptedLanguages().size() > 0)
		{
			try
			{
				requestHeaders.add(HttpConstants.HEADER_ACCEPT_LANGUAGE,
						PreferenceUtils.format(client.getAcceptedLanguages()));
			}
			catch (IOException ioe)
			{
				logger.log(Level.WARNING, "Unable to format the HTTP Accept header", ioe);
			}
		}

		// Add the security
		ChallengeResponse challengeResponse = request.getChallengeResponse();
		if (challengeResponse != null)
		{
			requestHeaders.add(HttpConstants.HEADER_AUTHORIZATION,
					SecurityUtils.format(challengeResponse));
		}

		// Send the input representation
		if (request.getInput() != null)
		{
			if (request.getInput().getMediaType() != null)
			{
				requestHeaders.add(HttpConstants.HEADER_CONTENT_TYPE,
						request.getInput().getMediaType().toString());
			}

			if (request.getInput().getEncoding() != null)
			{
				requestHeaders.add(HttpConstants.HEADER_CONTENT_ENCODING,
						request.getInput().getEncoding().toString());
			}

			if (request.getInput().getLanguage() != null)
			{
				requestHeaders.add(HttpConstants.HEADER_CONTENT_LANGUAGE,
						request.getInput().getLanguage().toString());
			}
		}

		
		// Add user-defined extension headers
		ParameterList additionalHeaders = (ParameterList)request.getAttributes().get("org.restlet.http.headers");
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
					requestHeaders.add(param);
				}
			}
		}
	}

	/**
	 * Reads the response headers of a handled HTTP client call to update the original uniform call.  
	 * @param httpCall The handled HTTP client call.
	 * @param response The high-level response.
	 */
	protected void readResponseHeaders(HttpClientCall httpCall, Response response)
	{
		try
		{
			// Put the response headers in the call's attributes map
			response.getAttributes().put("org.restlet.http.headers", httpCall.getResponseHeaders());
			
			// Read info from headers
			for (Parameter header : httpCall.getResponseHeaders())
			{
				if (header.getName().equalsIgnoreCase(HttpConstants.HEADER_LOCATION))
				{
					response.setRedirectRef(header.getValue());
				}
				else if ((header.getName().equalsIgnoreCase(HttpConstants.HEADER_SET_COOKIE))
						|| (header.getName().equalsIgnoreCase(HttpConstants.HEADER_SET_COOKIE2)))
				{
					try
					{
						CookieReader cr = new CookieReader(header.getValue());
						response.getCookieSettings().add(cr.readCookieSetting());
					}
					catch (Exception e)
					{
						logger.log(Level.WARNING,
								"Error during cookie setting parsing. Header: "
										+ header.getValue(), e);
					}
				}
				else if (header.getName().equalsIgnoreCase(
						HttpConstants.HEADER_WWW_AUTHENTICATE))
				{
					ChallengeRequest request = SecurityUtils.parseRequest(header.getValue());
					response.setChallengeRequest(request);
				}
				else if (header.getName().equalsIgnoreCase(HttpConstants.HEADER_SERVER))
				{
					response.getServer().setAgent(header.getValue());
				}
			}
		}
		catch (Exception e)
		{
			logger.log(Level.FINE,
					"An error occured during the processing of the HTTP response.", e);
			response.setStatus(new Status(Status.CONNECTOR_ERROR_INTERNAL,
					"Unable to process the response. " + e.getMessage()));
		}
	}

}
