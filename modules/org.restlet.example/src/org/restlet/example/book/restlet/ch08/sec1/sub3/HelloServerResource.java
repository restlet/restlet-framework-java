package org.restlet.example.book.restlet.ch08.sec1.sub3;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class HelloServerResource extends ServerResource {

    @Get("html")
    public String toHtml() {
        return "<html><body>hello, world</body></html>";
    }

    @Get("xml")
    public String toXml() {
        return "<txt>hello, world</txt>";
    }

    @Get("json")
    public String toJson() {
        return "{txt: \"hello, world\"}";
    }
}
