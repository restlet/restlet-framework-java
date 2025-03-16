/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.io.IoUtils;

/**
 * Restlet unit tests for HTTP Basic authentication client/server.
 *
 * @author Stian Soiland
 * @author Jerome Louvel
 */
public class HttpBasicTestCase {

	public static class AuthenticatedRestlet extends Restlet {
		@Override
		public void handle(Request request, Response response) {
			response.setEntity(AUTHENTICATED_MSG, MediaType.TEXT_PLAIN);
		}
	}

	public static class TestVerifier extends MapVerifier {
		public TestVerifier() {
			getLocalSecrets().put(SHORT_USERNAME, SHORT_PASSWORD.toCharArray());
			getLocalSecrets().put(LONG_USERNAME, LONG_PASSWORD.toCharArray());
		}

		@Override
		public int verify(String identifier, char[] inputSecret) {
			// NOTE: Allocating Strings are not really secure treatment of passwords
			String almostSecret = new String(inputSecret);

			try {
				return super.verify(identifier, inputSecret);
			} finally {
				// Clear secret from memory as soon as possible (This is better
				// treatment, but useless due to our almostSecret copy)
				Arrays.fill(inputSecret, '\000');
			}
		}
	}

	public static class BlockerVerifier implements Verifier {
		@Override
		public int verify(Request request, Response response) {
			return RESULT_INVALID;
		}
	}

	public static final String AUTHENTICATED_MSG = "You are authenticated";

	public static final String LONG_PASSWORD = "thisLongPasswordIsExtremelySecure";

	public static final String LONG_USERNAME = "aVeryLongUsernameIsIndeedRequiredForThisTest";

	public static final String SHORT_PASSWORD = "pw15";

	public static final String SHORT_USERNAME = "user13";

	public static final String WRONG_USERNAME = "wrongUser";

	static Stream<Arguments> invalidCredentials() {
		return Stream.of(arguments(LONG_USERNAME, SHORT_PASSWORD), arguments(SHORT_USERNAME, LONG_PASSWORD),
				arguments(WRONG_USERNAME, SHORT_PASSWORD));
	}

	static Stream<Arguments> validCredentials() {
		return Stream.of(arguments(LONG_USERNAME, LONG_PASSWORD), arguments(SHORT_USERNAME, SHORT_PASSWORD));
	}

	@Nested
	class TestMapVerifier {

		private final MapVerifier verifier = new TestVerifier();

		@ParameterizedTest
		@MethodSource("org.restlet.security.HttpBasicTestCase#invalidCredentials")
		void testInvalidCredentials(final String login, final String password) {
			assertEquals(Verifier.RESULT_INVALID, this.verifier.verify(login, password.toCharArray()));
		}

		@ParameterizedTest
		@MethodSource("org.restlet.security.HttpBasicTestCase#validCredentials")
		void testValidCredentials(final String login, final String password) {
			assertEquals(Verifier.RESULT_VALID, this.verifier.verify(login, password.toCharArray()));
		}

	}

	@Nested
	class TestHttpBasicServer {
		private Component component;
		private Request request;
		private Client client;

		@Test
		public void HttpBasicNone() throws Exception {
			final Response response = client.handle(request);
			assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());
		}

		@ParameterizedTest
		@MethodSource("org.restlet.security.HttpBasicTestCase#invalidCredentials")
		void testInvalidCredentials(final String login, final String password) throws Exception {
			ChallengeResponse authentication = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, login, password);
			request.setChallengeResponse(authentication);

			final Response response = client.handle(request);
			assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());
		}

		@ParameterizedTest
		@MethodSource("org.restlet.security.HttpBasicTestCase#validCredentials")
		void testValidCredentials(final String login, final String password) throws Exception {
			ChallengeResponse authentication = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, login, password);
			request.setChallengeResponse(authentication);

			final Response response = client.handle(request);
			assertEquals(Status.SUCCESS_OK, response.getStatus());
			assertEquals(AUTHENTICATED_MSG, response.getEntity().getText());
		}

		@BeforeEach
		public void makeServer() throws Exception {
			final String REALM = HttpBasicTestCase.class.getSimpleName();
			this.component = new Component();
			final Server server = this.component.getServers().add(Protocol.HTTP, 0);

			final Application application = new Application() {
				@Override
				public Restlet createInboundRoot() {
					ChallengeAuthenticator authenticator = new ChallengeAuthenticator(getContext(),
							ChallengeScheme.HTTP_BASIC, REALM);
					authenticator.setVerifier(new TestVerifier());
					authenticator.setNext(new AuthenticatedRestlet());
					return authenticator;
				}
			};

			this.component.getDefaultHost().attach(application);
			this.component.start();
			request = new Request(Method.GET, "http://localhost:" + server.getActualPort());
			client = new Client(Protocol.HTTP);
		}

		@AfterEach
		public void cleanup() throws Exception {
			client.stop();
			if (this.component.isStarted()) {
				this.component.stop();
			}
			this.component = null;
		}
	}

}
