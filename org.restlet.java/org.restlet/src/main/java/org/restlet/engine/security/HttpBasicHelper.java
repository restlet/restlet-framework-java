/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.security;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.*;
import org.restlet.engine.header.ChallengeWriter;
import org.restlet.engine.header.HeaderReader;
import org.restlet.engine.io.IoUtils;
import org.restlet.util.Series;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Level;

/**
 * Implements the HTTP BASIC authentication.
 * 
 * @author Jerome Louvel
 */
public class HttpBasicHelper extends AuthenticatorHelper {

	/**
	 * Name of the system property that enables the UTF-8 handling of the HTTP Basic
	 * credentials (RFC 7617, section 2.1). Disabled by default, to keep the
	 * historical ISO-8859-1 behavior. When set to "true": a server challenge
	 * advertises {@code charset="UTF-8"}, credentials received by a server are
	 * decoded as UTF-8 (falling back to ISO-8859-1 if they are not valid UTF-8) and
	 * a client answering a challenge that advertises UTF-8 encodes its credentials
	 * in UTF-8.
	 */
	public static final String UTF8_PROPERTY = "org.restlet.engine.security.basic.utf8";

	private static final String CHARSET = "charset";

	private static final String UTF_8 = "UTF-8";

	private static final String ISO_8859_1 = "ISO-8859-1";

	/**
	 * Indicates if the UTF-8 handling of the HTTP Basic credentials is enabled.
	 * 
	 * @return True if the {@link #UTF8_PROPERTY} system property is "true".
	 * @see #UTF8_PROPERTY
	 */
	public static boolean isUtf8Enabled() {
		return Boolean.getBoolean(UTF8_PROPERTY);
	}

	/**
	 * Decodes the credentials sent by a client. The bytes are read as UTF-8 first
	 * and, if they are not valid UTF-8, as ISO-8859-1, the historical default.
	 * 
	 * @param bytes The decoded Base64 credentials.
	 * @return The "user-id:password" string.
	 */
	private static String decodeUtf8First(byte[] bytes) {
		try {
			return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
					.onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString();
		} catch (CharacterCodingException e) {
			return new String(bytes, StandardCharsets.ISO_8859_1);
		}
	}

	/**
	 * Constructor.
	 */
	public HttpBasicHelper() {
		super(ChallengeScheme.HTTP_BASIC, true, true);
	}

	@Override
	public void formatRequest(ChallengeWriter cw, ChallengeRequest challenge, Response response,
			Series<Header> httpHeaders) throws IOException {
		String realm = challenge.getRealm();
		String charset = challenge.getParameters().getFirstValue(CHARSET, true);

		if (realm != null) {
			cw.appendQuotedChallengeParameter("realm", realm);
		} else {
			getLogger()
					.warning("The realm directive is required for all authentication schemes that issue a challenge.");
		}

		if (charset != null) {
			if (UTF_8.equalsIgnoreCase(charset)) {
				cw.appendQuotedChallengeParameter(CHARSET, UTF_8);
			} else {
				getLogger().warning("The \"charset\" parameter must be \"UTF-8\" per RFC 7617.");
			}
		}
	}

	@Override
	public void formatResponse(ChallengeWriter cw, ChallengeResponse challenge, Request request,
			Series<Header> httpHeaders) {
		try {
			if (challenge == null) {
				throw new RuntimeException("No challenge provided, unable to encode credentials");
			} else {
				String charset = challenge.getParameters().getFirstValue(CHARSET, true);

				if (charset != null) {
					if (UTF_8.equalsIgnoreCase(charset)) {
						charset = UTF_8;
					} else {
						getLogger().warning(
								"The \"charset\" parameter must be \"UTF-8\" per RFC 7617. Using \"ISO-8859-1\" instead.");
						charset = ISO_8859_1;
					}
				} else {
					charset = ISO_8859_1;
				}

				CharArrayWriter credentials = new CharArrayWriter();
				credentials.write(challenge.getIdentifier());
				credentials.write(":");
				credentials.write(challenge.getSecret());
				cw.append(Base64.getEncoder().encodeToString(IoUtils.toByteArray(credentials.toCharArray(), charset)));
			}
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unsupported encoding, unable to encode credentials");
		} catch (IOException e) {
			throw new RuntimeException("Unexpected exception, unable to encode credentials", e);
		}
	}

	@Override
	public void parseRequest(ChallengeRequest challenge, Response response, Series<Header> httpHeaders) {
		if (challenge.getRawValue() != null) {
			HeaderReader<Object> hr = new HeaderReader<Object>(challenge.getRawValue());

			try {
				Parameter param = hr.readParameter();

				while (param != null) {
					try {
						if ("realm".equalsIgnoreCase(param.getName())) {
							challenge.setRealm(param.getValue());
						} else {
							challenge.getParameters().add(param);
						}

						if (hr.skipValueSeparator()) {
							param = hr.readParameter();
						} else {
							param = null;
						}
					} catch (Exception e) {
						Context.getCurrentLogger().log(Level.WARNING,
								"Unable to parse the challenge request header parameter", e);
					}
				}
			} catch (Exception e) {
				Context.getCurrentLogger().log(Level.WARNING, "Unable to parse the challenge request header parameter",
						e);
			}
		}
	}

	@Override
	public void parseResponse(ChallengeResponse challenge, Request request, Series<Header> httpHeaders) {
		if (challenge.getRawValue() == null) {
			getLogger().info("Cannot decode credentials: " + challenge.getRawValue());
			return;
		}

		try {
			String charset = challenge.getParameters().getFirstValue(CHARSET, true);

			if (charset != null) {
				if (UTF_8.equalsIgnoreCase(charset)) {
					charset = UTF_8;
				} else {
					getLogger().warning(
							"The \"charset\" parameter must be \"UTF-8\" per RFC 7617. Using \"ISO-8859-1\" instead.");
					charset = ISO_8859_1;
				}
			} else {
				charset = ISO_8859_1;
			}

			byte[] credentialsEncoded = Base64.getDecoder().decode(challenge.getRawValue());

			String credentials = isUtf8Enabled() ? decodeUtf8First(credentialsEncoded)
					: new String(credentialsEncoded, charset);
			int separator = credentials.indexOf(':');

			if (separator == -1) {
				// Log the blocking
				getLogger().info("Invalid credentials given by client with IP: "
						+ ((request != null) ? request.getClientInfo().getAddress() : "?"));
			} else {
				challenge.setIdentifier(credentials.substring(0, separator));
				challenge.setSecret(credentials.substring(separator + 1));
			}
		} catch (UnsupportedEncodingException e) {
			getLogger().log(Level.INFO, "Unsupported HTTP Basic encoding error", e);
		} catch (IllegalArgumentException e) {
			getLogger().log(Level.INFO, "Unable to decode the HTTP Basic credential", e);
		}
	}

}
