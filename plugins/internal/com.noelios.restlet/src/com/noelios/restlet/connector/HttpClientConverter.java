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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Call;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ClientData;
import org.restlet.data.ConditionData;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Status;

import com.noelios.restlet.spi.Factory;
import com.noelios.restlet.util.CookieReader;
import com.noelios.restlet.util.CookieUtils;
import com.noelios.restlet.util.DateUtils;
import com.noelios.restlet.util.PreferenceUtils;
import com.noelios.restlet.util.SecurityUtils;

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
	 * @param call The uniform call to handle
	 * @return A new high-level uniform call.
	 */
	public HttpClientCall toSpecific(HttpClient client, Call call)
	{
		// Create the low-level HTTP client call
		HttpClientCall result = client.create(call);
		
		// Add the request headers
		addRequestHeaders(result, call);

		return result;
	}

	/**
	 * Commits the changes to a handled HTTP client call back into the original uniform call. The default 
	 * implementation first invokes the "addResponseHeaders" the asks the "htppCall" to send the 
	 * response back to the client.  
	 * @param httpCall The original HTTP call.
	 * @param call The handled uniform call.
	 */
	public void commit(HttpClientCall httpCall, Call call)
	{
		try
		{
         // Send the request to the client
         call.setStatus(httpCall.sendRequest(call.isInputAvailable() ? call.getInput() : null));

         // Get the server address
			call.getServer().setAddress(httpCall.getResponseAddress());

			// Read the response headers
			readResponseHeaders(httpCall, call);

			// Set the output representation
			call.setOutput(httpCall.getResponseOutput());
		}
		catch (Exception e)
		{
			logger.log(Level.INFO, "Exception intercepted", e);
		}
	}

	/**
	 * Adds the request headers of a uniform call to a HTTP client call.  
	 * @param httpCall The HTTP client call.
	 * @param call The uniform call.
	 */
	protected void addRequestHeaders(HttpClientCall httpCall, Call call)
	{
		// Add the user agent header
		if (call.getClient().getName() != null)
		{
			httpCall.getRequestHeaders().add(HttpConstants.HEADER_USER_AGENT,
					call.getClient().getName());
		}
		else
		{
			httpCall.getRequestHeaders().add(HttpConstants.HEADER_USER_AGENT,
					Factory.VERSION_HEADER);
		}

		// Add the conditions
		ConditionData condition = call.getCondition();
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
			httpCall.getRequestHeaders()
					.add(HttpConstants.HEADER_IF_MODIFIED_SINCE, imsDate);
		}

		if (condition.getNoneMatch() != null)
		{
			StringBuilder value = new StringBuilder();

			for (int i = 0; i < condition.getNoneMatch().size(); i++)
			{
				if (i > 0) value.append(", ");
				value.append(condition.getNoneMatch().get(i).getName());
			}

			httpCall.getRequestHeaders().add(HttpConstants.HEADER_IF_NONE_MATCH,
					value.toString());
		}

		if (condition.getUnmodifiedSince() != null)
		{
			String iusDate = DateUtils.format(condition.getUnmodifiedSince(),
					DateUtils.FORMAT_RFC_1123[0]);
			httpCall.getRequestHeaders().add(HttpConstants.HEADER_IF_UNMODIFIED_SINCE,
					iusDate);
		}

		// Add the cookies
		if (call.getCookies().size() > 0)
		{
			String cookies = CookieUtils.format(call.getCookies());
			httpCall.getRequestHeaders().add(HttpConstants.HEADER_COOKIE, cookies);
		}

		// Add the referrer header
		if (call.getReferrerRef() != null)
		{
			httpCall.getRequestHeaders().add(HttpConstants.HEADER_REFERRER,
					call.getReferrerRef().toString());
		}

		// Add the preferences
		ClientData client = call.getClient();
		if (client.getAcceptedMediaTypes().size() > 0)
		{
			try
			{
				httpCall.getRequestHeaders().add(HttpConstants.HEADER_ACCEPT,
						PreferenceUtils.format(client.getAcceptedMediaTypes()));
			}
			catch (IOException ioe)
			{
				logger.log(Level.WARNING, "Unable to format the HTTP Accept header", ioe);
			}
		}
		else
		{
			httpCall.getRequestHeaders().add(HttpConstants.HEADER_ACCEPT,
					MediaType.ALL.getName());
		}

		if (client.getAcceptedCharacterSets().size() > 0)
		{
			try
			{
				httpCall.getRequestHeaders().add(HttpConstants.HEADER_ACCEPT_CHARSET,
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
				httpCall.getRequestHeaders().add(HttpConstants.HEADER_ACCEPT_ENCODING,
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
				httpCall.getRequestHeaders().add(HttpConstants.HEADER_ACCEPT_LANGUAGE,
						PreferenceUtils.format(client.getAcceptedLanguages()));
			}
			catch (IOException ioe)
			{
				logger.log(Level.WARNING, "Unable to format the HTTP Accept header", ioe);
			}
		}

		// Add the security
		ChallengeResponse response = call.getSecurity().getChallengeResponse();
		if (response != null)
		{
			httpCall.getRequestHeaders().add(HttpConstants.HEADER_AUTHORIZATION,
					SecurityUtils.format(response));
		}

		// Send the input representation
		if (call.getInput() != null)
		{
			if (call.getInput().getMediaType() != null)
			{
				httpCall.getRequestHeaders().add(HttpConstants.HEADER_CONTENT_TYPE,
						call.getInput().getMediaType().toString());
			}

			if (call.getInput().getEncoding() != null)
			{
				httpCall.getRequestHeaders().add(HttpConstants.HEADER_CONTENT_ENCODING,
						call.getInput().getEncoding().toString());
			}

			if (call.getInput().getLanguage() != null)
			{
				httpCall.getRequestHeaders().add(HttpConstants.HEADER_CONTENT_LANGUAGE,
						call.getInput().getLanguage().toString());
			}
		}
	}

	/**
	 * Reads the response headers of a handled HTTP client call to update the original uniform call.  
	 * @param httpCall The handled HTTP client call.
	 * @param call The original uniform call.
	 */
	protected void readResponseHeaders(HttpClientCall httpCall, Call call)
	{
		try
		{
			// Read info from headers
			for (Parameter header : httpCall.getResponseHeaders())
			{
				if (header.getName().equalsIgnoreCase(HttpConstants.HEADER_LOCATION))
				{
					call.setRedirectRef(header.getValue());
				}
				else if ((header.getName().equalsIgnoreCase(HttpConstants.HEADER_SET_COOKIE))
						|| (header.getName().equalsIgnoreCase(HttpConstants.HEADER_SET_COOKIE2)))
				{
					try
					{
						CookieReader cr = new CookieReader(header.getValue());
						call.getCookieSettings().add(cr.readCookieSetting());
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
					call.getSecurity().setChallengeRequest(request);
				}
				else if (header.getName().equalsIgnoreCase(HttpConstants.HEADER_SERVER))
				{
					call.getServer().setName(header.getValue());
				}
			}
		}
		catch (Exception e)
		{
			logger.log(Level.FINE,
					"An error occured during the processing of the HTTP response.", e);
			call.setStatus(new Status(Status.CONNECTOR_ERROR_INTERNAL,
					"Unable to process the response. " + e.getMessage()));
		}
	}

}
