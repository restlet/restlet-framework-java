/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.CacheDirective;
import org.restlet.data.Dimension;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.engine.Engine;
import org.restlet.representation.StringRepresentation;

/**
 * Tests, on the wire, the headers of a 304 (Not Modified) response (#1712): RFC 9110 section 15.4.5
 * requires the server to send the validators and caching headers that a 200 would carry, including
 * "Expires".
 */
class NotModifiedHeadersTestCase {

	private static final int TIMEOUT_MS = 10_000;

	private Server server;

	@BeforeEach
	void setUp() throws Exception {
		Engine.clearThreadLocalVariables();
		Engine.register(false);
		Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(null));

		server =
				new Server(
						new Context(),
						Protocol.HTTP,
						0,
						new Restlet() {
							@Override
							public void handle(Request request, Response response) {
								StringRepresentation entity =
										new StringRepresentation("hello", MediaType.TEXT_PLAIN);
								entity.setTag(new Tag("v1", false));
								entity.setModificationDate(new Date(1_700_000_000_000L));
								entity.setExpirationDate(new Date(1_800_000_000_000L));
								entity.setLocationRef("http://localhost/loc");
								response.getCacheDirectives().add(CacheDirective.maxAge(60));
								response.getDimensions().add(Dimension.LANGUAGE);
								response.setEntity(entity);

								Status status =
										request.getConditions()
												.getStatus(
														request.getMethod(),
														true,
														entity.getTag(),
														entity.getModificationDate());
								if (status != null) {
									response.setStatus(status);
								}
							}
						});
		server.start();
	}

	@AfterEach
	void tearDown() throws Exception {
		if (server != null) {
			server.stop();
		}
		Engine.clearThreadLocalVariables();
	}

	/**
	 * Sends a request with "Connection: close" and returns the whole response, lower-cased headers
	 * included.
	 */
	private String exchange(String method, String conditionalHeader) throws IOException {
		try (Socket socket = new Socket("localhost", server.getEphemeralPort())) {
			socket.setSoTimeout(TIMEOUT_MS);
			OutputStream out = socket.getOutputStream();
			out.write(
					(method
									+ " /r HTTP/1.1\r\nHost: localhost\r\n"
									+ conditionalHeader
									+ "Connection: close\r\n\r\n")
							.getBytes(StandardCharsets.ISO_8859_1));
			out.flush();
			return new String(socket.getInputStream().readAllBytes(), StandardCharsets.ISO_8859_1);
		}
	}

	private static String headersOf(String response) {
		return response.substring(0, response.indexOf("\r\n\r\n")).toLowerCase(Locale.ROOT);
	}

	@Test
	void get_withMatchingIfNoneMatch_answers304WithTheCachingHeaders() throws Exception {
		String response = exchange("GET", "If-None-Match: \"v1\"\r\n");
		String headers = headersOf(response);

		assertTrue(response.startsWith("HTTP/1.1 304"), response);
		assertTrue(headers.contains("\r\netag: \"v1\""), response);
		assertTrue(headers.contains("\r\ncontent-location: http://localhost/loc"), response);
		assertTrue(headers.contains("\r\ncache-control: max-age=60"), response);
		assertTrue(headers.contains("\r\nvary: accept-language"), response);
		assertTrue(headers.contains("\r\ndate: "), response);
		assertTrue(headers.contains("\r\nexpires: fri, 15 jan 2027 08:00:00 gmt"), response);
	}

	@Test
	void head_withMatchingIfNoneMatch_answers304WithExpires() throws Exception {
		String response = exchange("HEAD", "If-None-Match: \"v1\"\r\n");

		assertTrue(response.startsWith("HTTP/1.1 304"), response);
		assertTrue(
				headersOf(response).contains("\r\nexpires: fri, 15 jan 2027 08:00:00 gmt"),
				response);
	}

	@Test
	void notModified_doesNotCarryTheOtherEntityHeaders() throws Exception {
		String headers = headersOf(exchange("GET", "If-None-Match: \"v1\"\r\n"));

		assertFalse(headers.contains("\r\nlast-modified: "), headers);
		assertFalse(headers.contains("\r\ncontent-type: "), headers);
	}

	@Test
	void ok_stillCarriesExpires() throws Exception {
		String response = exchange("GET", "");

		assertTrue(response.startsWith("HTTP/1.1 200"), response);
		assertTrue(
				headersOf(response).contains("\r\nexpires: fri, 15 jan 2027 08:00:00 gmt"),
				response);
	}
}
