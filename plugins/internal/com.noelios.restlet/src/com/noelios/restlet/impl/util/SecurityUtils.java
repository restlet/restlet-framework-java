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

package com.noelios.restlet.impl.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.ParameterList;
import org.restlet.data.Reference;
import org.restlet.data.Request;

import com.noelios.restlet.impl.http.HttpConstants;

/**
 * Security data manipulation utilities.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class SecurityUtils
{
	/**
	 * Formats a challenge request as a HTTP header value.
	 * @param request The challenge request to format.
	 * @return The authenticate header value.
	 */
	public static String format(ChallengeRequest request)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(request.getScheme().getTechnicalName());
		sb.append(" realm=\"").append(request.getRealm()).append('"');
		return sb.toString();
	}

	/**
	 * Formats a challenge response as raw credentials.
	 * @param challenge The challenge response to format.
	 * @param request The parent request.
	 * @param httpHeaders The current request HTTP headers.
	 * @return The authorization header value.
	 */
	public static String format(ChallengeResponse challenge, Request request,
			ParameterList httpHeaders)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(challenge.getScheme().getTechnicalName()).append(' ');

		if (challenge.getCredentials() != null)
		{
			sb.append(challenge.getCredentials());
		}
		else if (challenge.getScheme().equals(ChallengeScheme.HTTP_AWS))
		{
			// Setup the method name
			String methodName = request.getMethod().getName();

			// Setup the Date header
			String date = httpHeaders.getFirstValue("X-Amz-Date", true);

			if (date == null)
			{
				date = httpHeaders.getFirstValue(HttpConstants.HEADER_DATE, true);
			}

			if (date == null)
			{
				date = DateUtils.format(new Date(), DateUtils.FORMAT_RFC_1123[0]);
				httpHeaders.add(HttpConstants.HEADER_DATE, date);
			}

			// Setup the ContentType header
			String contentMd5 = httpHeaders.getFirstValue(HttpConstants.HEADER_CONTENT_MD5,
					true);
			if (contentMd5 == null) contentMd5 = "";

			// Setup the ContentType header
			String contentType = httpHeaders
					.getFirstValue(HttpConstants.HEADER_CONTENT_TYPE);
			if (contentType == null)
			{
				String javaVersion = System.getProperty("java.version");
				if (!request.getMethod().equals(Method.PUT)
						&& (javaVersion.startsWith("1.5") || javaVersion.startsWith("1.4")))
				{
					contentType = "application/x-www-form-urlencoded";
				}
				else
				{
					contentType = "";
				}
			}

			// Setup the canonicalized AmzHeaders 
			String canonicalizedAmzHeaders = getCanonicalizedAmzHeaders(httpHeaders);

			// Setup the canonicalized resource name
			String canonicalizedResource = getCanonicalizedResourceName(request
					.getResourceRef());

			// Setup the message part
			StringBuilder rest = new StringBuilder();
			rest.append(methodName).append('\n').append(contentMd5).append('\n').append(
					contentType).append('\n').append(date).append('\n').append(
					canonicalizedAmzHeaders).append('\n').append(canonicalizedResource);

			// Append the AWS credentials 
			sb.append(challenge.getIdentifier()).append(':').append(
					Base64.encodeBytes(toHMac(rest.toString(), challenge.getSecret())));
		}
		else if (challenge.getScheme().equals(ChallengeScheme.HTTP_BASIC))
		{
			try
			{
				String credentials = challenge.getIdentifier() + ':' + challenge.getSecret();
				sb.append(Base64.encodeBytes(credentials.getBytes("US-ASCII")));
			}
			catch (UnsupportedEncodingException e)
			{
				throw new RuntimeException(
						"Unsupported encoding, unable to encode credentials");
			}
		}
		else if (challenge.getScheme().equals(ChallengeScheme.SMTP_PLAIN))
		{
			try
			{
				String credentials = "^@" + challenge.getIdentifier() + "^@"
						+ challenge.getSecret();
				sb.append(Base64.encodeBytes(credentials.getBytes("US-ASCII")));
			}
			catch (UnsupportedEncodingException e)
			{
				throw new RuntimeException(
						"Unsupported encoding, unable to encode credentials");
			}
		}
		else
		{
			throw new IllegalArgumentException(
					"Challenge scheme not supported by this implementation, or credentials not set for custom schemes.");
		}

		return sb.toString();
	}

	/**
	 * Returns the canonicalized resource name.
	 * @param resourceRef The resource reference.
	 * @return The canonicalized resource name.
	 */
	private static String getCanonicalizedResourceName(Reference resourceRef)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(resourceRef.getPath());

		Form query = resourceRef.getQueryAsForm();
		if (query.getFirst("acl", true) != null)
		{
			sb.append("?acl");
		}
		else if (query.getFirst("torrent", true) != null)
		{
			sb.append("?torrent");
		}

		return sb.toString();
	}

	/**
	 * Returns the canonicalized AMZ headers.
	 * @param requestHeaders The list of request headers.
	 * @return The canonicalized AMZ headers.
	 */
	private static String getCanonicalizedAmzHeaders(ParameterList requestHeaders)
	{
		// Filter out all the AMZ headers required for AWS authentication
		SortedMap<String, String> amzHeaders = new TreeMap<String, String>();
		String headerName;
		for (Parameter param : requestHeaders)
		{
			headerName = param.getName().toLowerCase();

			if (headerName.equals("x-amz-date"))
			{
				// Ignore as we set the Date header.
			}
			else if (headerName.startsWith("x-amz-"))
			{
				if (!amzHeaders.containsKey(headerName))
				{
					amzHeaders.put(headerName, requestHeaders.getValues(headerName));
				}
			}
		}

		// Concatenate all AMZ headers
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String key : amzHeaders.keySet())
		{
			if (!first)
			{
				sb.append("\n");
			}
			else
			{
				first = false;
			}

			sb.append(key).append(':').append(amzHeaders.get(key));
		}

		return sb.toString();
	}

	/**
	 * Parses an authenticate header into a challenge request.
	 * @param header The HTTP header value to parse.
	 * @return The parsed challenge request.
	 */
	public static ChallengeRequest parseRequest(String header)
	{
		ChallengeRequest result = null;

		if (header != null)
		{
			int space = header.indexOf(' ');

			if (space != -1)
			{
				String scheme = header.substring(0, space);
				String realm = header.substring(space + 1);
				int equals = realm.indexOf('=');
				String realmValue = realm.substring(equals + 2, realm.length() - 1);
				result = new ChallengeRequest(new ChallengeScheme("HTTP_" + scheme, scheme),
						realmValue);
			}
		}

		return result;
	}

	/**
	 * Parses an authorization header into a challenge response.
	 * @param header The HTTP header value to parse.
	 * @return The parsed challenge response.
	 */
	public static ChallengeResponse parseResponse(String header)
	{
		ChallengeResponse result = null;

		if (header != null)
		{
			int space = header.indexOf(' ');

			if (space != -1)
			{
				String scheme = header.substring(0, space);
				String credentials = header.substring(space + 1);
				result = new ChallengeResponse(new ChallengeScheme("HTTP_" + scheme, scheme),
						credentials);
			}
		}

		return result;
	}

	/**
	 * Converts a source string to its HMAC/SHA-1 value.
	 * @param source The source string to convert.
	 * @param secretKey The secret key to use for conversion.
	 * @return The HMac value of the source string.
	 */
	public static byte[] toHMac(String source, String secretKey)
	{
		byte[] result = null;

		try
		{
			// Create the HMAC/SHA1 key
			SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes(), "HmacSHA1");

			// Create the message authentication code (MAC)
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(signingKey);

			// Compute the HMAC value
			result = mac.doFinal(source.getBytes());
		}
		catch (NoSuchAlgorithmException nsae)
		{
			throw new RuntimeException(
					"Could not find the SHA-1 algorithm. HMac conversion failed.", nsae);
		}
		catch (InvalidKeyException ike)
		{
			throw new RuntimeException(
					"Invalid key exception detected. HMac conversion failed.", ike);
		}

		return result;
	}
}
