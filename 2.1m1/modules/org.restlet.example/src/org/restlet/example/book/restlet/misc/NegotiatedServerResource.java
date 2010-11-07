package org.restlet.example.book.restlet.misc;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class NegotiatedServerResource extends ServerResource {
    @Get("txt")
    public String toString() {
        return "hello, world";
    }

    @Get("html")
    public String toHtml() {
        return "<html><body>hello, world</body></html>";
    }

}
