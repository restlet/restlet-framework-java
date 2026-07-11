/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.openapi.example;

import java.util.Arrays;
import org.restlet.data.ChallengeScheme;
import org.restlet.ext.openapi.OpenApiApplication;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.MapVerifier;

public class BasicAuthenticationRootRouterExample {

    public static class BasicAuthenticationRootRouterApplication extends OpenApiApplication {
        @Override
        public org.restlet.Restlet createInboundRoot() {
            var router = new Router(getContext());
            router.attach("/api", booksRouter());

            var authenticator =
                    new ChallengeAuthenticator(getContext(), ChallengeScheme.HTTP_BASIC, "realm");
            authenticator.setVerifier(new TestVerifier());
            authenticator.setNext(router);

            return authenticator;
        }

        private Router booksRouter() {
            var router = new Router(getContext());
            router.attach("/books", BookResources.BooksResource.class);
            router.attach("/books/{bookId}", BookResources.BookResource.class);
            return router;
        }
    }

    public static class TestVerifier extends MapVerifier {
        public TestVerifier() {
            getLocalSecrets().put("login", "password".toCharArray());
        }

        @Override
        public int verify(String identifier, char[] inputSecret) {
            try {
                return super.verify(identifier, inputSecret);
            } finally {
                // Clear secret from memory as soon as possible
                Arrays.fill(inputSecret, '\000');
            }
        }
    }
}
