package org.restlet.ext.openapi;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

import java.util.List;

public class LibraryApplication {
    public record Book(String id, String title, String author) {
    }

    public static class BooksResource extends ServerResource {
        @Get
        public List<Book> getBooks() {
            return List.of(
                new Book("1", "The Great Gatsby", "F. Scott Fitzgerald"),
                new Book("2", "To Kill a Mockingbird", "Harper Lee")
            );
        }
    }

    public static class Restlet extends RestletOpenApiApplication {
        @Override
        public org.restlet.Restlet createInboundRoot() {
            var router = new Router(getContext());
            router.attach("/books", BooksResource.class);
            return router;
        }
    }
}
