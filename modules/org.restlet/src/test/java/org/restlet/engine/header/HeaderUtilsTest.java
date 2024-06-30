/**
 * Copyright 2005-2024 Qlik
 *
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 *
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 *
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 *
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 *
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * https://restlet.talend.com/
 *
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.header;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Header;
import org.restlet.util.Series;

public class HeaderUtilsTest {

	@Test
	public void whenHeaderRetryAfterIsDecimalThenParsingIsStillFine() {
		// Given a retry_after header that contains a decimal value
		Header header = new Header(HeaderConstants.HEADER_RETRY_AFTER, "2.1");

		// Given a response
		Response response = new Response(new Request());

		// When I copy the retry_after header to the response
		HeaderUtils.copyResponseTransportHeaders(new Series<>(Header.class, Collections.singletonList(header)),
				response);

		// Then the response contains a valid value for the retry_after header
		assertNotNull(response.getRetryAfter());
	}

	@Test
	public void whenHeaderRetryAfterIsAlphabeticalThenParsingFailsSilently() {
		// Given a retry_after header that contains an alphabetical value
		Header header = new Header(HeaderConstants.HEADER_RETRY_AFTER, "2.1a");

		// Given a response
		Response response = new Response(new Request());

		// When I copy the retry_after header to the response
		HeaderUtils.copyResponseTransportHeaders(new Series<>(Header.class, Collections.singletonList(header)),
				response);

		// Then the response does not contain a retry_after header
		assertNull(response.getRetryAfter());
	}
}
