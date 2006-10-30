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

package org.restlet.ext.model;

import java.util.Iterator;
import java.util.List;

import org.restlet.data.Cookie;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Readable model wrapping a call's request and response. It can be passed directly passed to a string 
 * template. Repeating values can be retrieved by appending (index) or ("name")  or ('name') after the 
 * variable's name. Note that (first) is equivalent to (0) and that (last) returns the last value. 
 * Here is the list of currently supported variables:
 * <ul>
 * <li>attribute (first lookup the request, then the response)</li>
 * <li>authority</li>
 * <li>baseUri</li>
 *	<li>client.address (repeating and non-repeating, lookup by index only)</li>
 * <li>client.agent</li>
 * <li>cookie (repeating, lookup by name and by index)</li>
 * <li>fragment</li>
 * <li>hostIdentifier</li>
 * <li>hostDomain</li>
 * <li>hostPort</li>
 * <li>identifier</li>
 * <li>method</li>
 * <li>path</li>
 * <li>query (repeating and non-repeating, lookup by name and by index)</li>
 * <li>redirectUri</li>
 * <li>referrerUri</li>
 * <li>relativeUri</li>
 * <li>scheme</li>
 * <li>segment (repeating, lookup by index) : from Request.baseRef</li>
 * <li>server.address</li>
 * <li>server.agent</li>
 * <li>status</li>
 * <li>uri</li>
 * <li>userInfo</li>
 * </ul>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class CallModel implements ReadableModel
{
	public static final String NAME_ATTRIBUTE = "attribute";

	public static final String NAME_BASE_URI = "baseUri";

	public static final String NAME_CLIENT_ADDRESS = "client.address";

	public static final String NAME_CLIENT_AGENT = "client.agent";

	public static final String NAME_COOKIE = "cookie";

	public static final String NAME_METHOD = "method";

	public static final String NAME_REDIRECT_URI = "redirectUri";

	public static final String NAME_REFERRER_URI = "referrerUri";

	public static final String NAME_RELATIVE_URI = "relativeUri";

	public static final String NAME_RESOURCE_AUTHORITY = "authority";

	public static final String NAME_RESOURCE_FRAGMENT = "fragment";

	public static final String NAME_RESOURCE_HOST_DOMAIN = "hostDomain";

	public static final String NAME_RESOURCE_HOST_PORT = "hostPort";

	public static final String NAME_RESOURCE_HOST_IDENTIFIER = "hostIdentifier";

	public static final String NAME_RESOURCE_IDENTIFIER = "identifier";

	public static final String NAME_RESOURCE_PATH = "path";

	public static final String NAME_RESOURCE_QUERY = "query";

	public static final String NAME_RESOURCE_SCHEME = "scheme";

	public static final String NAME_RESOURCE_SEGMENT = "segment";

	public static final String NAME_RESOURCE_URI = "uri";

	public static final String NAME_RESOURCE_USER_INFO = "userInfo";

	public static final String NAME_SERVER_ADDRESS = "server.address";

	public static final String NAME_SERVER_AGENT = "server.agent";

	public static final String NAME_STATUS = "status";

	/** The wrapped request. */
	private Request request;

	/** The wrapped response. */
	private Response response;

	/** The default value to return if a lookup fails or returns null. */
	private String defaultValue;

	/**
	 * Constructor.
	 * @param request The wrapped request.
	 * @param response The wrapped response.
	 * @param defaultValue The default value to return if a lookup fails or returns null.
	 */
	public CallModel(Request request, Response response, String defaultValue)
	{
		this.request = request;
		this.response = response;
		this.defaultValue = defaultValue;
	}

	/**
	 * Returns the model value for a given name.
	 * @param name The name to look-up.
	 * @return The model value for the given name.
	 */
	public String get(String name)
	{
		String result = null;

		try
		{
			if (name.equals(NAME_ATTRIBUTE))
			{
				String rest = name.substring(NAME_ATTRIBUTE.length());

				if ((rest.charAt(0) == '(') && (rest.charAt(rest.length() - 1) == ')'))
				{
					rest = rest.substring(1, rest.length() - 1);

					if (isVariableName(rest))
					{
						// Lookup by name
						rest = getVariableName(rest);

						// First check the request attributes
						result = request.getAttributes().get(rest).toString();
						if (result == null)
						{
							// Then check the response attributes
							result = response.getAttributes().get(rest).toString();
						}
					}
					else
					{
						// Lookup by index
						result = request.getResourceRef().getQueryAsForm().get(
								Integer.parseInt(rest)).getValue();
					}
				}
				else
				{
					result = defaultValue;
				}
			}
			else if (name.equals(NAME_BASE_URI))
			{
				result = request.getBaseRef().toString();
			}
			else if (name.startsWith(NAME_CLIENT_ADDRESS))
			{
				if (name.equals(NAME_CLIENT_ADDRESS))
				{
					result = request.getClientInfo().getAddress();
				}
				else
				{
					String rest = name.substring(NAME_CLIENT_ADDRESS.length());

					if ((rest.charAt(0) == '(') && (rest.charAt(rest.length() - 1) == ')'))
					{
						rest = rest.substring(1, rest.length() - 1);

						if (rest.equals("first"))
						{
							result = request.getClientInfo().getAddresses().get(0);
						}
						else if (rest.equals("last"))
						{
							result = request.getClientInfo().getAddresses().get(
									request.getClientInfo().getAddresses().size() - 1);
						}
						else if (isVariableName(rest))
						{
							// Can't lookup by name
							result = defaultValue;
						}
						else
						{
							// Lookup by index
							result = request.getClientInfo().getAddresses().get(
									Integer.parseInt(rest));
						}
					}
					else
					{
						result = defaultValue;
					}
				}
			}
			else if (name.equals(NAME_CLIENT_AGENT))
			{
				result = request.getClientInfo().getAgent();
			}
			else if (name.startsWith(NAME_COOKIE))
			{
				String rest = name.substring(NAME_COOKIE.length());

				if ((rest.charAt(0) == '(') && (rest.charAt(rest.length() - 1) == ')'))
				{
					rest = rest.substring(1, rest.length() - 1);

					if (rest.equals("first"))
					{
						result = request.getCookies().get(0).getValue();
					}
					else if (rest.equals("last"))
					{
						result = request.getCookies().get(request.getCookies().size() - 1)
								.getValue();
					}
					else if (isVariableName(rest))
					{
						// Lookup by name
						rest = getVariableName(rest);
						result = getFirstCookie(request.getCookies(), rest).getValue();
					}
					else
					{
						// Lookup by index
						result = request.getCookies().get(Integer.parseInt(rest)).getValue();
					}
				}
				else
				{
					result = defaultValue;
				}
			}
			else if (name.equals(NAME_METHOD))
			{
				result = request.getMethod().getName();
			}
			else if (name.equals(NAME_REDIRECT_URI))
			{
				result = response.getRedirectRef().toString();
			}
			else if (name.equals(NAME_REFERRER_URI))
			{
				result = request.getReferrerRef().toString();
			}
			else if (name.equals(NAME_RELATIVE_URI))
			{
				result = request.getRelativePart();
			}
			else if (name.equals(NAME_RESOURCE_AUTHORITY))
			{
				result = request.getResourceRef().getAuthority();
			}
			else if (name.equals(NAME_RESOURCE_FRAGMENT))
			{
				result = request.getResourceRef().getFragment();
			}
			else if (name.equals(NAME_RESOURCE_HOST_DOMAIN))
			{
				result = request.getResourceRef().getHostDomain();
			}
			else if (name.equals(NAME_RESOURCE_HOST_PORT))
			{
				result = request.getResourceRef().getHostPort().toString();
			}
			else if (name.equals(NAME_RESOURCE_HOST_IDENTIFIER))
			{
				result = request.getResourceRef().getHostIdentifier();
			}
			else if (name.equals(NAME_RESOURCE_IDENTIFIER))
			{
				result = request.getResourceRef().getIdentifier();
			}
			else if (name.equals(NAME_RESOURCE_PATH))
			{
				result = request.getResourceRef().getPath();
			}
			else if (name.startsWith(NAME_RESOURCE_QUERY))
			{
				if (name.equals(NAME_RESOURCE_QUERY))
				{
					result = request.getResourceRef().getQuery();
				}
				else
				{
					String rest = name.substring(NAME_RESOURCE_QUERY.length());

					if ((rest.charAt(0) == '(') && (rest.charAt(rest.length() - 1) == ')'))
					{
						rest = rest.substring(1, rest.length() - 1);

						if (rest.equals("first"))
						{
							result = request.getResourceRef().getQueryAsForm().get(0).getValue();
						}
						else if (rest.equals("last"))
						{
							Form form = request.getResourceRef().getQueryAsForm();
							result = form.get(form.size() - 1).getValue();
						}
						else if (isVariableName(rest))
						{
							// Lookup by name
							rest = getVariableName(rest);
							result = request.getResourceRef().getQueryAsForm().getFirst(rest)
									.getValue();
						}
						else
						{
							// Lookup by index
							result = request.getResourceRef().getQueryAsForm().get(
									Integer.parseInt(rest)).getValue();
						}
					}
					else
					{
						result = defaultValue;
					}
				}
			}
			else if (name.equals(NAME_RESOURCE_SCHEME))
			{
				result = request.getResourceRef().getScheme();
			}
			else if (name.startsWith(NAME_RESOURCE_SEGMENT))
			{
				String rest = name.substring(NAME_RESOURCE_SEGMENT.length());

				if ((rest.charAt(0) == '(') && (rest.charAt(rest.length() - 1) == ')'))
				{
					rest = rest.substring(1, rest.length() - 1);

					if (rest.equals("first"))
					{
						result = request.getResourceRef().getSegments().get(0);
					}
					else if (rest.equals("last"))
					{
						result = request.getResourceRef().getSegments().get(
								request.getResourceRef().getSegments().size() - 1);
					}
					else
					{
						// Lookup by index
						result = request.getResourceRef().getSegments().get(
								Integer.parseInt(rest));
					}
				}
				else
				{
					result = defaultValue;
				}
			}
			else if (name.equals(NAME_RESOURCE_URI))
			{
				result = request.getResourceRef().toString();
			}
			else if (name.equals(NAME_RESOURCE_USER_INFO))
			{
				result = request.getResourceRef().getUserInfo();
			}
			else if (name.equals(NAME_SERVER_ADDRESS))
			{
				result = response.getServerInfo().getAddress();
			}
			else if (name.equals(NAME_SERVER_AGENT))
			{
				result = response.getServerInfo().getAgent();
			}
			else if (name.equals(NAME_STATUS))
			{
				result = Integer.toString(response.getStatus().getCode());
			}

			// Check if the default value should be returned
			if (result == null)
			{
				result = this.defaultValue;
			}
		}
		catch (Exception e)
		{
			result = this.defaultValue;
		}

		return result;
	}

	/**
	 * Indicates if the token contains a variable name.
	 * @param token The token to test.
	 * @return True if the token contains a variable name.
	 */
	protected boolean isVariableName(String token)
	{
		return (((token.charAt(0) == '"') && (token.charAt(token.length() - 1) == '"')) || ((token
				.charAt(0) == '\'') && (token.charAt(token.length() - 1) == '\'')));
	}

	/**
	 * Returns the variable name contained in the token.
	 * @param token The token containing the variable name.
	 * @return The variable name.
	 */
	protected String getVariableName(String token)
	{
		return token.substring(1, token.length() - 1);
	}

	/**
	 * Indicates if the model contains a value for a given name.
	 * @param name The name to look-up.
	 * @return True if the model contains a value for the given name.
	 */
	public boolean contains(String name)
	{
		boolean result = false;

		if (name.startsWith(NAME_ATTRIBUTE))
		{
			result = (request.getAttributes().size() > 0)
					|| (response.getAttributes().size() > 0);
		}
		else if (name.equals(NAME_BASE_URI))
		{
			result = (request.getBaseRef() != null);
		}
		else if (name.startsWith(NAME_CLIENT_ADDRESS))
		{
			result = (request.getClientInfo().getAddress() != null);
		}
		else if (name.equals(NAME_CLIENT_AGENT))
		{
			result = (request.getClientInfo().getAgent() != null);
		}
		else if (name.startsWith(NAME_COOKIE))
		{
			result = (request.getCookies() != null) && (request.getCookies().size() > 0);
		}
		else if (name.equals(NAME_METHOD))
		{
			result = (request.getMethod() != null);
		}
		else if (name.equals(NAME_REDIRECT_URI))
		{
			result = (response.getRedirectRef() != null);
		}
		else if (name.equals(NAME_REFERRER_URI))
		{
			result = (request.getReferrerRef() != null);
		}
		else if (name.equals(NAME_RELATIVE_URI))
		{
			result = (request.getRelativePart() != null);
		}
		else if (name.equals(NAME_RESOURCE_AUTHORITY))
		{
			result = (request.getResourceRef() != null)
					&& (request.getResourceRef().getAuthority() != null);
		}
		else if (name.equals(NAME_RESOURCE_FRAGMENT))
		{
			result = (request.getResourceRef() != null)
					&& (request.getResourceRef().getFragment() != null);
		}
		else if (name.equals(NAME_RESOURCE_HOST_DOMAIN))
		{
			result = (request.getResourceRef() != null)
					&& (request.getResourceRef().getHostDomain() != null);
		}
		else if (name.equals(NAME_RESOURCE_HOST_PORT))
		{
			result = (request.getResourceRef() != null)
					&& (request.getResourceRef().getHostPort() != null);
		}
		else if (name.equals(NAME_RESOURCE_HOST_IDENTIFIER))
		{
			result = (request.getResourceRef() != null)
					&& (request.getResourceRef().getHostIdentifier() != null);
		}
		else if (name.equals(NAME_RESOURCE_IDENTIFIER))
		{
			result = (request.getResourceRef() != null);
		}
		else if (name.equals(NAME_RESOURCE_PATH))
		{
			result = (request.getResourceRef() != null)
					&& (request.getResourceRef().getPath() != null);
		}
		else if (name.startsWith(NAME_RESOURCE_QUERY))
		{
			result = (request.getResourceRef() != null)
					&& (request.getResourceRef().getQuery() != null);
		}
		else if (name.equals(NAME_RESOURCE_SCHEME))
		{
			result = (request.getResourceRef() != null)
					&& (request.getResourceRef().getScheme() != null);
		}
		else if (name.startsWith(NAME_RESOURCE_SEGMENT))
		{
			result = (request.getResourceRef() != null)
					&& (request.getResourceRef().getSegments().size() > 0);
		}
		else if (name.equals(NAME_RESOURCE_URI))
		{
			result = (request.getResourceRef() != null);
		}
		else if (name.equals(NAME_RESOURCE_USER_INFO))
		{
			result = (request.getResourceRef() != null)
					&& (request.getResourceRef().getUserInfo() != null);
		}
		else if (name.equals(NAME_SERVER_ADDRESS))
		{
			result = (response.getServerInfo().getAddress() != null);
		}
		else if (name.equals(NAME_SERVER_AGENT))
		{
			result = (response.getServerInfo().getAgent() != null);
		}
		else if (name.equals(NAME_STATUS))
		{
			result = (response.getStatus() != null);
		}

		return result;
	}

	/**
	 * Reads the first cookie available with the given name or null.
	 * @param source The source list of cookies.
	 * @param name The name of the cookie to return.
	 * @return The first cookie available with the given name or null.
	 */
	public static Cookie getFirstCookie(List<Cookie> source, String name)
	{
		Cookie result = null;
		Cookie cookie;

		for (Iterator<Cookie> iter = source.iterator(); (result == null) && iter.hasNext();)
		{
			cookie = iter.next();

			if (name.equals(cookie.getName()))
			{
				result = cookie;
			}
		}

		return result;
	}

}
