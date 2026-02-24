/**
 * Copyright 2005-2026 Qlik
 *
 * The contents of this file is subject to the terms of the Apache 2.0 open source license available at
 * http://www.opensource.org/licenses/apache-2.0
 *
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.openapi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

import java.util.List;

@SuppressWarnings("unused")
public class Library {
    private static final List<Book> BOOKS = List.of(
        new Book("1", "The Great Gatsby", "F. Scott Fitzgerald"),
        new Book("2", "To Kill a Mockingbird", "Harper Lee")
    );

    public record Book(String id, String title, String author) {
    }

    public static class BookResource extends ServerResource {
        @Get
        public Book getBook() {
            String bookId = getAttribute("bookId");
            getContext().getLogger().info("Retrieving book with ID: " + bookId);
            return BOOKS.getFirst();
        }
    }

    public static class BooksResource extends ServerResource {
        @Get
        public List<Book> getBooks() {
            return BOOKS;
        }

        @Post
        @Operation(
            summary = "Add a new book",
            responses = {
                @ApiResponse(
                    responseCode = "201",
                    headers = @Header(
                        name = "Location",
                        schema = @Schema(type = "string")
                    ),
                    content = @Content()
                )
            }
        )
        public void addBook(Book book) {
            getResponse().setStatus(Status.SUCCESS_CREATED);
            Reference locationRef = getRequest().getResourceRef().addSegment(book.id);
            getResponse().setLocationRef(locationRef);
        }
    }

    public static class LibraryApplication extends OpenApiApplication {
        @Override
        public org.restlet.Restlet createInboundRoot() {
            var router = new Router(getContext());
            router.attach("/books", BooksResource.class);
            router.attach("/books/{bookId}", BookResource.class);
            return router;
        }
    }
}
