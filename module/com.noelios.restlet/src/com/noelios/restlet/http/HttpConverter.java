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

package com.noelios.restlet.http;

import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.data.Parameter;
import org.restlet.data.ParameterList;

/**
 * Converter between high-level and low-level HTTP calls.
 * @author Jerome Louvel (contact@noelios.com)
 */
public class HttpConverter
{
	/** The context. */
	private Context context;

	/**
	 * Constructor.
	 * @param context The context to use.
	 */
	public HttpConverter(Context context)
	{
		this.context = context;
	}

	/**
	 * Returns the context.
	 * @return The context.
	 */
	public Context getContext()
	{
		return this.context;
	}

	/**
	 * Returns the context's logger.
	 * @return The context's logger.
	 */
	public Logger getLogger()
	{
		return getContext().getLogger();
	}

	/**
	 * Adds additional headers if they are non-standard headers.
	 * @param existingHeaders The headers to update.
	 * @param additionalHeaders The headers to add.
	 */
	public void addAdditionalHeaders(ParameterList existingHeaders,
			ParameterList additionalHeaders)
	{
		if (additionalHeaders != null)
		{
			for (Parameter param : additionalHeaders)
			{
				if (param.getName().equalsIgnoreCase(HttpConstants.HEADER_ACCEPT)
						|| param.getName()
								.equalsIgnoreCase(HttpConstants.HEADER_ACCEPT_CHARSET)
						|| param.getName().equalsIgnoreCase(
								HttpConstants.HEADER_ACCEPT_ENCODING)
						|| param.getName().equalsIgnoreCase(
								HttpConstants.HEADER_ACCEPT_LANGUAGE)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_ACCEPT_RANGES)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_AGE)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_ALLOW)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_AUTHORIZATION)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_CACHE_CONTROL)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_CONNECTION)
						|| param.getName().equalsIgnoreCase(
								HttpConstants.HEADER_CONTENT_ENCODING)
						|| param.getName().equalsIgnoreCase(
								HttpConstants.HEADER_CONTENT_LANGUAGE)
						|| param.getName()
								.equalsIgnoreCase(HttpConstants.HEADER_CONTENT_LENGTH)
						|| param.getName().equalsIgnoreCase(
								HttpConstants.HEADER_CONTENT_LOCATION)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_CONTENT_MD5)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_CONTENT_RANGE)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_CONTENT_TYPE)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_COOKIE)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_DATE)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_ETAG)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_EXPECT)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_EXPIRES)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_FROM)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_HOST)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_IF_MATCH)
						|| param.getName().equalsIgnoreCase(
								HttpConstants.HEADER_IF_MODIFIED_SINCE)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_IF_NONE_MATCH)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_IF_RANGE)
						|| param.getName().equalsIgnoreCase(
								HttpConstants.HEADER_IF_UNMODIFIED_SINCE)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_LAST_MODIFIED)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_LOCATION)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_MAX_FORWARDS)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_PRAGMA)
						|| param.getName().equalsIgnoreCase(
								HttpConstants.HEADER_PROXY_AUTHENTICATE)
						|| param.getName().equalsIgnoreCase(
								HttpConstants.HEADER_PROXY_AUTHORIZATION)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_RANGE)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_REFERRER)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_RETRY_AFTER)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_SERVER)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_SET_COOKIE)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_SET_COOKIE2)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_TRAILER)
						|| param.getName().equalsIgnoreCase(
								HttpConstants.HEADER_TRANSFER_ENCODING)
						|| param.getName().equalsIgnoreCase(
								HttpConstants.HEADER_TRANSFER_EXTENSION)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_UPGRADE)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_USER_AGENT)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_VARY)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_VIA)
						|| param.getName().equalsIgnoreCase(HttpConstants.HEADER_WARNING)
						|| param.getName().equalsIgnoreCase(
								HttpConstants.HEADER_WWW_AUTHENTICATE))
				{
					// Standard headers can't be overriden
					getLogger().warning(
							"Addition of the standard header \"" + param.getName()
									+ "\" is not allowed.");
				}
				else
				{
					existingHeaders.add(param);
				}
			}
		}
	}

}
