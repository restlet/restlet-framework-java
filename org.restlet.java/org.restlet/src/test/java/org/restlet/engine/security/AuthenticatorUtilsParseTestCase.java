/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.engine.Engine;

/** Unit tests for the parsing of WWW-Authenticate headers in {@link AuthenticatorUtils}. */
class AuthenticatorUtilsParseTestCase {

	@Test
	void parseRequest_twoChallenges_eachGetsItsSchemeAndRealm() {
		Engine.register(); // other tests may leave an engine without the Basic helper
		List<ChallengeRequest> result =
				AuthenticatorUtils.parseRequest(
						new Response(new Request(Method.GET, "http://localhost/")),
						"Basic realm=\"my realm\", Bearer error=\"x\"",
						null);

		assertEquals(2, result.size());
		assertEquals(ChallengeScheme.HTTP_BASIC, result.get(0).getScheme());
		assertEquals("my realm", result.get(0).getRealm());
		assertEquals("Bearer", result.get(1).getScheme().getTechnicalName());
		assertEquals("error=x", result.get(1).getRawValue());
	}
}
