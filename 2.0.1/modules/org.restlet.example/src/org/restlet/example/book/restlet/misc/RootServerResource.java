package org.restlet.example.book.restlet.misc;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * Illustrating server resource life cycle.
 */
public class RootServerResource extends ServerResource {

    @Get("xml")
    public String toXml() {
        return "<root/>";
    }

    @Get("json")
    public String toJson() {
        return "[\"root\"]";
    }

    @Get("html")
    public String toHtml() {
        return "<html><body>root</body></html>";
    }

}
