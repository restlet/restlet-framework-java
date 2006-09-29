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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Call;
import org.restlet.Context;
import org.restlet.data.ClientData;
import org.restlet.data.ConditionData;
import org.restlet.data.Cookie;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Representation;
import org.restlet.data.SecurityData;
import org.restlet.data.ServerData;
import org.restlet.data.Status;
import org.restlet.data.Tag;

import com.noelios.restlet.impl.Factory;
import com.noelios.restlet.impl.util.CookieReader;
import com.noelios.restlet.impl.util.PreferenceUtils;
import com.noelios.restlet.impl.util.SecurityUtils;

/**
 * Call wrapper for server HTTP calls.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class HttpServerRestletCall extends Call
{
	/** Obtain a suitable logger. */
	private static Logger logger = Logger.getLogger(HttpServerRestletCall.class
			.getCanonicalName());

	/** The context of the HTTP server connector that issued the call. */
	private Context context;

	/** The low-level HTTP call. */
	private HttpCall httpCall;

	/** Indicates if the client data was parsed and added. */
	private boolean clientAdded;

	/** Indicates if the conditions were parsed and added. */
	private boolean conditionAdded;

	/** Indicates if the cookies were parsed and added. */
	private boolean cookiesAdded;

	/** Indicates if the input representation was added. */
	private boolean inputAdded;

	/** Indicates if the referrer was parsed and added. */
	private boolean referrerAdded;

	/** Indicates if the security data was parsed and added. */
	private boolean securityAdded;

	/** Indicates if the server data was parsed and added. */
	private boolean serverAdded;

	/**
	 * Constructor.
	 * @param context The context of the HTTP server connector that issued the call.
	 * @param httpCall The low-level HTTP server call.
	 */
	public HttpServerRestletCall(Context context, HttpServerCall httpCall)
	{
		this.context = context;
		this.clientAdded = false;
		this.conditionAdded = false;
		this.cookiesAdded = false;
		this.inputAdded = false;
		this.referrerAdded = false;
		this.securityAdded = false;
		this.serverAdded = false;
		this.httpCall = httpCall;

		// Set the properties
		setStatus(Status.SUCCESS_OK);
		setMethod(Method.valueOf(httpCall.getMethod()));
		setProtocol(httpCall.isConfidential() ? Protocol.HTTP : Protocol.HTTPS);

		// Set the resource reference
		String resource = httpCall.getRequestUri();
		if (resource != null)
		{
			setResourceRef(resource);
		}
	}

	/**
	 * Returns the client specific data.
	 * @return The client specific data.
	 */
	public ClientData getClient()
	{
		ClientData result = super.getClient();

		if (!this.clientAdded)
		{
			// Extract the header values
			String acceptCharset = getHttpCall().getRequestHeaders().getValues(
					HttpConstants.HEADER_ACCEPT_CHARSET);
			String acceptEncoding = getHttpCall().getRequestHeaders().getValues(
					HttpConstants.HEADER_ACCEPT_ENCODING);
			String acceptLanguage = getHttpCall().getRequestHeaders().getValues(
					HttpConstants.HEADER_ACCEPT_LANGUAGE);
			String acceptMediaType = getHttpCall().getRequestHeaders().getValues(
					HttpConstants.HEADER_ACCEPT);

			// Parse the headers and update the call preferences
			PreferenceUtils.parseCharacterSets(acceptCharset, result);
			PreferenceUtils.parseEncodings(acceptEncoding, result);
			PreferenceUtils.parseLanguages(acceptLanguage, result);
			PreferenceUtils.parseMediaTypes(acceptMediaType, result);

			// Set other properties
			result.setAgent(getHttpCall().getRequestHeaders().getValues(
					HttpConstants.HEADER_USER_AGENT));
			result.setAddress(getHttpCall().getClientAddress());

			// Special handling for the non standard but common "X-Forwarded-For" header.
			boolean useForwardedForHeader = Boolean.parseBoolean(this.context
					.getParameters().getFirstValue("useForwardedForHeader", false));
			if (useForwardedForHeader)
			{
				// Lookup the "X-Forwarded-For" header supported by popular proxies and caches.
				// This information is only safe for intermediary components within your local network.
				// Other addresses could easily be changed by setting a fake header and should never
				// be trusted for serious security checks.
				String header = getHttpCall().getRequestHeaders().getValues(
						HttpConstants.HEADER_X_FORWARDED_FOR);
				if (header != null)
				{
					String[] addresses = header.split(",");
					for (int i = addresses.length - 1; i >= 0; i--)
					{
						result.getAddresses().add(addresses[i].trim());
					}
				}
			}

			this.clientAdded = true;
		}

		return result;
	}

	/**
	 * Returns the condition data applying to this call.
	 * @return The condition data applying to this call.
	 */
	public ConditionData getCondition()
	{
		ConditionData result = super.getCondition();

		if (!this.conditionAdded)
		{
			// Extract the header values
			String ifMatchHeader = getHttpCall().getRequestHeaders().getValues(
					HttpConstants.HEADER_IF_MATCH);
			String ifNoneMatchHeader = getHttpCall().getRequestHeaders().getValues(
					HttpConstants.HEADER_IF_NONE_MATCH);
			Date ifModifiedSince = null;
			Date ifUnmodifiedSince = null;

			for (Parameter header : getHttpCall().getRequestHeaders())
			{
				if (header.getName().equalsIgnoreCase(HttpConstants.HEADER_IF_MODIFIED_SINCE))
				{
					ifModifiedSince = getHttpCall().parseDate(header.getValue(), false);
				}
				else if (header.getName().equalsIgnoreCase(
						HttpConstants.HEADER_IF_UNMODIFIED_SINCE))
				{
					ifUnmodifiedSince = getHttpCall().parseDate(header.getValue(), false);
				}
			}

			// Set the If-Modified-Since date
			if ((ifModifiedSince != null) && (ifModifiedSince.getTime() != -1))
			{
				result.setModifiedSince(ifModifiedSince);
			}

			// Set the If-Unmodified-Since date
			if ((ifUnmodifiedSince != null) && (ifUnmodifiedSince.getTime() != -1))
			{
				result.setUnmodifiedSince(ifUnmodifiedSince);
			}

			// Set the If-Match tags
			List<Tag> match = null;
			Tag current = null;
			if (ifMatchHeader != null)
			{
				try
				{
					String[] tags = ifMatchHeader.split(",");
					for (int i = 0; i < tags.length; i++)
					{
						current = new Tag(tags[i]);

						// Is it the first tag?
						if (match == null)
						{
							match = new ArrayList<Tag>();
							result.setMatch(match);
						}

						// Add the new tag
						match.add(current);
					}
				}
				catch (Exception e)
				{
					logger.log(Level.WARNING, "Unable to process the if-match header: "
							+ ifMatchHeader);
				}
			}

			// Set the If-None-Match tags
			List<Tag> noneMatch = null;
			if (ifNoneMatchHeader != null)
			{
				try
				{
					String[] tags = ifNoneMatchHeader.split(",");
					for (int i = 0; i < tags.length; i++)
					{
						current = new Tag(tags[i]);

						// Is it the first tag?
						if (noneMatch == null)
						{
							noneMatch = new ArrayList<Tag>();
							result.setNoneMatch(noneMatch);
						}

						noneMatch.add(current);
					}
				}
				catch (Exception e)
				{
					logger.log(Level.WARNING, "Unable to process the if-none-match header: "
							+ ifNoneMatchHeader);
				}
			}

			this.conditionAdded = true;
		}

		return result;
	}

	/**
	 * Returns the low-level HTTP call.
	 * @return The low-level HTTP call.
	 */
	public HttpCall getHttpCall()
	{
		return this.httpCall;
	}

	/**
	 * Returns the cookies provided by the client.
	 * @return The cookies provided by the client.
	 */
	public List<Cookie> getCookies()
	{
		List<Cookie> result = super.getCookies();

		if (!cookiesAdded)
		{
			String cookiesValue = getHttpCall().getRequestHeaders().getValues(
					HttpConstants.HEADER_COOKIE);

			if (cookiesValue != null)
			{
				try
				{
					CookieReader cr = new CookieReader(cookiesValue);
					Cookie current = cr.readCookie();
					while (current != null)
					{
						result.add(current);
						current = cr.readCookie();
					}
				}
				catch (Exception e)
				{
					logger.log(Level.WARNING,
							"An exception occured during cookies parsing. Headers value: "
									+ cookiesValue, e);
				}
			}

			this.cookiesAdded = true;
		}

		return result;
	}

	/**
	 * Returns the representation provided by the client.
	 * @return The representation provided by the client.
	 */
	public Representation getInput()
	{
		if (!this.inputAdded)
		{
			setInput(((HttpServerCall) getHttpCall()).getRequestInput());
			this.inputAdded = true;
		}

		return super.getInput();
	}

	/**
	 * Returns the referrer reference if available.
	 * @return The referrer reference.
	 */
	public Reference getReferrerRef()
	{
		if (!this.referrerAdded)
		{
			String referrerValue = getHttpCall().getRequestHeaders().getValues(
					HttpConstants.HEADER_REFERRER);
			if (referrerValue != null)
			{
				setReferrerRef(new Reference(referrerValue));
			}

			this.referrerAdded = true;
		}

		return super.getReferrerRef();
	}

	/**
	 * Returns the security data related to this call.
	 * @return The security data related to this call.
	 */
	public SecurityData getSecurity()
	{
		SecurityData result = super.getSecurity();

		if (!this.securityAdded)
		{
			if (getHttpCall().isConfidential())
			{
				getSecurity().setConfidential(true);
			}
			else
			{
				// We don't want to autocreate the security data just for this information
				// Because that will by the default value of this property if read by someone.
			}

			// Extract the header value
			String authorization = getHttpCall().getRequestHeaders().getValues(
					HttpConstants.HEADER_AUTHORIZATION);

			// Set the challenge response
			result.setChallengeResponse(SecurityUtils.parseResponse(authorization));

			this.securityAdded = true;
		}

		return result;
	}

	/**
	 * Returns the server specific data.
	 * @return The server specific data.
	 */
	public ServerData getServer()
	{
		ServerData result = super.getServer();

		if (!this.serverAdded)
		{
			result.setAddress(httpCall.getServerAddress());
			result.setAgent(Factory.VERSION_HEADER);
			result.setName(httpCall.getServerName());
			result.setPort(httpCall.getServerPort());
		}

		return result;
	}
}
