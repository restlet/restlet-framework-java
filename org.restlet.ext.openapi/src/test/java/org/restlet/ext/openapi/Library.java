/**
 * Copyright 2005-2026 Qlik
 *
 * The contents of this file is subject to the terms of the Apache 2.0 open source license available at
 * http://www.opensource.org/licenses/apache-2.0
 *
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.openapi;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

import java.util.List;

public class Library {
    public record Book(String id, String title, String author) {
    }

    public static class BooksResource extends ServerResource {
        @SuppressWarnings("unused")
        @Get
        public List<Book> getBooks() {
            return List.of(
                new Book("1", "The Great Gatsby", "F. Scott Fitzgerald"),
                new Book("2", "To Kill a Mockingbird", "Harper Lee")
            );
        }
    }

    public static class LibraryApplication extends OpenApiApplication {
        @Override
        public org.restlet.Restlet createInboundRoot() {
            var router = new Router(getContext());
            router.attach("/books", BooksResource.class);
            return router;
        }
    }
}
