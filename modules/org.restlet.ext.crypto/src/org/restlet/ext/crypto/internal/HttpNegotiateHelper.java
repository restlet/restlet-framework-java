/**
 * Copyright 2005-2013 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.crypto.internal;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.Header;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.header.ChallengeWriter;
import org.restlet.engine.header.CookieWriter;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.header.HeaderUtils;
import org.restlet.engine.security.AuthenticatorHelper;
import org.restlet.engine.util.Base64;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

/**
 * Implements the HTTP Negotiate helper for form based authentication. It will
 * submit the username/password passed as credentials from consumer to default
 * action url. Once user is authenticated then we add the cookies sent by server
 * to request object.
 * 
 * @author <a href="mailto:onkar.dhuri@synerzip.com">Onkar Dhuri</a>
 */
public class HttpNegotiateHelper extends AuthenticatorHelper {

	/** The action url where form needs to submitted. */
	public static String ACTION_URL = "/j_security_check";

	/** The field name of username input box. */
	public static String USERNAME = "j_username";

	/** The field name of password input box. */
	public static String PASSWORD = "j_password";

	/**
	 * Constructor.
	 */
	public HttpNegotiateHelper() {
		super(ChallengeScheme.HTTP_NEGOTIATE, true, true);
	}

	/**
	 * This method submits the form to {ACTION_URL} as defined to authenticate
	 * the request. <br />
	 * Once credentials are validated, the server will send out the 302 Response
	 * with "JSESSIONID" & "JSESSIONIDSSO" cookies. <br />
	 * We set these cookies into request header for subsequent operations.
	 * 
	 * @see org.restlet.engine.security.AuthenticatorHelper#formatResponse(org.restlet.engine.header.ChallengeWriter,
	 *      org.restlet.data.ChallengeResponse, org.restlet.Request,
	 *      org.restlet.util.Series)
	 * 
	 */
	@Override
	public void formatResponse(ChallengeWriter cw, ChallengeResponse challenge,
			Request request, Series<Header> httpHeaders) {
		if (challenge == null) {
			throw new RuntimeException(
					"No challenge provided, unable to encode credentials");
		} else {
			/*
			 * Submit the form only if we dont have any cookies set in the request.
			 * For first /$metadata request, it wont have cookies, so it will submit the form and cookies are cached.
			 * Double check if cached cookies really has JSESSIONID/JSESSIONIDSSO, if not then submit the form again.
			 */
			if(request.getCookies().size() == 0 || 
					(request.getCookies().getFirst("JSESSIONID") == null || request.getCookies().getFirst("JSESSIONIDSSO") == null)){
				Reference resourceRef = request.getResourceRef();
				String relativeURL = resourceRef.toString().substring(0,
						resourceRef.toString().lastIndexOf("/"));
				ClientResource loginCr = new ClientResource(relativeURL
						+ ACTION_URL);
				Form loginForm = new Form();
				loginForm.add(USERNAME, challenge.getIdentifier());
				loginForm.add(PASSWORD, new String(challenge.getSecret()));
				loginCr.post(loginForm);
				Response response = loginCr.getResponse();
				// check if we get 302 response status; then only add the cookies
				// else throw exception
				if (response.getStatus().equals(Status.REDIRECTION_FOUND)
						|| response.getStatus()
								.equals(Status.REDIRECTION_PERMANENT)) {
					Series<CookieSetting> cookieSetting = loginCr
							.getCookieSettings();
					for (CookieSetting cs : cookieSetting) {
						request.getCookies().add(cs.getName(), cs.getValue());
					}
					// finally add all the cookies in header
					HeaderUtils.addHeader(HeaderConstants.HEADER_COOKIE,
							CookieWriter.write(request.getCookies()), httpHeaders);
				} else {
					throw new RuntimeException(
							"Can't authorize the request with passed credentials. "
									+ "Please check if you are passing correct credentials");
				}
			}
		}
	}

	/**
	 * For "Negotiate" we are not currently using this API but we might use it
	 * in future if we need to set the credentials to ChallengeResponse
	 * 
	 * @see org.restlet.engine.security.AuthenticatorHelper#parseResponse(org.restlet.data.ChallengeResponse,
	 *      org.restlet.Request, org.restlet.util.Series)
	 * 
	 */
	@Override
	public void parseResponse(ChallengeResponse challenge, Request request,
			Series<Header> httpHeaders) {
		try {
			// Check if Negotiate auth header uses Base64 encoding
			byte[] credentialsEncoded = Base64.decode(challenge.getRawValue());

			if (credentialsEncoded == null) {
				getLogger()
						.info("Cannot decode credentials: "
								+ challenge.getRawValue());
			}

			String credentials = new String(credentialsEncoded, "ISO-8859-1");
			int separator = credentials.indexOf(':');

			if (separator == -1) {
				// Log the blocking
				getLogger().info(
						"Invalid credentials given by client with IP: "
								+ ((request != null) ? request.getClientInfo()
										.getAddress() : "?"));
			} else {
				challenge.setIdentifier(credentials.substring(0, separator));
				challenge.setSecret(credentials.substring(separator + 1));
			}
		} catch (UnsupportedEncodingException e) {
			getLogger().log(Level.INFO,
					"Unsupported HTTP negotiate encoding error", e);
		} catch (IllegalArgumentException e) {
			getLogger().log(Level.INFO,
					"Unable to decode the HTTP Negotiate credential", e);
		}
	}

}
