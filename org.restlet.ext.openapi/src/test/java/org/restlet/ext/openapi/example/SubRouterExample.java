/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.openapi.example;

import org.restlet.ext.openapi.OpenApiApplication;
import org.restlet.routing.Router;

public class SubRouterExample {

    public static class SubRouterApplication extends OpenApiApplication {
        @Override
        public org.restlet.Restlet createInboundRoot() {
            var router = new Router(getContext());
            router.attach("/api", apiRouter());
            return router;
        }

        private Router apiRouter() {
            var router = new Router(getContext());
            router.attach("/items", booksRouter());
            return router;
        }

        private Router booksRouter() {
            var router = new Router(getContext());
            router.attach("/books", BookResources.BooksResource.class);
            router.attach("/books/{bookId}", BookResources.BookResource.class);
            return router;
        }
    }
}
