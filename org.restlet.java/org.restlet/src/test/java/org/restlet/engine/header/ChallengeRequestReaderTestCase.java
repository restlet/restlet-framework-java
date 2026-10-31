/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.header;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.ChallengeRequest;

class ChallengeRequestReaderTestCase {

	@Test
	void readValue_schemeOnly_parsesSchemeName() throws IOException {
		ChallengeRequestReader reader = new ChallengeRequestReader("Basic");
		ChallengeRequest result = reader.readValue();
		assertEquals("Basic", result.getScheme().getTechnicalName());
	}

	@Test
	void readValue_schemeWithParameters_capturesRawValue() throws IOException {
		ChallengeRequestReader reader = new ChallengeRequestReader("Digest realm=\"testrealm\"");
		ChallengeRequest result = reader.readValue();
		assertEquals("Digest", result.getScheme().getTechnicalName());
		assertTrue(result.getRawValue().contains("realm"));
	}

	@Test
	void readValue_emptyHeader_returnsNull() throws IOException {
		ChallengeRequestReader reader = new ChallengeRequestReader("");
		assertNull(reader.readValue());
	}

	@Test
	void readValues_twoChallenges_returnsBoth() throws IOException {
		List<ChallengeRequest> result =
				new ChallengeRequestReader("Basic realm=\"a\", Digest realm=\"b\", nonce=\"x\"")
						.readValues();
		assertEquals(2, result.size());
		assertEquals("Basic", result.get(0).getScheme().getTechnicalName());
		assertEquals("realm=a", result.get(0).getRawValue());
		assertEquals("Digest", result.get(1).getScheme().getTechnicalName());
		assertEquals("realm=b, nonce=x", result.get(1).getRawValue());
	}

	@Test
	void readValues_schemeWithoutParametersThenChallenge_returnsBoth() throws IOException {
		List<ChallengeRequest> result =
				new ChallengeRequestReader("Negotiate, Basic realm=\"a\"").readValues();
		assertEquals(2, result.size());
		assertEquals("Negotiate", result.get(0).getScheme().getTechnicalName());
		assertEquals("Basic", result.get(1).getScheme().getTechnicalName());
	}

	@Test
	void readValues_commaInsideQuotedValue_staysInOneChallenge() throws IOException {
		List<ChallengeRequest> result =
				new ChallengeRequestReader("Basic realm=\"a, Digest b\"").readValues();
		assertEquals(1, result.size());
		assertEquals("Basic", result.get(0).getScheme().getTechnicalName());
	}

	/** RFC 9110 section 11.2: token68 may end with "=" padding, as Negotiate tokens do. */
	@Test
	void readValues_token68Challenge_keepsToken() throws IOException {
		List<ChallengeRequest> result =
				new ChallengeRequestReader("Negotiate YIIabc==, Basic realm=\"a\"").readValues();
		assertEquals(2, result.size());
		assertEquals("Negotiate", result.get(0).getScheme().getTechnicalName());
		assertTrue(result.get(0).getRawValue().contains("YIIabc"));
		assertEquals("Basic", result.get(1).getScheme().getTechnicalName());
	}
}
