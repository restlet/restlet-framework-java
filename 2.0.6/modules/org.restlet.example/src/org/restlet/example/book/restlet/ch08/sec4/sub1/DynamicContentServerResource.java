package org.restlet.example.book.restlet.ch08.sec4.sub1;

import java.io.IOException;
import java.io.Writer;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * Resource that generates content dynamically.
 */
public class DynamicContentServerResource extends ServerResource {

    @Get
    public Representation getDynamicContent() {
        // Inline sub class of WriterRepresentation that writes
        // its dynamic content.
        Representation result = new WriterRepresentation(MediaType.TEXT_PLAIN) {

            @Override
            public void write(Writer writer) throws IOException {
                for (int i = 0; i < 10; i++) {
                    writer.append(Integer.toString(i));
                }
            }
        };

        return result;
    }

}
