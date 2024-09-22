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

package org.restlet.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;

public class HeaderTestCase extends RestletTestCase {

	/**
	 * Restlet that returns as a new Representation the list of values of
	 * "testHeader" header.
	 */
	public static class TestHeaderRestlet extends Restlet {
		@Override
		public void handle(Request request, Response response) {
			StringBuilder stb = new StringBuilder();

			for (Header header : request.getHeaders()) {
				if (TEST_HEADER.equalsIgnoreCase(header.getName())) {
					stb.append(header.getValue());
					stb.append('\n');
				}
			}

			response.setEntity(stb.toString(), MediaType.TEXT_PLAIN);
		}
	}

	/**
	 * Name of a test header field
	 */
	private static final String TEST_HEADER = "testHeader";

	private Client client;

	private Component component;

	/**
	 * Handle a new request built according to the parameters and return the
	 * response object.
	 *
	 * @param additionalHeaders The list of header used to build the request.
	 * @return The response of the request.
	 */
	private Response getWithParams(Header... additionalHeaders) {
		Request request = new Request(Method.GET, "http://localhost:" + TEST_PORT);
		Collections.addAll(request.getHeaders(), additionalHeaders);
		return client.handle(request);
	}

	@BeforeEach
	void setUpEach() throws Exception {
		this.client = new Client(Protocol.HTTP);
		component = new Component();
		component.getServers().add(Protocol.HTTP, TEST_PORT);
		component.getDefaultHost().attachDefault(new TestHeaderRestlet());
		component.start();
	}

	@AfterEach
	void tearDownEach() throws Exception {
		this.client.stop();
		this.component.stop();
		this.component = null;
	}

	/** Test with no test header */
	@Test
	public void test0() throws Exception {
		Response response = getWithParams();
		assertEquals(Status.SUCCESS_OK, response.getStatus());
		assertNull(response.getEntity().getText());
	}

	/** Test with one test header */
	@Test
	public void test1() throws Exception {
		Response response = getWithParams(new Header(TEST_HEADER, "a"));
		assertEquals(Status.SUCCESS_OK, response.getStatus());
		assertEquals("a\n", response.getEntity().getText());
	}

	/** Test with two test headers */
	@Test
	public void test2() throws Exception {
		Response response = getWithParams(new Header(TEST_HEADER, "a"), new Header(TEST_HEADER, "b"));
		assertEquals(Status.SUCCESS_OK, response.getStatus());
		assertEquals("a\nb\n", response.getEntity().getText());
	}
}
