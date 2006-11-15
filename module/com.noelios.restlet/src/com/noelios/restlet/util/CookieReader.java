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

package com.noelios.restlet.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.Parameter;
import org.restlet.util.DateUtils;

/**
 * Cookie header reader.
 * @author Jerome Louvel (contact@noelios.com)
 */
public class CookieReader extends HeaderReader
{
	private static final String NAME_VERSION = "$Version";

	private static final String NAME_PATH = "$Path";

	private static final String NAME_DOMAIN = "$Domain";

	private static final String NAME_SET_COMMENT = "comment";

	private static final String NAME_SET_COMMENT_URL = "commentURL";

	private static final String NAME_SET_DISCARD = "discard";

	private static final String NAME_SET_DOMAIN = "domain";

	private static final String NAME_SET_EXPIRES = "expires";

	private static final String NAME_SET_MAX_AGE = "max-age";

	private static final String NAME_SET_PATH = "path";

	private static final String NAME_SET_PORT = "port";

	private static final String NAME_SET_SECURE = "secure";

	private static final String NAME_SET_VERSION = "version";

	/** The logger to use. */
	private Logger logger;

	/** The cached pair. Used by the readPair() method. */
	private Parameter cachedPair;

	/** The global cookie specification version. */
	private int globalVersion;

	/**
	 * Constructor.
	 * @param logger The logger to use.
	 * @param header The header to read.
	 */
	public CookieReader(Logger logger, String header)
	{
		super(header);
		this.logger = logger;
		this.cachedPair = null;
		this.globalVersion = -1;
	}

	/**
	 * Reads the next cookie available or null.
	 * @return The next cookie available or null.
	 */
	public Cookie readCookie() throws IOException
	{
		Cookie result = null;
		Parameter pair = readPair();

		if (this.globalVersion == -1)
		{
			// Cookies version not yet detected
			if (pair.getName().equalsIgnoreCase(NAME_VERSION))
			{
				if (pair.getValue() != null)
				{
					this.globalVersion = Integer.parseInt(pair.getValue());
				}
				else
				{
					throw new IOException(
							"Empty cookies version attribute detected. Please check your cookie header");
				}
			}
			else
			{
				// Set the default version for old Netscape cookies
				this.globalVersion = 0;
			}
		}

		while ((pair != null) && (pair.getName().charAt(0) == '$'))
		{
			// Unexpected special attribute
			// Silently ignore it as it may have been introduced by new
			// specifications
			pair = readPair();
		}

		if (pair != null)
		{
			// Set the cookie name and value
			result = new Cookie(this.globalVersion, pair.getName(), pair.getValue());
			pair = readPair();
		}

		while ((pair != null) && (pair.getName().charAt(0) == '$'))
		{
			if (pair.getName().equalsIgnoreCase(NAME_PATH))
			{
				result.setPath(pair.getValue());
			}
			else if (pair.getName().equalsIgnoreCase(NAME_DOMAIN))
			{
				result.setDomain(pair.getValue());
			}
			else
			{
				// Unexpected special attribute
				// Silently ignore it as it may have been introduced by new
				// specifications
			}

			pair = readPair();
		}

		if (pair != null)
		{
			// We started to read the next cookie
			// So let's put it back into the stream
			this.cachedPair = pair;
		}

		return result;
	}

	/**
	 * Reads the next cookie setting available or null.
	 * @return The next cookie setting available or null.
	 */
	public CookieSetting readCookieSetting() throws IOException
	{
		CookieSetting result = null;
		Parameter pair = readPair();

		while ((pair != null) && (pair.getName().charAt(0) == '$'))
		{
			// Unexpected special attribute
			// Silently ignore it as it may have been introduced by new
			// specifications
			pair = readPair();
		}

		if (pair != null)
		{
			// Set the cookie name and value
			result = new CookieSetting(pair.getName(), pair.getValue());
			pair = readPair();
		}

		while (pair != null)
		{
			if (pair.getName().equalsIgnoreCase(NAME_SET_PATH))
			{
				result.setPath(pair.getValue());
			}
			else if (pair.getName().equalsIgnoreCase(NAME_SET_DOMAIN))
			{
				result.setDomain(pair.getValue());
			}
			else if (pair.getName().equalsIgnoreCase(NAME_SET_COMMENT))
			{
				result.setComment(pair.getValue());
			}
			else if (pair.getName().equalsIgnoreCase(NAME_SET_COMMENT_URL))
			{
				// No yet supported
			}
			else if (pair.getName().equalsIgnoreCase(NAME_SET_DISCARD))
			{
				result.setMaxAge(-1);
			}
			else if (pair.getName().equalsIgnoreCase(NAME_SET_EXPIRES))
			{
				Date current = new Date(System.currentTimeMillis());
				Date expires = DateUtils.parse(pair.getValue(), DateUtils.FORMAT_RFC_1036);

				if (expires == null)
				{
					expires = DateUtils.parse(pair.getValue(), DateUtils.FORMAT_RFC_1123);
				}

				if (expires == null)
				{
					expires = DateUtils.parse(pair.getValue(), DateUtils.FORMAT_ASC_TIME);
				}

				if (expires != null)
				{
					if (DateUtils.after(current, expires))
					{
						result
								.setMaxAge((int) ((expires.getTime() - current.getTime()) / 1000));
					}
					else
					{
						result.setMaxAge(0);
					}
				}
				else
				{
					// Ignore the expires header
					this.logger.log(Level.WARNING,
							"Ignoring cookie setting expiration date. Unable to parse the date: "
									+ pair.getValue());
				}
			}
			else if (pair.getName().equalsIgnoreCase(NAME_SET_MAX_AGE))
			{
				result.setMaxAge(Integer.valueOf(pair.getValue()));
			}
			else if (pair.getName().equalsIgnoreCase(NAME_SET_PORT))
			{
				// No yet supported
			}
			else if (pair.getName().equalsIgnoreCase(NAME_SET_SECURE))
			{
				if ((pair.getValue() == null) || (pair.getValue().length() == 0))
				{
					result.setSecure(true);
				}
			}
			else if (pair.getName().equalsIgnoreCase(NAME_SET_VERSION))
			{
				result.setVersion(Integer.valueOf(pair.getValue()));
			}
			else
			{
				// Unexpected special attribute
				// Silently ignore it as it may have been introduced by new
				// specifications
			}

			pair = readPair();
		}

		return result;
	}

	/**
	 * Reads the next pair as a parameter.
	 * @return The next pair as a parameter.
	 * @throws IOException
	 */
	private Parameter readPair() throws IOException
	{
		Parameter result = null;

		if (cachedPair != null)
		{
			result = cachedPair;
			cachedPair = null;
		}
		else
		{
			try
			{
				boolean readingName = true;
				boolean readingValue = false;
				StringBuilder nameBuffer = new StringBuilder();
				StringBuilder valueBuffer = new StringBuilder();

				int nextChar = 0;
				while ((result == null) && (nextChar != -1))
				{
					nextChar = read();

					if (readingName)
					{
						if ((HeaderUtils.isSpace(nextChar)) && (nameBuffer.length() == 0))
						{
							// Skip spaces
						}
						else if ((nextChar == -1) || (nextChar == ';') || (nextChar == ','))
						{
							if (nameBuffer.length() > 0)
							{
								// End of pair with no value
								result = createParameter(nameBuffer, null);
							}
							else if (nextChar == -1)
							{
								// Do nothing return null preference
							}
							else
							{
								throw new IOException(
										"Empty cookie name detected. Please check your cookies");
							}
						}
						else if (nextChar == '=')
						{
							readingName = false;
							readingValue = true;
						}
						else if (HeaderUtils.isTokenChar(nextChar) || (this.globalVersion < 1))
						{
							nameBuffer.append((char) nextChar);
						}
						else
						{
							throw new IOException(
									"Separator and control characters are not allowed within a token. Please check your cookie header");
						}
					}
					else if (readingValue)
					{
						if ((HeaderUtils.isSpace(nextChar)) && (valueBuffer.length() == 0))
						{
							// Skip spaces
						}
						else if ((nextChar == -1) || (nextChar == ';'))
						{
							// End of pair
							result = createParameter(nameBuffer, valueBuffer);
						}
						else if ((nextChar == '"') && (valueBuffer.length() == 0))
						{
							valueBuffer.append(readQuotedString());
						}
						else if (HeaderUtils.isTokenChar(nextChar) || (this.globalVersion < 1))
						{
							valueBuffer.append((char) nextChar);
						}
						else
						{
							throw new IOException(
									"Separator and control characters are not allowed within a token. Please check your cookie header");
						}
					}
				}
			}
			catch (UnsupportedEncodingException uee)
			{
				throw new IOException(
						"Unsupported encoding. Please contact the administrator");
			}
		}

		return result;
	}

}
