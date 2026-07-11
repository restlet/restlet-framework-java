/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.openapi.example;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import java.util.Optional;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class BookResources {
    private static final List<Book> BOOKS =
            List.of(
                    new Book("1", "The Great Gatsby", "F. Scott Fitzgerald"),
                    new Book("2", "To Kill a Mockingbird", "Harper Lee"));

    public record Book(String id, String title, String author) {}

    public static class BookResource extends ServerResource {
        @Delete
        @Operation(summary = "Delete a book by ID")
        public void deleteBook() {
            Optional.ofNullable(getBook())
                    .ifPresent(BOOKS::remove); // Yes, this is an immutable list :)
        }

        @Get
        @Operation(summary = "Get a book by ID")
        public Book getBook() {
            String bookId = getAttribute("bookId");
            getContext().getLogger().info("Retrieving book with ID: " + bookId);
            return BOOKS.getFirst();
        }
    }

    public static class BooksResource extends ServerResource {
        @Get
        @Operation(
                summary = "Get a list of all books",
                parameters = {
                    @Parameter(
                            name = "filter",
                            description = "Filter books",
                            in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY,
                            schema = @Schema(type = "string"))
                })
        public List<Book> getBooks() {
            return BOOKS;
        }

        @Post
        @Operation(
                summary = "Add a new book",
                responses = {
                    @ApiResponse(
                            responseCode = "201",
                            headers = @Header(name = "Location", schema = @Schema(type = "string")),
                            content = @Content())
                })
        public void addBook(Book book) {
            getResponse().setStatus(Status.SUCCESS_CREATED);
            Reference locationRef = getRequest().getResourceRef().addSegment(book.id);
            getResponse().setLocationRef(locationRef);
        }
    }
}
