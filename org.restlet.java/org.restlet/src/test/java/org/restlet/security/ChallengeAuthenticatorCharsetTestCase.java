/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeScheme;
import org.restlet.engine.security.HttpBasicHelper;

/** Unit tests for the credentials charset advertised by {@link ChallengeAuthenticator}. */
class ChallengeAuthenticatorCharsetTestCase {

	@AfterEach
	void restoreDefault() {
		System.clearProperty(HttpBasicHelper.UTF8_PROPERTY);
	}

	/** Issue #1455: a Basic challenge advertises UTF-8 credentials when enabled. */
	@Test
	void createChallengeRequest_basic_advertisesUtf8() {
		System.setProperty(HttpBasicHelper.UTF8_PROPERTY, "true");
		ChallengeRequest challenge =
				new ChallengeAuthenticator(null, ChallengeScheme.HTTP_BASIC, "realm")
						.createChallengeRequest(false);
		assertEquals("realm", challenge.getRealm());
		assertEquals("UTF-8", challenge.getParameters().getFirstValue("charset"));
	}

	/** The default is unchanged: without the opt-in, no charset is advertised. */
	@Test
	void createChallengeRequest_basic_utf8Disabled_hasNoCharsetParameter() {
		ChallengeRequest challenge =
				new ChallengeAuthenticator(null, ChallengeScheme.HTTP_BASIC, "realm")
						.createChallengeRequest(false);
		assertNull(challenge.getParameters().getFirstValue("charset"));
	}

	@Test
	void createChallengeRequest_digest_hasNoCharsetParameter() {
		System.setProperty(HttpBasicHelper.UTF8_PROPERTY, "true");
		ChallengeRequest challenge =
				new ChallengeAuthenticator(null, ChallengeScheme.HTTP_DIGEST, "realm")
						.createChallengeRequest(false);
		assertNull(challenge.getParameters().getFirstValue("charset"));
	}
}
